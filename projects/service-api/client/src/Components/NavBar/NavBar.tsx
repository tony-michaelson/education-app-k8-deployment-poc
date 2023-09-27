import React, { Component } from 'react';
import { Auth0Authentication } from '../../auth/Auth0Authentication';
import { NavLink } from 'react-router-dom';
import './NavBar.css';
import {
  toggleNotifier,
  setNotifierComponent,
  setNotifierCallback,
  setNotifierText,
} from 'src/redux/modules/notifier';
import { BackendCalls } from 'src/api/BackendCalls';
import { ProfileSettingsDrowdown } from '../Members/ProfileSettingsDropdown';
import { RootState } from 'src/redux';
import { setAccessInfo, setPermissions, setOrganization } from 'src/redux/modules/member';
import { connect } from 'react-redux';
import autobind from 'autobind-decorator';

declare global {
  interface Window {
    mpio: BackendCalls;
  }
}

const mapStateToProps = (
  state: RootState,
  ownProps: { auth: Auth0Authentication },
) => ({
  accessToken: state.member.accessToken,
  auth: ownProps.auth,
  orgPermissions: state.member.permissions,
  orgID: state.member.organization,
});

const mapDispatchToProps = {
  setAccessInfo,
  toggleNotifier,
  setNotifierComponent,
  setNotifierCallback,
  setNotifierText,
  setPermissions,
  setOrganization,
};

type Props = ReturnType<typeof mapStateToProps> & typeof mapDispatchToProps;

class UnconnectedNavBar extends Component<Props> {
  logoutWarningDelay = 60;

  componentDidMount() {
    const accessToken =
      this.props.accessToken || localStorage.getItem('access_token');
    if (accessToken && this.props.auth.authenticated) {
      this.props.setAccessInfo(accessToken, undefined, this.props.auth.logout);
    }
  }

  componentDidUpdate(prevProps: Props) {
    if (this.props.accessToken !== prevProps.accessToken) {
      if (this.props.accessToken && this.props.auth.authenticated) {
        this.setupLogoutTimer();
        this.setupMPIO(this.props.accessToken);
      }
    }
  }

  @autobind
  setupLogoutTimer() {
    const accessToken = localStorage.getItem('access_token');
    const expiresAt = localStorage.getItem('expires_at');
    if (accessToken && expiresAt && this.props.auth.authenticated) {
      const date = new Date().getTime();
      const expiresIn = (parseInt(expiresAt, 10) - date) / 1000;
      this.setLogoutWarningTimer(expiresIn, accessToken);
    }
  }

  @autobind
  refreshTokenCb() {
    this.props.auth.renewAuth((error, result) => {
      if (result) {
        this.props.auth.setSession(result);
        this.props.setAccessInfo(
          result.accessToken,
          undefined,
          this.props.auth.logout,
        );
      }
    });
  }

  @autobind
  showSessionExpirationWarning() {
    this.props.setNotifierComponent('tokenRefresh');
    this.props.setNotifierText('Your session is about to expire.');
    this.props.setNotifierCallback(this.refreshTokenCb);
    this.props.toggleNotifier(true);
    this.setLogoutTimer();
  }

  @autobind
  setLogoutTimer() {
    const timeout = setTimeout(
      this.props.auth.logout,
      this.logoutWarningDelay * 1000,
    );
    this.props.setAccessInfo(
      this.props.accessToken,
      timeout,
      this.props.auth.logout,
    );
  }

  @autobind
  setLogoutWarningTimer(expiresIn: number, accessToken: string) {
    // console.log(expiresIn);
    const secondsToTokenExpiration = expiresIn - this.logoutWarningDelay;
    const timeout = setTimeout(
      this.showSessionExpirationWarning,
      secondsToTokenExpiration * 1000,
    );
    this.props.setAccessInfo(accessToken, timeout, this.props.auth.logout);
  }

  @autobind
  setupMPIO(accessToken: string) {
    const notifierFunctions = {
      toggle: this.props.toggleNotifier,
      setComponent: this.props.setNotifierComponent,
      setText: this.props.setNotifierText,
    };
    const orgID = localStorage.getItem('orgID') || '';
    if (!this.props.orgID) {
      setOrganization(orgID);
    }
    window.mpio = new BackendCalls(
      accessToken,
      orgID,
      notifierFunctions,
    );
    // first call after mpio object is created
    if (!this.props.orgPermissions) {
          window.mpio.getMyPermissions((perms) => {
            this.props.setPermissions(perms);
      });
    }
  }

  render() {
    // used for page refreshes as lower level components rely on the mpio object
    const accessToken = localStorage.getItem('access_token');
    if (accessToken && this.props.auth.authenticated && !window.mpio) {
      this.setupMPIO(accessToken);
    }

    return (
      <nav className="navbar navbar-expand navbar-dark">
        <NavLink className="navbar-brand" to="/">
          <img className="logo" src="/assets/images/logo.svg"/>
        </NavLink>
        <ul className="navbar-nav mr-auto">
          {this.props.accessToken && (
            <>
              <li className="nav-item">
                <NavLink
                  className="nav-link"
                  to="/dashboard"
                  activeClassName="active"
                >
                  Dashboard
                </NavLink>
              </li>
              <li className="nav-item">
                <NavLink
                  className="nav-link"
                  to="/maps"
                  activeClassName="active"
                >
                  My Content
                </NavLink>
              </li>
              <li className="nav-item">
                <NavLink
                  className="nav-link"
                  to="/training"
                  activeClassName="active"
                >
                  Training
                </NavLink>
              </li>
              <li className="nav-item">
                <NavLink
                  className="nav-link"
                  to="/catalog"
                  activeClassName="active"
                >
                  Catalog
                </NavLink>
              </li>
            </>
          )}
        </ul>
        <ul className="navbar-nav ml-auto">
          <ProfileSettingsDrowdown auth={this.props.auth} />
        </ul>
      </nav>
    );
  }
}

const NavBar = connect(mapStateToProps, mapDispatchToProps)(UnconnectedNavBar);

export default NavBar;
