package services

import java.time.Instant
import java.util.UUID

import io.masterypath.slick.Tables.{AnswerLogRow, CardDueRow}
import io.masterypath.slick.{
  AnswerChoice,
  AnswerChoiceRepo,
  AnswerLogRepo,
  Card,
  CardDueRepo,
  CardRepo,
  CodeExercise,
  CodeExerciseRepo,
  FlashcardType,
  FlashcardTypeID,
  FlashcardTypeRepo,
  MapID,
  NodeID,
  NodeRepo
}
import javax.inject.{Inject, Singleton}
import models.flashcard.dto.{CardMetaData, FlashcardTypeBrief}
import models.flashcard.{AnswerChoices, ChildCard, Flashcard}
import models.organization.Member
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FlashcardRepoService @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  implicit val dbConfProvider: DatabaseConfigProvider = dbConfigProvider
  private val dbConfig                                = dbConfigProvider.get[JdbcProfile]

  import dbConfig.{db, profile}
  import profile.api._

  private val answerChoices  = new AnswerChoiceRepo
  private val answerLogs     = new AnswerLogRepo
  private val cards          = new CardRepo
  private val exercises      = new CodeExerciseRepo
  private val cardsDue       = new CardDueRepo
  private val flashcardTypes = new FlashcardTypeRepo
  private val nodes          = new NodeRepo

  private def calcEasinessFactor(ef: Double, quality: Int) = {
    val new_ef = ef + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02))
    if (new_ef < 1.3) 1.3 else new_ef
  }

  private def updateInterval(interval: Int, quality: Int) = quality match {
    case x if x >= 4 => interval + 1
    case 3           => interval
    case _           => 1
  }

  private def intervalToDays(interval: Int, ef: Double): Double = interval match {
    case 0 => 0
    case 1 => 1
    case 2 => 6
    case n => intervalToDays(n - 1, ef) * ef
  }

  private def getDueDays(interval: Int, ef: Double, quality: Int): Double = quality match {
    case x if x >= 4 => intervalToDays(interval, ef)
    case _           => 0.0
  }

  def getFlashcardTypes: Future[Seq[FlashcardType]] = flashcardTypes.getAll

  def log_quality(member: Member, cardID: NodeID, mapID: MapID, quality: Int) = {
    def updateCardDue(cardDue: CardDueRow) = {
      val timestamp   = Instant.now.getEpochSecond
      val newInterval = updateInterval(cardDue.interval, quality)
      val new_ef      = calcEasinessFactor(cardDue.ef, quality)
      val due_days    = getDueDays(newInterval, new_ef, quality).toLong
      cardDue.copy(
        ef = new_ef,
        interval = newInterval,
        lastAnswerTime = timestamp,
        due = timestamp + (86400 * due_days)
      )
    }

    def newCardDue = {
      val timestamp = Instant.now.getEpochSecond
      val interval  = updateInterval(0, quality)
      val ef        = calcEasinessFactor(2.5, quality)
      val due_days  = getDueDays(interval, ef, quality).toLong
      CardDueRow(
        id = cardID,
        profileID = member.profile.id,
        ef = ef,
        interval = interval,
        lastAnswerTime = timestamp,
        due = timestamp + (86400 * due_days)
      )
    }

    val cardDueLookup = for {
      card    <- cards.filterQuery(_.id === cardID.uuid).filter(_.mapID === mapID.uuid)
      cardDue <- cardsDue.filterQuery(row => row.id === card.id && row.profileID === member.profile.id.uuid)
    } yield (cardDue)

    val q = for {
      existing <- cardDueLookup.result.headOption
      row      = existing.map(updateCardDue) getOrElse newCardDue
      result   <- cardsDue.query.insertOrUpdate(row)
    } yield result
    db.run(q)
  }

  def getCardsDue(parentID: NodeID, cards: Seq[ChildCard]): Seq[CardMetaData] = {
    def _cardDue(card: ChildCard): CardMetaData =
      (card.cardDue match {
        case Some(due) =>
          val cardType = card.flashcardType.get // checked by _isAValidFlashCard
          CardMetaData(
            nodeID = card.node.id,
            parentID = card.node.parentID.get,
            segmentID = card.node.segmentID,
            ef = due.ef,
            lastAnswer = due.lastAnswerTime,
            due = due.due,
            color = card.nodeColor,
            flashCardType = FlashcardTypeBrief(id = cardType.id,
                                               cardType = cardType.cardType,
                                               name = cardType.name,
                                               commonName = cardType.commonName,
                                               description = cardType.description),
          )
        case None =>
          val cardType = card.flashcardType.get // checked by _isAValidFlashCard
          CardMetaData(
            nodeID = card.node.id,
            parentID = card.node.parentID.get,
            segmentID = card.node.segmentID,
            ef = 2.5,
            lastAnswer = 0,
            due = 0,
            color = card.nodeColor,
            flashCardType = FlashcardTypeBrief(id = cardType.id,
                                               cardType = cardType.cardType,
                                               name = cardType.name,
                                               commonName = cardType.commonName,
                                               description = cardType.description),
          )
      })

    def _isAValidFlashCard(x: ChildCard): Boolean =
      x.node.`type` == "flashcard" && x.card.isDefined && x.flashcardType.isDefined

    @scala.annotation.tailrec
    def _run(children: Seq[ChildCard], acc: Seq[CardMetaData]): Seq[CardMetaData] = (children, acc) match {
      case (Nil, Nil) => Seq()
      case (Nil, acc) => acc
      case (list, acc) =>
        val childList =
          cards.filter(x => x.node.parentID exists (_.uuid == list.head.node.id.uuid)).sortBy(_.node.order)
        _run(childList :++ list.tail, acc :++ childList.filter(_isAValidFlashCard).map(_cardDue))
    }

    def _negativesLast(a: ChildCard, b: ChildCard): Boolean = {
      def _isNeg(n: Double) = n < 0
      def _isPos(n: Double) = n >= 0

      if (_isNeg(a.node.order) && _isNeg(b.node.order)) {
        a.node.order < b.node.order
      } else if (_isNeg(a.node.order) && _isPos(b.node.order)) {
        false
      } else if (_isPos(a.node.order) && _isNeg(b.node.order)) {
        true
      } else {
        a.node.order < b.node.order
      }
    }

    val childList =
      cards.filter(x => x.node.parentID exists (_.uuid == parentID.uuid)).sortWith(_negativesLast)
    val acc = childList.filter(_isAValidFlashCard).map(_cardDue)
    _run(childList, acc)
  }

  def getChildCardsDeep(member: Member, pathID: String): Future[Seq[ChildCard]] = {
    val cardsQ = for {
      card     <- cards.query
      cardType <- card.flashcardTypeTableFk
    } yield (card, cardType)

    val q = for {
      ((node, card), card_due) <- nodes.getNodesByPathIDQuery(pathID) joinLeft cardsQ on (_.id === _._1.id) joinLeft cardsDue.query
                                   .filter(_.profileID === member.profile.id.uuid) on (_._1.id === _.id)
    } yield (node, card, card_due)
    db.run(q.result)
      .map(seq =>
        seq.map {
          case (node, card, cardDue) =>
            ChildCard(
              node = nodes.fromRow(node),
              card = card.flatMap(card => Option(cards.fromRow(card._1))),
              cardDue = cardDue.flatMap(cardDue => Option(cardsDue.fromRow(cardDue))),
              flashcardType = card.flatMap(card => Option(flashcardTypes.fromRow(card._2)))
            )
      })
  }

  def getFlashcard(cardID: NodeID, mapID: MapID): Future[Option[Flashcard]] = {
    val q = for {
      card           <- cards.filterQuery(_.id === cardID.uuid).filter(_.mapID === mapID.uuid)
      node           <- nodes.query if card.id === node.id
      flashcard_type <- card.flashcardTypeTableFk
    } yield (card, node, flashcard_type)
    db.run(q.result.headOption).map {
      case Some((card, node, card_type)) =>
        Some(Flashcard(cards.fromRow(card), nodes.fromRow(node), flashcardTypes.fromRow(card_type)))
      case None => None
    }
  }

  def getCodeExerciseAndCardType(nodeID: NodeID): Future[Option[(CodeExercise, FlashcardType)]] = {
    val q = for {
      codeExercise <- exercises.getByIdQuery(nodeID.uuid)
      card         <- codeExercise.cardTableFk
      cardType     <- card.flashcardTypeTableFk
    } yield (codeExercise, cardType)
    db.run(q.result)
      .map(seq => seq.map(tuple => (exercises.fromRow(tuple._1), flashcardTypes.fromRow(tuple._2))).headOption)
  }

  def getCardTypeByID(cardTypeID: FlashcardTypeID): Future[Option[FlashcardType]] =
    flashcardTypes.getById(cardTypeID.uuid)

  def getAnswerChoices(cardID: NodeID, mapID: MapID): Future[AnswerChoices] = {
    val q = for {
      card          <- cards.filterQuery(_.id === cardID.uuid).filter(_.mapID === mapID.uuid)
      answerChoices <- answerChoices.filterQuery(_.cardID === card.id)
    } yield (answerChoices)
    db.run(q.result).map(seq => AnswerChoices(seq.map(answerChoices.fromRow)))
  }

  def createCard(card: Card, answers: Seq[AnswerChoice]): Future[Int] = {
    val cardRow    = cards.toRow(card)
    val answerRows = answers.map(answerChoices.toRow)
    db.run {
      (cards.saveQuery(cardRow) andThen answerChoices.saveQuery(answerRows)).transactionally
        .map(_.getOrElse(0))
    }
  }

  def createCodeExerciseCard(exercise: CodeExercise, card: Card): Future[Int] = db.run {
    (cards.query += cards.toRow(card)) andThen
      (exercises.query += exercises.toRow(exercise)).transactionally
  }

  def insertOrUpdateCodeExercise(exercise: CodeExercise): Future[Int] = db.run {
    exercises.query.insertOrUpdate(exercises.toRow(exercise))
  }

  def updateCard(card: Card, answers: Seq[AnswerChoice]): Future[Int] = {
    val answerRows = answers.map(answerChoices.toRow)
    db.run {
      (cards.query.insertOrUpdate(cards.toRow(card)) andThen
        answerChoices.filterQuery(_.cardID === card.id.uuid).delete andThen
        answerChoices.saveQuery(answerRows)).transactionally
        .map(_.getOrElse(0))
    }
  }

  def updateFlashcardAudio(mapID: MapID, nodeID: NodeID, publicUrl: String): Future[Int] = db.run {
    cards.filterQuery(_.id === nodeID.uuid).filter(_.mapID === mapID.uuid).map(_.audio).update(Some(publicUrl))
  }

  def logAnswer(member: Member, cardID: NodeID, correct: Boolean, seconds: Int): Future[Int] = {
    val timestamp = Instant.now.getEpochSecond()
    db.run(answerLogs.query += AnswerLogRow(UUID.randomUUID(), member.profile.id, cardID, correct, timestamp, seconds))
  }
}
