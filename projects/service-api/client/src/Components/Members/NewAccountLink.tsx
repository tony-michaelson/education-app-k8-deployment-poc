import autobind from 'autobind-decorator';
import React, { Component } from 'react';
import history from '../../utils/history';
import { Badge } from 'reactstrap';
import { Auth0Authentication } from '../../auth/Auth0Authentication';
import { MemberRegistration } from 'src/api/models';

export interface NewAccountProps {
  auth: Auth0Authentication;
  lastIdToken: string;
  idToken: string;
  lastEmail: string;
}
interface NewAccountLinkState {
  lastIdToken: string;
  idToken: string;
  profile: {
    firstName: string;
    lastName: string;
  };
  email: string;
}

export default class NewAccountLink extends Component<NewAccountProps, {}> {
  state: Readonly<NewAccountLinkState> = {
    lastIdToken: this.props.lastIdToken,
    idToken: this.props.idToken,
    profile: {
      firstName: '',
      lastName: '',
    },
    email: '',
  };

  @autobind
  loginToAnotherAccount() {
    this.props.auth.loginToAnotherAccount();
  }

  @autobind
  linkAccount() {
    const memberRegistration: MemberRegistration = {
      firstName: this.state.profile.firstName,
      lastName: this.state.profile.lastName,
      idToken: this.props.lastIdToken,
    };
    window.mpio.linkAccount(memberRegistration, () => {
      this.props.auth.clearLastSession();
      history.replace('/dashboard');
    });
  }

  componentDidMount() {
    window.mpio.getMemberProfile((member) => {
      this.setState({
        profile: {
          firstName: member.profile.firstName,
          lastName: member.profile.lastName,
        },
        email: member.email,
      });
    });
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
                  disabled={true}
                  value={this.state.profile.firstName}
                />
                <p />
                <input
                  className="form-control"
                  type="text"
                  disabled={true}
                  value={this.state.profile.lastName}
                />
                <p />
                <input
                  className="form-control"
                  type="text"
                  disabled={true}
                  value={this.state.email}
                />
                <p />
                <p>
                  Link <Badge variant="primary">{this.props.lastEmail}</Badge>{' '}
                  to this profile?
                </p>
                <button
                  onClick={() => this.linkAccount()}
                  className="btn btn-primary align-self-end"
                >
                  Join Accounts
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }
}
