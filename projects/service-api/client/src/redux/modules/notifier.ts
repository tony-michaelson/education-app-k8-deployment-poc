import { typedAction } from '../helpers';
import { NotifierComponent, NotifierState } from 'src/types';
import produce from 'immer';

const initialState: NotifierState = {
  toggle: false,
  component: 'message',
  text: '',
};

export const toggleNotifier = (toggle: boolean) => {
  return typedAction('notifier/TOGGLE', toggle);
};

export const setNotifierComponent = (component: NotifierComponent) => {
  return typedAction('notifier/SET_COMPONENT', component);
};

export const setNotifierCallback = (callback: () => void) => {
  return typedAction('notifier/SET_CALLBACK', callback);
};

export const clearNotifierCallback = () => {
  return typedAction('notifier/SET_CALLBACK', undefined);
};

export const setNotifierText = (text: string) => {
  return typedAction('notifier/SET_TEXT', text);
};

type MemberAction = ReturnType<
  | typeof toggleNotifier
  | typeof setNotifierComponent
  | typeof setNotifierText
  | typeof setNotifierCallback
>;

export function notifierReducer(
  state: NotifierState = initialState,
  action: MemberAction,
): NotifierState {
  switch (action.type) {
    case 'notifier/TOGGLE':
      return produce(state, (draftState: NotifierState) => {
        draftState.toggle = action.payload;
      });
    case 'notifier/SET_COMPONENT':
      return produce(state, (draftState: NotifierState) => {
        draftState.component = action.payload;
      });
    case 'notifier/SET_CALLBACK':
      return produce(state, (draftState: NotifierState) => {
        draftState.callback = action.payload;
      });
    case 'notifier/SET_TEXT':
      return produce(state, (draftState: NotifierState) => {
        draftState.text = action.payload;
      });
    default:
      return state;
  }
}
