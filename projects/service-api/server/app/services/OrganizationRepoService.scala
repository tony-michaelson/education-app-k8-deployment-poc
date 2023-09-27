package services

import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import io.masterypath.slick.{
  Account,
  AccountRepo,
  Config,
  ConfigRepo,
  LinkAccount,
  LinkAccountRepo,
  LinkMapMember,
  LinkMapMemberRepo,
  LinkMember,
  LinkMemberRepo,
  LinkRole,
  LinkRoleRepo,
  LoginTime,
  LoginTimeRepo,
  MapRightsID,
  MemberProfile,
  MemberProfileRepo,
  OrgID,
  OrgProfile,
  OrgProfileRepo,
  ProfileID,
  Role,
  RoleID,
  RoleInvite,
  RoleInviteID,
  RoleInviteRepo,
  RoleRepo,
  Site,
  SiteID,
  SiteRepo
}
import io.masterypath.slick.Tables.{
  AccountRow,
  LinkAccountRow,
  LinkMemberRow,
  LinkRoleRow,
  MapRightsRow,
  MapRightsTable,
  MemberProfileRow,
  RoleInviteRow,
  RoleRow,
  RoleTable,
  SiteRow
}
import models.organization
import models.organization.Member
import models.organization.dto.{OrgLink, PostOrg}

