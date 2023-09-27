
DROP TABLE flashcard.card_due;
CREATE TABLE flashcard.card_due (
                                    "id" UUID NOT NULL PRIMARY KEY REFERENCES flashcard.card("id") ON DELETE CASCADE,
                                    "profileID" UUID NOT NULL REFERENCES organization.member_profile("id"),
                                    "ef" DOUBLE PRECISION NOT NULL,
                                    "interval" INTEGER NOT NULL,
                                    "lastAnswerTime" BIGINT NOT NULL,
                                    "due" BIGINT NOT NULL
);

DROP TABLE flashcard.answer_log;
CREATE TABLE flashcard.answer_log (
                                      "id" UUID NOT NULL PRIMARY KEY,
                                      "profileID" UUID NOT NULL REFERENCES organization.member_profile("id"),
                                      "cardID" UUID NOT NULL REFERENCES flashcard.card("id") ON DELETE CASCADE,
                                      "correct" BOOLEAN NOT NULL,
                                      "time" BIGINT NOT NULL,
                                      "elapsedTime" INTEGER NOT NULL
);