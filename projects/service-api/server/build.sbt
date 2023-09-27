name := """masterypath.io"""
maintainer := "customersupport@masterypath.io"

version := "0.7.6"
scalaVersion := "2.13.2"

resolvers += Resolver.jcenterRepo

resolvers += "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

// Updated 30JUNE2020
libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-slick" % "5.0.0",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.12.3",
  "com.amazonaws" % "aws-java-sdk-s3" % "1.12.10",
  ws,
  "org.postgresql"    % "postgresql" % "42.2.22",
  "com.pauldijou"     %% "jwt-play" % "5.0.0",
  "com.auth0"         % "jwks-rsa" % "0.18.0",
  "net.codingwell"    %% "scala-guice" % "5.0.1",
  "com.iheart"        %% "ficus" % "1.5.0",
  "ai.x"              %% "play-json-extensions" % "0.42.0",
  "com.beachape"      % "enumeratum-play-json_2.13" % "1.7.0",
  "com.sendgrid"      % "sendgrid-java" % "4.7.2",
  "com.vladsch.flexmark" % "flexmark" % "0.62.2",
  "com.google.cloud" % "google-cloud-storage" % "1.111.2",
  "com.sksamuel.scrimage" % "scrimage-core" % "4.0.20",
  specs2              % Test,
  ehcache,
  guice,
  filters
)

routesGenerator := InjectedRoutesGenerator

//https://github.com/playframework/twirl/issues/105
TwirlKeys.templateImports := Seq()

//https://github.com/playframework/playframework/issues/7382
import play.sbt.routes.RoutesKeys
RoutesKeys.routesImport := Seq("controllers.Assets.Asset", "io.masterypath.slick.{MapID, NodeID, ProfileID, MapRightsID, RoleID, RoleInviteID, OrgID, SegmentID}")

scalacOptions ++= Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-feature", // Emit warning and location for usages of features that should be imported explicitly.
  "-unchecked",       // Enable additional warnings where generated code depends on assumptions.
  "-Xfatal-warnings", // Fail the compilation if there are any warnings.
  // https://github.com/scala/bug/issues/11965
  "-Xlint:_,-unit-special", // Enable recommended additional warnings.
  "-Ywarn-dead-code", // Warn when dead code is identified.
  "-Ywarn-numeric-widen", // Warn when numerics are widened.
  "-Ywarn-unused:imports", // Warn if an import selector is not referenced.
  "-Ywarn-unused:patvars", // Warn if a variable bound in a pattern is unused.
  "-Ywarn-unused:privates", // Warn if a private member is unused.
  "-Ywarn-unused:locals", // Warn if a local definition is unused.
  "-Ywarn-unused:explicits", // Warn if an explicit parameter is unused.
  "-Ywarn-unused:implicits", // Warn if an implicit parameter is unused.
  "-Wconf:src=src_managed/.*:s" // Exclude managed source files from warnings (scalac -Wconf:help)
)

//********************************************************
// sbt-native-packager
//********************************************************
topLevelDirectory := None

// scalacOptions += "-verbose"
// scalacOptions += "-Ylog:all"
//scalacOptions += "-Ytyper-debug"

//********************************************************
// Flyway
//********************************************************
lazy val flyway = (project in file("modules/flyway"))
  .enablePlugins(FlywayPlugin)
    .settings(
      flywayUrl := s"jdbc:postgresql://" + sys.env.getOrElse("POSTGRES_HOST", "") + ":" + sys.env.getOrElse("POSTGRES_PORT", "") + "/" + sys.env
        .getOrElse("POSTGRES_DB", ""),
      flywayUser := sys.env.getOrElse("POSTGRES_USER", ""),
      flywayPassword := sys.env.getOrElse("POSTGRES_PASSWORD", ""),
      flywayLocations := Seq("filesystem:modules/flyway/migration"),
      Test / flywayUrl := s"jdbc:postgresql://" + sys.env.getOrElse("POSTGRES_HOST", "") + ":" + sys.env.getOrElse("POSTGRES_PORT", "") + "/" + sys.env
        .getOrElse("POSTGRES_DB", ""),
      Test / flywayUser := sys.env.getOrElse("POSTGRES_USER", ""),
      Test / flywayPassword := sys.env.getOrElse("POSTGRES_PASSWORD", "")
    )

//********************************************************
// Slick
//********************************************************
lazy val slick = (project in file("modules/slick"))
  .enablePlugins(CodegenPlugin)

//********************************************************
// Root
//********************************************************
lazy val masterypath = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    // Include the package(s) of the classes here, and make sure to import your typescript conversions
    typescriptGenerationImports := Seq(
      "io.masterypath.slick._",
      "models.TypescriptExports._",
      "models.flashcard._",
      "models.flashcard.dto._",
      "models.flashcard.exercise.dto._",
      "models.mindmap.dto._",
      "models.organization.dto._",
    ),
    // The classes that you want to generate typescript interfaces for
    typescriptExports := Seq(
      "AnswerChoice",
      "AnswerChoiceBriefEdit",
      "AnswerChoiceBriefTest",
      "BlogCreate",
      "BlogPage",
      "BlogPatch",
      "CardGradeAnswer",
      "CardMetaData",
      "CardBriefEdit",
      "CardBriefTest",
      "CardPost",
      "CardsDue",
      "CodeExerciseBriefEdit",
      "CodeExerciseBriefTest",
      "Config",
      "ConfigPatch",
      "ExerciseAnswer",
      "Exercise",
      "ExerciseUpdate",
      "FlashcardTypeBrief",
      "MapProperties",
      "MapPropertiesPatch",
      "MapRightsInvite",
      "MapRightsPost",
      "MapRightsPatch",
      "MapRightsMembers",
      "MemberOrgPermissions",
      "MemberProfileEmail",
      "MemberProfile",
      "MemberRegistration",
      "MindMap",
      "Node",
      "NodeCardPostInfo",
      "NodePatch",
      "NodePatchAttributes",
      "NodePost",
      "OrgLink",
      "Post",
      "PostTimeRead",
      "PostMarkdown",
      "Quality",
      "Role",
      "RoleInviteRequest",
      "RoleInviteResponse",
      "SegmentID",
      "Site",
      "TestAnswer",
      "UserCode",
      "Quality",
    ),
    // The output file which will contain the typescript interfaces
    typescriptOutputFile := baseDirectory.value / "../client/src/api/models.ts"
  )
  .aggregate(slick)
  .dependsOn(slick)