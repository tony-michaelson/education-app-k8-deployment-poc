create schema websites;

create table websites.site (
                                   "id" UUID NOT NULL PRIMARY KEY,
                                   "orgID" UUID NOT NULL REFERENCES organization.org_profile("id"),
                                   "domain" VARCHAR NOT NULL UNIQUE,
                                   "theme" VARCHAR NOT NULL UNIQUE,
                                   "ssl" BOOLEAN NOT NULL DEFAULT FALSE
);

create table websites.alias (
                                   "id" UUID NOT NULL PRIMARY KEY,
                                   "siteID" UUID NOT NULL REFERENCES websites.site("id"),
                                   "alias" VARCHAR NOT NULL UNIQUE,
                                   "ssl" BOOLEAN NOT NULL DEFAULT FALSE
);