import React, { Component } from 'react';
import { RootState } from 'src/redux';
import { Auth0Authentication } from 'src/auth/Auth0Authentication';
import { setAccessInfo, setOrganization } from 'src/redux/modules/member';
import { connect } from 'react-redux';
import { Auth0DecodedHash } from 'auth0-js';
import { LoadingSpinner } from '../Animations/LoadingSpinner';
import { API_CONFIG } from '../../api/ApiConfig';
import { OrgID, OrgLink } from 'src/api/models';
import OrgList from '../Organization/OrgList';
import autobind from 'autobind-decorator';

const mapStateToProps = (
  state: RootState,
  ownProps: { auth: Auth0Authentication },
) => ({
  accessToken: state.member.accessToken,
  auth: ownProps.auth,
});

const mapDispatchToProps = {
  setAccessInfo,
  setOrganization,
};

type Props = ReturnType<typeof mapStateToProps> & typeof mapDispatchToProps;

interface CallbackState {
  auth0Hash?: Auth0DecodedHash;
  organizations?: OrgLink[];
}

class UnconnectedCallback extends Component<Props> {
  state: Readonly<CallbackState> = {
    auth0Hash: undefined,
    organizations: undefined
  };

  componentDidMount() {
    if (/access_token|id_token|error/.test(location.hash)) {
      this.props.auth.parseHash((auth0Hash: Auth0DecodedHash) => {
        // first call after sign-in is to get all the member's organizations
        fetch(API_CONFIG.api_host + '/member/organizations', {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
            Authorization: 'Bearer ' + auth0Hash.accessToken,
          },
        })
          .then(response => response.json())
          .then((data: OrgLink[]) => {
            this.props.setAccessInfo(
              auth0Hash.accessToken,
              undefined,
              this.props.auth.logout,
            );
            this.setState({ auth0Hash: auth0Hash, organizations: data });
        });
      });
    }
  }

  redirect(auth0Hash: Auth0DecodedHash) {
    this.props.auth.redirect(auth0Hash);
  }

  @autobind
  selectOrganization(orgID: OrgID) {
    this.props.setOrganization(orgID);
    localStorage.setItem('orgID', orgID);
    if (this.state.auth0Hash) {
      this.redirect(this.state.auth0Hash);
    }
  }

  render() {
    if (this.state.organizations) {
      return <OrgList organizations={this.state.organizations} selectOrgFn={this.selectOrganization}/>;
    } else {
      return <LoadingSpinner />;
    }
  }
}

const Callback = connect(
  mapStateToProps,
  mapDispatchToProps,
)(UnconnectedCallback);

export default Callback;
