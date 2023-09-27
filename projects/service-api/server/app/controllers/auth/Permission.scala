package controllers.auth

import io.masterypath.slick.OrgID
import io.masterypath.slick.Tables.{MapRightsRow, MapRightsTable, RoleTable}
import slick.lifted.{Query, Rep}

case class Permission(
    orgID: Option[OrgID],
    permission: Option[(RoleTable) => (Rep[Boolean])],
    permList: Option[Seq[(RoleTable) => (Rep[Boolean])]],
    mapRights: Option[Query[MapRightsTable, MapRightsRow, Seq]]
)
object Permission {
  def apply(): Permission = {
    new Permission(None, None, None, None)
  }
  def apply(orgID: OrgID): Permission = {
    new Permission(Some(orgID), None, None, None)
  }
  def apply(
      orgID: OrgID,
      permission: (RoleTable) => (Rep[Boolean])
  ): Permission = {
    new Permission(Some(orgID), Some(permission), None, None)
  }
  def apply(
      orgID: OrgID,
      permList: Seq[(RoleTable) => (Rep[Boolean])]
  ): Permission = {
    new Permission(Some(orgID), None, Some(permList), None)
  }
  def apply(
      orgID: OrgID,
      permission: (RoleTable) => (Rep[Boolean]),
      mapRights: Query[MapRightsTable, MapRightsRow, Seq]
  ): Permission = {
    new Permission(Some(orgID), Some(permission), None, Some(mapRights))
  }
  def apply(
      orgID: OrgID,
      permList: Seq[(RoleTable) => (Rep[Boolean])],
      mapRights: Query[MapRightsTable, MapRightsRow, Seq]
  ): Permission = {
    new Permission(Some(orgID), None, Some(permList), Some(mapRights))
  }
}
