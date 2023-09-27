ALTER TABLE mind_map.mind_map ADD COLUMN "originMapID" UUID default NULL REFERENCES mind_map.mind_map("id");
ALTER TABLE mind_map.mind_map ADD COLUMN "releaseVersion" FLOAT CHECK ("releaseVersion" >= 0) default 0.0;
ALTER TABLE mind_map.node ADD COLUMN "publishedID" UUID default NULL REFERENCES mind_map.node("id");