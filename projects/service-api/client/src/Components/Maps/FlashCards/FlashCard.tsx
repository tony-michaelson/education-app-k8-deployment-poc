import { CodeExercise } from './Types/CodeExercise';
import { FlashCardBasic } from './Types/FlashCardBasic';
import { MapID, SegmentID, NodeID, FlashcardTypeBrief } from 'src/api/models';
import React from 'react';

export type FlashCardMode = 'edit' | 'test';

const getFlashCardComponentByCardType = {
  basic: FlashCardBasic,
  code_exercise: CodeExercise,
};

export interface FlashCardProps {
  mapID: MapID;
  segmentID: SegmentID;
  nodeID: NodeID;
  // tslint:disable-next-line: no-any
  toggleFunction: () => void;
  cardType: FlashcardTypeBrief;
}

export const FlashCard = (props: FlashCardProps) => {
  let ComponentLink = getFlashCardComponentByCardType[props.cardType.cardType];
  return <ComponentLink {...props} />;
};
