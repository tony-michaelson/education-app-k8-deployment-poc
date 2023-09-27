package repos

import java.util.UUID

import slick.jdbc.PostgresProfile.api._
import slick.lifted.{CanBeQueryCondition, Rep, TableQuery}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.{JdbcProfile, PostgresProfile}

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect._

trait BaseRepositoryComponent[Tbl <: BaseTable[Row], Row <: BaseEntity, NonRow] {
  def getById(id: UUID): Future[Option[NonRow]]
  def getAll: Future[Seq[NonRow]]
  def filter[Condition <: Rep[_]](expr: Tbl => Condition)(
      implicit wt: CanBeQueryCondition[Condition]): Future[Seq[NonRow]]
  def save(row: NonRow): Future[Int]
  def save(row: Seq[NonRow]): Future[Int]
  def deleteById(id: UUID): Future[Int]
  def updateById(id: UUID, row: NonRow): Future[Int]
}

trait BaseRepositoryQuery[Tbl <: BaseTable[Row], Row <: BaseEntity, NonRow] {

  val query: TableQuery[Tbl]

  def getByIdQuery(id: UUID): Query[Tbl, Tbl#TableElementType, scala.Seq] = {
    query.filter(_.id === id)
  }

  def getAllQuery: TableQuery[Tbl] = {
    query
  }

  def filterQuery[Condition <: Rep[_]](expr: Tbl => Condition)(
      implicit wt: CanBeQueryCondition[Condition]): Query[Tbl, Tbl#TableElementType, scala.Seq] = {
    query.filter(expr)
  }

  def saveQuery(row: Row): PostgresProfile.ProfileAction[Int, NoStream, Effect.Write] = {
    query += row
  }

  def saveQuery(rows: Seq[Row]): PostgresProfile.ProfileAction[
    PostgresProfile.InsertActionExtensionMethods[Tbl#TableElementType]#MultiInsertResult,
    NoStream,
    Effect.Write] = {
    query ++= rows
  }

  def deleteByIdQuery(id: UUID): PostgresProfile.ProfileAction[Int, NoStream, Effect.Write] = {
    query.filter(_.id === id).delete
  }

  def updateByIdQuery(id: UUID, row: Row): PostgresProfile.ProfileAction[Int, NoStream, Effect.Write] = {
    query.filter(_.id === id).update(row)
  }

}

abstract class BaseRepository[Tbl <: BaseTable[Row], Row <: BaseEntity: ClassTag, NonRow](
    tableQuery: TableQuery[Tbl],
    dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
    extends BaseRepositoryQuery[Tbl, Row, NonRow]
    with BaseRepositoryComponent[Tbl, Row, NonRow] {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  val db    = dbConfig.db
  val query = tableQuery

  def fromRow(row: Row): NonRow
  def toRow(nonRow: NonRow): Row

  def getAll: Future[Seq[NonRow]] = {
    db.run(getAllQuery.result).map(seq => seq.map(fromRow))
  }

  def getById(id: UUID): Future[Option[NonRow]] = {
    db.run(getByIdQuery(id).result.headOption).map(maybeRow => maybeRow.map(fromRow))
  }

  def filter[Condition <: Rep[_]](expr: Tbl => Condition)(
      implicit wt: CanBeQueryCondition[Condition]): Future[Seq[NonRow]] = {
    db.run(filterQuery(expr).result).map(seq => seq.map(fromRow))
  }

  def save(row: NonRow): Future[Int] = {
    db.run(saveQuery(toRow(row)))
  }

  def save(rows: Seq[NonRow]): Future[Int] = {
    db.run(saveQuery(rows.map(toRow))).map(_.getOrElse(0))
  }

  def updateById(id: UUID, row: NonRow): Future[Int] = {
    db.run(updateByIdQuery(id, toRow(row)))
  }

  def getByIdAndUpdate(id: UUID, updateFn: (NonRow => NonRow)): Future[Int] = {
    db.run(getByIdQuery(id).result.headOption).flatMap {
      case Some(row) => updateById(id, updateFn(fromRow(row)))
      case None      => Future.successful(0)
    }
  }

  def deleteById(id: UUID): Future[Int] = {
    db.run(deleteByIdQuery(id))
  }

}
