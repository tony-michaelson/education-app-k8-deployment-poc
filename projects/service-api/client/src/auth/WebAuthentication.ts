import boundMethod from 'autobind-decorator';
import history from '../utils/history';
import { AUTH_CONFIG } from './configuration';
import { Auth0Authentication } from './Auth0Authentication';
import { Auth0DecodedHash, Auth0Error, WebAuth } from 'auth0-js';
import { API_CONFIG } from '../api/ApiConfig';
/**
 * Web based Auth0 authentication
 *
 * @export
 * @class WebAuthentication
 * @implements {Auth0Authentication}
 */
export class WebAuthentication implements Auth0Authentication {
  authOptions = {
    audience: AUTH_CONFIG.audience,
    domain: AUTH_CONFIG.domain,
    clientID: AUTH_CONFIG.clientId,
    redirectUri: AUTH_CONFIG.callbackUrl,
    responseType: 'token id_token',
    scope: 'openid profile email',
  };

  /**
   * @property
   * @private
   * @type {WebAuth}
   * @memberof WebAuthenticationManager
   */
  auth0: WebAuth = new WebAuth(this.authOptions);

  get authenticated(): boolean {
    // Check whether the current time is past the
    // access token's expiry time
    let expiresAt = JSON.parse(localStorage.getItem('expires_at')!);
    return new Date().getTime() < expiresAt;
  }

  @boundMethod
  login(): void {
    this.auth0.authorize();
  }

  @boundMethod
  parseHash(callback: (auth0Hash: Auth0DecodedHash) => void): void {
    this.auth0.parseHash((e: Auth0Error, result: Auth0DecodedHash) => {
      if (result && result.accessToken && result.idToken) {
        callback(result);
      } else if (e) {
        history.replace('/dashboard');
        // tslint:disable-next-line:no-console
        console.error(e);
        alert(`Error: ${e.error}. Check the console for further details.`);
      }
    });
  }

  @boundMethod
  // tslint:disable-next-line: no-any
  renewAuth(callback: (error: Auth0Error | null, result: any) => void): void {
    this.auth0.checkSession(
      {
        audience: AUTH_CONFIG.audience,
        scope: 'openid profile email',
      },
      callback,
    );
  }

  @boundMethod
  logSignIn(): void {
    fetch(API_CONFIG.api_host + '/member/logSignIn', {
      method: 'POST',
      headers: {
        AUTHORIZATION: 'Bearer ' + localStorage.getItem('access_token'),
        'Content-Type': 'application/json',
      },
    })
      .then((response) => {
        if (response.status === 200) {
          const redirectedFromUrl = localStorage.getItem('redirectedFromUrl');
          if (redirectedFromUrl) {
            localStorage.setItem('redirectedFromUrl', '');
            history.replace(redirectedFromUrl);
          } else {
            history.replace('/dashboard');
          }
        } else {
          history.replace('/signup');
        }
      })
      // TODO handle error
      .catch((error) => alert(error));
  }

  redirect(authResult: Auth0DecodedHash): void {
    this.setSession(authResult);
    this.logSignIn();
  }

  setSession(authResult: Auth0DecodedHash): void {
    const { accessToken, expiresIn, idToken } = authResult;
    // Set the time that the access token will expire at
    let expiresAt = JSON.stringify(expiresIn! * 1000 + new Date().getTime());
    localStorage.setItem('subject', authResult.idTokenPayload.sub);
    localStorage.setItem('email', authResult.idTokenPayload.email);
    localStorage.setItem('access_token', accessToken!);
    localStorage.setItem('id_token', idToken!);
    localStorage.setItem('expires_at', expiresAt);
  }

  @boundMethod
  clearSession(): void {
    // Clear access token and ID token from local storage
    localStorage.removeItem('subject');
    localStorage.removeItem('email');
    localStorage.removeItem('last_email');
    localStorage.removeItem('access_token');
    localStorage.removeItem('id_token');
    localStorage.removeItem('last_id_token');
    localStorage.removeItem('last_subject');
    localStorage.removeItem('expires_at');
  }

  @boundMethod
  clearLastSession(): void {
    localStorage.removeItem('last_id_token');
    localStorage.removeItem('last_subject');
    localStorage.removeItem('last_email');
  }

  @boundMethod
  logout(): void {
    this.clearSession();
    this.auth0.logout({
      clientID: AUTH_CONFIG.clientId,
      returnTo: AUTH_CONFIG.loggedOutCallbackUrl,
    });
  }

  @boundMethod
  loginToAnotherAccount(): void {
    const lastToken = localStorage.getItem('id_token');
    const lastSubject = localStorage.getItem('subject');
    const lastEmail = localStorage.getItem('email');
    this.clearSession();
    localStorage.setItem('last_id_token', lastToken!);
    localStorage.setItem('last_subject', lastSubject!);
    localStorage.setItem('last_email', lastEmail!);
    this.auth0.logout({
      clientID: AUTH_CONFIG.clientId,
      returnTo: AUTH_CONFIG.loginAgainRedirect,
    });
  }
}
