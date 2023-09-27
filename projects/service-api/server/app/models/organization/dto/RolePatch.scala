package models.organization.dto

import ai.x.play.json.Jsonx
import ai.x.play.json.Encoders.encoder
import play.api.libs.json.OFormat

case class RolePatch(
    name: Option[String],
    autoJoin: Option[Boolean],
    blogApprove: Option[Boolean],
    blogCreate: Option[Boolean],
    blogDelete: Option[Boolean],
    blogPublish: Option[Boolean],
    manageBlog: Option[Boolean],
    manageContests: Option[Boolean],
    manageMarketingCampaigns: Option[Boolean],
    manageOrganizationBilling: Option[Boolean],
    manageOrganizationConfig: Option[Boolean],
    manageOrganizationMembers: Option[Boolean],
    manageOrganizationPermissions: Option[Boolean],
    manageOrganizationWhitelabel: Option[Boolean],
    manageRewardsProgram: Option[Boolean],
    manageSalesAds: Option[Boolean],
    manageSalesCertificates: Option[Boolean],
    manageSalesCourses: Option[Boolean],
    manageSalesMemberships: Option[Boolean],
    manageSalesOrganizations: Option[Boolean],
    manageTrainingBreakTime: Option[Boolean],
    manageTrainingComplianceEnforcement: Option[Boolean],
    manageTrainingQuotes: Option[Boolean],
    manageTrainingSessionSettings: Option[Boolean],
    manageProblemBoard: Option[Boolean],
    mapApprove: Option[Boolean],
    mapCreate: Option[Boolean],
    mapCreateDocuments: Option[Boolean],
    mapDirectory: Option[Boolean],
    mapFeedback: Option[Boolean],
    mapFork: Option[Boolean],
    mapMnemonics: Option[Boolean],
    mapModify: Option[Boolean],
    mapPermissions: Option[Boolean],
    mapPublish: Option[Boolean],
    mapShare: Option[Boolean],
    mapStats: Option[Boolean],
    mapTraining: Option[Boolean],
    mapTransfer: Option[Boolean],
    mapView: Option[Boolean],
    organizationInvite: Option[Boolean],
    organizationPublish: Option[Boolean],
    trainingRankings: Option[Boolean],
    trainingReporting: Option[Boolean]
)
object RolePatch {
  implicit val rolePatchJsonFormat: OFormat[RolePatch] = Jsonx.formatCaseClass[RolePatch]
}
