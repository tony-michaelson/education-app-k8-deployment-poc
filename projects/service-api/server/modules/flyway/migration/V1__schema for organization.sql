create schema organization;

create table organization.org_profile (
                                      "id" UUID NOT NULL PRIMARY KEY,
                                      "name" VARCHAR NOT NULL UNIQUE,
                                      "domain" VARCHAR NOT NULL UNIQUE,
                                      "familyPlan" BOOLEAN NOT NULL DEFAULT FALSE
);

insert into organization.org_profile ("id", "name", "domain") values ('57d00d57-28de-410c-b1cf-d97c4ccba08d', 'Mastery Path', 'masterypath.io');

create table organization.member_profile (
                                "id" UUID NOT NULL PRIMARY KEY,
                                "firstName" VARCHAR NOT NULL,
                                "lastName" VARCHAR NOT NULL,
                                "avatarURL" VARCHAR NOT NULL,
                                "enabled" BOOLEAN NOT NULL
);

create table organization.account (
                                "id" UUID NOT NULL PRIMARY KEY,
                                "profileID" UUID NOT NULL REFERENCES organization.member_profile("id"),
                                "tokenSubject" VARCHAR NOT NULL UNIQUE,
                                "email" VARCHAR NOT NULL,
                                "enabled" BOOLEAN NOT NULL,
                                "created" BIGINT NOT NULL
);

create table organization.link_member (
                                     "id" UUID NOT NULL REFERENCES organization.member_profile("id"),
                                     "orgID" UUID NOT NULL REFERENCES organization.org_profile("id"),
                                     CONSTRAINT link_member_no_dups UNIQUE  ("id", "orgID")
);

create table organization.link_account (
                                      "id" UUID NOT NULL REFERENCES organization.account("id"),
                                      "orgID" UUID NOT NULL REFERENCES organization.org_profile("id"),
                                      CONSTRAINT link_account_no_dups UNIQUE  ("id", "orgID")
);

create table organization.login_time (
                                   "id" UUID NOT NULL REFERENCES organization.account("id"),
                                   "timestamp" BIGINT NOT NULL
);

create table organization.link_organization (
                                         "id" UUID NOT NULL REFERENCES organization.org_profile("id"),
                                         "orgMemberID" UUID NOT NULL REFERENCES organization.org_profile("id"),
                                         CONSTRAINT link_organization_no_dups UNIQUE  ("id", "orgMemberID")
);

