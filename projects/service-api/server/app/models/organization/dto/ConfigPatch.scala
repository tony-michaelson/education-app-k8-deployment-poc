package models.organization.dto

import ai.x.play.json.Jsonx
import ai.x.play.json.Encoders.encoder
import play.api.libs.json.OFormat

case class ConfigPatch(
    blog: Option[Boolean],
    contests: Option[Boolean],
    defaultLanguage: Option[String],
    mapDocumentationGeneration: Option[Boolean],
    marketingCampaigns: Option[Boolean],
    marketingEngagementCampaigns: Option[Boolean],
    problemBoard: Option[Boolean],
    salesAds: Option[Boolean],
    salesCertificates: Option[Boolean],
    salesCourses: Option[Boolean],
    salesFreeTrials: Option[Boolean],
    salesMemberFeesActive: Option[Boolean],
    salesMemberFeesStatic: Option[Boolean],
    salesMemberships: Option[Boolean],
    salesOrganizations: Option[Boolean],
    supportTier: Option[Short],
    trainingAnswerTimeTracking: Option[Boolean],
    trainingBreakTime: Option[Boolean],
    trainingComplianceEnforcement: Option[Boolean],
    trainingComments: Option[Boolean],
    trainingContentPageStudentSubmission: Option[Boolean],
    trainingContentPageTimeTracking: Option[Boolean],
    trainingContentPageUpvote: Option[Boolean],
    trainingCorrectAnswerAnimation: Option[Boolean],
    trainingCorrectAnswerSound: Option[Boolean],
    trainingFeedback: Option[Boolean],
    trainingLearningPaths: Option[Boolean],
    trainingMnemonics: Option[Boolean],
    trainingQuotes: Option[Boolean],
    trainingRankings: Option[Boolean],
    trainingReporting: Option[Boolean],
    trainingRewardsProgram: Option[Boolean],
    trainingSessionEndCelebration: Option[Boolean],
    trainingSessionEndFeedback: Option[Boolean],
    trainingStraightThruMode: Option[Boolean],
    trainingStudyGoals: Option[Boolean],
    trainingVirtualLabs: Option[Boolean],
    whiteLabeled: Option[Boolean],
    memberMonthlyCost: Option[Double],
    memberAnnualCost: Option[Double],
    memberPaymentMethodRequired: Option[Boolean],
)
object ConfigPatch {
  implicit val rolePostJsonFormat: OFormat[ConfigPatch] = Jsonx.formatCaseClass[ConfigPatch]
}
