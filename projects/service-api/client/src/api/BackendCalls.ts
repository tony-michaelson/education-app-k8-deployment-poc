import { Backend } from './Backend';
import {
  MemberProfileEmail,
  MemberRegistration,
  OrgID,
  Config,
  ConfigPatch,
  MemberProfile,
  ProfileID,
  Role,
  RoleID,
  RoleInviteRequest,
  RoleInviteResponse,
  RoleInviteID,
  MapProperties,
  MapID,
  PostTimeRead,
  NodePost,
  Node,
  SegmentID,
  NodeID,
  CardPost,
  Exercise,
  NodePatch,
  NodePatchAttributes,
  TestAnswer,
  ExerciseAnswer,
  Quality,
  PostMarkdown,
  Post,
  MapRightsID,
  MapRightsPost,
  MapRightsMembers,
  MapRightsInvite,
  MapRightsPatch,
  FlashcardTypeBrief,
  MindMap,
  MapPropertiesPatch,
  CardGradeAnswer,
  CardsDue,
  BlogCreate,
  BlogPage,
  Site,
  MemberOrgPermissions,
  BlogPatch,
} from './models';
import { API_CONFIG } from './ApiConfig';
import { AxiosResponse, AxiosRequestConfig, AxiosError } from 'axios';
import {
  NotifierComponent,
  NotifierFunction,
  StoreImageFileResponse,
} from 'src/types';

const axios = require('axios').default;

export class BackendCalls implements Backend {
         config: AxiosRequestConfig;
         launchNotifier: NotifierFunction;

         constructor(
           accessToken: string,
           public orgID: OrgID,
           public notifierFunctions: {
             toggle: Function;
             setComponent: Function;
             setText: Function;
           },
         ) {
           this.config = {
             baseURL: API_CONFIG.api_host,
             headers: {
               Authorization: 'Bearer ' + accessToken,
               'Content-Type': 'application/json',
             },
           };
           this.launchNotifier = (
             component: NotifierComponent,
             text: string,
           ): void => {
             this.notifierFunctions.setComponent(component);
             this.notifierFunctions.setText(text);
             this.notifierFunctions.toggle(true);
           };
         }

