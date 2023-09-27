CREATE SCHEMA flashcard;

CREATE TABLE flashcard.flashcard_type(
                                         "id" UUID NOT NULL PRIMARY KEY,
                                         "cardType" VARCHAR (40) NOT NULL,
                                         "name" VARCHAR (20) NOT NULL,
                                         "commonName" VARCHAR (40) NOT NULL,
                                         "description" TEXT DEFAULT NULL,
                                         "config" TEXT DEFAULT NULL,
                                         CONSTRAINT flashcard_types_no_dups UNIQUE  ("name")
);

CREATE TABLE flashcard.card (
                                "id" UUID NOT NULL PRIMARY KEY REFERENCES mind_map.node("id") ON DELETE CASCADE,
                                "mapID" UUID NOT NULL REFERENCES mind_map.mind_map("id") ON DELETE CASCADE,
                                "flashcardTypeID" UUID NOT NULL REFERENCES flashcard.flashcard_type("id"),
                                "question" TEXT NOT NULL
);

CREATE INDEX basic_flashcard_id ON flashcard.card("id");

CREATE TABLE flashcard.code_exercise(
                                        "id" UUID NOT NULL PRIMARY KEY REFERENCES flashcard.card("id") ON DELETE CASCADE,
                                        "explanation" TEXT NOT NULL,
                                        "explanationHTML" TEXT NOT NULL,
                                        "template" TEXT NOT NULL,
                                        "test" TEXT NOT NULL,
                                        "solution" TEXT NOT NULL
);

CREATE TABLE flashcard.card_due (
                                    "id" UUID NOT NULL PRIMARY KEY REFERENCES flashcard.card("id") ON DELETE CASCADE,
                                    "ef" DOUBLE PRECISION NOT NULL,
                                    "interval" INTEGER NOT NULL,
                                    "lastAnswerTime" BIGINT NOT NULL,
                                    "due" BIGINT NOT NULL
);

CREATE TABLE flashcard.answer_choice (
                                         "id" UUID NOT NULL PRIMARY KEY,
                                         "cardID" UUID NOT NULL REFERENCES flashcard.card("id") ON DELETE CASCADE,
                                         "answer" TEXT NOT NULL,
                                         "correct" BOOLEAN NOT NULL
);

CREATE TABLE flashcard.answer_log (
                                      "id" UUID NOT NULL PRIMARY KEY,
                                      "cardID" UUID NOT NULL REFERENCES flashcard.card("id") ON DELETE CASCADE,
                                      "correct" BOOLEAN NOT NULL,
                                      "time" BIGINT NOT NULL,
                                      "elapsedTime" INTEGER NOT NULL
);
