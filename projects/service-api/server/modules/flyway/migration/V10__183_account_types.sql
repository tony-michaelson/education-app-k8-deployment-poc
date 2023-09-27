ALTER TABLE organization.link_member ADD COLUMN internal BOOLEAN NOT NULL default FALSE;
ALTER TABLE organization.link_member ADD COLUMN "payPerCourse" BOOLEAN NOT NULL default FALSE;