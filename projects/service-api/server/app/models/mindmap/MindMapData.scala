package models.mindmap

import io.masterypath.slick.{AnswerChoice, Card, CodeExercise, Node, NodeAttributes, Post}

case class MindMapData(
    nodes: Seq[Node],
    attributes: Seq[NodeAttributes],
    posts: Seq[Post],
    cards: Seq[Card],
    codeExercises: Seq[CodeExercise],
    answerChoices: Seq[AnswerChoice],
)
