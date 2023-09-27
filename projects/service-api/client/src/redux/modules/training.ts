import { typedAction } from '../helpers';
import { TrainingState } from 'src/types';
import produce from 'immer';

const initialState: TrainingState = {
  loadingLogResponse: false,
  timeStart: 0,
  timeElapsed: 0,
  timeElapsedMin: 0,
  timeElapsedSec: 0,
  currentCardElapsedTime: 0,
};

export const setTrainingState = (trainingState: TrainingState) => {
  return typedAction('training/SET_STATE', trainingState);
};

export const setTrainingTimeStart = (start: number) => {
  return typedAction('training/SET_TIME_START', start);
};

export const setTrainingTimeElapsed = (elapsedTime: {
  timeElapsed: number;
  timeElapsedMin: number;
  timeElapsedSec: number;
  currentCardElapsedTime: number;
}) => {
  return typedAction('training/SET_TIME_ELAPSED', elapsedTime);
};

export const setCurrentCardElapsedTime = (seconds: number) => {
  return typedAction('training/SET_CC_ELAPSED_TIME', seconds);
};

export const setLoadingLogResponse = (loading: boolean) => {
  return typedAction('training/SET_LOADING_LOG_RESPONSE', loading);
};

export const trainingClearState = () => {
  return typedAction('training/CLEAR', initialState);
};

type MemberAction = ReturnType<
  | typeof setTrainingState
  | typeof setTrainingTimeStart
  | typeof setTrainingTimeElapsed
  | typeof setCurrentCardElapsedTime
  | typeof setLoadingLogResponse
  | typeof trainingClearState
>;

export function trainingReducer(
  state: TrainingState = initialState,
  action: MemberAction,
): TrainingState {
  switch (action.type) {
    case 'training/SET_STATE':
      return action.payload;
    case 'training/SET_TIME_START':
      return produce(state, (draftState: TrainingState) => {
        draftState.timeStart = action.payload;
      });
    case 'training/SET_TIME_ELAPSED':
      return produce(state, (draftState: TrainingState) => {
        draftState.timeElapsed = action.payload.timeElapsed;
        draftState.timeElapsedMin = action.payload.timeElapsedMin;
        draftState.timeElapsedSec = action.payload.timeElapsedSec;
        draftState.currentCardElapsedTime =
          action.payload.currentCardElapsedTime;
      });
    case 'training/SET_CC_ELAPSED_TIME':
      return produce(state, (draftState: TrainingState) => {
        draftState.currentCardElapsedTime = action.payload;
      });
    case 'training/SET_LOADING_LOG_RESPONSE':
      return produce(state, (draftState: TrainingState) => {
        draftState.loadingLogResponse = action.payload;
      });
    case 'training/CLEAR':
      return action.payload;
    default:
      return state;
  }
}
