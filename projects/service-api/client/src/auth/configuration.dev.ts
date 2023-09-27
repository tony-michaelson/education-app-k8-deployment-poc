import { Auth0Config } from './Auth0Config';
export const AUTH_CONFIG: Auth0Config = {
  audience: 'http://localhost:9000',
  domain: 'masterypath.auth0.com',
  clientId: 'oUP4aphuLpw2ydBg8xYK5YH1P3tkuLjs',
  callbackUrl: 'http://localhost:3000/callback',
  loggedOutCallbackUrl: 'http://localhost:3000/',
  loginAgainRedirect: 'http://localhost:3000/loginAgain',
};
