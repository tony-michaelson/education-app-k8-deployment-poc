import autobind from 'autobind-decorator';
import React, { Component } from 'react';
import { Auth0Authentication } from '../../auth/Auth0Authentication';
import NewAccountLink from '../Members/NewAccountLink';

export interface HomeProps {
  auth: Auth0Authentication;
}
export default class Home extends Component<HomeProps, {}> {

  @autobind
  login() {
    this.props.auth.login();
  }

  @autobind
  loginToAnotherAccount() {
      this.props.auth.loginToAnotherAccount();
  }

  render() {
    const lastIdToken = localStorage.getItem('last_id_token');
    const lastSubject = localStorage.getItem('last_subject');
    const lastEmail = localStorage.getItem('last_email');
    const subject = localStorage.getItem('subject');
    const idToken = localStorage.getItem('id_token');
    const { authenticated } = this.props.auth;

    if (lastSubject && lastSubject !== subject) {
      return (
        <NewAccountLink
          auth={this.props.auth}
          lastIdToken={lastIdToken + ''}
          idToken={idToken + ''}
          lastEmail={lastEmail + ''}
        />
      );
    } else {
      return (
        <div>
          {authenticated && (
              <div>
                Auth Home
              </div>
          )}
          {!authenticated && (
            <div className="jumbotron">
              <div className="container">
                <h1 className="display-3">You are not logged in!</h1>
                <p>Please log in to continue.</p>
                <p>
                  <button className="btn btn-primary btn-lg" onClick={this.login}>
                    Log In
                  </button>
                </p>
              </div>
            </div>
          )}
        </div>
      );
    }
  }
}