create table organization.config (
                                     "id" UUID NOT NULL PRIMARY KEY REFERENCES organization.org_profile("id"),
                                     "blog" BOOLEAN NOT NULL DEFAULT FALSE,
                                     "contests" BOOLEAN NOT NULL DEFAULT FALSE,
                                     "defaultLanguage" VARCHAR NOT NULL,
                                     "mapDocumentationGeneration" BOOLEAN NOT NULL DEFAULT FALSE,
                                     "marketingCampaigns" BOOLEAN NOT NULL DEFAULT FALSE,
                                     "marketingEngagementCampaigns" BOOLEAN NOT NULL DEFAULT FALSE,
                                     "problemBoard" BOOLEAN NOT NULL DEFAULT FALSE,
                                     "salesAds" BOOLEAN NOT NULL DEFAULT FALSE,
                                     "salesCertificates" BOOLEAN NOT NULL DEFAULT FALSE,
                                     "salesCourses" BOOLEAN NOT NULL DEFAULT FALSE,
                                     "salesFreeTrials" BOOLEAN NOT NULL DEFAULT FALSE,
                                     "salesMemberFeesActive" BOOLEAN NOT NULL DEFAULT FALSE,
                                     "salesMemberFeesStatic" BOOLEAN NOT NULL DEFAULT FALSE,
                                     "salesMemberships" BOOLEAN NOT NULL DEFAULT FALSE,
                                     "salesOrganizations" BOOLEAN NOT NULL DEFAULT FALSE,
                                     "supportTier" SMALLINT NOT NULL,
                                     "trainingAnswerTimeTracking" BOOLEAN NOT NULL DEFAULT FALSE,
                                     "trainingBreakTime" BOOLEAN NOT NULL DEFAULT FALSE,
                                     "trainingComplianceEnforcement" BOOLEAN NOT NULL DEFAULT FALSE,
                                     "trainingComments" BOOLEAN NOT NULL DEFAULT FALSE,
                                     "trainingContentPageStudentSubmission" BOOLEAN NOT NULL DEFAULT FALSE,
                                     "trainingContentPageTimeTracking" BOOLEAN NOT NULL DEFAULT FALSE,
                                     "trainingContentPageUpvote" BOOLEAN NOT NULL DEFAULT FALSE,
                                     "trainingCorrectAnswerAnimation" BOOLEAN NOT NULL DEFAULT FALSE,
                                     "trainingCorrectAnswerSound" BOOLEAN NOT NULL DEFAULT FALSE,
                                     "trainingFeedback" BOOLEAN NOT NULL DEFAULT FALSE,
                                     "trainingLearningPaths" BOOLEAN NOT NULL DEFAULT FALSE,
                                     "trainingMnemonics" BOOLEAN NOT NULL DEFAULT FALSE,
                                     "trainingQuotes" BOOLEAN NOT NULL DEFAULT FALSE,
                                     "trainingRankings" BOOLEAN NOT NULL DEFAULT FALSE,
                                     "trainingReporting" BOOLEAN NOT NULL DEFAULT FALSE,
                                     "trainingRewardsProgram" BOOLEAN NOT NULL DEFAULT FALSE,
                                     "trainingSessionEndCelebration" BOOLEAN NOT NULL DEFAULT FALSE,
                                     "trainingSessionEndFeedback" BOOLEAN NOT NULL DEFAULT FALSE,
                                     "trainingStraightThruMode" BOOLEAN NOT NULL DEFAULT FALSE,
                                     "trainingStudyGoals" BOOLEAN NOT NULL DEFAULT FALSE,
                                     "trainingVirtualLabs" BOOLEAN NOT NULL DEFAULT FALSE,
                                     "whiteLabeled" BOOLEAN NOT NULL DEFAULT FALSE
);

create table organization.role (
                                   "id" UUID NOT NULL PRIMARY KEY,
                                   "orgID" UUID NOT NULL REFERENCES organization.org_profile("id"),
                                   "roleName" VARCHAR NOT NULL,
                                   "autoJoin" BOOLEAN NOT NULL DEFAULT FALSE,
                                   "blogApprove" BOOLEAN NOT NULL DEFAULT FALSE,
                                   "blogCreate" BOOLEAN NOT NULL DEFAULT FALSE,
                                   "blogDelete" BOOLEAN NOT NULL DEFAULT FALSE,
                                   "blogPublish" BOOLEAN NOT NULL DEFAULT FALSE,
                                   "manageBlog" BOOLEAN NOT NULL DEFAULT FALSE,
                                   "manageContests" BOOLEAN NOT NULL DEFAULT FALSE,
                                   "manageMarketingCampaigns" BOOLEAN NOT NULL DEFAULT FALSE,
                                   "manageOrganizationBilling" BOOLEAN NOT NULL DEFAULT FALSE,
                                   "manageOrganizationConfig" BOOLEAN NOT NULL DEFAULT FALSE,
                                   "manageOrganizationMembers" BOOLEAN NOT NULL DEFAULT FALSE,
                                   "manageOrganizationPermissions" BOOLEAN NOT NULL DEFAULT FALSE,
                                   "manageOrganizationWhitelabel" BOOLEAN NOT NULL DEFAULT FALSE,
                                   "manageRewardsProgram" BOOLEAN NOT NULL DEFAULT FALSE,
                                   "manageSalesAds" BOOLEAN NOT NULL DEFAULT FALSE,
                                   "manageSalesCertificates" BOOLEAN NOT NULL DEFAULT FALSE,
                                   "manageSalesCourses" BOOLEAN NOT NULL DEFAULT FALSE,
                                   "manageSalesMemberships" BOOLEAN NOT NULL DEFAULT FALSE,
                                   "manageSalesOrganizations" BOOLEAN NOT NULL DEFAULT FALSE,
                                   "manageTrainingBreakTime" BOOLEAN NOT NULL DEFAULT FALSE,
                                   "manageTrainingComplianceEnforcement" BOOLEAN NOT NULL DEFAULT FALSE,
                                   "manageTrainingQuotes" BOOLEAN NOT NULL DEFAULT FALSE,
                                   "manageTrainingSessionSettings" BOOLEAN NOT NULL DEFAULT FALSE,
                                   "manageProblemBoard" BOOLEAN NOT NULL DEFAULT FALSE,
                                   "mapApprove" BOOLEAN NOT NULL DEFAULT FALSE,
                                   "mapCreate" BOOLEAN NOT NULL DEFAULT FALSE,
                                   "mapCreateDocuments" BOOLEAN NOT NULL DEFAULT FALSE,
                                   "mapDirectory" BOOLEAN NOT NULL DEFAULT FALSE,
                                   "mapFeedback" BOOLEAN NOT NULL DEFAULT FALSE,
                                   "mapFork" BOOLEAN NOT NULL DEFAULT FALSE,
                                   "mapMnemonics" BOOLEAN NOT NULL DEFAULT FALSE,
                                   "mapModify" BOOLEAN NOT NULL DEFAULT FALSE,
                                   "mapPermissions" BOOLEAN NOT NULL DEFAULT FALSE,
                                   "mapPublish" BOOLEAN NOT NULL DEFAULT FALSE,
                                   "mapShare" BOOLEAN NOT NULL DEFAULT FALSE,
                                   "mapStats" BOOLEAN NOT NULL DEFAULT FALSE,
                                   "mapTraining" BOOLEAN NOT NULL DEFAULT FALSE,
                                   "mapTransfer" BOOLEAN NOT NULL DEFAULT FALSE,
                                   "mapView" BOOLEAN NOT NULL DEFAULT FALSE,
                                   "organizationInvite" BOOLEAN NOT NULL DEFAULT FALSE,
                                   "organizationPublish" BOOLEAN NOT NULL DEFAULT FALSE,
                                   "trainingRankings" BOOLEAN NOT NULL DEFAULT FALSE,
                                   "trainingReporting" BOOLEAN NOT NULL DEFAULT FALSE
);

