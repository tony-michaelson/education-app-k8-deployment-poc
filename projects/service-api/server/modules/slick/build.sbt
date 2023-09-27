// Updated 30MAY2020
libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-slick" % "5.0.0",
  "ai.x"              %% "play-json-extensions" % "0.42.0",
  guice
)

scalaVersion := "2.13.2"

//********************************************************
// Slick Codegen
//********************************************************
import slick.codegen.SourceCodeGenerator
import slick.{model => m}

Compile / sourceGenerators += slickCodegen
slickCodegenDatabaseUrl := s"jdbc:postgresql://" + sys.env.getOrElse("POSTGRES_HOST", "") + ":" + sys.env.getOrElse("POSTGRES_PORT", "") + "/" + sys.env
  .getOrElse("POSTGRES_DB", "")
slickCodegenDatabaseUser := sys.env.getOrElse("POSTGRES_USER", "")
slickCodegenDatabasePassword := sys.env.getOrElse("POSTGRES_PASSWORD", "")
slickCodegenDriver := slick.jdbc.PostgresProfile
slickCodegenJdbcDriver := "org.postgresql.Driver"
slickCodegenOutputPackage := "io.masterypath.slick"
slickCodegenExcludedTables := Seq("flyway_schema_history")

slickCodegenOutputDir := (Compile / sourceManaged).value

// Custom modifications to support "repository pattern"
// Companion objects added for case classes for automatic json conversion
slickCodegenCodeGenerator := { (model: m.Model) =>
  new SourceCodeGenerator(model) {
    override def code =
      "import repos.{BaseEntity, BaseTable}\n" +
        super.code

    override def Table = new Table(_) {
      // append "Table" to the camelCased table name
      override def TableClass = new TableClass {
        override def rawName = model.name.table.toLowerCase.toCamelCase + "Table"
      }
      // preserve column names exactly as they appear in the schema
      override def Column = new Column(_) {
        override def rawName = model.name
      }
      override def code =
        super.code.map(
          // extends BaseTable instead of profile.api.Table
          _.replaceAll("profile\\.api\\.Table", "BaseTable").
            // case classes must extend BaseEntity
            replaceAll("case class (\\w+)\\((.*)\\)", "case class $1($2) extends BaseEntity").
            // add "override" modifier to val since we now extend BaseTable
            replaceAll("val id: ", "override val id: ")
        )
    }
  }
}

//********************************************************
// Generate code for repos
//********************************************************
import sbt.File
import Keys.streams

lazy val generateRepos = taskKey[Seq[File]]("Generates Repos to Wrap Tables with BaseRepository")
Compile / sourceGenerators += generateRepos

