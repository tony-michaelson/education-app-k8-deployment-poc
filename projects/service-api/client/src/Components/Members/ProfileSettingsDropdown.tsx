import React, { useState } from 'react';
import { BsFillGearFill } from 'react-icons/bs';
import {
  DropdownToggle,
  DropdownMenu,
  DropdownItem,
  Dropdown,
} from 'reactstrap';
import { RootState } from 'src/redux';
import { Auth0Authentication } from 'src/auth/Auth0Authentication';
import { connect } from 'react-redux';

const mapStateToProps = (
  state: RootState,
  ownProps: { auth: Auth0Authentication },
) => ({
  accessToken: state.member.accessToken,
  mapID: state.mindmap.mapID,
  segmentID: state.mindmap.segmentID,
  mapPermissions: state.mindmap.permissions,
  orgPermissions: state.member.permissions,
  auth: ownProps.auth,
});

type Props = ReturnType<typeof mapStateToProps>;

const UnconnectedProfileSettingsDropdown: React.FC<Props> = ({
  accessToken,
  mapID,
  segmentID,
  mapPermissions,
  orgPermissions,
  auth,
}) => {
  const [isSettingsOpen, setSettingsOpen] = useState(false);
  const toggleSettings = () => setSettingsOpen((prevState) => !prevState);

  if (!accessToken) {
    return (
      <li className="nav-item">
        <button
          className="btn btn-outline-primary my-2 my-sm-0"
          type="submit"
          onClick={auth.login}
        >
          Log In
        </button>
      </li>
    );
  } else {
    return (
      <Dropdown
        direction="down"
        isOpen={isSettingsOpen}
        toggle={toggleSettings}
      >
        <DropdownToggle color="settings">
          <BsFillGearFill title="Settings" size="25" color="black" />
        </DropdownToggle>
        <DropdownMenu right={true}>
          {mapID && mapPermissions?.publish && (
            <DropdownItem href={'/maps/' + mapID + '/' + segmentID + '/#/preview'}>
              Publish Preview
            </DropdownItem>
          )}
          {mapID && mapPermissions?.modify && (
            <DropdownItem
              href={'/maps/' + mapID + '/' + segmentID + '/#/settings'}
            >
              Course Settings
            </DropdownItem>
          )}
          {mapID && mapPermissions?.permissions && (
            <DropdownItem
              href={'/maps/' + mapID + '/' + segmentID + '/#/rights'}
            >
              Course Access
            </DropdownItem>
          )}
          {orgPermissions?.manageOrganizationConfig && (
            <DropdownItem
              href={'/organization/settings/#/'}
            >
              Organization Settings
            </DropdownItem>
          )}
          <DropdownItem onClick={() => auth.logout()}>Logout</DropdownItem>
        </DropdownMenu>
      </Dropdown>
    );
  }
};

export const ProfileSettingsDrowdown = connect(mapStateToProps)(
  UnconnectedProfileSettingsDropdown,
);
