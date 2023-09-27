import React, { Component } from 'react';
import { RootState } from 'src/redux';
import { connect } from 'react-redux';
import { Col, Container, Nav, NavLink, Row } from 'reactstrap';
import { PrivateRoute } from 'src/utils/Routes';
import { HashRouter } from 'react-router-dom';
import Website from './Settings/Website';

const mapStateToProps = (state: RootState) => ({
  orgPermissions: state.member.permissions,
});

const mapDispatchToProps = {};

type Props = ReturnType<typeof mapStateToProps> & typeof mapDispatchToProps;

class UnconnectedOrgSettings extends Component<Props> {
  render() {
    return (
      <Container>
        <p>&nbsp;</p>
        <Row>
          <Col sm="4">
            <div>
              <p>Settings</p>
              <Nav vertical={true}>
                <NavLink href="#/general">General</NavLink>
                <NavLink href="#/config">Configuration</NavLink>
                <NavLink href="#/billing">Billing</NavLink>
                <NavLink href="#/website">Website</NavLink>
              </Nav>
              <hr />
              <p>Access</p>
              <Nav vertical={true}>
                <NavLink href="#/roles">Roles</NavLink>
                <NavLink href="#/members">Members</NavLink>
              </Nav>
            </div>
          </Col>
          <Col sm="8">
            <HashRouter>
              <PrivateRoute path="/website" component={Website} />
            </HashRouter>
          </Col>
        </Row>
      </Container>
    );
  }
}

const OrgSettings = connect(
  mapStateToProps,
  mapDispatchToProps,
)(UnconnectedOrgSettings);

export default OrgSettings;
