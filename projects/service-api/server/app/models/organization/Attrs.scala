package models.organization

import play.api.libs.typedmap.TypedKey

object Attrs {
  val FakeUser: TypedKey[String] = TypedKey.apply[String]("fakeuser")
}
