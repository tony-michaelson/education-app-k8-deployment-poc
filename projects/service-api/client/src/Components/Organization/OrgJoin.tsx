import React, { useState } from 'react';
import { RouteComponentProps } from 'react-router';
import { LoadingSpinner } from '../Animations/LoadingSpinner';

export interface OrgJoinProps {
  inviteID: string;
}

const OrgJoin: React.FunctionComponent<
  OrgJoinProps & RouteComponentProps<{ inviteID: string }>
> = (props) => {
  const inviteID = props.match.params.inviteID;
  const [inviteResult, setInviteResult] = useState<
    'loading' | 'success' | 'error'
  >('loading');

  if (inviteResult === 'loading') {
    window.mpio.joinWithInviteID(
      inviteID,
      () => {
        setInviteResult('success');
      },
      (notifier, response) => {
        setInviteResult('error');
      },
    );
  }

  switch (inviteResult) {
    case 'loading': {
      return <LoadingSpinner />;
      break;
    }
    case 'success': {
      return (
        <div className="jumbotron">
          <div className="container">
            <h1 className="display-3">Thank you for joining!</h1>
            <p>Please go to "My Maps" to see what you have access to.</p>
          </div>
        </div>
      );
      break;
    }
    case 'error': {
      return (
        <div className="jumbotron">
          <div className="container">
            <h1 className="display-3">Uh oh,</h1>
            <p>The link is either expired or invalid.</p>
            <p>
              Please check with the persion who provided the link and try again.
            </p>
          </div>
        </div>
      );
    }
    default: {
      return <LoadingSpinner />;
      break;
    }
  }
};

export default OrgJoin;
