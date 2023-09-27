import { Auth0DecodedHash, Auth0Error } from 'auth0-js';
// What a shitty 3rd party solution from Auth0; wish this was functional code!
/**
 * Auth0 authentication contract
 * @export
 * @interface Auth0Authentication
 */
export interface Auth0Authentication {
  /**
   * @property readonly
   * @type {boolean}
   * @memberof Auth0Authentication
   */
  readonly authenticated: boolean;
  /**
   * Start authentication session
   * @memberof Auth0Authentication
   */
  login(): void;
  /**
   * Consume authentication results
   * @memberof Auth0Authentication
   */
  // tslint:disable-next-line: no-any
  renewAuth(callback: (error: Auth0Error | null, result: any) => void): void;
  /**
   * Consume authentication results
   * @memberof Auth0Authentication
   */
  redirect(result: Auth0DecodedHash): void;
  /**
   * Callback for authentication session
   * @param {Auth0DecodedHash} authResult
   * @memberof AuthenticationManager
   */
  setSession(authResult: Auth0DecodedHash): void;
  /**
   * Destroy session
   * @memberof AuthenticationManager
   */
  logout(): void;
  /**
   * Login to Another Account
   * @memberof AuthenticationManager
   */
  loginToAnotherAccount(): void;
  /**
   * Clear Storage Session
   * @memberof AuthenticationManager
   */
  clearSession(): void;
  /**
   * Clear Storage For Last Session
   * @memberof AuthenticationManager
   */
  clearLastSession(): void;
  /**
   * Parse hash
   * @memberof AuthenticationManager
   */
  parseHash(callback: (auth0Hash: Auth0DecodedHash) => void): void;
}
