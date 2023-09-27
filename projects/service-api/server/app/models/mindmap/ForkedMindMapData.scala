package models.mindmap

import io.masterypath.slick.{AnswerChoice, Card, CodeExercise, Node, NodeAttributes, NodeID, Post}

case class ForkedMindMapData(
    originNodes: Seq[Node],
    newNodeID: Map[NodeID, NodeID],
    nodes: Seq[Node],
    attributes: Seq[NodeAttributes],
    posts: Seq[Post],
    cards: Seq[Card],
    codeExercises: Seq[CodeExercise],
    answerChoices: Seq[AnswerChoice],
)
