package repos

import java.util.UUID

import slick.jdbc.PostgresProfile.api._

trait BaseEntity {
  val id: UUID
}

abstract class BaseTable[Entity](
    tag: Tag,
    schemaName: Option[String],
    tableName: String
) extends Table[Entity](tag, schemaName, tableName) {
  val id: Rep[UUID] = column[UUID]("id", O.PrimaryKey)
}
