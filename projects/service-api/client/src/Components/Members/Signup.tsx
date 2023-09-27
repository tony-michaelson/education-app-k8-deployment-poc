import autobind, { boundMethod } from 'autobind-decorator';
import React, { Component } from 'react';
import history from '../../utils/history';
import { Auth0Authentication } from '../../auth/Auth0Authentication';
import { MemberRegistration } from 'src/api/models';

export interface SignupProps {
  auth: Auth0Authentication;
}
interface SignupFormState {
  firstName: string;
  lastName: string;
}

export default class Home extends Component<SignupProps, {}> {
  state: Readonly<SignupFormState> = {
    firstName: '',
    lastName: '',
  };

  @autobind
  loginToAnotherAccount() {
    this.props.auth.loginToAnotherAccount();
  }

  @autobind
  completeRegistration() {
    const idToken = localStorage.getItem('id_token');
    if (idToken) {
      const memberRegistration: MemberRegistration = {
        firstName: this.state.firstName,
        lastName: this.state.lastName,
        idToken: idToken,
      };
      window.mpio.memberRegistration(memberRegistration, () => {
        this.logSignIn();
      });
    } else {
      // TODO - handle no idToken
    }
  }

  @boundMethod
  logSignIn(): void {
    window.mpio.logSignIn(
      () => {
        const redirectedFromUrl = localStorage.getItem('redirectedFromUrl');
        if (redirectedFromUrl) {
          localStorage.setItem('redirectedFromUrl', '');
          history.replace(redirectedFromUrl);
        } else {
          history.replace('/dashboard');
        }
      },
      (error) => history.replace('/signup'),
    );
  }

  render() {
    return (
      <div className="jumbotron">
        <div className="container">
          <div className="row justify-content-center d-flex">
            <div className="col-6">
              <div className="card card-body">
                <input
                  className="form-control"
                  type="text"
                  placeholder="First Name"
                  value={this.state.firstName}
                  onChange={(e) =>
                    this.setState({
                      firstName: e.target.value,
                    })
                  }
                />
                <p />
                <input
                  className="form-control"
                  type="text"
                  placeholder="Last Name"
                  value={this.state.lastName}
                  onChange={(e) =>
                    this.setState({
                      lastName: e.target.value,
                    })
                  }
                />
                <p />
                <button
                  onClick={() => this.completeRegistration()}
                  className="btn btn-primary align-self-end"
                >
                  Complete Registration
                </button>
              </div>
            </div>
            <div className="col-6">
              <div className="card card-body">
                <button
                  onClick={() => this.loginToAnotherAccount()}
                  className="btn btn-primary align-self-end"
                >
                  Link to My Other Account
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }
}
