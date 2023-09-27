import history from './history';
import React, { SFC } from 'react';
import { Callback, Home, NavBar, Notifier } from '../Components';
import { Route, RouteComponentProps } from 'react-router';
import { Router, Redirect } from 'react-router-dom';
import { WebAuthentication } from '../auth/WebAuthentication';
import MapList from '../Components/Maps/MapList';
import MapEditor from '../Components/Maps/MapEditor';
import Signup from '../Components/Members/Signup';
import MapCreate from 'src/Components/Maps/MapCreate';
import OrgJoin from 'src/Components/Organization/OrgJoin';
import { store } from 'src/index';
import { clearAccessInfo } from 'src/redux/modules/member';
import OrgSettings from 'src/Components/Organization/OrgSettings';

const auth = new WebAuthentication();

const handleAuthentication = (props: RouteComponentProps<{}>) => {
  return <Callback auth={auth} {...props} />;
};

const redirectToLogin = (props: RouteComponentProps<{}>) => {
  auth.login();
  return <></>;
};

const redirectToHomePage = () => {
  localStorage.setItem('redirectedFromUrl', location.pathname);
  store.dispatch(clearAccessInfo());
  return <Redirect to="/" />;
};

// tslint:disable-next-line: no-any
export const PrivateRoute = ({ component: Component, ...rest }: any) => (
  <Route
    {...rest}
    render={(props) =>
      auth.authenticated === true ? (
        <Component {...props} {...rest} />
      ) : (
        redirectToHomePage()
      )
    }
  />
);

const Routes: SFC<{}> = () => {
  return (
    <Router history={history}>
      <Route path="/" component={Notifier} />
      <Route path="/" render={(props) => <NavBar auth={auth} {...props} />} />
      <main role="main">
        <Route
          exact={true}
          path="/"
          render={(props) => <Home auth={auth} {...props} />}
        />
        <Route
          exact={true}
          path="/dashboard"
          render={(props) => <Home auth={auth} {...props} />}
        />
        <Route
          exact={true}
          path="/callback"
          render={(props) => handleAuthentication(props)}
        />
        <Route
          exact={true}
          path="/loginAgain"
          render={(props) => redirectToLogin(props)}
        />
        <PrivateRoute
          exact={true}
          path="/join/:inviteID/"
          component={OrgJoin}
        />
        <PrivateRoute
          exact={true}
          path="/signup"
          component={Signup}
          auth={auth}
        />
        <PrivateRoute
          path="/organization/settings/"
          component={OrgSettings}
          auth={auth}
        />
        <PrivateRoute exact={true} path="/maps" component={MapList} />
        <PrivateRoute exact={true} path="/maps/new" component={MapCreate} />
        <PrivateRoute path="/maps/:mapID/:segmentID/" component={MapEditor} />
      </main>
    </Router>
  );
};
export default Routes;