         // *********** *********** ***********
         // members
         // *********** *********** ***********
         getMemberProfile(
           callback: (member: MemberProfileEmail) => void,
           errorHandler: (
             notifier: NotifierFunction,
             response?: AxiosResponse,
           ) => void = this.genericErrorHandler,
         ) {
           return axios
             .get('/member/profile', this.config)
             .then((response: AxiosResponse) => {
               if (response.status === 200) {
                 callback(response.data);
               } else {
                 errorHandler(this.launchNotifier, response);
               }
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }

         linkAccount(
           data: MemberRegistration,
           callback: () => void,
           errorHandler: (
             notifier: NotifierFunction,
             response?: AxiosResponse,
           ) => void = this.genericErrorHandler,
         ) {
           return axios
             .post('/member/linkAccount', data, this.config)
             .then((response: AxiosResponse) => {
               if (response.status === 200) {
                 callback();
               } else {
                 errorHandler(this.launchNotifier, response);
               }
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }

        logSignIn(
          callback: () => void,
          errorHandler: (
            notifier: NotifierFunction,
            response?: AxiosResponse,
          ) => void = this.genericErrorHandler,
        ) {
          return axios
            .post('/member/logSignIn', {}, this.config)
            .then((response: AxiosResponse) => {
              if (response.status === 200) {
                callback();
              } else {
                errorHandler(this.launchNotifier, response);
              }
            })
            .catch((error: AxiosError) =>
              errorHandler(this.launchNotifier, error.response),
            );
        }

         memberRegistration(
           data: MemberRegistration,
           callback: () => void,
           errorHandler: (
             notifier: NotifierFunction,
             response?: AxiosResponse,
           ) => void = this.genericErrorHandler,
         ) {
           return axios
             .post('/member/register', data, this.config)
             .then((response: AxiosResponse) => {
               if (response.status === 200) {
                 callback();
               } else {
                 errorHandler(this.launchNotifier, response);
               }
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }

         getMemberOrganizations(
           callback: (orgs: OrgID[]) => void,
           errorHandler: (
             notifier: NotifierFunction,
             response?: AxiosResponse,
           ) => void = this.genericErrorHandler,
         ) {
           return axios
             .get('/member/organizations', this.config)
             .then((response: AxiosResponse) => {
               if (response.status === 200) {
                 callback(response.data);
               } else {
                 errorHandler(this.launchNotifier, response);
               }
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }

         // *********** *********** ***********
         // organizations
         // *********** *********** ***********
         createBlog(
          data: BlogCreate,
          callback: () => void,
          errorHandler: (
            notifier: NotifierFunction,
            response?: AxiosResponse,
          ) => void = this.genericErrorHandler,
         ) {
          return axios
          .post('/org/' + this.orgID + '/blog', data, this.config)
          .then((response: AxiosResponse) => {
            if (response.status === 200) {
              callback();
            } else {
              errorHandler(this.launchNotifier, response);
            }
          })
          .catch((error: AxiosError) =>
            errorHandler(this.launchNotifier, error.response),
          );
         }
  
         patchBlogSettings(
          data: BlogPatch,
          callback: () => void,
          errorHandler: (
            notifier: NotifierFunction,
            response?: AxiosResponse,
          ) => void = this.genericErrorHandler,
         ) {
          return axios
          .patch('/org/' + this.orgID + '/blog', data, this.config)
          .then((response: AxiosResponse) => {
            if (response.status === 200) {
              callback();
            } else {
              errorHandler(this.launchNotifier, response);
            }
          })
          .catch((error: AxiosError) =>
            errorHandler(this.launchNotifier, error.response),
          );
         }
  
         deleteBlog(
          callback: () => void,
          errorHandler: (
            notifier: NotifierFunction,
            response?: AxiosResponse,
          ) => void = this.genericErrorHandler,
         ) {
          return axios
          .delete('/org/' + this.orgID + '/blog', this.config)
          .then((response: AxiosResponse) => {
            if (response.status === 200) {
              callback();
            } else {
              errorHandler(this.launchNotifier, response);
            }
          })
          .catch((error: AxiosError) =>
            errorHandler(this.launchNotifier, error.response),
          );
         }
  
         buildBlog(
          callback: () => void,
          errorHandler: (
            notifier: NotifierFunction,
            response?: AxiosResponse,
          ) => void = this.genericErrorHandler,
         ) {
          return axios
          .post('/org/' + this.orgID + '/blog/build', {}, this.config)
          .then((response: AxiosResponse) => {
            if (response.status === 200) {
              callback();
            } else {
              errorHandler(this.launchNotifier, response);
            }
          })
          .catch((error: AxiosError) =>
            errorHandler(this.launchNotifier, error.response),
          );
         }
  
         publishBlog(
          callback: () => void,
          errorHandler: (
            notifier: NotifierFunction,
            response?: AxiosResponse,
          ) => void = this.genericErrorHandler,
         ) {
          return axios
          .post('/org/' + this.orgID + '/blog/publish', {}, this.config)
          .then((response: AxiosResponse) => {
            if (response.status === 200) {
              callback();
            } else {
              errorHandler(this.launchNotifier, response);
            }
          })
          .catch((error: AxiosError) =>
            errorHandler(this.launchNotifier, error.response),
          );
         }

         getBlogSettings(
          callback: (site: Site) => void,
          errorHandler: (
            notifier: NotifierFunction,
            response?: AxiosResponse,
          ) => void = this.genericErrorHandler,
         ) {
          return axios
          .get('/org/' + this.orgID + '/blog', this.config)
          .then((response: AxiosResponse) => {
            if (response.status === 200) {
              callback(response.data);
            } else {
              errorHandler(this.launchNotifier, response);
            }
          })
          .catch((error: AxiosError) =>
            errorHandler(this.launchNotifier, error.response),
          );
         }

         getOrgConfig(
           callback: (config: Config) => void,
           errorHandler: (
             notifier: NotifierFunction,
             response?: AxiosResponse,
           ) => void = this.genericErrorHandler,
         ) {
           return axios
             .get('/org/' + this.orgID + '/config', this.config)
             .then((response: AxiosResponse) => {
               if (response.status === 200) {
                 callback(response.data);
               } else {
                 errorHandler(this.launchNotifier, response);
               }
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }

         patchOrgConfig(
           data: ConfigPatch,
           callback: () => void,
           errorHandler: (
             notifier: NotifierFunction,
             response?: AxiosResponse,
           ) => void = this.genericErrorHandler,
         ) {
           return axios
             .patch('/org/' + this.orgID + '/config', data, this.config)
             .then((response: AxiosResponse) => {
               if (response.status === 200) {
                 callback();
               } else {
                 errorHandler(this.launchNotifier, response);
               }
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }

         getOrgMembers(
           callback: (members: MemberProfile[]) => void,
           errorHandler: (
             notifier: NotifierFunction,
             response?: AxiosResponse,
           ) => void = this.genericErrorHandler,
         ) {
           return axios
             .get('/org/' + this.orgID + '/members', this.config)
             .then((response: AxiosResponse) => {
               if (response.status === 200) {
                 callback(response.data);
               } else {
                 errorHandler(this.launchNotifier, response);
               }
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }

         deleteOrgMember(
           profileID: ProfileID,
           callback: () => void,
           errorHandler: (
             notifier: NotifierFunction,
             response?: AxiosResponse,
           ) => void = this.genericErrorHandler,
         ) {
           return axios
             .delete('/org/' + this.orgID + '/member/' + profileID, this.config)
             .then((response: AxiosResponse) => {
               if (response.status === 200) {
                 callback();
               } else {
                 errorHandler(this.launchNotifier, response);
               }
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }

         getOrgRoles(
           callback: (roles: Role[]) => void,
           errorHandler: (
             notifier: NotifierFunction,
             response?: AxiosResponse,
           ) => void = this.genericErrorHandler,
         ) {
           return axios
             .get('/org/' + this.orgID + '/roles', this.config)
             .then((response: AxiosResponse) => {
               if (response.status === 200) {
                 callback(response.data);
               } else {
                 errorHandler(this.launchNotifier, response);
               }
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }

         getOrgRoleMembers(
           roleID: RoleID,
           callback: (members: MemberProfile) => void,
           errorHandler: (
             notifier: NotifierFunction,
             response?: AxiosResponse,
           ) => void = this.genericErrorHandler,
         ) {
           return axios
             .get(
               '/org/' + this.orgID + '/role/' + roleID + '/members',
               this.config,
             )
             .then((response: AxiosResponse) => {
               if (response.status === 200) {
                 callback(response.data);
               } else {
                 errorHandler(this.launchNotifier, response);
               }
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }

         createOrgRole(
           data: Role,
           callback: (roleID: RoleID) => void,
           errorHandler: (
             notifier: NotifierFunction,
             response?: AxiosResponse,
           ) => void = this.genericErrorHandler,
         ) {
           return axios
             .post('/org/' + this.orgID + '/role', data, this.config)
             .then((response: AxiosResponse) => {
               callback(response.data.id);
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }

         patchOrgRole(
           data: Role,
           roleID: RoleID,
           callback: () => void,
           errorHandler: (
             notifier: NotifierFunction,
             response?: AxiosResponse,
           ) => void = this.genericErrorHandler,
         ) {
           return axios
             .patch('/org/' + this.orgID + '/role/' + roleID, data, this.config)
             .then((response: AxiosResponse) => {
               if (response.status === 200) {
                 callback();
               } else {
                 errorHandler(this.launchNotifier, response);
               }
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }

         getOrgRole(
           roleID: RoleID,
           callback: (role: Role) => void,
           errorHandler: (
             notifier: NotifierFunction,
             response?: AxiosResponse,
           ) => void = this.genericErrorHandler,
         ) {
           return axios
             .get('/org/' + this.orgID + '/role/' + roleID, this.config)
             .then((response: AxiosResponse) => {
               if (response.status === 200) {
                 callback(response.data);
               } else {
                 errorHandler(this.launchNotifier, response);
               }
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }
  
        getMyPermissions(
           callback: (permissions: MemberOrgPermissions) => void,
           errorHandler: (
             notifier: NotifierFunction,
             response?: AxiosResponse,
           ) => void = this.genericErrorHandler,
         ) {
           return axios
             .get('/org/' + this.orgID + '/mypermissions', this.config)
             .then((response: AxiosResponse) => {
               if (response.status === 200) {
                 callback(response.data);
               } else {
                 errorHandler(this.launchNotifier, response);
               }
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }

         addOrgRoleMember(
           roleID: RoleID,
           profileID: ProfileID,
           callback: () => void,
           errorHandler: (
             notifier: NotifierFunction,
             response?: AxiosResponse,
           ) => void = this.genericErrorHandler,
         ) {
           return axios
             .post(
               '/org/' +
                 this.orgID +
                 '/role/' +
                 roleID +
                 '/member/' +
                 profileID,
               {},
               this.config,
             )
             .then((response: AxiosResponse) => {
               if (response.status === 200) {
                 callback();
               } else {
                 errorHandler(this.launchNotifier, response);
               }
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }

         deleteOrgRoleMember(
           roleID: RoleID,
           profileID: ProfileID,
           callback: () => void,
           errorHandler: (
             notifier: NotifierFunction,
             response?: AxiosResponse,
           ) => void = this.genericErrorHandler,
         ) {
           return axios
             .delete(
               '/org/' +
                 this.orgID +
                 '/role/' +
                 roleID +
                 '/member/' +
                 profileID,
               this.config,
             )
             .then((response: AxiosResponse) => {
               if (response.status === 200) {
                 callback();
               } else {
                 errorHandler(this.launchNotifier, response);
               }
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }

         orgRoleInvite(
           data: RoleInviteRequest,
           roleID: RoleID,
           callback: (response: RoleInviteResponse) => void,
           errorHandler: (
             notifier: NotifierFunction,
             response?: AxiosResponse,
           ) => void = this.genericErrorHandler,
         ) {
           return axios
             .post(
               '/org/' + this.orgID + '/role/' + roleID + '/invite',
               data,
               this.config,
             )
             .then((response: AxiosResponse) => {
               if (response.status === 200) {
                 callback(response.data);
               } else {
                 errorHandler(this.launchNotifier, response);
               }
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }

         joinWithInviteID(
           inviteID: RoleInviteID,
           callback: () => void,
           errorHandler: (
             notifier: NotifierFunction,
             response?: AxiosResponse,
           ) => void = this.genericErrorHandler,
         ) {
           return axios
             .post('/invite/' + inviteID + '/join', {}, this.config)
             .then((response: AxiosResponse) => {
               if (response.status === 200) {
                 callback();
               } else {
                 errorHandler(this.launchNotifier, response);
               }
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }

         // *********** *********** ***********
         // maps
         // *********** *********** ***********
         getMyMaps(
           callback: (maps: MapProperties[]) => void,
           errorHandler: (
             notifier: NotifierFunction,
             response?: AxiosResponse,
           ) => void = this.genericErrorHandler,
         ) {
           return axios
             .get('/org/' + this.orgID + '/mymaps', this.config)
             .then((response: AxiosResponse) => {
               if (response.status === 200) {
                 callback(response.data);
               } else {
                 errorHandler(this.launchNotifier, response);
               }
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }

         getFlashcardTypes(
           callback: (flashcardTypes: FlashcardTypeBrief[]) => void,
           errorHandler: (
             notifier: NotifierFunction,
             response?: AxiosResponse,
           ) => void = this.genericErrorHandler,
         ) {
           return axios
             .get('/org/' + this.orgID + '/flashcardTypes', this.config)
             .then((response: AxiosResponse) => {
               if (response.status === 200) {
                 callback(response.data);
               } else {
                 errorHandler(this.launchNotifier, response);
               }
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }

         getMap(
           mapID: MapID,
           segmentID: SegmentID,
           callback: (map: {}) => void, // TODO - describe map data type
           errorHandler: (
             notifier: NotifierFunction,
             response?: AxiosResponse,
           ) => void = this.genericErrorHandler,
         ) {
           return axios
             .get(
               '/org/' + this.orgID + '/map/' + mapID + '/segment/' + segmentID,
               this.config,
             )
             .then((response: AxiosResponse) => {
               if (response.status === 200) {
                 callback(response.data);
               } else {
                 errorHandler(this.launchNotifier, response);
               }
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }

         getMapBreadcrumb(
           mapID: MapID,
           segmentID: SegmentID,
           callback: (nodes: Node[]) => void,
           errorHandler: (
             notifier: NotifierFunction,
             response?: AxiosResponse,
           ) => void = this.genericErrorHandler,
         ) {
           return axios
             .get(
               '/org/' +
                 this.orgID +
                 '/map/' +
                 mapID +
                 '/segment/' +
                 segmentID +
                 '/breadcrumb',
               this.config,
             )
             .then((response: AxiosResponse) => {
               if (response.status === 200) {
                 callback(response.data);
               } else {
                 errorHandler(this.launchNotifier, response);
               }
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }

         getMapSettings(
           mapID: MapID,
           callback: (map: MindMap) => void,
           errorHandler: (
             notifier: NotifierFunction,
             response?: AxiosResponse,
           ) => void = this.genericErrorHandler,
         ) {
           return axios
             .get(
               '/org/' + this.orgID + '/map/' + mapID + '/settings',
               this.config,
             )
             .then((response: AxiosResponse) => {
               if (response.status === 200) {
                 callback(response.data);
               } else {
                 errorHandler(this.launchNotifier, response);
               }
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }

         setMapIcon(
           mapID: MapID,
           image: File,
           callback: () => void,
           errorHandler: (
             notifier: NotifierFunction,
             response?: AxiosResponse,
           ) => void = this.genericErrorHandler,
         ) {
           const formData = new FormData();
           formData.append('icon', image);
           return axios
             .post(
               '/org/' + this.orgID + '/map/' + mapID + '/icon',
               formData,
               this.config,
             )
             .then((response: AxiosResponse) => {
               if (response.status === 200) {
                 callback();
               } else {
                 errorHandler(this.launchNotifier, response);
               }
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }

         setSiteLogo(
           image: File,
           callback: () => void,
           errorHandler: (
             notifier: NotifierFunction,
             response?: AxiosResponse,
           ) => void = this.genericErrorHandler,
         ) {
           const formData = new FormData();
           formData.append('logo', image);
           return axios
             .post(
               '/org/' + this.orgID + '/blog/logo',
               formData,
               this.config,
             )
             .then((response: AxiosResponse) => {
               if (response.status === 200) {
                 callback();
               } else {
                 errorHandler(this.launchNotifier, response);
               }
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }

         createMap(
           data: MapProperties,
           callback: (newMapID: MapID) => void,
           errorHandler: (
             notifier: NotifierFunction,
             response?: AxiosResponse,
           ) => void = this.genericErrorHandler,
         ) {
           return axios
             .post('/org/' + this.orgID + '/mymaps', data, this.config)
             .then((response: AxiosResponse) => {
               callback(response.data.id);
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }

         patchMapSettings(
           mapID: MapID,
           data: MapPropertiesPatch,
           callback: () => void,
           errorHandler: (
             notifier: NotifierFunction,
             response?: AxiosResponse,
           ) => void = this.genericErrorHandler,
         ) {
           return axios
             .patch(
               '/org/' + this.orgID + '/map/' + mapID + '/settings',
               data,
               this.config,
             )
             .then((response: AxiosResponse) => {
               if (response.status === 200) {
                 callback();
               } else {
                 errorHandler(this.launchNotifier, response);
               }
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }

         createMapNode(
           data: NodePost,
           mapID: MapID,
           segmentID: SegmentID,
           callback: (newNodeID: NodeID) => void,
           errorHandler: (
             notifier: NotifierFunction,
             response?: AxiosResponse,
           ) => void = this.genericErrorHandler,
         ) {
           return axios
             .post(
               '/org/' +
                 this.orgID +
                 '/map/' +
                 mapID +
                 '/segment/' +
                 segmentID +
                 '/node',
               data,
               this.config,
             )
             .then((response: AxiosResponse) => {
               callback(response.data.id);
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }

         patchMapNode(
           data: NodePatch,
           mapID: MapID,
           segmentID: SegmentID,
           nodeID: NodeID,
           callback: (data: Node) => void,
           errorHandler: (
             notifier: NotifierFunction,
             response?: AxiosResponse,
           ) => void = this.genericErrorHandler,
         ) {
           return axios
             .patch(
               '/org/' +
                 this.orgID +
                 '/map/' +
                 mapID +
                 '/segment/' +
                 segmentID +
                 '/node/' +
                 nodeID,
               data,
               this.config,
             )
             .then((response: AxiosResponse) => {
               if (response.status === 200) {
                 callback(response.data);
               } else {
                 errorHandler(this.launchNotifier, response);
               }
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }

         deleteMapNode(
           mapID: MapID,
           segmentID: SegmentID,
           nodeID: NodeID,
           callback: () => void,
           errorHandler: (
             notifier: NotifierFunction,
             response?: AxiosResponse,
           ) => void = this.genericErrorHandler,
         ) {
           return axios
             .delete(
               '/org/' +
                 this.orgID +
                 '/map/' +
                 mapID +
                 '/segment/' +
                 segmentID +
                 '/node/' +
                 nodeID,
               this.config,
             )
             .then((response: AxiosResponse) => {
               if (response.status === 200) {
                 callback();
               } else {
                 errorHandler(this.launchNotifier, response);
               }
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }

         makeSubMap(
           mapID: MapID,
           segmentID: SegmentID,
           nodeID: NodeID,
           callback: (newMapID: MapID) => void,
           errorHandler: (
             notifier: NotifierFunction,
             response?: AxiosResponse,
           ) => void = this.genericErrorHandler,
         ) {
           return axios
             .post(
               '/org/' +
                 this.orgID +
                 '/map/' +
                 mapID +
                 '/segment/' +
                 segmentID +
                 '/node/' +
                 nodeID +
                 '/makeSubMap',
               {},
               this.config,
             )
             .then((response: AxiosResponse) => {
               callback(response.data.subMapID);
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }

         patchMapNodeAttributes(
           data: NodePatchAttributes,
           mapID: MapID,
           segmentID: SegmentID,
           nodeID: NodeID,
           callback: () => void,
           errorHandler: (
             notifier: NotifierFunction,
             response?: AxiosResponse,
           ) => void = this.genericErrorHandler,
         ) {
           return axios
             .patch(
               '/org/' +
                 this.orgID +
                 '/map/' +
                 mapID +
                 '/segment/' +
                 segmentID +
                 '/node/' +
                 nodeID +
                 '/attributes',
               data,
               this.config,
             )
             .then((response: AxiosResponse) => {
               if (response.status === 200) {
                 callback();
               } else {
                 errorHandler(this.launchNotifier, response);
               }
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }

         storeImageFile(
           data: File,
           mapID: MapID,
           callback: (response: StoreImageFileResponse) => void,
           errorHandler: (
             notifier: NotifierFunction,
             response?: AxiosResponse,
           ) => void = this.genericErrorHandler,
         ) {
           return axios
             .post(
               '/org/' + this.orgID + '/map/' + mapID + '/image',
               data,
               this.config,
             )
             .then((response: AxiosResponse) => {
               if (response.status === 200) {
                 callback(response.data);
               } else {
                 errorHandler(this.launchNotifier, response);
               }
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }

         validateFlashcard(
           data: Exercise,
           mapID: MapID,
           cardType: string,
           callback: (response: { message: string; output: string }) => void,
           errorHandler: (
             notifier: NotifierFunction,
             response?: AxiosResponse,
           ) => void = this.genericErrorHandler,
         ) {
           return axios
             .post(
               '/org/' +
                 this.orgID +
                 '/map/' +
                 mapID +
                 '/card/validate' +
                 '?cardType=' +
                 cardType,
               data,
               this.config,
             )
             .then((response: AxiosResponse) => {
               if (response.status === 200) {
                 callback(response.data);
               } else {
                 errorHandler(this.launchNotifier, response);
               }
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }

         storeFlashcardAudio(
           data: Blob,
           mapID: MapID,
           nodeID: NodeID,
           callback: () => void,
           errorHandler: (
             notifier: NotifierFunction,
             response?: AxiosResponse,
           ) => void = this.genericErrorHandler,
         ) {
           return axios
             .post(
               '/org/' +
                 this.orgID +
                 '/map/' +
                 mapID +
                 '/card/' +
                 nodeID +
                 '/audio',
               data,
               this.config,
             )
             .then((response: AxiosResponse) => {
               if (response.status === 200) {
                 callback();
               } else {
                 errorHandler(this.launchNotifier, response);
               }
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }

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
           ) => void = this.genericErrorHandler,
         ) {
           return axios
             .post(
               '/org/' +
                 this.orgID +
                 '/map/' +
                 mapID +
                 '/segment/' +
                 segmentID +
                 '/node/' +
                 nodeID +
                 '/card' +
                 '?cardType=' +
                 cardType,
               data,
               this.config,
             )
             .then((response: AxiosResponse) => {
               if (response.status === 200) {
                 callback();
               } else {
                 errorHandler(this.launchNotifier, response);
               }
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }

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
           ) => void = this.genericErrorHandler,
         ) {
           return axios
             .put(
               '/org/' +
                 this.orgID +
                 '/map/' +
                 mapID +
                 '/segment/' +
                 segmentID +
                 '/node/' +
                 nodeID +
                 '/card' +
                 '?cardType=' +
                 cardType,
               data,
               this.config,
             )
             .then((response: AxiosResponse) => {
               if (response.status === 200) {
                 callback();
               } else {
                 errorHandler(this.launchNotifier, response);
               }
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }

         getFlashcard<T>(
           mapID: MapID,
           segmentID: SegmentID,
           nodeID: NodeID,
           callback: (response: T) => void,
           errorHandler: (
             notifier: NotifierFunction,
             response?: AxiosResponse,
           ) => void = this.genericErrorHandler,
         ) {
           return axios
             .get(
               '/org/' +
                 this.orgID +
                 '/map/' +
                 mapID +
                 '/segment/' +
                 segmentID +
                 '/node/' +
                 nodeID +
                 '/card',
               this.config,
             )
             .then((response: AxiosResponse) => {
               if (response.status === 200) {
                 callback(response.data);
               } else {
                 errorHandler(this.launchNotifier, response);
               }
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }

         getFlashcardType(
           mapID: MapID,
           segmentID: SegmentID,
           nodeID: NodeID,
           callback: (response: FlashcardTypeBrief) => void,
           errorHandler: (
             notifier: NotifierFunction,
             response?: AxiosResponse,
           ) => void = this.genericErrorHandler,
         ) {
           return axios
             .get(
               '/org/' +
                 this.orgID +
                 '/map/' +
                 mapID +
                 '/segment/' +
                 segmentID +
                 '/node/' +
                 nodeID +
                 '/cardType',
               this.config,
             )
             .then((response: AxiosResponse) => {
               if (response.status === 200) {
                 callback(response.data);
               } else {
                 errorHandler(this.launchNotifier, response);
               }
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }

         createBlogPost(
           data: PostMarkdown,
           mapID: MapID,
           segmentID: SegmentID,
           nodeID: NodeID,
           callback: () => void,
           errorHandler: (
             notifier: NotifierFunction,
             response?: AxiosResponse,
           ) => void = this.genericErrorHandler,
         ) {
           return axios
             .post(
               '/org/' +
                 this.orgID +
                 '/map/' +
                 mapID +
                 '/segment/' +
                 segmentID +
                 '/node/' +
                 nodeID +
                 '/post',
               data,
               this.config,
             )
             .then((response: AxiosResponse) => {
               if (response.status === 200) {
                 callback();
               } else {
                 errorHandler(this.launchNotifier, response);
               }
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }

         updateBlogPost(
           data: PostMarkdown,
           mapID: MapID,
           segmentID: SegmentID,
           nodeID: NodeID,
           callback: () => void,
           errorHandler: (
             notifier: NotifierFunction,
             response?: AxiosResponse,
           ) => void = this.genericErrorHandler,
         ) {
           return axios
             .put(
               '/org/' +
                 this.orgID +
                 '/map/' +
                 mapID +
                 '/segment/' +
                 segmentID +
                 '/node/' +
                 nodeID +
                 '/post',
               data,
               this.config,
             )
             .then((response: AxiosResponse) => {
               if (response.status === 200) {
                 callback();
               } else {
                 errorHandler(this.launchNotifier, response);
               }
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }

         getBlogPost(
           mapID: MapID,
           segmentID: SegmentID,
           nodeID: NodeID,
           callback: (response: Post) => void,
           errorHandler: (
             notifier: NotifierFunction,
             response?: AxiosResponse,
           ) => void = this.genericErrorHandler,
         ) {
           return axios
             .get(
               '/org/' +
                 this.orgID +
                 '/map/' +
                 mapID +
                 '/segment/' +
                 segmentID +
                 '/node/' +
                 nodeID +
                 '/post',
               this.config,
             )
             .then((response: AxiosResponse) => {
               if (response.status === 200) {
                 callback(response.data);
               } else {
                 errorHandler(this.launchNotifier, response);
               }
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }
  
        getBlogPostPreview(
           mapID: MapID,
           callback: (response: BlogPage) => void,
           errorHandler: (
             notifier: NotifierFunction,
             response?: AxiosResponse,
           ) => void = this.genericErrorHandler,
         ) {
           return axios
             .get(
               '/org/' +
                 this.orgID +
                 '/map/' +
                 mapID +
                 '/page',
               this.config,
             )
             .then((response: AxiosResponse) => {
               if (response.status === 200) {
                 callback(response.data);
               } else {
                 errorHandler(this.launchNotifier, response);
               }
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }

         deleteBlogPost(
           mapID: MapID,
           segmentID: SegmentID,
           nodeID: NodeID,
           callback: () => void,
           errorHandler: (
             notifier: NotifierFunction,
             response?: AxiosResponse,
           ) => void = this.genericErrorHandler,
         ) {
           return axios
             .delete(
               '/org/' +
                 this.orgID +
                 '/map/' +
                 mapID +
                 '/segment/' +
                 segmentID +
                 '/node/' +
                 nodeID +
                 '/post',
               this.config,
             )
             .then((response: AxiosResponse) => {
               if (response.status === 200) {
                 callback();
               } else {
                 errorHandler(this.launchNotifier, response);
               }
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }

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
           ) => void = this.genericErrorHandler,
         ) {
           return axios
             .post(
               '/org/' + this.orgID + '/map/' + mapID + '/rights',
               data,
               this.config,
             )
             .then((response: AxiosResponse) => {
               callback(response.data.mapRightsID);
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }

         getAllMapRights(
           mapID: MapID,
           callback: (data: MapRightsMembers[]) => void,
           errorHandler: (
             notifier: NotifierFunction,
             response?: AxiosResponse,
           ) => void = this.genericErrorHandler,
         ) {
           return axios
             .get(
               '/org/' + this.orgID + '/map/' + mapID + '/rights',
               this.config,
             )
             .then((response: AxiosResponse) => {
               if (response.status === 200) {
                 callback(response.data);
               } else {
                 errorHandler(this.launchNotifier, response);
               }
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }

         mapRightsInvite(
           data: MapRightsInvite,
           mapID: MapID,
           mapRightsID: MapRightsID,
           callback: (response: RoleInviteResponse) => void,
           errorHandler: (
             notifier: NotifierFunction,
             response?: AxiosResponse,
           ) => void = this.genericErrorHandler,
         ) {
           return axios
             .post(
               '/org/' +
                 this.orgID +
                 '/map/' +
                 mapID +
                 '/rights/' +
                 mapRightsID +
                 '/invite',
               data,
               this.config,
             )
             .then((response: AxiosResponse) => {
               if (response.status === 200) {
                 callback(response.data);
               } else {
                 errorHandler(this.launchNotifier, response);
               }
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }

         getMapRights(
           mapID: MapID,
           mapRightsID: MapRightsID,
           callback: (data: MapRightsMembers) => void,
           errorHandler: (
             notifier: NotifierFunction,
             response?: AxiosResponse,
           ) => void = this.genericErrorHandler,
         ) {
           return axios
             .get(
               '/org/' +
                 this.orgID +
                 '/map/' +
                 mapID +
                 '/rights/' +
                 mapRightsID,
               this.config,
             )
             .then((response: AxiosResponse) => {
               if (response.status === 200) {
                 callback(response.data);
               } else {
                 errorHandler(this.launchNotifier, response);
               }
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }

         patchMapRights(
           data: MapRightsPatch,
           mapID: MapID,
           mapRightsID: MapRightsID,
           callback: (mapRights: MapRightsID) => void,
           errorHandler: (
             notifier: NotifierFunction,
             response?: AxiosResponse,
           ) => void = this.genericErrorHandler,
         ) {
           return axios
             .patch(
               '/org/' +
                 this.orgID +
                 '/map/' +
                 mapID +
                 '/rights/' +
                 mapRightsID,
               data,
               this.config,
             )
             .then((response: AxiosResponse) => {
               if (response.status === 200) {
                 callback(response.data);
               } else {
                 errorHandler(this.launchNotifier, response);
               }
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }

         deleteMapRights(
           mapID: MapID,
           mapRightsID: MapRightsID,
           callback: () => void,
           errorHandler: (
             notifier: NotifierFunction,
             response?: AxiosResponse,
           ) => void = this.genericErrorHandler,
         ) {
           return axios
             .delete(
               '/org/' +
                 this.orgID +
                 '/map/' +
                 mapID +
                 '/rights/' +
                 mapRightsID,
               this.config,
             )
             .then((response: AxiosResponse) => {
               if (response.status === 200) {
                 callback();
               } else {
                 errorHandler(this.launchNotifier, response);
               }
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }

         addMapRightsMember(
           mapID: MapID,
           mapRightsID: MapRightsID,
           profileID: ProfileID,
           callback: () => void,
           errorHandler: (
             notifier: NotifierFunction,
             response?: AxiosResponse,
           ) => void = this.genericErrorHandler,
         ) {
           return axios
             .post(
               '/org/' +
                 this.orgID +
                 '/map/' +
                 mapID +
                 '/rights/' +
                 mapRightsID +
                 '/member/' +
                 profileID,
               {},
               this.config,
             )
             .then((response: AxiosResponse) => {
               if (response.status === 200) {
                 callback();
               } else {
                 errorHandler(this.launchNotifier, response);
               }
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }

         removeMapRightsMember(
           mapID: MapID,
           mapRightsID: MapRightsID,
           profileID: ProfileID,
           callback: () => void,
           errorHandler: (
             notifier: NotifierFunction,
             response?: AxiosResponse,
           ) => void = this.genericErrorHandler,
         ) {
           return axios
             .delete(
               '/org/' +
                 this.orgID +
                 '/map/' +
                 mapID +
                 '/rights/' +
                 mapRightsID +
                 '/member/' +
                 profileID,
               this.config,
             )
             .then((response: AxiosResponse) => {
               if (response.status === 200) {
                 callback();
               } else {
                 errorHandler(this.launchNotifier, response);
               }
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }

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
           ) => void = this.genericErrorHandler,
         ) {
           return axios
             .get(
               '/org/' +
                 this.orgID +
                 '/train/' +
                 mapID +
                 '/card/' +
                 nodeID +
                 '/cardType',
               this.config,
             )
             .then((response: AxiosResponse) => {
               if (response.status === 200) {
                 callback(response.data);
               } else {
                 errorHandler(this.launchNotifier, response);
               }
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }

         getFlashcardTest<T>(
           mapID: MapID,
           nodeID: NodeID,
           callback: (response: T) => void,
           errorHandler: (
             notifier: NotifierFunction,
             response?: AxiosResponse,
           ) => void = this.genericErrorHandler,
         ) {
           return axios
             .get(
               '/org/' + this.orgID + '/train/' + mapID + '/card/' + nodeID,
               this.config,
             )
             .then((response: AxiosResponse) => {
               if (response.status === 200) {
                 callback(response.data);
               } else {
                 errorHandler(this.launchNotifier, response);
               }
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }

         getMapPostsReadTimes(
           mapID: MapID,
           callback: (postsTimes: PostTimeRead[]) => void,
           errorHandler: (
             notifier: NotifierFunction,
             response?: AxiosResponse,
           ) => void = this.genericErrorHandler,
         ) {
           return axios
             .get(
               '/org/' + this.orgID + '/train/' + mapID + '/postsReadTimes',
               this.config,
             )
             .then((response: AxiosResponse) => {
               if (response.status === 200) {
                 callback(response.data);
               } else {
                 errorHandler(this.launchNotifier, response);
               }
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }

         getCards(
           mapID: MapID,
           segmentID: SegmentID,
           callback: (cardsDue: CardsDue) => void,
           errorHandler: (
             notifier: NotifierFunction,
             response?: AxiosResponse,
           ) => void = this.genericErrorHandler,
         ) {
           return axios
             .get(
               '/org/' +
                 this.orgID +
                 '/train/' +
                 mapID +
                 '/segment/' +
                 segmentID +
                 '/cards',
               this.config,
             )
             .then((response: AxiosResponse) => {
               if (response.status === 200) {
                 callback(response.data);
               } else {
                 errorHandler(this.launchNotifier, response);
               }
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }

         getBlogPostTest(
           mapID: MapID,
           nodeID: NodeID,
           callback: (response: Post) => void,
           errorHandler: (
             notifier: NotifierFunction,
             response?: AxiosResponse,
           ) => void = this.genericErrorHandler,
         ) {
           return axios
             .get(
               '/org/' + this.orgID + '/train/' + mapID + '/post/' + nodeID,
               this.config,
             )
             .then((response: AxiosResponse) => {
               if (response.status === 200) {
                 callback(response.data);
               } else {
                 errorHandler(this.launchNotifier, response);
               }
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }

         markBlogPostRead(
           mapID: MapID,
           nodeID: NodeID,
           callback: () => void,
           errorHandler: (
             notifier: NotifierFunction,
             response?: AxiosResponse,
           ) => void = this.genericErrorHandler,
         ) {
           return axios
             .post(
               '/org/' +
                 this.orgID +
                 '/train/' +
                 mapID +
                 '/post/' +
                 nodeID +
                 '/markRead',
               {},
               this.config,
             )
             .then((response: AxiosResponse) => {
               if (response.status === 200) {
                 callback();
               } else {
                 errorHandler(this.launchNotifier, response);
               }
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }

         gradeFlashcard(
           data: TestAnswer | ExerciseAnswer,
           mapID: MapID,
           nodeID: NodeID,
           cardType: string,
           callback: (response: CardGradeAnswer) => void,
           errorHandler: (
             notifier: NotifierFunction,
             response?: AxiosResponse,
           ) => void = this.genericErrorHandler,
         ) {
           return axios
             .post(
               '/org/' +
                 this.orgID +
                 '/train/' +
                 mapID +
                 '/card/' +
                 nodeID +
                 '/grade' +
                 '?cardType=' +
                 cardType,
               data,
               this.config,
             )
             .then((response: AxiosResponse) => {
               if (response.status === 200) {
                 callback(response.data);
               } else {
                 errorHandler(this.launchNotifier, response);
               }
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }

         logFlashcardQuality(
           data: Quality,
           mapID: MapID,
           nodeID: NodeID,
           callback: () => void,
           errorHandler: (
             notifier: NotifierFunction,
             response?: AxiosResponse,
           ) => void = this.genericErrorHandler,
         ) {
           return axios
             .post(
               '/org/' +
                 this.orgID +
                 '/train/' +
                 mapID +
                 '/card/' +
                 nodeID +
                 '/quality',
               data,
               this.config,
             )
             .then((response: AxiosResponse) => {
               if (response.status === 200) {
                 callback();
               } else {
                 errorHandler(this.launchNotifier, response);
               }
             })
             .catch((error: AxiosError) =>
               errorHandler(this.launchNotifier, error.response),
             );
         }

         // *********** *********** ***********
         // Private Functions
         // *********** *********** ***********
         private genericErrorHandler(
           notifier: NotifierFunction,
           error?: AxiosResponse,
         ) {
           if (error) {
            //  console.log(error);
             if (error.status === 401) {
               notifier('permission', error.data.message);
             } else {
               notifier('message', (error.data.message ? error.data.message : error.statusText));
             }
           } else {
            //  console.log('unknown error occurred');
           }
         }
       }