INSERT INTO organization.config (
    "id",
    "blog",
    "contests",
    "defaultLanguage",
    "mapDocumentationGeneration",
    "marketingCampaigns",
    "marketingEngagementCampaigns",
    "problemBoard",
    "salesAds",
    "salesCertificates",
    "salesCourses",
    "salesFreeTrials",
    "salesMemberFeesActive",
    "salesMemberFeesStatic",
    "salesMemberships",
    "salesOrganizations",
    "supportTier",
    "trainingAnswerTimeTracking",
    "trainingBreakTime",
    "trainingComplianceEnforcement",
    "trainingComments",
    "trainingContentPageStudentSubmission",
    "trainingContentPageTimeTracking",
    "trainingContentPageUpvote",
    "trainingCorrectAnswerAnimation",
    "trainingCorrectAnswerSound",
    "trainingFeedback",
    "trainingLearningPaths",
    "trainingMnemonics",
    "trainingQuotes",
    "trainingRankings",
    "trainingReporting",
    "trainingRewardsProgram",
    "trainingSessionEndCelebration",
    "trainingSessionEndFeedback",
    "trainingStraightThruMode",
    "trainingStudyGoals",
    "trainingVirtualLabs",
    "whiteLabeled"
) VALUES (
             '57d00d57-28de-410c-b1cf-d97c4ccba08d',
             true,
             true,
             'en',
             true,
             true,
             true,
             true,
             true,
             true,
             true,
             true,
             true,
             true,
             true,
             true,
             1,
             true,
             true,
             true,
             true,
             true,
             true,
             true,
             true,
             true,
             true,
             true,
             true,
             true,
             true,
             true,
             true,
             true,
             true,
             true,
             true,
             true,
             true
         );

create table organization.link_role (
                             "id" UUID NOT NULL REFERENCES organization.member_profile("id"),
                             "orgID" UUID NOT NULL REFERENCES organization.org_profile("id"),
                             "roleID" UUID NOT NULL REFERENCES organization.role("id") ON DELETE CASCADE,
                             CONSTRAINT link_role_no_dups UNIQUE  ("id", "roleID")
);

create index member_login_time_memberID ON organization.login_time("id");
create index organization_member_orgID ON organization.link_member("orgID");
create index organization_member_memberID ON organization.link_member("id");
create index organization_account_orgID ON organization.link_account("orgID");
create index organization_account_memberID on organization.link_account("id");