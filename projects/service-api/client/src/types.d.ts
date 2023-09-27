import { MapID, SegmentID, Node, FlashcardTypeBrief, MapRightsMembers, NodeCardPostInfo, OrgID, MemberOrgPermissions } from './api/models';

export type MapPermissions = {
  admin: boolean;
  feedback: boolean;
  mnemonics: boolean;
  modify: boolean;
  permissions: boolean;
  publish: boolean;
  share: boolean;
  stats: boolean;
  training: boolean;
  transfer: boolean;
  view: boolean;
};

export type MemberState = {
  accessToken?: string;
  organization?: OrgID;
  permissions?: MemberOrgPermissions;
  timeout?: NodeJS.Timeout;
  logout?: () => void;
};

export type MindMapState = {
  mapID?: MapID;
  segmentID?: SegmentID;
  breadcrumb?: Node[];
  permissions?: MapPermissions;
  mapRights?: MapRightsMembers[];
  flashcardTypes?: FlashcardTypeBrief[];
  reloadMap: boolean;
};

export type TrainingState = {
  loadingLogResponse: boolean;
  timeStart: number;
  timeElapsed: number;
  timeElapsedMin: number;
  timeElapsedSec: number;
  currentCardElapsedTime: number;
};

export type NotifierComponent = 'tokenRefresh' | 'permission' | 'message';
export type NotifierState = {
  toggle: boolean;
  component: NotifierComponent;
  callback?: () => void;
  text: string;
};

export type NotifierFunction = (
  component: NotifierComponent,
  text: string,
) => void;

export interface StoreImageFileResponse {
  url?: string;
  message?: string;
}

export interface MapIdeasExport {
  item: NodeCardPostInfo;
  children: MapIdeasExport[];
}