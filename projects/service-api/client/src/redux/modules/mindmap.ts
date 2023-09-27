import { typedAction } from '../helpers';
import { MapPermissions, MindMapState } from 'src/types';
import {
  FlashcardTypeBrief,
  MapID,
  SegmentID,
  Node,
  MapRightsMembers,
} from 'src/api/models';
import produce from 'immer';

const initialState: MindMapState = {
  reloadMap: false,
};

export const setMindMap = (
  mapID: MapID,
  segmentID: SegmentID,
  breadcrumb: Node[],
  permissions: MapPermissions,
) => {
  return typedAction('mindmap/SET_MINDMAP', {
    mapID: mapID,
    segmentID: segmentID,
    breadcrumb: breadcrumb,
    permissions: permissions,
  });
};

export const setFlashcardTypes = (flashcardTypes: FlashcardTypeBrief[]) => {
  return typedAction('mindmap/SET_MINDMAP_FLASHCARD_TYPES', flashcardTypes);
};

export const setMapRights = (mapRights: MapRightsMembers[] | undefined) => {
  return typedAction('mindmap/SET_MINDMAP_RIGHTS', mapRights);
};

export const requestMindMapReload = (reload: boolean) => {
  return typedAction('mindmap/SET_MINDMAP_RELOAD', reload);
};

export const clearMindMap = () => {
  return typedAction('mindmap/CLEAR_MINDMAP', initialState);
};

type MemberAction = ReturnType<
  | typeof setMindMap
  | typeof setMapRights
  | typeof setFlashcardTypes
  | typeof clearMindMap
  | typeof requestMindMapReload
>;

export function mindMapReducer(
  state: MindMapState = initialState,
  action: MemberAction,
): MindMapState {
  switch (action.type) {
    case 'mindmap/SET_MINDMAP':
      return produce(state, (draftState: MindMapState) => {
        draftState.mapID = action.payload.mapID;
        draftState.segmentID = action.payload.segmentID;
        draftState.breadcrumb = action.payload.breadcrumb;
        draftState.permissions = action.payload.permissions;
        // if the map is reloaded then resetting to initial state is helpful
        draftState.reloadMap = initialState.reloadMap;
      });
    case 'mindmap/SET_MINDMAP_RIGHTS':
      return produce(state, (draftState: MindMapState) => {
        draftState.mapRights = action.payload;
      });
    case 'mindmap/SET_MINDMAP_FLASHCARD_TYPES':
      return produce(state, (draftState: MindMapState) => {
        draftState.flashcardTypes = action.payload;
      });
    case 'mindmap/SET_MINDMAP_RELOAD':
      return produce(state, (draftState: MindMapState) => {
        draftState.reloadMap = action.payload;
      });
    case 'mindmap/CLEAR_MINDMAP':
      return action.payload;
    default:
      return state;
  }
}
