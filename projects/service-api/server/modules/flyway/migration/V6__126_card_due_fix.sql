ALTER TABLE flashcard.card_due DROP CONSTRAINT card_due_pkey;
ALTER TABLE flashcard.card_due ADD CONSTRAINT card_due_pkey PRIMARY KEY ("id", "profileID");
