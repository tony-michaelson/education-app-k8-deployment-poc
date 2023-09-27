CREATE SCHEMA mind_map;

CREATE TABLE mind_map.mind_map (
    "id" UUID NOT NULL PRIMARY KEY,
    "published" BOOLEAN NOT NULL DEFAULT FALSE,
    "icon" VARCHAR (255) DEFAULT NULL,
    "orgID" UUID NOT NULL REFERENCES organization.org_profile("id"),
    "name" VARCHAR (255) NOT NULL,
    "mode" VARCHAR (12) NOT NULL,
    "description" TEXT DEFAULT NULL,
    "cost" FLOAT CHECK (cost > 0) DEFAULT NULL
);

CREATE INDEX map_id_prop ON mind_map.mind_map("id");

CREATE TABLE mind_map.node (
    "id" UUID NOT NULL PRIMARY KEY,
    "nodeNumber" SMALLINT NOT NULL,
    "parentID" UUID DEFAULT NULL REFERENCES mind_map.node("id") ON DELETE CASCADE,
    "mapID" UUID NOT NULL REFERENCES mind_map.mind_map("id") ON DELETE CASCADE,
    "segmentID" UUID NOT NULL,
    "path" TEXT NOT NULL,
    "order" FLOAT NOT NULL,
    "name" VARCHAR (255) NOT NULL,
    "type" VARCHAR (40) NOT NULL,
    "root" SMALLINT DEFAULT NULL,
    "disabled" BOOLEAN DEFAULT FALSE NOT NULL,
    CONSTRAINT map_nodeNumber UNIQUE  ("segmentID", "nodeNumber"),
    CONSTRAINT parent_order UNIQUE  ("parentID", "order"),
    CONSTRAINT title_parent UNIQUE  ("parentID", "name")
);

CREATE TABLE mind_map.node_attributes (
    "id" UUID NOT NULL PRIMARY KEY,
    "nodeID" UUID NOT NULL REFERENCES mind_map.node("id") ON DELETE CASCADE,
    "profileID" UUID NOT NULL REFERENCES organization.member_profile("id"),
    "collapsed" BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE mind_map.map_rights (
    "id" UUID NOT NULL PRIMARY KEY,
    "name" VARCHAR (40) NOT NULL,
    "mapID" UUID NOT NULL REFERENCES mind_map.mind_map("id") ON DELETE CASCADE,
    "admin" BOOLEAN NOT NULL DEFAULT FALSE,
    "feedback" BOOLEAN NOT NULL DEFAULT FALSE,
    "mnemonics" BOOLEAN NOT NULL DEFAULT FALSE,
    "modify" BOOLEAN NOT NULL DEFAULT FALSE,
    "permissions" BOOLEAN NOT NULL DEFAULT FALSE,
    "publish" BOOLEAN NOT NULL DEFAULT FALSE,
    "share" BOOLEAN NOT NULL DEFAULT FALSE,
    "stats" BOOLEAN NOT NULL DEFAULT FALSE,
    "training" BOOLEAN NOT NULL DEFAULT FALSE,
    "transfer" BOOLEAN NOT NULL DEFAULT FALSE,
    "view" BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE mind_map.link_map_member (
    "id" UUID NOT NULL REFERENCES mind_map.map_rights("id") ON DELETE CASCADE,
    "orgID" UUID NOT NULL REFERENCES organization.org_profile("id"),
    "profileID" UUID NOT NULL REFERENCES organization.member_profile("id"),
    CONSTRAINT link_maprights_no_dups UNIQUE  ("id", "profileID")
);

CREATE TABLE mind_map.post (
    "id" UUID NOT NULL PRIMARY KEY REFERENCES mind_map.node("id") ON DELETE CASCADE,
    "mapID" UUID NOT NULL REFERENCES mind_map.mind_map("id") ON DELETE CASCADE,
    "markdown" TEXT NOT NULL,
    "html" TEXT NOT NULL
);

CREATE TABLE mind_map.post_read (
    "id" UUID NOT NULL PRIMARY KEY REFERENCES mind_map.post("id") ON DELETE CASCADE,
    "profileID" UUID NOT NULL REFERENCES organization.member_profile("id"),
    "timeRead" BIGINT NOT NULL
);

create table organization.role_invite (
    "id" UUID PRIMARY KEY NOT NULL,
    "roleID" UUID NOT NULL REFERENCES organization.role("id") ON DELETE CASCADE,
    "mapRightsID" UUID DEFAULT NULL REFERENCES mind_map.map_rights("id") ON DELETE CASCADE,
    "expires" BIGINT NOT NULL
);

CREATE INDEX parent_id ON mind_map.node("parentID");
CREATE INDEX map_id ON mind_map.node("mapID");
CREATE INDEX root_node ON mind_map.node("root");
CREATE INDEX top_map_id ON mind_map.node("segmentID");
CREATE INDEX node_id ON mind_map.node("id");