package models.mindmap

import io.masterypath.slick.{Post, PostRead}

case class PostInfo(
    post: Post,
    postRead: Option[PostRead]
)
