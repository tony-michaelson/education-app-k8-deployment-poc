import { typedAction } from '../helpers';
import { MemberState } from 'src/types';
import produce from 'immer';
import { MemberOrgPermissions, OrgID } from 'src/api/models';

const initialState: MemberState = { accessToken: undefined };

export const setAccessInfo = (
  accessToken?: string,
  timeout?: NodeJS.Timeout,
  logout?: () => void,
) => {
  return typedAction('user/SET_ACCESS_INFO', {
    accessToken: accessToken,
    timeout: timeout,
    logout: logout,
  });
};

export const clearAccessInfo = () => {
  return typedAction('user/CLEAR_ACCESS_INFO');
};

export const setOrganization = (orgID: OrgID) => {
  return typedAction('user/SET_ORGANIZATION', orgID);
};

export const setPermissions = (permissions: MemberOrgPermissions) => {
  return typedAction('user/SET_PERMISSIONS', permissions);
};

type MemberAction = ReturnType<
  typeof setAccessInfo
  | typeof clearAccessInfo
  | typeof setOrganization
  | typeof setPermissions>;

export function memberReducer(
  state: MemberState = initialState,
  action: MemberAction,
): MemberState {
  switch (action.type) {
    case 'user/SET_ACCESS_INFO':
      if (state.timeout) {
        clearTimeout(state.timeout);
      }
      return produce(state, (draftState: MemberState) => {
        draftState.accessToken = action.payload.accessToken;
        draftState.timeout = action.payload.timeout;
        draftState.logout = action.payload.logout;
      });
    case 'user/SET_ORGANIZATION':
      return produce(state, (draftState: MemberState) => {
        draftState.organization = action.payload;
      });
    case 'user/SET_PERMISSIONS':
      return produce(state, (draftState: MemberState) => {
        draftState.permissions = action.payload;
      });
    case 'user/CLEAR_ACCESS_INFO':
      if (state.timeout) {
        clearTimeout(state.timeout);
      }
      return {
        accessToken: undefined,
        organization: undefined,
        permissions: undefined,
        timeout: undefined,
        logout: undefined
      };
    default:
      return state;
  }
}
