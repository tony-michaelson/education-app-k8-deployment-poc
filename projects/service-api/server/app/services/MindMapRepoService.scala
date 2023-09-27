package services

import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.{JsObject, Json}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import io.masterypath.slick.{
  AnswerChoice,
  AnswerChoiceID,
  AnswerChoiceRepo,
  AnswerLogRepo,
  CardDueRepo,
  CardRepo,
  CodeExercise,
  CodeExerciseRepo,
  LinkMapMember,
  LinkMapMemberRepo,
  MapID,
  MapRights,
  MapRightsID,
  MapRightsRepo,
  MemberProfile,
  MemberProfileRepo,
  MindMap,
  MindMapRepo,
  Node,
  NodeAttributes,
  NodeAttributesID,
  NodeAttributesRepo,
  NodeID,
  NodeRepo,
  OrgID,
  Post,
  PostRead,
  PostReadRepo,
  PostRepo,
  ProfileID,
  SegmentID
}
import io.masterypath.slick.Tables.{
  CardDueRow,
  CardRow,
  MapRightsRow,
  MapRightsTable,
  NodeAttributesRow,
  NodeRow,
  PostRow
}
import models.mindmap.dto.{
  BlogPage,
  MapIdea,
  MapIdeas,
  MapIdeasExport,
  MapRightsBrief,
  MapRightsMembers,
  NodeAttr,
  NodeAttrStyle,
  NodeCardPostInfo
}
import models.mindmap.{ChildNode, ForkedMindMapData, MindMapData, PostInfo}
import models.organization.Member

import java.util.UUID

@Singleton
class MindMapRepoService @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  implicit val dbConfProvider: DatabaseConfigProvider = dbConfigProvider

  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig.{db, profile}
  import profile.api._

  private val answerChoices  = new AnswerChoiceRepo
  private val answerLogs     = new AnswerLogRepo
  private val cards          = new CardRepo
  private val cardsDue       = new CardDueRepo
  private val codeExercises  = new CodeExerciseRepo
  private val mapMember      = new LinkMapMemberRepo
  private val mapRepo        = new MindMapRepo
  private val mapRightsRepo  = new MapRightsRepo
  private val nodeAttributes = new NodeAttributesRepo
  private val nodesRepo      = new NodeRepo
  private val posts          = new PostRepo
  private val postsRead      = new PostReadRepo
  private val memberProfile  = new MemberProfileRepo

  private val blankUUID = NodeID(java.util.UUID.fromString("00000000-0000-0000-0000-000000000000"))

  def insertOrUpdatePost(post: Post): Future[Int] = db.run {
    posts.query.insertOrUpdate(posts.toRow(post))
  }

  def getPost(postID: NodeID, mapID: MapID): Future[Option[Post]] =
    posts.filter(row => row.id === postID.uuid && row.mapID === mapID.uuid).map(_.headOption)

  def insertOrUpdatePostRead(post_read: PostRead): Future[Int] = db.run {
    postsRead.query.insertOrUpdate(postsRead.toRow(post_read))
  }

  def deletePost(postID: NodeID, mapID: MapID): Future[Int] = db.run {
    postsRead.filterQuery(_.id === postID.uuid).delete andThen
      posts.filterQuery(_.id === postID.uuid).filter(_.mapID === mapID.uuid).delete.transactionally
  }

  def getPostsByMapID(mapID: MapID): Future[Seq[PostInfo]] = {
    val query = for {
      (post, post_read) <- posts.query.filter(_.mapID === mapID.uuid) joinLeft postsRead.query on (_.id === _.id)
    } yield (post, post_read)
    db.run(query.result)
      .map(seq =>
        seq.map {
          case (post, post_read) => PostInfo(posts.fromRow(post), post_read.flatMap(x => Option(postsRead.fromRow(x))))
      })
  }

  def createMapRights(mapRights: MapRights): Future[Int] = {
    db.run(mapRightsRepo.query += mapRightsRepo.toRow(mapRights))
  }

  def deleteMapRights(mapID: MapID, mapRightsID: MapRightsID): Future[Int] = db.run {
    mapRightsRepo.filterQuery(x => x.mapID === mapID.uuid && x.id === mapRightsID.uuid && x.name =!= "Default").delete
  }

  def addMapRightsMember(orgID: OrgID, mapRightsID: MapRightsID, profileID: ProfileID): Future[Int] = {
    val newMapMember = LinkMapMember(
      id = mapRightsID,
      orgID = orgID,
      profileID = profileID
    )
    db.run(mapMember.query += mapMember.toRow(newMapMember))
  }

  def removeMapRightsMember(mapRightsID: MapRightsID, profileID: ProfileID): Future[Int] =
    db.run(mapMember.filterQuery(row => row.id === mapRightsID.uuid && row.profileID === profileID.uuid).delete)

  def createUserMindMap(orgID: OrgID, mindMap: MindMap, profileID: ProfileID): Future[Int] = {
    val newRootNode = Node(
      id = NodeID.random,
      nodeNumber = 1,
      parentID = None,
      mapID = mindMap.id,
      segmentID = SegmentID(mindMap.id),
      path = mindMap.id.toString,
      order = 1,
      name = mindMap.name,
      `type` = "root",
      root = Some(1)
    )

    val mapRightsID = MapRightsID.random
    val newMapRights = MapRights(
      id = mapRightsID,
      name = "Default",
      mapID = mindMap.id,
      admin = true,
      feedback = true,
      mnemonics = true,
      modify = true,
      permissions = true,
      publish = true,
      share = true,
      stats = true,
      training = true,
      transfer = true,
      view = true
    )
    val newMapMember = LinkMapMember(
      id = mapRightsID,
      orgID = orgID,
      profileID = profileID
    )

    db.run(
      ((mapRepo.query += mapRepo.toRow(mindMap)) andThen
        (mapRightsRepo.query += mapRightsRepo.toRow(newMapRights)) andThen
        (mapMember.query += mapMember.toRow(newMapMember)) andThen
        (nodesRepo.query += nodesRepo.toRow(newRootNode))).transactionally)
  }

  //  ****************
  //  Publish MindMaps
  //  ****************
