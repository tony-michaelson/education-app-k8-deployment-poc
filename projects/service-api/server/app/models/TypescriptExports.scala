package models

import com.scalatsi.TypescriptType.{TSLiteralString, TSUnion}
import io.masterypath.slick.{
  AnswerChoice,
  Config,
  FlashcardTypeID,
  MapID,
  MapRightsID,
  MemberProfile,
  MindMap,
  NodeID,
  OrgID,
  ProfileID,
  RoleID,
  RoleInviteID,
  SegmentID
}
import models.flashcard.{AnswerChoiceBriefEdit, AnswerChoiceBriefTest}
import models.flashcard.dto.{
  CardAnswer,
  CardBriefEdit,
  CardBriefTest,
  CardMetaData,
  CardPost,
  CardsDue,
  FlashcardTypeBrief,
  Quality,
  TestAnswer
}
import models.flashcard.exercise.dto.{Exercise, ExerciseAnswer, ExerciseUpdate, UserCode}
import models.mindmap.dto.{
  BlogPage,
  MapMode,
  MapProperties,
  MapPropertiesPatch,
  MapRightsMembers,
  MapRightsPatch,
  NodeCardPostInfo,
  NodePatch,
  NodePost,
  PostMarkdown,
  PostTimeRead
}
import models.organization.dto.{ConfigPatch, MemberOrgPermissions, MemberProfileEmail, MemberRegistration, OrgLink}
import com.scalatsi._

object TypescriptExports extends DefaultTSTypes {
  // *note; no static type definitions on these implicits or scalatsi will error on compile
  implicit val tsFlashcardTypeID = TSType.alias[FlashcardTypeID, String]
  implicit val tsMapID           = TSType.alias[MapID, String]
  implicit val tsNodeID          = TSType.alias[NodeID, String]
  implicit val tsProfileID       = TSType.alias[ProfileID, String]
  implicit val tsMapRightsID     = TSType.alias[MapRightsID, String]
  implicit val tsRoleID          = TSType.alias[RoleID, String]
  implicit val tsRoleInviteID    = TSType.alias[RoleInviteID, String]
  implicit val tsOrgID           = TSType.alias[OrgID, String]
  implicit val tsSegmentID       = TSType.alias[SegmentID, String]

  // exercise
  implicit val tsExerciseAnswer = TSType.fromCaseClass[ExerciseAnswer]
  implicit val tsExercise       = TSType.fromCaseClass[Exercise]
  implicit val tsExerciseUpdate = TSType.fromCaseClass[ExerciseUpdate]
  implicit val tsUserCode       = TSType.fromCaseClass[UserCode]

  // flashcard
  implicit val tsAnswerChoice       = TSType.fromCaseClass[AnswerChoice]
  implicit val tsFlashcardTypeBrief = TSType.fromCaseClass[FlashcardTypeBrief]
  implicit val tsAnswer             = TSType.fromCaseClass[CardAnswer]
  implicit val tsCardDue            = TSType.fromCaseClass[CardMetaData]
  implicit val tsCard               = TSType.fromCaseClass[CardPost]
  implicit val tsCardsDue           = TSType.fromCaseClass[CardsDue]
  implicit val tsQuality            = TSType.fromCaseClass[Quality]
  implicit val tsTestAnswer         = TSType.fromCaseClass[TestAnswer]

  // member
  implicit val tsMemberRegistration   = TSType.fromCaseClass[MemberRegistration]
  implicit val tsMember               = TSType.fromCaseClass[MemberProfileEmail]
  implicit val tsMemberProfile        = TSType.fromCaseClass[MemberProfile]
  implicit val tsConfig               = TSType.fromCaseClass[Config]
  implicit val tsConfigPatch          = TSType.fromCaseClass[ConfigPatch]
  implicit val tsMemberOrgs           = TSType.fromCaseClass[OrgLink]
  implicit val tsMemberOrgPermissions = TSType.fromCaseClass[MemberOrgPermissions]

  // maps
  def enumTsType[E <: enumeratum.EnumEntry, AllValues <: enumeratum.Enum[E]: Manifest](enum: AllValues)(
      implicit m: Manifest[AllValues]): TSType[E] =
    TSType.alias[E](
      m.runtimeClass.getSimpleName.stripSuffix("$"),
      TSUnion(enum.values.map(e => TSLiteralString(e.entryName)))
    )
  implicit val myEnumTsType: TSType[MapMode] = enumTsType(MapMode)

  implicit val tsBlogPage              = TSType.fromCaseClass[BlogPage]
  implicit val tsMapProperties         = TSType.fromCaseClass[MapProperties]
  implicit val tsMapRightsMembers      = TSType.fromCaseClass[MapRightsMembers]
  implicit val tsMindMap               = TSType.fromCaseClass[MindMap]
  implicit val tsMapPropertiesPatch    = TSType.fromCaseClass[MapPropertiesPatch]
  implicit val tsMapRightsPatch        = TSType.fromCaseClass[MapRightsPatch]
  implicit val tsNodePatch             = TSType.fromCaseClass[NodePatch]
  implicit val tsNodeCardPostInfo      = TSType.fromCaseClass[NodeCardPostInfo]
  implicit val tsNodePost              = TSType.fromCaseClass[NodePost]
  implicit val tsAnswerChoiceBriefEdit = TSType.fromCaseClass[AnswerChoiceBriefEdit]
  implicit val tsAnswerChoiceBriefTest = TSType.fromCaseClass[AnswerChoiceBriefTest]
  implicit val tsCardBriefEdit         = TSType.fromCaseClass[CardBriefEdit]
  implicit val tsCardBriefTest         = TSType.fromCaseClass[CardBriefTest]
  implicit val tsPostInfo              = TSType.fromCaseClass[PostTimeRead]
  implicit val tsPost                  = TSType.fromCaseClass[PostMarkdown]
}
