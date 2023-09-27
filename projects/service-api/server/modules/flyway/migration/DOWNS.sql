DROP TABLE if EXISTS flashcard.code_exercise;
DROP TABLE if EXISTS flashcard.card_due;
DROP TABLE if EXISTS flashcard.answer_log;
DROP TABLE if EXISTS flashcard.answer_choice;
DROP TABLE if EXISTS flashcard.card;
DROP TABLE if EXISTS flashcard.flashcard_type;
DROP SCHEMA if EXISTS flashcard;

DROP TABLE if EXISTS mind_map.post_read;
DROP TABLE if EXISTS mind_map.post;
DROP TABLE if EXISTS mind_map.node_attributes;
DROP TABLE if EXISTS mind_map.node;
drop table if exists organization.link_role;
drop table if exists organization.role_invite;
DROP TABLE if EXISTS mind_map.link_map_member;
DROP TABLE if EXISTS mind_map.map_rights;
DROP TABLE if EXISTS mind_map.mind_map;
DROP SCHEMA if EXISTS mind_map;

drop table if exists organization.role;
drop table if exists organization.config;
drop table if exists organization.link_organization;

drop table if exists websites.alias;
drop table if exists websites.site;
drop schema if exists websites;

drop table if exists organization.login_time;
drop table if exists organization.link_account;
drop table if exists organization.link_member;
drop table if exists organization.account;
drop table if exists organization.member_profile;
drop table if exists organization.billing_transactions;
drop table if exists organization.billing_items;
drop table if exists organization.org_profile;
drop schema if exists organization;

drop table public.flyway_schema_history;