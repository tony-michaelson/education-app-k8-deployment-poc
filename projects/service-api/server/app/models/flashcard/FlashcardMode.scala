package models.flashcard

import enumeratum.{PlayJsonEnum, Enum, EnumEntry}

sealed trait FlashcardMode extends EnumEntry

object FlashcardMode extends Enum[FlashcardMode] with PlayJsonEnum[FlashcardMode] {

  val values = findValues

  case object EDIT extends FlashcardMode
  case object TEST extends FlashcardMode

}
