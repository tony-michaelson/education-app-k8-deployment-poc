package models.organization

import io.masterypath.slick.{Account, MemberProfile}

case class Member(
    profile: MemberProfile,
    account: Account
)
