import {
  MemberProfileEmail,
  MemberRegistration,
  OrgID,
  Config,
  ConfigPatch,
  MemberProfile,
  NodePatch,
  ProfileID,
  Role,
  RoleID,
  RoleInviteRequest,
  RoleInviteResponse,
  RoleInviteID,
  MapProperties,
  MapID,
  SegmentID,
  PostTimeRead,
  NodePost,
  Node,
  NodeID,
  NodePatchAttributes,
  CardPost,
  Exercise,
  TestAnswer,
  ExerciseAnswer,
  Quality,
  PostMarkdown,
  Post,
  MapRightsPost,
  MapRightsID,
  MapRightsMembers,
  MapRightsInvite,
  MapRightsPatch,
  FlashcardTypeBrief,
  MindMap,
  MapPropertiesPatch,
  CardGradeAnswer,
  CardsDue,
  BlogPage,
  BlogCreate,
  Site,
  MemberOrgPermissions,
  BlogPatch,
} from './models';
import { AxiosPromise, AxiosResponse } from 'axios';
import { NotifierFunction, StoreImageFileResponse } from 'src/types';

export interface Backend {
  // *********** *********** ***********
  // Members
  // *********** *********** ***********
  getMemberProfile(
    callback: (member: MemberProfileEmail) => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;

  linkAccount(
    data: MemberRegistration,
    callback: () => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;

  logSignIn(callback: () => void): AxiosPromise;

  memberRegistration(
    data: MemberRegistration,
    callback: () => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;

  getMemberOrganizations(
    callback: (orgs: OrgID[]) => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;

  // *********** *********** ***********
  // Organizations
  // *********** *********** ***********
  createBlog(
    data: BlogCreate,
    callback: () => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;

  deleteBlog(
    callback: () => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;
  
  buildBlog(
    callback: () => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;
  
  publishBlog(
    callback: () => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
    ): AxiosPromise;

  getBlogSettings(
    callback: (site: Site) => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;
  
  patchBlogSettings(
    data: BlogPatch,
    callback: () => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;

  getOrgConfig(
    callback: (config: Config) => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;

  patchOrgConfig(
    data: ConfigPatch,
    callback: () => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;

  getOrgMembers(
    callback: (members: MemberProfile[]) => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;

  deleteOrgMember(
    profileID: ProfileID,
    callback: () => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;

  getOrgRoles(callback: (roles: Role[]) => void): AxiosPromise;

  getOrgRoleMembers(
    roleID: RoleID,
    callback: (members: MemberProfile) => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;

  createOrgRole(
    data: Role,
    callback: (roleID: RoleID) => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;

  patchOrgRole(
    data: Role,
    roleID: RoleID,
    callback: () => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;

  getOrgRole(
    roleID: RoleID,
    callback: (role: Role) => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;

  getMyPermissions(
    callback: (permissions: MemberOrgPermissions) => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;

  addOrgRoleMember(
    roleID: RoleID,
    profileID: ProfileID,
    callback: () => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;

  deleteOrgRoleMember(
    roleID: RoleID,
    profileID: ProfileID,
    callback: () => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;

  orgRoleInvite(
    data: RoleInviteRequest,
    roleID: RoleID,
    callback: (response: RoleInviteResponse) => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;

  joinWithInviteID(
    inviteID: RoleInviteID,
    callback: () => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;

  // *********** *********** ***********
  // Maps
  // *********** *********** ***********
  getMyMaps(callback: (maps: MapProperties[]) => void): AxiosPromise;

  getFlashcardTypes(
    callback: (flashcardTypes: FlashcardTypeBrief[]) => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;

  getMap(
    mapID: MapID,
    segmentID: SegmentID,
    callback: (map: {}) => void, // TODO - describe map data type
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;

  getMapBreadcrumb(
    mapID: MapID,
    segmentID: SegmentID,
    callback: (nodes: Node[]) => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;

  getMapSettings(
    mapID: MapID,
    callback: (map: MindMap) => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;

  setMapIcon(
    mapID: MapID,
    image: File,
    callback: () => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;

  createMap(
    data: MapProperties,
    callback: (mapID: MapID) => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;

  patchMapSettings(
    mapID: MapID,
    data: MapPropertiesPatch,
    callback: () => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;

  createMapNode(
    data: NodePost,
    mapID: MapID,
    segmentID: SegmentID,
    callback: (newNodeID: NodeID) => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;

  patchMapNode(
    data: NodePatch,
    mapID: MapID,
    segmentID: SegmentID,
    nodeID: NodeID,
    callback: (data: Node) => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;

  deleteMapNode(
    mapID: MapID,
    segmentID: SegmentID,
    nodeID: NodeID,
    callback: () => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;

  makeSubMap(
    mapID: MapID,
    segmentID: SegmentID,
    nodeID: NodeID,
    callback: (newMapID: MapID) => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;

  patchMapNodeAttributes(
    data: NodePatchAttributes,
    mapID: MapID,
    segmentID: SegmentID,
    nodeID: NodeID,
    callback: () => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;

  storeImageFile(
    data: File,
    mapID: MapID,
    callback: (response: StoreImageFileResponse) => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;

  validateFlashcard(
    data: Exercise,
    mapID: MapID,
    cardType: string,
    callback: (response: { message: string; output: string }) => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;

  storeFlashcardAudio(
    data: Blob,
    mapID: MapID,
    nodeID: NodeID,
    callback: () => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;

  createFlashcard(
    data: CardPost | Exercise,
    mapID: MapID,
    segmentID: SegmentID,
    nodeID: NodeID,
    cardType: string,
    callback: () => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;

  updateFlashcard(
    data: CardPost | Exercise,
    mapID: MapID,
    segmentID: SegmentID,
    nodeID: NodeID,
    cardType: string,
    callback: () => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;

  getFlashcard<T>(
    mapID: MapID,
    segmentID: SegmentID,
    nodeID: NodeID,
    callback: (response: T) => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;

  getFlashcardType(
    mapID: MapID,
    segmentID: SegmentID,
    nodeID: NodeID,
    callback: (response: FlashcardTypeBrief) => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;

  createBlogPost(
    data: PostMarkdown,
    mapID: MapID,
    segmentID: SegmentID,
    nodeID: NodeID,
    callback: () => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;

  updateBlogPost(
    data: PostMarkdown,
    mapID: MapID,
    segmentID: SegmentID,
    nodeID: NodeID,
    callback: () => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;

  getBlogPost(
    mapID: MapID,
    segmentID: SegmentID,
    nodeID: NodeID,
    callback: (response: Post) => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;
 
  getBlogPostPreview(
    mapID: MapID,
    callback: (response: BlogPage) => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;

  deleteBlogPost(
    mapID: MapID,
    segmentID: SegmentID,
    nodeID: NodeID,
    callback: () => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;

  // *********** *********** ***********
  // MapRights
  // *********** *********** ***********

  createMapRights(
    data: MapRightsPost,
    mapID: MapID,
    callback: (newMapRightsID: MapRightsID) => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;

  getAllMapRights(
    mapID: MapID,
    callback: (data: MapRightsMembers[]) => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;

  mapRightsInvite(
    data: MapRightsInvite,
    mapID: MapID,
    mapRightsID: MapRightsID,
    callback: (response: RoleInviteResponse) => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;

  getMapRights(
    mapID: MapID,
    mapRightsID: MapRightsID,
    callback: (data: MapRightsMembers) => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;

  patchMapRights(
    data: MapRightsPatch,
    mapID: MapID,
    mapRightsID: MapRightsID,
    callback: (mapRights: MapRightsID) => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;

  deleteMapRights(
    mapID: MapID,
    mapRightsID: MapRightsID,
    callback: () => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;

  addMapRightsMember(
    mapID: MapID,
    mapRightsID: MapRightsID,
    profileID: ProfileID,
    callback: () => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;

  removeMapRightsMember(
    mapID: MapID,
    mapRightsID: MapRightsID,
    profileID: ProfileID,
    callback: () => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;

  // *********** *********** ***********
  // Training
  // *********** *********** ***********

  getFlashcardTestType(
    mapID: MapID,
    nodeID: NodeID,
    callback: (response: FlashcardTypeBrief) => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;

  getFlashcardTest<T>(
    mapID: MapID,
    nodeID: NodeID,
    callback: (response: T) => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;

  getMapPostsReadTimes(
    mapID: MapID,
    callback: (postsTimes: PostTimeRead[]) => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;

  getCards(
    mapID: MapID,
    segmentID: SegmentID,
    callback: (cardsDue: CardsDue) => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;

  getBlogPostTest(
    mapID: MapID,
    nodeID: NodeID,
    callback: (response: Post) => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;

  markBlogPostRead(
    mapID: MapID,
    nodeID: NodeID,
    callback: () => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;

  gradeFlashcard(
    data: TestAnswer | ExerciseAnswer,
    mapID: MapID,
    nodeID: NodeID,
    cardType: string,
    callback: (response: CardGradeAnswer) => void,
  ): AxiosPromise;

  logFlashcardQuality(
    data: Quality,
    mapID: MapID,
    nodeID: NodeID,
    callback: () => void,
    errorHandler: (
      notifier: NotifierFunction,
      response?: AxiosResponse,
    ) => void,
  ): AxiosPromise;
}
