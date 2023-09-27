package models.organization.dto

import ai.x.play.json.Jsonx
import ai.x.play.json.Encoders.encoder
import play.api.libs.json.OFormat

case class MemberOrgPermissions(blogApprove: Boolean = false,
                                blogCreate: Boolean = false,
                                blogDelete: Boolean = false,
                                blogPublish: Boolean = false,
                                manageBlog: Boolean = false,
                                manageContests: Boolean = false,
                                manageMarketingCampaigns: Boolean = false,
                                manageOrganizationBilling: Boolean = false,
                                manageOrganizationConfig: Boolean = false,
                                manageOrganizationMembers: Boolean = false,
                                manageOrganizationPermissions: Boolean = false,
                                manageOrganizationWhitelabel: Boolean = false,
                                manageRewardsProgram: Boolean = false,
                                manageSalesAds: Boolean = false,
                                manageSalesCertificates: Boolean = false,
                                manageSalesCourses: Boolean = false,
                                manageSalesMemberships: Boolean = false,
                                manageSalesOrganizations: Boolean = false,
                                manageTrainingBreakTime: Boolean = false,
                                manageTrainingComplianceEnforcement: Boolean = false,
                                manageTrainingQuotes: Boolean = false,
                                manageTrainingSessionSettings: Boolean = false,
                                manageProblemBoard: Boolean = false,
                                mapApprove: Boolean = false,
                                mapCreate: Boolean = false,
                                mapCreateDocuments: Boolean = false,
                                mapDirectory: Boolean = false,
                                mapFeedback: Boolean = false,
                                mapFork: Boolean = false,
                                mapMnemonics: Boolean = false,
                                mapModify: Boolean = false,
                                mapPermissions: Boolean = false,
                                mapPublish: Boolean = false,
                                mapShare: Boolean = false,
                                mapStats: Boolean = false,
                                mapTraining: Boolean = false,
                                mapTransfer: Boolean = false,
                                mapView: Boolean = false,
                                organizationInvite: Boolean = false,
                                organizationPublish: Boolean = false,
                                trainingRankings: Boolean = false,
                                trainingReporting: Boolean = false)
object MemberOrgPermissions {
  implicit val f: OFormat[MemberOrgPermissions] = Jsonx.formatCaseClass[MemberOrgPermissions]
}
