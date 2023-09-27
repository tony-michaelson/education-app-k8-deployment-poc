package services

import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext
import io.masterypath.slick.{MapID, MapRightsRepo, MindMapRepo, OrgID, RoleRepo}
import io.masterypath.slick.Tables.{MapRightsRow, MapRightsTable}

@Singleton
class PermissionService @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  implicit val dbConfProvider: DatabaseConfigProvider = dbConfigProvider
  private val dbConfig                                = dbConfigProvider.get[JdbcProfile]
  import dbConfig.profile
  import profile.api._
  import io.masterypath.slick.Tables.{RoleTable, RoleRow}
  import slick.lifted.{Query, Rep}

  private val orgPermissions = new RoleRepo
  private val mapRights      = new MapRightsRepo
  private val maps           = new MindMapRepo

  def buildQuery(orgID: OrgID, permList: Seq[RoleTable => Rep[Boolean]]): Query[RoleTable, RoleRow, Seq] = {
    @scala.annotation.tailrec
    def _helper(acc: Query[RoleTable, RoleRow, Seq],
                permList: Seq[RoleTable => Rep[Boolean]]): Query[RoleTable, RoleRow, Seq] =
      permList match {
        case Nil          => acc
        case head :: tail => _helper(acc.filter(head), tail)
      }
    _helper(orgPermissions.filterQuery(_.orgID === orgID.uuid), permList)
  }

  object Blog {
    val approve: RoleTable => Rep[Boolean] = (t: RoleTable) => t.blogApprove === true
    val create: RoleTable => Rep[Boolean]  = (t: RoleTable) => t.blogCreate === true
    val delete: RoleTable => Rep[Boolean]  = (t: RoleTable) => t.blogDelete === true
    val publish: RoleTable => Rep[Boolean] = (t: RoleTable) => t.blogPublish === true
  }

  object Manage {
    val blog: RoleTable => Rep[Boolean]                    = (t: RoleTable) => t.manageBlog === true
    val contests: RoleTable => Rep[Boolean]                = (t: RoleTable) => t.manageContests === true
    val marketingCampaigns: RoleTable => Rep[Boolean]      = (t: RoleTable) => t.manageMarketingCampaigns === true
    val organizationBilling: RoleTable => Rep[Boolean]     = (t: RoleTable) => t.manageOrganizationBilling === true
    val organizationConfig: RoleTable => Rep[Boolean]      = (t: RoleTable) => t.manageOrganizationConfig === true
    val organizationMembers: RoleTable => Rep[Boolean]     = (t: RoleTable) => t.manageOrganizationMembers === true
    val organizationPermissions: RoleTable => Rep[Boolean] = (t: RoleTable) => t.manageOrganizationPermissions === true
    val organizationWhitelabel: RoleTable => Rep[Boolean]  = (t: RoleTable) => t.manageOrganizationWhitelabel === true
    val rewardsProgram: RoleTable => Rep[Boolean]          = (t: RoleTable) => t.manageRewardsProgram === true
    val salesAds: RoleTable => Rep[Boolean]                = (t: RoleTable) => t.manageSalesAds === true
    val salesCertificates: RoleTable => Rep[Boolean]       = (t: RoleTable) => t.manageSalesCertificates === true
    val salesCourses: RoleTable => Rep[Boolean]            = (t: RoleTable) => t.manageSalesCourses === true
    val salesMemberships: RoleTable => Rep[Boolean]        = (t: RoleTable) => t.manageSalesMemberships === true
    val salesOrganizations: RoleTable => Rep[Boolean]      = (t: RoleTable) => t.manageSalesOrganizations === true
    val trainingBreakTime: RoleTable => Rep[Boolean]       = (t: RoleTable) => t.manageTrainingBreakTime === true
    val trainingComplianceEnforcement: RoleTable => Rep[Boolean] = (t: RoleTable) =>
      t.manageTrainingComplianceEnforcement === true
    val trainingQuotes: RoleTable => Rep[Boolean]          = (t: RoleTable) => t.manageTrainingQuotes === true
    val trainingSessionSettings: RoleTable => Rep[Boolean] = (t: RoleTable) => t.manageTrainingSessionSettings === true
    val problemBoard: RoleTable => Rep[Boolean]            = (t: RoleTable) => t.manageProblemBoard === true
  }

  object MapsGlobal {
    val approve: RoleTable => Rep[Boolean]         = (t: RoleTable) => t.mapApprove === true
    val create: RoleTable => Rep[Boolean]          = (t: RoleTable) => t.mapCreate === true
    val createDocuments: RoleTable => Rep[Boolean] = (t: RoleTable) => t.mapCreateDocuments === true
    val directory: RoleTable => Rep[Boolean]       = (t: RoleTable) => t.mapDirectory === true
    val feedback: RoleTable => Rep[Boolean]        = (t: RoleTable) => t.mapFeedback === true
    val fork: RoleTable => Rep[Boolean]            = (t: RoleTable) => t.mapFork === true
    val mnemonics: RoleTable => Rep[Boolean]       = (t: RoleTable) => t.mapMnemonics === true
    val modify: RoleTable => Rep[Boolean]          = (t: RoleTable) => t.mapModify === true
    val permissions: RoleTable => Rep[Boolean]     = (t: RoleTable) => t.mapPermissions === true
    val publish: RoleTable => Rep[Boolean]         = (t: RoleTable) => t.mapPublish === true
    val share: RoleTable => Rep[Boolean]           = (t: RoleTable) => t.mapShare === true
    val stats: RoleTable => Rep[Boolean]           = (t: RoleTable) => t.mapStats === true
    val training: RoleTable => Rep[Boolean]        = (t: RoleTable) => t.mapTraining === true
    val transfer: RoleTable => Rep[Boolean]        = (t: RoleTable) => t.mapTransfer === true
    val view: RoleTable => Rep[Boolean]            = (t: RoleTable) => t.mapView === true
  }

  object MapsLocal {
    // If map.published == true, then these permissions are not applicable.
    // Only global checks are valid for published maps.
    def admin(orgID: OrgID, mapID: MapID): Query[MapRightsTable, MapRightsRow, Seq] =
      for {
        map       <- maps.filterQuery(row => row.id === mapID.uuid && row.orgID === orgID.uuid)
        mapRights <- mapRights.filterQuery(row => row.mapID === map.id)
        if mapRights.admin === true || map.published === true
      } yield mapRights

    def feedback(orgID: OrgID, mapID: MapID): Query[MapRightsTable, MapRightsRow, Seq] =
      for {
        map       <- maps.filterQuery(row => row.id === mapID.uuid && row.orgID === orgID.uuid)
        mapRights <- mapRights.filterQuery(row => row.mapID === map.id)
        if (mapRights.feedback === true || mapRights.admin === true) || map.published === true
      } yield mapRights

    def mnemonics(orgID: OrgID, mapID: MapID): Query[MapRightsTable, MapRightsRow, Seq] =
      for {
        map       <- maps.filterQuery(row => row.id === mapID.uuid && row.orgID === orgID.uuid)
        mapRights <- mapRights.filterQuery(row => row.mapID === map.id)
        if (mapRights.mnemonics === true || mapRights.admin === true) || map.published === true
      } yield mapRights

    def modify(orgID: OrgID, mapID: MapID): Query[MapRightsTable, MapRightsRow, Seq] =
      for {
        map       <- maps.filterQuery(row => row.id === mapID.uuid && row.orgID === orgID.uuid)
        mapRights <- mapRights.filterQuery(row => row.mapID === map.id)
        if (mapRights.modify === true || mapRights.admin === true) || map.published === true
      } yield mapRights

    def permissions(orgID: OrgID, mapID: MapID): Query[MapRightsTable, MapRightsRow, Seq] =
      for {
        map       <- maps.filterQuery(row => row.id === mapID.uuid && row.orgID === orgID.uuid)
        mapRights <- mapRights.filterQuery(row => row.mapID === map.id)
        if (mapRights.permissions === true || mapRights.admin === true) || map.published === true
      } yield mapRights

    def publish(orgID: OrgID, mapID: MapID): Query[MapRightsTable, MapRightsRow, Seq] =
      for {
        map       <- maps.filterQuery(row => row.id === mapID.uuid && row.orgID === orgID.uuid)
        mapRights <- mapRights.filterQuery(row => row.mapID === map.id)
        if (mapRights.publish === true || mapRights.admin === true) || map.published === true
      } yield mapRights

    def share(orgID: OrgID, mapID: MapID): Query[MapRightsTable, MapRightsRow, Seq] =
      for {
        map       <- maps.filterQuery(row => row.id === mapID.uuid && row.orgID === orgID.uuid)
        mapRights <- mapRights.filterQuery(row => row.mapID === map.id)
        if (mapRights.share === true || mapRights.admin === true) || map.published === true
      } yield mapRights

    def stats(orgID: OrgID, mapID: MapID): Query[MapRightsTable, MapRightsRow, Seq] =
      for {
        map       <- maps.filterQuery(row => row.id === mapID.uuid && row.orgID === orgID.uuid)
        mapRights <- mapRights.filterQuery(row => row.mapID === map.id)
        if (mapRights.stats === true || mapRights.admin === true) || map.published === true
      } yield mapRights

    def training(orgID: OrgID, mapID: MapID): Query[MapRightsTable, MapRightsRow, Seq] =
      for {
        map       <- maps.filterQuery(row => row.id === mapID.uuid && row.orgID === orgID.uuid)
        mapRights <- mapRights.filterQuery(row => row.mapID === map.id)
        if (mapRights.training === true || mapRights.admin === true) || map.published === true
      } yield mapRights

    def transfer(orgID: OrgID, mapID: MapID): Query[MapRightsTable, MapRightsRow, Seq] =
      for {
        map       <- maps.filterQuery(row => row.id === mapID.uuid && row.orgID === orgID.uuid)
        mapRights <- mapRights.filterQuery(row => row.mapID === map.id)
        if (mapRights.transfer === true || mapRights.admin === true) || map.published === true
      } yield mapRights

    def view(orgID: OrgID, mapID: MapID): Query[MapRightsTable, MapRightsRow, Seq] =
      for {
        map       <- maps.filterQuery(row => row.id === mapID.uuid && row.orgID === orgID.uuid)
        mapRights <- mapRights.filterQuery(row => row.mapID === map.id)
        if (mapRights.view === true || mapRights.admin === true) || map.published === true
      } yield mapRights
  }

  object Organization {
    val invite: RoleTable => Rep[Boolean]  = (t: RoleTable) => t.organizationInvite === true
    val publish: RoleTable => Rep[Boolean] = (t: RoleTable) => t.organizationPublish === true
  }

  object Training {
    val rankings: RoleTable => Rep[Boolean]  = (t: RoleTable) => t.trainingRankings === true
    val reporting: RoleTable => Rep[Boolean] = (t: RoleTable) => t.trainingReporting === true
  }

}
