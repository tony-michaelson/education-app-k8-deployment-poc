// this file is copied to src_managed/.../io.masterypath.slick/
// SBT build uses comments to edit code during copy operation
// this is done so we can extend BaseRepositories and let calling classes keep the same import statements
package repos.custom // codeGen:remove
// codeGen:add package io.masterypath.slick

import java.util.UUID

import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.PostgresProfile.api._
import io.masterypath.slick.Tables.NodeTable
import io.masterypath.slick.{BaseNodeRepo, MapID}
import play.api.libs.json.{Format, JsResult, JsValue, Json}
import play.api.mvc.PathBindable
import scala.language.implicitConversions

import scala.concurrent.ExecutionContext

class NodeRepo @Inject()(implicit dbConfigProvider: DatabaseConfigProvider, ec: ExecutionContext)
    extends BaseNodeRepo(dbConfigProvider = dbConfigProvider) {

  def getNonRootNodesBySegmentIDQuery(segmentID: SegmentID): Query[NodeTable, NodeTable#TableElementType, scala.Seq] = {
    filterQuery(
      node =>
        node.`type` =!= "root"
          && node.segmentID === segmentID.uuid)
  }

  def getNodesByMapIDQuery(mapID: MapID): Query[NodeTable, NodeTable#TableElementType, scala.Seq] = {
    filterQuery(node => node.mapID === mapID.uuid)
  }

  def getNodesByPathIDQuery(pathID: String): Query[NodeTable, NodeTable#TableElementType, scala.Seq] = {
    filterQuery(node => node.path like pathID + "%")
  }

}

case class SegmentID(uuid: UUID) {
  override def toString = uuid.toString
}
object SegmentID {
  implicit val formatter: Format[SegmentID] =
    new Format[SegmentID] with Serializable {
      override def writes(o: SegmentID): JsValue = Json.valueWrites[SegmentID].writes(o)

      override def reads(json: JsValue): JsResult[SegmentID] = Json.valueReads[SegmentID].reads(json)
    }

  def apply(id: UUID): SegmentID           = new SegmentID(uuid = id)
  implicit def toUUID(id: SegmentID): UUID = id.uuid
  implicit def toUUIDOption(id: Option[SegmentID]): Option[UUID] = id flatMap { x =>
    Some(x.uuid)
  }

  def random: SegmentID = new SegmentID(uuid = UUID.randomUUID())

  implicit def pathBinder(implicit stringBinder: PathBindable[String]) = new PathBindable[SegmentID] {
    def tryUUID(str: String) =
      try {
        Right(SegmentID(id = UUID.fromString(str)))
      } catch {
        case _: Exception =>
          Left("Unable to parse UUID: " + str)
      }

    override def bind(key: String, value: String): Either[String, SegmentID] = {
      stringBinder.bind(key, value) match {
        case Right(idString) => tryUUID(idString)
        case Left(error)     => Left(error)
      }
    }
    override def unbind(key: String, id: SegmentID): String = {
      id.toString
    }
  }
}
