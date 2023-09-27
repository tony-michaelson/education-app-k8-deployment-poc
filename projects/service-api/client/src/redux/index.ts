import { combineReducers } from 'redux';
import { memberReducer } from './modules/member';
import { mindMapReducer } from './modules/mindmap';
import { notifierReducer } from './modules/notifier';
import { trainingReducer } from './modules/training';

export const rootReducer = combineReducers({
  member: memberReducer,
  mindmap: mindMapReducer,
  notifier: notifierReducer,
  training: trainingReducer,
});

export type RootState = ReturnType<typeof rootReducer>;
