ALTER TABLE organization.config ADD COLUMN "memberMonthlyCost" DOUBLE PRECISION NOT NULL default 0.0;
ALTER TABLE organization.config ADD COLUMN "memberAnnualCost" DOUBLE PRECISION NOT NULL default 0.0;
ALTER TABLE organization.config ADD COLUMN "memberPaymentMethodRequired" BOOLEAN NOT NULL default FALSE;