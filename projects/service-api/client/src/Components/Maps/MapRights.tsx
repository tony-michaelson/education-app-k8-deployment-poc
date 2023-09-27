import React, { useState } from 'react';
import { connect } from 'react-redux';
import {
  Button,
  Card,
  CardBody,
  Collapse,
  Container,
  CustomInput,
  Input,
  InputGroup,
  InputGroupAddon,
  ListGroup,
  ListGroupItem,
  Modal,
  ModalBody,
  ModalFooter,
  ModalHeader,
  Spinner,
} from 'reactstrap';
import { setMapRights } from 'src/redux/modules/mindmap';
import {
  MapID,
  MapRightsMembers,
  MapRightsPatch,
  MapRightsPost,
} from 'src/api/models';
import { RootState } from 'src/redux';
import { LoadingSpinner } from '../Animations/LoadingSpinner';

const mapStateToProps = (state: RootState, ownProps: { mapID: MapID }) => ({
  mapID: ownProps.mapID,
  mapRights: state.mindmap.mapRights,
  permissions: state.mindmap.permissions,
});

const mapDispatchToProps = { setMapRights };

type Props = ReturnType<typeof mapStateToProps> & typeof mapDispatchToProps;

const UnconnectedMapRights: React.FunctionComponent<Props> = (props) => {
  const [currentMapRightsID, setActiveTab] = useState('');
  const [membersListOpen, setMembersListOpen] = useState('');
  const [isOpen, setOpen] = useState(true);
  const [newGroupName, setNewGroupName] = useState('');
  const [emailAddress, setEmailAddress] = useState('');
  const [loadingChange, setLoadingChange] = useState(false);
  const [sendingInvite, setSendingInvite] = useState(false);

  function loadMapRights() {
    setLoadingChange(true);
    window.mpio.getAllMapRights(props.mapID, (data: MapRightsMembers[]) => {
      const dataSorted = data.sort((a, b) => {
        if (a.rights.name === 'Default' || b.rights.name === 'Default') {
          return a.rights.name === 'Default' ? -1 : 1;
        } else {
          return a.rights.name > b.rights.name ? 1 : -1;
        }
      });
      if (!currentMapRightsID) {
        setActiveTab(dataSorted[0].rights.id);
      }
      props.setMapRights(dataSorted);
      setLoadingChange(false);
    });
  }

  function addNewGroup() {
    setLoadingChange(true);
    const data: MapRightsPost = {
      name: newGroupName,
      admin: false,
      feedback: false,
      mnemonics: false,
      modify: false,
      permissions: false,
      publish: false,
      share: false,
      stats: false,
      training: false,
      transfer: false,
      view: false,
    };
    window.mpio.createMapRights(data, props.mapID, () => {
      setNewGroupName('');
      loadMapRights();
    });
  }

  function sendInvite() {
    setSendingInvite(true);
    window.mpio.mapRightsInvite(
      { emailAddress: emailAddress },
      props.mapID,
      currentMapRightsID,
      (response) => {
        setEmailAddress('');
        setSendingInvite(false);
      },
      (notifier, error) => {
        notifier(
          'message',
          'Error sending invite, please verify email address is valid.',
        );
        setSendingInvite(false);
      },
    );
  }

  function deleteMapRights() {
    setLoadingChange(true);
    window.mpio.deleteMapRights(props.mapID, currentMapRightsID, () => {
      loadMapRights();
    });
  }

  function deleteMapRightsMember(memberID: string) {
    setLoadingChange(true);
    window.mpio.removeMapRightsMember(
      props.mapID,
      currentMapRightsID,
      memberID,
      () => {
        loadMapRights();
      },
    );
  }

  function toggleSwitch(data: MapRightsPatch) {
    setLoadingChange(true);
    window.mpio.patchMapRights(data, props.mapID, currentMapRightsID, () => {
      loadMapRights();
    });
  }

  function isActive(id: string) {
    return currentMapRightsID === id ? true : false;
  }

  function toggleMembersListOpen(id: string) {
    membersListOpen === id ? setMembersListOpen('') : setMembersListOpen(id);
  }

  function isMembersListOpen(id: string) {
    return membersListOpen === id ? true : false;
  }

  function exit() {
    setOpen(false);
    props.setMapRights(undefined);
    history.back();
  }

  if (!props.mapRights && isOpen && !loadingChange) {
    loadMapRights();
    return <LoadingSpinner />;
  } else {
    return (
      <>
        {loadingChange && <LoadingSpinner />}
        <Modal
          isOpen={true}
          autoFocus={true}
          toggle={exit}
          scrollable={false}
          centered={true}
          onClosed={() => exit()}
          role="document"
          backdrop="static"
          size="lg"
        >
          <ModalHeader toggle={exit}>Course Access</ModalHeader>
          <ModalBody role="document">
            <Container>
              <ListGroup>
                {props.mapRights?.map((mapRights, i) => {
                  return (
                    <div key={i} className="pb-2">
                      <ListGroupItem
                        tag="a"
                        className="pointer"
                        onClick={() => setActiveTab(mapRights.rights.id)}
                        active={isActive(mapRights.rights.id)}
                      >
                        {mapRights.rights.name}
                      </ListGroupItem>
                      <Collapse isOpen={isActive(mapRights.rights.id)}>
                        <Card>
                          <CardBody>
                            <CustomInput
                              type="switch"
                              id={'admin_' + mapRights.rights.id}
                              name="admin"
                              label="Admin"
                              onChange={(e) =>
                                toggleSwitch({
                                  admin: e.currentTarget.checked,
                                })
                              }
                              checked={mapRights.rights.admin}
                            />
                            <CustomInput
                              type="switch"
                              id={'permissions_' + mapRights.rights.id}
                              name="permissions"
                              label="Edit Permissions (access to this page)"
                              onChange={(e) =>
                                toggleSwitch({
                                  permissions: e.currentTarget.checked,
                                })
                              }
                              disabled={mapRights.rights.admin}
                              checked={mapRights.rights.permissions}
                            />
                            <CustomInput
                              type="switch"
                              id={'share_' + mapRights.rights.id}
                              name="share"
                              label="Add Members"
                              onChange={(e) =>
                                toggleSwitch({
                                  share: e.currentTarget.checked,
                                })
                              }
                              disabled={mapRights.rights.admin}
                              checked={mapRights.rights.share}
                            />
                            <CustomInput
                              type="switch"
                              id={'view_' + mapRights.rights.id}
                              name="view"
                              label="View Course"
                              onChange={(e) =>
                                toggleSwitch({
                                  view: e.currentTarget.checked,
                                })
                              }
                              disabled={mapRights.rights.admin}
                              checked={mapRights.rights.view}
                            />
                            <CustomInput
                              type="switch"
                              id={'modify_' + mapRights.rights.id}
                              name="modify"
                              label="Edit Course Content"
                              onChange={(e) =>
                                toggleSwitch({
                                  modify: e.currentTarget.checked,
                                })
                              }
                              disabled={mapRights.rights.admin}
                              checked={mapRights.rights.modify}
                            />
                            <CustomInput
                              type="switch"
                              id={'publish_' + mapRights.rights.id}
                              name="publish"
                              label="Publish Course"
                              onChange={(e) =>
                                toggleSwitch({
                                  publish: e.currentTarget.checked,
                                })
                              }
                              disabled={mapRights.rights.admin}
                              checked={mapRights.rights.publish}
                            />
                            <CustomInput
                              type="switch"
                              id={'training_' + mapRights.rights.id}
                              name="training"
                              label="Course Training"
                              onChange={(e) =>
                                toggleSwitch({
                                  training: e.currentTarget.checked,
                                })
                              }
                              disabled={mapRights.rights.admin}
                              checked={mapRights.rights.training}
                            />
                            <div className="mt-3">
                              {mapRights.rights.name !== 'Default' && (
                                <Button
                                  className="mx-2"
                                  color="danger"
                                  onClick={() => deleteMapRights()}
                                >
                                  Delete Group
                                </Button>
                              )}
                              <Button
                                color="info"
                                onClick={() =>
                                  toggleMembersListOpen(mapRights.rights.id)
                                }
                              >
                                Members
                              </Button>
                            </div>
                            <Collapse
                              isOpen={isMembersListOpen(mapRights.rights.id)}
                            >
                              <Card className="mt-3">
                                <CardBody>
                                  {mapRights.members.map((member, i2) => {
                                    return (
                                      <div key={i2}>
                                        <Button
                                          className="mr-2"
                                          size="sm"
                                          color="danger"
                                          onClick={() =>
                                            deleteMapRightsMember(member.id)
                                          }
                                        >
                                          X
                                        </Button>
                                        {member.firstName +
                                          ' ' +
                                          member.lastName}
                                      </div>
                                    );
                                  })}
                                  {mapRights.members.length === 0 && (
                                    <>No Members in This Group</>
                                  )}
                                  {props.permissions?.share && (
                                    <InputGroup className="mt-3">
                                      <Input
                                        onChange={(e) =>
                                          setEmailAddress(e.currentTarget.value)
                                        }
                                        id={
                                          'emailAddress_' + mapRights.rights.id
                                        }
                                        type="email"
                                        placeholder="Enter Email Address"
                                        value={emailAddress}
                                      />
                                      <InputGroupAddon addonType="append">
                                        {sendingInvite ? (
                                          <Spinner
                                            className="ml-2"
                                            color="primary"
                                          />
                                        ) : (
                                          <Button
                                            onClick={() =>
                                              emailAddress && sendInvite()
                                            }
                                            color="primary"
                                          >
                                            + Send Invite
                                          </Button>
                                        )}
                                      </InputGroupAddon>
                                    </InputGroup>
                                  )}
                                </CardBody>
                              </Card>
                            </Collapse>
                          </CardBody>
                        </Card>
                      </Collapse>
                    </div>
                  );
                })}
              </ListGroup>
              <br />
              <InputGroup>
                <Input
                  onChange={(e) => setNewGroupName(e.currentTarget.value)}
                  id="newGroupName"
                  placeholder="Enter New Group Name Here"
                  value={newGroupName}
                />
                <InputGroupAddon addonType="append">
                  <Button
                    onClick={() => newGroupName && addNewGroup()}
                    color="primary"
                  >
                    + Add New Group
                  </Button>
                </InputGroupAddon>
              </InputGroup>
            </Container>
          </ModalBody>
          <ModalFooter>
            <Button color="success" onClick={() => exit()}>
              Close
            </Button>
          </ModalFooter>
        </Modal>
      </>
    );
  }
};

export const MapRights = connect(
  mapStateToProps,
  mapDispatchToProps,
)(UnconnectedMapRights);