generateRepos := {
  import scala.io.Source
  import java.io.PrintWriter

  val s = streams.value
  val packagePath = slickCodegenOutputPackage.value.replaceAllLiterally(".", "/") + "/"
  // TODO - make this path into ENV
  val unManagedRepoSourcesPath = "./modules/slick/src/main/scala/repos/custom/"
  val codeDir = slickCodegenOutputDir.value.toString + "/" + packagePath
  val lines = Source.fromFile(codeDir + "Tables.scala").getLines.toList
  val idClassNameOverride: Map[String, String] = Map(
    "CardDueID" -> "NodeID",
    "CardID" -> "NodeID",
    "CodeExerciseID" -> "NodeID",
    "ConfigID" -> "OrgID",
    "LinkAccountID" -> "AccountID",
    "LinkMapMemberID" -> "MapRightsID",
    "LinkMemberID" -> "ProfileID",
    "LinkOrganizationID" -> "OrgID",
    "LinkRoleID" -> "ProfileID",
    "LoginTimeID" -> "AccountID",
    "MemberProfileID" -> "ProfileID",
    "MindMapID" -> "MapID",
    "OrgProfileID" -> "OrgID",
    "PostID" -> "NodeID",
    "PostReadID" -> "NodeID",
  )
  val caseClassFieldOverride: Map[String, String] = Map(
    "Account.profileID" -> "ProfileID",
    "AnswerChoice.cardID" -> "NodeID",
    "AnswerLog.cardID" -> "NodeID",
    "AnswerLog.profileID" -> "ProfileID",
    "Card.MapID" -> "MapID",
    "Card.flashcardTypeID" -> "FlashcardTypeID",
    "Card.id" -> "NodeID", // paired with an idClassNameOverride member
    "CardDue.id" -> "NodeID", // paired with an idClassNameOverride member
    "CardDue.profileID" -> "ProfileID",
    "CodeExercise.id" -> "NodeID", // paired with an idClassNameOverride member
    "Config.id" -> "OrgID", // paired with an idClassNameOverride member
    "LinkAccount.id" -> "AccountID", // paired with an idClassNameOverride member
    "LinkAccount.orgID" -> "OrgID",
    "LinkMapMember.id" -> "MapRightsID", // paired with an idClassNameOverride member
    "LinkMapMember.orgID" -> "OrgID",
    "LinkMapMember.profileID" -> "ProfileID",
    "LinkMember.id" -> "ProfileID", // paired with an idClassNameOverride member
    "LinkMember.orgID" -> "OrgID",
    "LinkOrganization.id" -> "OrgID", // paired with an idClassNameOverride member
    "LinkOrganization.orgID" -> "OrgID",
    "LinkRole.id" -> "ProfileID", // paired with an idClassNameOverride member
    "LinkRole.orgID" -> "OrgID",
    "LinkRole.roleID" -> "RoleID",
    "LoginTime.id" -> "AccountID", // paired with an idClassNameOverride member
    "MapRights.mapID" -> "MapID",
    "MindMap.orgID" -> "OrgID",
    "Node.mapID" -> "MapID",
    "Node.parentID" -> "Option[NodeID]",
    "Node.parentMapID" -> "Option[MapID]",
    "Node.segmentID" -> "SegmentID",
    "NodeAttributes.nodeID" -> "NodeID",
    "NodeAttributes.profileID" -> "ProfileID",
    "Post.id" -> "NodeID", // paired with an idClassNameOverride member
    "Post.mapID" -> "MapID",
    "PostRead.id" -> "NodeID", // paired with an idClassNameOverride member
    "PostRead.profileID" -> "ProfileID",
    "Role.orgID" -> "OrgID",
    "RoleInvite.orgID" -> "OrgID",
    "RoleInvite.roleID" -> "RoleID",
    "RoleInvite.mapRightsID" -> "Option[MapRightsID]",
  )

  def writeToFile(fileName: String, code: String): File = {
    val path = codeDir + fileName
    val writer = new PrintWriter(new File(path))
    s.log.info(s"Source code has generated in $path")
    writer.write(code)
    writer.close()
    file(path)
  }

  def getListOfFiles(dir: String):Seq[File] = {
    val d = new File(dir)
    if (d.exists && d.isDirectory) {
      d.listFiles.filter(_.isFile).toList
    } else {
      Seq[File]()
    }
  }
  def copyDirFiles(fromDir: String, toDir: String): Seq[File] = {
    // brittle syntax rules using comments
    def copy(file: File) = {
      val lines = Source.fromFile(file.getAbsolutePath).getLines.toList.map{
        case line if line.contains("// codeGen:remove") => ""
        case line if line.contains("// codeGen:add") => line.replaceAll("// codeGen:add ", "")
        case line => line
      }
      val content = lines.foldLeft("")((acc, a) => acc + a + "\n")
      writeToFile(file.getName, content)
    }
    getListOfFiles(fromDir).map(copy)
  }
  val customRepos = copyDirFiles(unManagedRepoSourcesPath, codeDir)

  def repoCode(className: String, caseClass: String) = {
    def classExists(className: String): Boolean = {
      val path: String = codeDir + className + ".scala"
      new java.io.File(path).exists
    }
    def customFieldMappings(fields: List[String], classCode: String) = fields.foldLeft(classCode)( (acc, field) =>
      if (caseClassFieldOverride.contains(className + "." + field)) {
        val newType = caseClassFieldOverride(className + "." + field)
        acc.replaceAll("%s: (.*?)(?=[,\\)])".format(field), s"$field: $newType")
      } else {
        acc
      }
    )

    val idClassNameIsAssigned = idClassNameOverride.contains(className + "ID")
    val idClassName = if (idClassNameIsAssigned) idClassNameOverride(className + "ID") else className + "ID"

    val pattern = """([\w`]+)(?=:)""".r
    val classFields = pattern.findAllIn(caseClass).toList
    val customCaseClass = customFieldMappings(classFields,
      caseClass.
      replaceAll("\\(id: java\\.util\\.UUID", s"(id: $idClassName").
      replaceAll("\\s+case class (\\w+)Row", "case class $1").
      replaceAll(" extends BaseEntity", ""))

    val customIDClass =
      if (idClassNameIsAssigned && caseClassFieldOverride.contains(className + ".id")) ""
      else s"""case class $idClassName(uuid: UUID) {
                    |  override def toString = uuid.toString
                    |}
                    |object $idClassName {
                    |  implicit val formatter: Format[$idClassName] =
                    |    new Format[$idClassName] with Serializable {
                    |      override def writes(o: $idClassName): JsValue = Json.valueWrites[$idClassName].writes(o)
                    |
                    |      override def reads(json: JsValue): JsResult[$idClassName] = Json.valueReads[$idClassName].reads(json)
                    |    }
                    |
                    |  def apply(id: UUID): $idClassName           = new $idClassName(uuid = id)
                    |  implicit def toUUID(id: $idClassName): UUID = id.uuid
                    |  implicit def toUUIDOption(id: Option[$idClassName]): Option[UUID] = id flatMap { x => Some(x.uuid) }
                    |
                    |  def random: $idClassName = new $idClassName(uuid = UUID.randomUUID())
                    |
                    |  implicit def pathBinder(implicit stringBinder: PathBindable[String]) = new PathBindable[$idClassName] {
                    |    def tryUUID(str: String) =
                    |      try {
                    |        Right($idClassName(id = UUID.fromString(str)))
                    |      } catch {
                    |        case _: Exception =>
                    |          Left("Unable to parse UUID: " + str)
                    |      }
                    |
                    |    override def bind(key: String, value: String): Either[String, $idClassName] = {
                    |      stringBinder.bind(key, value) match {
                    |        case Right(idString) => tryUUID(idString)
                    |        case Left(error)     => Left(error)
                    |      }
                    |    }
                    |    override def unbind(key: String, id: $idClassName): String = {
                    |      id.toString
                    |    }
                    |  }
                    |}
                    |  """.stripMargin

    def generateRowAssign(fieldName: String) = fieldName match {
      case fieldName if caseClassFieldOverride.contains(className + "." + fieldName) =>
        val fieldOverrideName = caseClassFieldOverride(className + "." + fieldName)
        if (fieldOverrideName.matches(".*Option.*")) {
          val extractedName = fieldOverrideName.replaceAll(".*?\\[(\\w+).*", "$1") // e.g. get NodeID from "Option[NodeID]"
          s"""\t\t\t\t$fieldName = row.$fieldName.flatMap(x => Option($extractedName(x))),\n"""
        } else {
          s"""\t\t\t\t$fieldName = $fieldOverrideName(row.$fieldName),\n"""
        }
      case fieldName if fieldName == "id" =>
        s"""\t\t\t\tid = $idClassName(row.id),\n"""
      case _ =>
        s"""\t\t\t\t$fieldName = row.$fieldName,\n"""
    }

    val fromRowAssigns = classFields.fold("")((acc, fieldName) => acc + generateRowAssign(fieldName))
    val fromRowFunction =
      s"""
         |    def fromRow(row: ${className}Row): $className =
         |      $className(
         |$fromRowAssigns\t\t\t)""".stripMargin

    val toRowAssigns = classFields.fold("")((acc, fieldName) => acc + s"""\t\t\t\t$fieldName = nonRow.$fieldName,\n""")
    val toRowFunction =
      s"""
         |    def toRow(nonRow: $className): ${className}Row =
         |      ${className}Row(
         |$toRowAssigns\t\t\t)""".stripMargin

    // if we've defined a Repo then forego implementation code in favor of our custom Repo class
    val implementation = if (classExists(className + "Repo")) "" else {
      s"""class ${className}Repo @Inject()(implicit dbConfigProvider: DatabaseConfigProvider, ec: ExecutionContext)
         |    extends Base${className}Repo(dbConfigProvider = dbConfigProvider)""".stripMargin
    }

    s"""package ${slickCodegenOutputPackage.value}
       |
       |import java.util.UUID
       |
       |import javax.inject.Inject
       |import ai.x.play.json.Jsonx
       |import ai.x.play.json.Encoders.encoder
       |import play.api.libs.json.{Format, JsResult, JsValue, Json, OFormat}
       |import play.api.db.slick.DatabaseConfigProvider
       |import play.api.mvc.PathBindable
       |import slick.jdbc.PostgresProfile.api._
       |import repos.BaseRepository
       |import Tables.{${className}Row, ${className}Table}
       |
       |import scala.concurrent.ExecutionContext
       |import scala.language.implicitConversions
       |
       |$implementation
       |abstract class Base${className}Repo(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
       |    extends BaseRepository[${className}Table, ${className}Row, $className](TableQuery[${className}Table], dbConfigProvider) {
       |$fromRowFunction
       |$toRowFunction
       |  }
       |
       |$customCaseClass
       |object $className {
       |  implicit val f: OFormat[$className] = Jsonx.formatCaseClass[$className]
       |}
       |$customIDClass
       |""".stripMargin
  }

  val generatedFiles: Seq[File] = lines.
    filter(_.contains("extends BaseEntity")).
    map(caseClass => (caseClass.replaceAll(".*?case class (\\w+)Row.*", "$1"), caseClass)).
    map {
      case (name: String, code: String) => writeToFile("Base" + name + "Repo.scala", repoCode(name, code))
    }
  generatedFiles ++ customRepos
}

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