//  TODO - add a check to prevent publish requests on mapIDs already published
  def publishMap(member: Member, mapID: MapID): Future[Option[MapID]] = {
    mapRepo.getById(mapID).flatMap {
      case Some(mindMap) => publishMindMap(member, mindMap).map(Some(_))
      case None          => Future.successful(None)
    }
  }

  private def publishMindMap(member: Member, mindMap: MindMap): Future[MapID] = {
    getPublishedMapByOriginID(mindMap.id).flatMap { latestRelease =>
      createMindMapReleaseVersion(mindMap).flatMap { newMapID =>
        publishMap(member, mindMap.id, newMapID, latestRelease).map(_ => newMapID)
      }
    }
  }

  private def getPublishedMapByOriginID(originID: MapID): Future[Option[MindMap]] = {
    mapRepo.filter(a => a.originMapID === originID.uuid && a.published === true).map(_.headOption)
  }

  private def createMindMapReleaseVersion(mindMap: MindMap): Future[MapID] = {
    val newReleaseVersion =
      mindMap.releaseVersion.map(a => BigDecimal(a + .1).setScale(1, BigDecimal.RoundingMode.HALF_UP).toDouble)

    val newMindMap = mindMap.copy(
      id = MapID.random,
      published = true,
      originMapID = Some(mindMap.id),
      releaseVersion = newReleaseVersion,
    )

    val updatedMindMap = mindMap.copy(
      releaseVersion = newReleaseVersion
    )

    val dbAction = (
      for {
        _     <- mapRepo.query.filter(_.originMapID === mindMap.id.uuid).map(_.published).update(false)
        mapID <- mapRepo.query returning mapRepo.query.map(_.id) += mapRepo.toRow(newMindMap)
        _     <- mapRepo.updateByIdQuery(mindMap.id.uuid, mapRepo.toRow(updatedMindMap))
      } yield (mapID)
    ).transactionally

    db.run(dbAction).map(MapID(_))
  }

  private def publishMap(member: Member, mapID: MapID, newMapID: MapID, latestRelease: Option[MindMap]): Future[Int] = {
    getForkedMindMapData(member, mapID, newMapID).flatMap { forkedMap =>
      val newNodeID = forkedMap.newNodeID

      val nodeUpdates = DBIO.sequence(forkedMap.originNodes.map(n => {
        val pubID: Option[NodeID] = Some(newNodeID(NodeID(n.id)))
        nodesRepo.query.filter(_.id === n.id.uuid).map(_.publishedID).update(pubID)
      }))

      val mapRights = MapRights(
        id = MapRightsID.random,
        name = "Default",
        mapID = newMapID
      )

      val mapRightsUpdate = if (latestRelease.isDefined) {
        mapRightsRepo.query.filter(_.mapID === latestRelease.get.id.uuid).map(_.mapID).update(newMapID)
      } else {
        mapRightsRepo.query += mapRightsRepo.toRow(mapRights)
      }

      val answerLogUpdates =
        DBIO.sequence(
          forkedMap.originNodes
            .filter(_.publishedID.isDefined)
            .map(n => {
              val pubID: NodeID = n.publishedID match {
                case Some(id) => NodeID(id)
                case None     => blankUUID
              }
              val newCardID: NodeID = newNodeID(n.id)
              answerLogs.query
                .filter(_.cardID === pubID.uuid)
                .map(_.cardID)
                .update(newCardID)
            }))

      val cardDueUpdates =
        DBIO.sequence(
          forkedMap.originNodes
            .filter(_.publishedID.isDefined)
            .map(n => {
              val pubID: NodeID = n.publishedID match {
                case Some(id) => NodeID(id)
                case None     => blankUUID
              }
              val newCardID: NodeID = newNodeID(n.id)
              cardsDue.query
                .filter(_.id === pubID.uuid)
                .map(_.id)
                .update(newCardID)
            }))

      val postsReadUpdates =
        DBIO.sequence(
          forkedMap.originNodes
            .filter(_.publishedID.isDefined)
            .map(n => {
              val pubID: NodeID = n.publishedID match {
                case Some(id) => NodeID(id)
                case None     => blankUUID
              }
              val newCardID: NodeID = newNodeID(n.id)
              postsRead.query
                .filter(_.id === pubID.uuid)
                .map(_.id)
                .update(newCardID)
            }))

//      we can just use the nodes list and use the publishedID field to migrate data
//      create an update for answer_log, card_due, and post_read for each forkedMap.newNodeID
      val dbAction = (
        for {
          c1 <- nodesRepo.saveQuery(forkedMap.nodes.map(nodesRepo.toRow))
          c2 <- posts.saveQuery(forkedMap.posts.map(posts.toRow))
          c3 <- cards.saveQuery(forkedMap.cards.map(cards.toRow))
          c4 <- codeExercises.saveQuery(forkedMap.codeExercises.map(codeExercises.toRow))
          c5 <- answerChoices.saveQuery(forkedMap.answerChoices.map(answerChoices.toRow))
          _  <- mapRightsUpdate
          _  <- nodeUpdates
          _  <- answerLogUpdates
          _  <- cardDueUpdates
          _  <- postsReadUpdates
        } yield c1.getOrElse(0) + c2.getOrElse(0) + c3.getOrElse(0) + c4.getOrElse(0) + c5.getOrElse(0)
      ).transactionally

      db.run(dbAction)
    }
  }
  //  ****************
  //  Forking MindMaps
  //  ****************
  def forkMap(member: Member, mapID: MapID): Future[Option[MapID]] = {
    mapRepo.getById(mapID).flatMap {
      case Some(mindMap) => forkMindMap(member, mindMap)
      case None          => Future.successful(None)
    }
  }

  private def rollbackMapCreation(mapID: MapID): Future[Int] = {
    mapRepo.deleteById(mapID.uuid)
  }

  private def forkMindMap(member: Member, mindMap: MindMap): Future[Option[MapID]] = {
    createEmptyMindMap(member, mindMap).flatMap { newMapID =>
      forkMap(member, mindMap.id, newMapID).map {
        case c if c > 0 => Some(newMapID)
        case _ =>
          rollbackMapCreation(newMapID)
          None
      }
    }
  }

  private def createEmptyMindMap(member: Member, mindMap: MindMap): Future[MapID] = {
    val newMindMap = mindMap.copy(id = MapID.random)

    val mapRightsID = MapRightsID.random
    val newMapRights = MapRights(
      id = mapRightsID,
      name = "Default",
      mapID = newMindMap.id,
      admin = true,
      feedback = true,
      mnemonics = true,
      modify = true,
      permissions = true,
      publish = true,
      share = true,
      stats = true,
      training = true,
      transfer = true,
      view = true
    )
    val newMapMember = LinkMapMember(
      id = mapRightsID,
      orgID = newMindMap.orgID,
      profileID = member.profile.id
    )

    val dbAction = (
      for {
        mapID <- (mapRepo.query returning mapRepo.query.map(_.id) += mapRepo.toRow(newMindMap))
        _     <- mapRightsRepo.query += mapRightsRepo.toRow(newMapRights)
        _     <- mapMember.query += mapMember.toRow(newMapMember)
      } yield (mapID)
    ).transactionally

    db.run(dbAction).map(MapID(_))
  }

  private def forkMap(member: Member, mapID: MapID, newMapID: MapID): Future[Int] = {
    getForkedMindMapData(member, mapID, newMapID).flatMap { forkedMap =>
      val dbAction = (
        for {
          c1 <- nodesRepo.saveQuery(forkedMap.nodes.map(nodesRepo.toRow))
          c2 <- nodeAttributes.saveQuery(forkedMap.attributes.map(nodeAttributes.toRow))
          c3 <- posts.saveQuery(forkedMap.posts.map(posts.toRow))
          c4 <- cards.saveQuery(forkedMap.cards.map(cards.toRow))
          c5 <- codeExercises.saveQuery(forkedMap.codeExercises.map(codeExercises.toRow))
          c6 <- answerChoices.saveQuery(forkedMap.answerChoices.map(answerChoices.toRow))
        } yield
          c1.getOrElse(0) + c2.getOrElse(0) + c3.getOrElse(0) + c4.getOrElse(0) + c5.getOrElse(0) + c6.getOrElse(0)
      ).transactionally

      db.run(dbAction)
    }
  }

  private def getForkedMindMapData(member: Member, mapID: MapID, newMapID: MapID): Future[ForkedMindMapData] = {
    getMindMapData(member, mapID).map(mindMapData => {
      val newNodeID: Map[NodeID, NodeID] =
        mindMapData.nodes.foldLeft(Map[NodeID, NodeID]()) { (map, node) =>
          map + (node.id -> NodeID.random)
        }

      def newParentID(parentID: Option[NodeID]): Option[NodeID] = parentID match {
        case Some(nodeID) => Some(newNodeID(nodeID))
        case None         => None
      }

      def newSegmentID(segmentID: SegmentID) =
        if (segmentID.uuid == mapID.uuid) {
          SegmentID(newMapID.uuid)
        } else {
          SegmentID(newNodeID(NodeID(segmentID.uuid)).uuid)
        }

      def newPath(path: String): String =
        path
          .split(':')
          .map(txtID => {
            val segmentID: SegmentID = SegmentID(UUID.fromString(txtID))
            newSegmentID(segmentID)
          })
          .mkString(sep = ":")

      val newNodes =
        mindMapData.nodes.map(
          node =>
            node.copy(
              id = newNodeID(node.id),
              parentID = newParentID(node.parentID),
              mapID = newMapID,
              segmentID = newSegmentID(node.segmentID),
              path = newPath(node.path),
              publishedID = None,
          )
        )

      val newPosts = mindMapData.posts.map(
        post =>
          post.copy(
            id = newNodeID(post.id),
            mapID = newMapID
        )
      )

      val newCards = mindMapData.cards.map(
        card =>
          card.copy(
            id = newNodeID(card.id),
            mapID = newMapID
        )
      )

      val newCodeExercises = mindMapData.codeExercises.map(
        exercise =>
          exercise.copy(
            id = newNodeID(exercise.id)
        )
      )

      val newAnswerChoices = mindMapData.answerChoices.map(
        choice =>
          choice.copy(
            id = AnswerChoiceID.random,
            cardID = newNodeID(choice.cardID)
        )
      )

      val newAttributes = mindMapData.attributes.map(
        attr =>
          attr.copy(
            id = NodeAttributesID.random,
            nodeID = newNodeID(attr.nodeID)
        )
      )

      val newRootNode = newNodes.filter(node => node.`type` == "root" && node.parentID.isEmpty).head
      val newNodesSorted = {
        Seq(newRootNode) ++
          getChildNodesByParentID(newNodes, NodeID(newRootNode.id), filterRootNodes = false)
      }

      ForkedMindMapData(
        originNodes = mindMapData.nodes,
        newNodeID = newNodeID,
        nodes = newNodesSorted,
        attributes = newAttributes,
        posts = newPosts,
        cards = newCards,
        codeExercises = newCodeExercises,
        answerChoices = newAnswerChoices,
      )
    })
  }

  def getMindMapData(member: Member, mapID: MapID): Future[MindMapData] = {
    for {
      nodes          <- nodesRepo.filter(_.mapID === mapID.uuid)
      posts          <- posts.filter(_.mapID === mapID.uuid)
      cards          <- cards.filter(_.mapID === mapID.uuid)
      choices        <- getAnswerChoicesByMapID(mapID)
      exercises      <- getCodeExercisesByMapID(mapID)
      nodeAttributes <- getNodeAttrByMapIDProfileID(mapID, member.profile.id)
    } yield
      MindMapData(
        nodes = nodes,
        attributes = nodeAttributes,
        posts = posts,
        cards = cards,
        codeExercises = exercises,
        answerChoices = choices
      )
  }

  private def getAnswerChoicesByMapID(mapID: MapID): Future[Seq[AnswerChoice]] = {
    val q = for {
      card   <- cards.query.filter(_.mapID === mapID.uuid)
      answer <- answerChoices.query.filter(_.cardID === card.id)
    } yield answer
    db.run(q.result).map(seq => seq.map(answerChoices.fromRow))
  }

  private def getNodeAttrByMapIDProfileID(mapID: MapID, profileID: ProfileID): Future[Seq[NodeAttributes]] = {
    val q = for {
      node     <- nodesRepo.query.filter(_.mapID === mapID.uuid)
      nodeAttr <- nodeAttributes.query.filter(na => na.nodeID === node.id && na.profileID === profileID.uuid)
    } yield nodeAttr
    db.run(q.result).map(seq => seq.map(nodeAttributes.fromRow))
  }

  private def getCodeExercisesByMapID(mapID: MapID): Future[Seq[CodeExercise]] = {
    val q = for {
      card   <- cards.query.filter(_.mapID === mapID.uuid)
      answer <- codeExercises.query.filter(_.id === card.id)
    } yield answer
    db.run(q.result).map(seq => seq.map(codeExercises.fromRow))
  }
  //  ********************
  //  END Forking MindMaps
  //  ********************

  def getAllMapsByUserID(orgID: OrgID, profileID: ProfileID): Future[Seq[MindMap]] = {
    val q = for {
      mapMember <- mapMember.query.filter(_.profileID === profileID.uuid)
      mapRights <- mapMember.mapRightsTableFk
      mindMap   <- mapRepo.query.filter(a => a.id === mapRights.mapID && a.orgID === orgID.uuid && a.published === false)
    } yield mindMap
    db.run(q.result).map(seq => seq.map(mapRepo.fromRow))
  }

  def getMapSettingsByID(mapID: MapID): Future[Option[MindMap]] =
    mapRepo.getById(mapID)

  def getMapSettingsByIDAndUpdate(mapID: MapID)(updateFn: MindMap => MindMap): Future[Int] = {
    getMapSettingsByID(mapID).flatMap {
      case Some(row) => mapRepo.updateById(row.id, updateFn(row))
      case None      => Future.successful(0)
    }
  }

  def updateMapIcon(mapID: MapID, icon: String): Future[Int] =
    db.run(mapRepo.filterQuery(_.id === mapID.uuid).map(_.icon).update(Some(icon)))

  def getMapRightsByMapID(mapID: MapID): Future[Seq[MapRights]] = db.run(
    mapRightsRepo.filterQuery(_.mapID === mapID.uuid).result.map(seq => seq.map(mapRightsRepo.fromRow))
  )

  def getMapRightsByProfileID(mapID: MapID, profileID: ProfileID): Future[Seq[MapRights]] = {
    val q = for {
      mapRights <- mapRightsRepo.query.filter(_.mapID === mapID.uuid)
      _         <- mapMember.query.filter(x => x.id === mapRights.id && x.profileID === profileID.uuid)
    } yield mapRights
    db.run(q.result).map(seq => seq.map(mapRightsRepo.fromRow))
  }

  def getMapRightsMembers(mapID: MapID): Future[Seq[MapRightsMembers]] =
    getMapRightsMembers(mapRightsRepo.filterQuery(_.mapID === mapID.uuid))

  def getMapRightsMembers(mapID: MapID, mapRightsID: MapRightsID): Future[Seq[MapRightsMembers]] =
    getMapRightsMembers(mapRightsRepo.filterQuery(row => row.id === mapRightsID.uuid && row.mapID === mapID.uuid))

  private def getMapRightsMembers(
      mapRightsQuery: Query[MapRightsTable, MapRightsRow, Seq]): Future[Seq[MapRightsMembers]] = {
    def _getAllMembers(mapRightsID: MapRightsID, tuples: Seq[(MapRights, Option[MemberProfile])])
      : (Seq[MemberProfile], Seq[(MapRights, Option[MemberProfile])]) =
      (
        tuples.filter(_._1.id.uuid == mapRightsID.uuid).flatMap(_._2),
        tuples.filter(_._1.id.uuid != mapRightsID.uuid)
      )

    def _toMapRightsMembers(
        tuples: Seq[(MapRights, Option[MemberProfile])],
        acc: Seq[MapRightsMembers]
    ): Seq[MapRightsMembers] = tuples match {
      case x if x.isEmpty => acc
      case x =>
        val (profiles, newTail) = _getAllMembers(x.head._1.id, tuples)
        val rights              = MapRightsMembers(rights = x.head._1, members = profiles)
        _toMapRightsMembers(newTail, acc :+ rights)
    }

    val mapMemberQ = for {
      mapMember <- mapMember.query
      profile   <- mapMember.memberProfileTableFk
    } yield (mapMember, profile)

    val q = for {
      (mapRights, mapMember) <- mapRightsQuery joinLeft mapMemberQ on (_.id === _._1.id)
    } yield (mapRights, mapMember)

    db.run(q.result)
      .map(
        seq =>
          seq.map(
            tuples =>
              (mapRightsRepo.fromRow(tuples._1), tuples._2.flatMap { x =>
                Option(memberProfile.fromRow(x._2))
              })
        ))
      .map(_toMapRightsMembers(_, Seq()))
  }

  def getMapRightsByID(mapID: MapID, mapRightsID: MapRightsID): Future[Option[MapRights]] =
    mapRightsRepo.filter(row => row.id === mapRightsID.uuid && row.mapID === mapID.uuid).map(seq => seq.headOption)

  def getMapRightsAndUpdate(mapID: MapID, mapRightsID: MapRightsID, updateFn: MapRights => MapRights): Future[Int] = {
    getMapRightsByID(mapID, mapRightsID).flatMap {
      case Some(row) => mapRightsRepo.updateById(row.id, updateFn(row))
      case None      => Future.successful(0)
    }
  }

  def getNodeAsJSON(nodeID: NodeID): Future[Option[JsObject]] =
    nodesRepo.getById(nodeID).map {
      case Some(node) => Some(Json.toJsObject(node))
      case None       => None
    }

  def createNode(node: Node): Future[Int] = {
    nodesRepo.save(node)
  }

  def getNodeByID(mapID: MapID, segmentID: SegmentID, nodeID: NodeID): Future[Option[Node]] =
    nodesRepo
      .filter(row => row.id === nodeID.uuid && row.mapID === mapID.uuid && row.segmentID === segmentID.uuid)
      .map(seq => seq.headOption)

  def getNodeByIDAndUpdate(mapID: MapID, segmentID: SegmentID, nodeID: NodeID)(updateFn: Node => Node): Future[Int] = {
    getNodeByID(mapID, segmentID, nodeID).flatMap {
      case Some(row) => nodesRepo.updateById(row.id, updateFn(row))
      case None      => Future.successful(0)
    }
  }

  def getNodeAttributesByID(nodeID: NodeID, profileID: ProfileID): Future[Option[NodeAttributes]] =
    nodeAttributes
      .filter(row => row.nodeID === nodeID.uuid && row.profileID === profileID.uuid)
      .map(seq => seq.headOption)

  def getNodeAttrAndSave(node: Node, profileID: ProfileID)(
      updateFn: (NodeAttributes => NodeAttributes)): Future[Int] = {
    getNodeAttributesByID(node.id, profileID).flatMap {
      case Some(row) => nodeAttributes.updateById(row.id, updateFn(row))
      case None =>
        nodeAttributes.save(
          updateFn(
            NodeAttributes(
              id = NodeAttributesID.random,
              nodeID = node.id,
              profileID = profileID,
              collapsed = false
            )))
    }
  }

  def getHighestOrderByParentID(nodeID: NodeID): Future[Double] = {
    db.run(nodesRepo.filterQuery(_.parentID === nodeID.uuid).sortBy(_.order.desc).take(1).result)
      .map(
        nodes => nodes.headOption
      )
      .map {
        case Some(node) => node.order
        case None       => 0.0
      }
  }

  def getMapRootNodes(mapID: MapID): Future[Seq[Node]] =
    nodesRepo
      .filter(row => row.mapID === mapID.uuid && row.`type` === "root")

  def getMapRootNode(mapID: MapID, segmentID: SegmentID): Future[Option[Node]] =
    nodesRepo
      .filter(row => row.mapID === mapID.uuid && row.segmentID === segmentID.uuid && row.`type` === "root")
      .map(_.headOption)

  def getMapPosts(mapID: MapID): Future[Seq[NodeCardPostInfo]] = {
    def _toNodeCardPostInfo(nodeRow: NodeRow,
                            cardMaybeRow: Option[CardRow],
                            postMaybeRow: Option[PostRow]): NodeCardPostInfo = {
      NodeCardPostInfo(
        node = nodesRepo.fromRow(nodeRow),
        card = cardMaybeRow.map(cards.fromRow),
        post = postMaybeRow.map(posts.fromRow)
      )
    }

    val q = for {
      ((node, card), post) <- nodesRepo.getNodesByMapIDQuery(mapID) joinLeft
                               cards.query on (_.id === _.id) joinLeft
                               posts.query on (_._1.id === _.id)
    } yield (node, card, post)
    db.run(q.result)
      .map(seq =>
        seq.map {
          case (node, card, post) =>
            _toNodeCardPostInfo(node, card, post)
      })
  }

  private def negativesLast(a: NodeCardPostInfo, b: NodeCardPostInfo): Boolean = {
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

  def getMapContentAsJson(rootNode: Node, posts: Seq[NodeCardPostInfo]): MapIdeasExport = {
    def _nodes(parentID: NodeID): Seq[MapIdeasExport] = {
      posts
        .filter(_.node.parentID.getOrElse(blankUUID) == parentID)
        .sortWith(negativesLast)
        .map(
          x =>
            MapIdeasExport(item = NodeCardPostInfo(
                             node = x.node,
                             card = x.card,
                             post = x.post,
                           ),
                           children = _nodes(x.node.id))
        )
    }

    MapIdeasExport(item = NodeCardPostInfo(
                     node = rootNode,
                     card = None,
                     post = None,
                   ),
                   children = _nodes(rootNode.id))
  }

  def generateBlogPage(rootID: NodeID, posts: Seq[NodeCardPostInfo]): BlogPage = {
    def _formatPost(title: String, post: Option[Post]): String = (title, post) match {
      case (title, Some(post)) =>
        s"""<h3>$title</h3>""" + '\n' + post.html
      case (title, _) =>
        s"""<h3>$title</h3>""" + '\n'
    }

    def _html(parentID: NodeID, html: String): String = {
      val blankUUID = NodeID(java.util.UUID.fromString("00000000-0000-0000-0000-000000000000"))
      posts
        .filter(_.node.parentID.getOrElse(blankUUID) == parentID)
        .sortWith(negativesLast)
        .foldLeft(html)((acc, x) =>
          x match {
            case y if y.post.isDefined =>
              _html(y.node.id, acc + _formatPost(y.node.name, y.post))
            case y if (y.node.`type` == "mindmap") =>
              _html(y.node.id, acc)
            case y if (y.node.`type` == "category" || y.node.`type` == "root") =>
              _html(y.node.id, acc)
            case _ =>
              acc
        })
    }
    BlogPage(html = _html(rootID, ""))
  }

  def getChildNodes(segmentID: SegmentID, profileID: ProfileID): Future[Seq[ChildNode]] = {
    def _toChildNode(nodeNumber: Short,
                     nodeRow: NodeRow,
                     cardDueMaybeRow: Option[CardDueRow],
                     nodeAttributesMaybeRow: Option[NodeAttributesRow],
                     postMaybeRow: Option[PostRow]): ChildNode = {
      ChildNode(
        parent_node_id = nodeNumber,
        node = nodesRepo.fromRow(nodeRow),
        cardDue = cardDueMaybeRow.map(cardsDue.fromRow),
        postExists = postMaybeRow.isDefined,
        memberAttributes = nodeAttributesMaybeRow.map(nodeAttributes.fromRow)
      )
    }

    val q = for {
      (((node, cardDue), nodeAttributes), post) <- nodesRepo.getNonRootNodesBySegmentIDQuery(segmentID) joinLeft
                                                    cardsDue.query.filter(_.profileID === profileID.uuid) on (_.id === _.id) joinLeft
                                                    nodeAttributes.query.filter(_.profileID === profileID.uuid) on (_._1.id === _.nodeID) joinLeft
                                                    posts.query on (_._1._1.id === _.id)
      parent <- nodesRepo.query if node.parentID === parent.id
    } yield (parent.nodeNumber, node, cardDue, nodeAttributes, post)
    db.run(q.result)
      .map(seq =>
        seq.map {
          case (nodeNumber, node, cardDue, nodeAttributes, post) =>
            _toChildNode(nodeNumber, node, cardDue, nodeAttributes, post)
      })
  }

  def getAllChildNodeIDs(nodes: Seq[Node], parentID: NodeID): Future[Seq[NodeID]] = Future {
    getChildNodesByParentID(nodes, parentID).map(_.id)
  }

  def getFutureChildNodesByParentID(nodes: Seq[Node], parentID: NodeID): Future[Seq[Node]] = Future {
    getChildNodesByParentID(nodes, parentID)
  }

  def getChildNodesByParentID(nodes: Seq[Node], parentID: NodeID, filterRootNodes: Boolean = true): Seq[Node] = {
    val _nodes = if (filterRootNodes) nodes.filter(_.`type` != "root") else nodes
    @scala.annotation.tailrec
    def _run(children: Seq[Node], acc: Seq[Node]): Seq[Node] = (children, acc) match {
      case (Nil, Nil) => Seq()
      case (Nil, acc) => acc
      case (list, acc) =>
        val childList = _nodes.filter(x => x.parentID exists (_.uuid == list.head.id.uuid))
        _run(list.tail :++ childList, acc :++ childList)
    }

    val childList = _nodes.filter(x => x.parentID exists (_.uuid == parentID.uuid))
    val nodeList  = _run(childList, childList)
    nodeList
  }

  def getMindMap(node: Node, children: Seq[ChildNode], mapRightsBrief: MapRightsBrief): MapIdeas = {
    def someOrNone(ideas: Map[String, MapIdea]): Option[Map[String, MapIdea]] = {
      if (ideas.nonEmpty) { Some(ideas) } else { None }
    }

    def getCollapsed(attr: Option[NodeAttributes]): Boolean = attr match {
      case Some(attributes) => attributes.collapsed
      case None             => false
    }

    def mapIdeas(parent_id: Short): Map[String, MapIdea] = {
      children
        .filter(_.parent_node_id == parent_id)
        .map(
          x =>
            (
              x.node.order.toString.replaceAll("\\.0$", ""), // because Javascript is retarded
              MapIdea(
                id = x.node.nodeNumber.toString,
                title = x.node.name,
                attr = NodeAttr(
                  collapsed = getCollapsed(x.memberAttributes),
                  postExists = x.postExists,
                  id = x.node.id.toString,
                  nodeType = x.node.`type`,
                  parentID = None,
                  mapID = x.node.mapID.toString,
                  path = x.node.path,
                  style = NodeAttrStyle(background = x.nodeColor)
                ),
                ideas = someOrNone(mapIdeas(x.node.nodeNumber))
              )
          ))
        .toMap
    }

    MapIdeas(
      id = node.nodeNumber.toString,
      title = node.name,
      attr = NodeAttr(
        collapsed = false,
        postExists = false,
        id = node.id.toString,
        parentID = Some(node.parentID.getOrElse("").toString),
        mapID = node.mapID.toString,
        path = node.path,
        nodeType = node.`type`,
        style = NodeAttrStyle(background = "#e0e0e0")
      ),
      formatVersion = "2.0",
      ideas = someOrNone(mapIdeas(node.nodeNumber)),
      permissions = mapRightsBrief
    )
  }

  def deleteNode(mapID: MapID, segmentID: SegmentID, nodeID: NodeID): Future[Int] = {
    db.run(
      nodesRepo
        .filterQuery(row => row.mapID === mapID.uuid && row.segmentID === segmentID.uuid && row.id === nodeID.uuid)
        .delete)
  }

  def convertNodeToMindMap(node: Node, profileID: ProfileID): Future[SegmentID] = {
    for {
      children     <- getChildNodes(node.segmentID, profileID) // TODO verify segmentID is the right choice
      subNodes     <- getFutureChildNodesByParentID(children.map(_.node), node.id)
      newSegmentID <- convertNodeToMindMap(node, subNodes)
    } yield newSegmentID
  }

  private def convertNodeToMindMap(rootNode: Node, subNodes: Seq[Node]): Future[SegmentID] = {
    val newSegmentID = SegmentID(rootNode.id)
    val newPath      = rootNode.path + ":" + newSegmentID.uuid.toString

    val updateSubNodesSegmentIDs = DBIO.sequence(subNodes.map(node => {
      nodesRepo.query.filter(_.id === node.id.uuid).map(_.segmentID).update(newSegmentID)
    }))
    val updateSubNodesPathIDs = DBIO.sequence(subNodes.map(node => {
      nodesRepo.query.filter(_.id === node.id.uuid).map(_.path).update(newPath)
    }))
    // TODO - test updateSubMapsPathIDs
    val updateSubMapsPathIDs = DBIO.sequence(
      subNodes
        .filter(_.`type` == "mindmap")
        .map(node => {
          val oldPathPrefix = node.path + ":" + node.id
          val newPathPrefix = s"""^${node.path}:(.*)""".r.replaceAllIn(oldPathPrefix, m => newPath + ":" + m.group(1))
          nodesRepo.query
            .filter(row => row.path like s"#$oldPathPrefix%")
            .map(_.path)
            .update(node.path.replaceAll("^%s(.*)".format(oldPathPrefix), s"$newPathPrefix" + "$1"))
        }))

//    val _updateSubMapsPathIDs = DBIO.sequence(subNodes
//      .filter(_.`type` == "mindmap")
//      .map(node => {
//        //nodes.query.filter(_.id === node.id).map(_.path).update(rootNode.path + ":" + rootNode.id.toString)
//        val oldPathPrefix = node.path + ":" + node.id
//        val newPathPrefix = s"""^${node.path}:(.*)""".r.replaceAllIn(oldPathPrefix, m => newPath + ":" + m.group(1))
//        // TODO - rewrite this in slick
//        val sqlq =
//          sqlu"""UPDATE map.node SET path = REGEXP_REPLACE(path, '^#$oldPathPrefix(.*)', '#$newPathPrefix\1') WHERE path LIKE '#$oldPathPrefix%'"""
//        sqlq
//      }))

    val newRootNode = Node(
      id = NodeID.random,
      nodeNumber = 1,
      parentID = None, // this will be assigned in the following DB run
      mapID = rootNode.mapID,
      segmentID = newSegmentID,
      path = newPath,
      order = 1,
      name = rootNode.name,
      `type` = "root",
      root = Some(1)
    )

    db.run(
        ((nodesRepo.query += nodesRepo.toRow(newRootNode)) andThen
          nodesRepo.filterQuery(_.id === rootNode.id.uuid).map(_.`type`).update("mindmap") andThen
          nodesRepo.filterQuery(_.parentID === rootNode.id.uuid).map(_.parentID).update(Some(newRootNode.id)) andThen
          nodesRepo.filterQuery(_.id === newRootNode.id.uuid).map(_.parentID).update(Some(rootNode.id)) andThen
          updateSubNodesSegmentIDs andThen
          updateSubMapsPathIDs andThen
          updateSubNodesPathIDs).transactionally)
      .map(_ => newRootNode.segmentID)
  }
}
