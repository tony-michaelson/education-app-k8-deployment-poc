create table organization.billing_items (
                                          "id" UUID NOT NULL PRIMARY KEY,
                                          "name" VARCHAR NOT NULL UNIQUE,
                                          "description" VARCHAR,
                                          "cost" DOUBLE PRECISION NOT NULL default 0.0,
                                          "monthly" BOOLEAN NOT NULL DEFAULT FALSE
);

insert into organization.billing_items ("id", "name", "description", "cost", "monthly")
    values ('eed1efa5-b4d1-44b5-9f30-5b074d869061', 'Public Website Services', 'Website for content distribution and sales lead generation.', 34.95, true);

create table organization.billing_transactions (
    "id" UUID NOT NULL PRIMARY KEY,
    "orgID" UUID NOT NULL REFERENCES organization.org_profile("id"),
    "billingItem" UUID NOT NULL REFERENCES organization.billing_items("id"),
    "timestamp" BIGINT NOT NULL,
    "paid" BOOLEAN NOT NULL DEFAULT FALSE
);

create index organization_billing_transactions_orgID ON organization.billing_transactions("orgID");
create index organization_billing_transactions_paid ON organization.billing_transactions("paid");