@Singleton
class OrganizationRepoService @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  implicit val dbConfProvider: DatabaseConfigProvider = dbConfigProvider
  private val dbConfig                                = dbConfigProvider.get[JdbcProfile]

  import dbConfig.{db, profile}
  import profile.api._

  private val accounts    = new AccountRepo
  private val configs     = new ConfigRepo
  private val loginTimes  = new LoginTimeRepo
  private val mapMember   = new LinkMapMemberRepo
  private val memberRole  = new LinkRoleRepo
  private val orgAccount  = new LinkAccountRepo
  private val orgMember   = new LinkMemberRepo
  private val orgProfile  = new OrgProfileRepo
  private val profiles    = new MemberProfileRepo
  private val roleInvites = new RoleInviteRepo
  private val sites       = new SiteRepo
  private val roles       = new RoleRepo

  def getSiteByOrgID(orgID: OrgID): Future[Option[Site]] = {
    sites.filter(_.orgID === orgID.uuid).map(_.headOption)
  }

  def getSiteByOrgIDAndUpdate(orgID: OrgID, updateFn: Site => Site): Future[Int] =
    getSiteByOrgID(orgID).flatMap {
      case Some(row) => sites.updateById(row.id, updateFn(row))
      case None      => Future.successful(0)
    }

  def getSiteByDomain(domain: String): Future[Option[Site]] = {
    sites.filter(_.domain === domain).map(_.headOption)
  }

  def createBlog(orgID: OrgID, domain: String, theme: String, ssl: Boolean): Future[Int] = {
    db.run(sites.query += SiteRow(id = SiteID.random, orgID = orgID, domain = domain, theme = theme, ssl = ssl))
  }

  def deleteBlog(site: Site): Future[Int] = {
    db.run(sites.getByIdQuery(site.id).delete)
  }

  def createOrganization(member: Member, org: PostOrg): (OrgID, Future[Int]) = {
    val newOrgID = OrgID.random
    val newOrg = OrgProfile(
      id = newOrgID,
      name = org.name,
      domain = org.domain,
      familyPlan = org.familyPlan,
    )
    val newOrgConfig = Config(
      id = newOrgID,
      blog = false,
      contests = false,
      defaultLanguage = "en",
      mapDocumentationGeneration = false,
      marketingCampaigns = false,
      marketingEngagementCampaigns = false,
      problemBoard = false,
      salesAds = false,
      salesCertificates = false,
      salesCourses = false,
      salesFreeTrials = false,
      salesMemberFeesActive = false,
      salesMemberFeesStatic = false,
      salesMemberships = false,
      salesOrganizations = false,
      supportTier = 1,
      trainingAnswerTimeTracking = false,
      trainingBreakTime = false,
      trainingComplianceEnforcement = false,
      trainingComments = false,
      trainingContentPageStudentSubmission = false,
      trainingContentPageTimeTracking = false,
      trainingContentPageUpvote = false,
      trainingCorrectAnswerAnimation = false,
      trainingCorrectAnswerSound = false,
      trainingFeedback = false,
      trainingLearningPaths = false,
      trainingMnemonics = false,
      trainingQuotes = false,
      trainingRankings = false,
      trainingReporting = false,
      trainingRewardsProgram = false,
      trainingSessionEndCelebration = false,
      trainingSessionEndFeedback = false,
      trainingStraightThruMode = false,
      trainingStudyGoals = false,
      trainingVirtualLabs = false,
      whiteLabeled = false
    )
    val newAdminRoleID = RoleID.random
    val newAdminRole = Role(
      id = newAdminRoleID,
      orgID = newOrgID,
      roleName = "Admin",
      autoJoin = false,
      blogApprove = true,
      blogCreate = true,
      blogDelete = true,
      blogPublish = true,
      manageBlog = true,
      manageContests = true,
      manageMarketingCampaigns = true,
      manageOrganizationBilling = true,
      manageOrganizationConfig = true,
      manageOrganizationMembers = true,
      manageOrganizationPermissions = true,
      manageOrganizationWhitelabel = true,
      manageRewardsProgram = true,
      manageSalesAds = true,
      manageSalesCertificates = true,
      manageSalesCourses = true,
      manageSalesMemberships = true,
      manageSalesOrganizations = true,
      manageTrainingBreakTime = true,
      manageTrainingComplianceEnforcement = true,
      manageTrainingQuotes = true,
      manageTrainingSessionSettings = true,
      manageProblemBoard = true,
      mapApprove = true,
      mapCreate = true,
      mapCreateDocuments = true,
      mapDirectory = true,
      mapFeedback = true,
      mapFork = true,
      mapMnemonics = true,
      mapModify = true,
      mapPermissions = true,
      mapPublish = true,
      mapShare = true,
      mapStats = true,
      mapTraining = true,
      mapTransfer = true,
      mapView = true,
      organizationInvite = true,
      organizationPublish = true,
      trainingRankings = true,
      trainingReporting = true
    )
    val newAllUsersRoleID = RoleID.random
    val newAllUsersRole = Role(
      id = newAllUsersRoleID,
      orgID = newOrgID,
      roleName = "All Users",
      autoJoin = true,
      blogApprove = false,
      blogCreate = false,
      blogDelete = false,
      blogPublish = false,
      manageBlog = false,
      manageContests = false,
      manageMarketingCampaigns = false,
      manageOrganizationBilling = false,
      manageOrganizationConfig = false,
      manageOrganizationMembers = false,
      manageOrganizationPermissions = false,
      manageOrganizationWhitelabel = false,
      manageRewardsProgram = false,
      manageSalesAds = false,
      manageSalesCertificates = false,
      manageSalesCourses = false,
      manageSalesMemberships = false,
      manageSalesOrganizations = false,
      manageTrainingBreakTime = false,
      manageTrainingComplianceEnforcement = false,
      manageTrainingQuotes = false,
      manageTrainingSessionSettings = false,
      manageProblemBoard = false,
      mapApprove = false,
      mapCreate = false,
      mapCreateDocuments = false,
      mapDirectory = false,
      mapFeedback = false,
      mapFork = false,
      mapMnemonics = false,
      mapModify = false,
      mapPermissions = false,
      mapPublish = false,
      mapShare = false,
      mapStats = false,
      mapTraining = false,
      mapTransfer = false,
      mapView = false,
      organizationInvite = false,
      organizationPublish = false,
      trainingRankings = false,
      trainingReporting = false
    )
    val newAccountLink = LinkAccount(
      id = member.account.id,
      orgID = newOrgID
    )
    val newMemberLink = LinkMember(
      id = member.profile.id,
      orgID = newOrgID,
      internal = true,
      payPerCourse = false,
    )
    val updates = db.run(
      (orgProfile.query += orgProfile.toRow(newOrg)) andThen
        (configs.query += configs.toRow(newOrgConfig)) andThen
        (roles.query += roles.toRow(newAdminRole)) andThen
        (roles.query += roles.toRow(newAllUsersRole)) andThen
        (memberRole.query += memberRole.toRow(
          LinkRole(id = member.profile.id, orgID = newOrgID, roleID = newAdminRoleID))) andThen
        (memberRole.query += memberRole.toRow(
          LinkRole(id = member.profile.id, orgID = newOrgID, roleID = newAllUsersRoleID))) andThen
        (orgAccount.query += orgAccount.toRow(newAccountLink)) andThen
        (orgMember.query += orgMember.toRow(newMemberLink)).transactionally
    )
    (newOrgID, updates)
  }

  def orgNameDomainCheck(orgName: String, domain: String): Future[(Boolean, Boolean)] = {
    for {
      orgs <- orgProfile.getAll
    } yield (orgs.exists(x => x.name == orgName), orgs.exists(x => x.domain == domain))
  }

  def getConfigByID(orgID: OrgID): Future[Option[Config]] =
    configs.getById(orgID)

  def getConfigByIDAndUpdate(orgID: OrgID, updateFn: Config => Config): Future[Int] =
    configs.getByIdAndUpdate(orgID, updateFn)

  def getRoleInvite(inviteID: RoleInviteID): Future[Option[RoleInvite]] =
    roleInvites.getById(inviteID)

  def getInviteRole(inviteID: RoleInviteID): Future[(Option[Role], Option[MapRightsID])] =
    getRoleInvite(inviteID).flatMap {
      case Some(invite) =>
        roles.getById(invite.roleID).map {
          case Some(role) => (Some(role), invite.mapRightsID)
          case None       => (None, None)
        }
      case None => Future.successful((None, None))
    }

  def joinOrgRole(orgID: OrgID, roleID: RoleID, inviteID: RoleInviteID, member: Member): Future[Int] = {
    roles.filter(row => row.orgID === orgID.uuid && (row.autoJoin || row.id === roleID.uuid)).flatMap { seq =>
      {
        val roleLinks = DBIO.seq(seq.map(role =>
          memberRole.query += LinkRoleRow(id = member.profile.id, orgID = role.orgID, roleID = role.id)): _*)
        db.run(
          roleInvites.query.filter(_.id === inviteID.uuid).delete andThen
            roleLinks andThen
            (orgAccount.query += LinkAccountRow(id = member.account.id, orgID = orgID)) andThen
            (orgMember.query += LinkMemberRow(id = member.profile.id, orgID = orgID)).transactionally
        )
      }
    }
  }

  def joinRole(orgID: OrgID, roleID: RoleID, inviteID: RoleInviteID, member: Member): Future[Int] = {
    memberRole
      .filter(row => row.id === member.profile.id.uuid && row.orgID === orgID.uuid && row.roleID === roleID.uuid)
      .map(_.headOption)
      .flatMap {
        case Some(_) =>
          db.run(
            roleInvites.query.filter(_.id === inviteID.uuid).delete
          )
        case None =>
          db.run(
            roleInvites.query.filter(_.id === inviteID.uuid).delete andThen
              (memberRole.query += LinkRoleRow(id = member.profile.id, orgID = orgID, roleID = roleID)).transactionally
          )
      }
  }

  def joinOrgRoleAndMapRights(orgID: OrgID,
                              roleID: RoleID,
                              mapRightsID: MapRightsID,
                              inviteID: RoleInviteID,
                              member: Member): Future[Int] = {
    val newMapMember = LinkMapMember(
      id = mapRightsID,
      orgID = orgID,
      profileID = member.profile.id
    )
    db.run(
      roleInvites.query.filter(_.id === inviteID.uuid).delete andThen
        (orgAccount.query += LinkAccountRow(id = member.account.id, orgID = orgID)) andThen
        (orgMember.query += LinkMemberRow(id = member.profile.id, orgID = orgID)) andThen
        (memberRole.query += LinkRoleRow(id = member.profile.id, orgID = orgID, roleID = roleID)) andThen
        (mapMember.query += mapMember.toRow(newMapMember)).transactionally
    )
  }

  def joinRoleAndMapRights(orgID: OrgID,
                           roleID: RoleID,
                           mapRightsID: MapRightsID,
                           inviteID: RoleInviteID,
                           member: Member): Future[Int] = {
    val newMapMember = LinkMapMember(
      id = mapRightsID,
      orgID = orgID,
      profileID = member.profile.id
    )

    def addMemberToMapRights(): Future[Int] =
      mapMember
        .filter(row => row.id === mapRightsID.uuid && row.profileID === member.profile.id.uuid)
        .map(_.headOption)
        .flatMap {
          case Some(_) =>
            db.run(
              roleInvites.query.filter(_.id === inviteID.uuid).delete
            )
          case None =>
            db.run(
              roleInvites.query.filter(_.id === inviteID.uuid).delete andThen
                (mapMember.query += mapMember.toRow(newMapMember)).transactionally
            )
        }

    def addMemberToRoleAndMapRights(): Future[Int] = db.run(
      roleInvites.query.filter(_.id === inviteID.uuid).delete andThen
        (mapMember.query += mapMember.toRow(newMapMember)) andThen
        (memberRole.query += LinkRoleRow(id = member.profile.id, orgID = orgID, roleID = roleID)).transactionally
    )

    memberRole
      .filter(row => row.id === member.profile.id.uuid && row.orgID === orgID.uuid && row.roleID === roleID.uuid)
      .map(_.headOption)
      .flatMap {
        case Some(_) => addMemberToMapRights()
        case None    => addMemberToRoleAndMapRights()
      }
  }

  def addRoleMember(orgID: OrgID, roleID: RoleID, profileID: ProfileID): Future[Int] =
    memberRole.save(LinkRole(profileID, orgID = orgID, roleID = roleID))

  def removeRoleMember(roleID: RoleID, profileID: ProfileID): Future[Int] = db.run(
    memberRole.filterQuery(_.roleID === roleID.uuid).filter(_.id === profileID.uuid).delete
  )

  def createRoleInvite(roleID: RoleID, mapRightsID: Option[MapRightsID], time: Long): Future[RoleInviteID] =
    db.run(
        roleInvites.query returning roleInvites.query.map(_.id) +=
          RoleInviteRow(id = RoleInviteID.random, roleID = roleID, mapRightsID = mapRightsID, expires = time)
      )
      .map(RoleInviteID(_))

  def logUserSignIn(member: Member, time: Long): Future[Int] =
    loginTimes.save(LoginTime(member.account.id, timestamp = time))

  def saveAccount(account: Account): Future[Int] =
    accounts.save(account)

  def getOrganizationsByProfileID(profileID: ProfileID): Future[Seq[OrgLink]] = {
    val q = for {
      orgLink <- orgMember.filterQuery(_.id === profileID.uuid)
      org     <- orgProfile.filterQuery(_.id === orgLink.orgID)
    } yield (orgLink, org)
    db.run(q.result)
      .map(
        seq =>
          seq.map(
            x =>
              OrgLink(name = x._2.name,
                      orgID = OrgID(x._1.orgID),
                      internal = x._1.internal,
                      payPerCourse = x._1.payPerCourse)))
  }

  def getRolesByMember(orgID: OrgID, profileID: ProfileID): Future[Seq[Role]] = {
    val q = for {
      roleLink <- memberRole.filterQuery(x => x.id === profileID.uuid && x.orgID === orgID.uuid)
      role     <- roles.filterQuery(_.id === roleLink.roleID)
    } yield (role)
    db.run(q.result).map(seq => seq.map(roles.fromRow))
  }

  def getRolesByOrgID(orgID: OrgID): Future[Seq[Role]] =
    roles.filter(_.orgID === orgID.uuid)

  def getRoleMembers(orgID: OrgID, roleID: RoleID): Future[Seq[MemberProfile]] = {
    val q = for {
      role      <- roles.filterQuery(_.id === roleID.uuid).filter(_.orgID === orgID.uuid)
      link_role <- memberRole.filterQuery(_.roleID === role.id)
      profile   <- link_role.memberProfileTableFk
    } yield profile
    db.run(q.result).map(seq => seq.map(profiles.fromRow))
  }

  def getOrgMembers(orgID: OrgID): Future[Seq[MemberProfile]] = {
    val q = for {
      link_org <- orgMember.filterQuery(_.orgID === orgID.uuid)
      profile  <- link_org.memberProfileTableFk
    } yield profile
    db.run(q.result).map(seq => seq.map(profiles.fromRow))
  }

  def removeOrgMember(orgID: OrgID, member: Member): Future[Int] = db.run(
    orgAccount.filterQuery(_.orgID === orgID.uuid).filter(_.id === member.account.id.uuid).delete andThen
      orgMember.filterQuery(_.orgID === orgID.uuid).filter(_.id === member.profile.id.uuid).delete andThen
      mapMember.filterQuery(_.orgID === orgID.uuid).filter(_.profileID === member.profile.id.uuid).delete andThen
      memberRole.filterQuery(_.orgID === orgID.uuid).filter(_.id === member.profile.id.uuid).delete.transactionally
  )

  def saveRole(role: Role): Future[Int] =
    roles.save(role)

  def getRoleByID(orgID: OrgID, roleID: RoleID): Future[Option[Role]] =
    roles.filter(row => row.id === roleID.uuid && row.orgID === orgID.uuid).map(seq => seq.headOption)

  def getRoleByIDAndUpdate(roleID: RoleID, orgID: OrgID, updateFn: Role => Role): Future[Int] = {
    getRoleByID(orgID, roleID).flatMap {
      case Some(row) => roles.updateById(row.id, updateFn(row))
      case None      => Future.successful(0)
    }
  }

  def getOrgMember(orgID: OrgID, profileID: ProfileID): Future[Option[Member]] = {
    val q = for {
      orgMember <- orgMember.filterQuery(row => row.id === profileID.uuid && row.orgID === orgID.uuid)
      profile   <- profiles.filterQuery(_.id === orgMember.id)
      account   <- accounts.filterQuery(_.profileID === orgMember.id)
    } yield (profile, account)
    toMember(db.run(q.result))
  }

  def getMemberByProfileID(profileID: ProfileID): Future[Option[Member]] = {
    val q = for {
      profile <- profiles.filterQuery(_.id === profileID.uuid)
      account <- accounts.filterQuery(_.profileID === profileID.uuid)
    } yield (profile, account)
    toMember(db.run(q.result))
  }

  def getMemberBySubject(subject: String): Future[Option[Member]] = {
    val q = for {
      account <- accounts.query.filter(_.tokenSubject === subject)
      profile <- account.memberProfileTableFk
    } yield (profile, account)
    toMember(db.run(q.result))
  }

  def getMemberBySubjectAndOrgID(subject: String, orgID: OrgID): Future[Option[Member]] = {
    val q = for {
      account   <- accounts.query.filter(_.tokenSubject === subject)
      profile   <- account.memberProfileTableFk
      orgMember <- orgMember.query.filter(_.id === profile.id).filter(_.orgID === orgID.uuid)
      _         <- orgMember.orgProfileTableFk
    } yield (profile, account)
    toMember(db.run(q.result))
  }

  def getAuthorizedMember(
      subject: String,
      orgID: OrgID,
      permissionQuery: Query[RoleTable, RoleRow, Seq]
  ): Future[Option[Member]] = {
    val q = for {
      account   <- accounts.query.filter(_.tokenSubject === subject)
      profile   <- account.memberProfileTableFk
      orgMember <- orgMember.filterQuery(_.id === profile.id).filter(_.orgID === orgID.uuid)
      _         <- orgProfile.filterQuery(_.id === orgMember.orgID)
      role      <- permissionQuery
      _         <- memberRole.filterQuery(_.roleID === role.id).filter(_.id === profile.id)
    } yield (profile, account)
    toMember(db.run(q.result))
  }

  def getAuthorizedMember(
      subject: String,
      orgID: OrgID,
      permissionQuery: Query[RoleTable, RoleRow, Seq],
      mapRightsQuery: Query[MapRightsTable, MapRightsRow, Seq]
  ): Future[Option[Member]] = {
    val q = for {
      account   <- accounts.filterQuery(_.tokenSubject === subject)
      profile   <- account.memberProfileTableFk
      orgMember <- orgMember.filterQuery(_.id === profile.id).filter(_.orgID === orgID.uuid)
      _         <- orgProfile.filterQuery(_.id === orgMember.orgID)
      role      <- permissionQuery
      _         <- memberRole.filterQuery(_.roleID === role.id).filter(_.id === profile.id)
      mapMember <- mapMember.query.filter(_.profileID === profile.id)
      _         <- mapRightsQuery.filter(_.id === mapMember.id)
    } yield (profile, account)
    toMember(db.run(q.result))
  }

  private def toMember(future: Future[Seq[(MemberProfileRow, AccountRow)]]) = future.map(_.headOption).map {
    case Some((profile, account)) =>
      Some(organization.Member(profile = profiles.fromRow(profile), account = accounts.fromRow(account)))
    case None => None
  }

  def createNewMember(newMember: Member, domain: String): Future[Int] =
    for {
      orgID       <- orgProfile.filter(_.domain === domain)
      updateCount <- createNewMemberWithOrgID(newMember, orgID.head.id)
    } yield updateCount

  private def createNewMemberWithOrgID(newMember: Member, orgID: OrgID): Future[Int] = {
    roles.filter(row => row.orgID === orgID.uuid && row.autoJoin).flatMap { seq =>
      {
        val roleLinks = DBIO.seq(seq.map(role =>
          memberRole.query += LinkRoleRow(id = newMember.profile.id, orgID = role.orgID, roleID = role.id)): _*)
        db.run(
          (
            profiles.saveQuery(profiles.toRow(newMember.profile)) andThen
              accounts.saveQuery(accounts.toRow(newMember.account)) andThen
              orgMember.saveQuery(LinkMemberRow(id = newMember.profile.id, orgID = orgID)) andThen
              roleLinks andThen
              orgAccount.saveQuery(LinkAccountRow(id = newMember.account.id, orgID = orgID))
          ).transactionally
        )
      }
    }
  }
}
