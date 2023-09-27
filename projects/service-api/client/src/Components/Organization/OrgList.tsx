import React, { Component } from 'react';
import { RootState } from 'src/redux';
import { connect } from 'react-redux';
import { setOrganization } from 'src/redux/modules/member';
import { Modal, ModalHeader, ModalBody, ListGroupItem, ListGroup } from 'reactstrap';
import autobind from 'autobind-decorator';
import { OrgLink } from 'src/api/models';

const mapStateToProps = (state: RootState) => ({
  organization: state.member.organization,
});

export interface OrgListProps {
  organizations: OrgLink[];
  selectOrgFn: Function;
}

const mapDispatchToProps = { setOrganization };

type Props = ReturnType<typeof mapStateToProps> & typeof mapDispatchToProps;

interface OrgListState {
  modal: boolean;
}

class UnconnectedOrgList extends Component<Props & OrgListProps> {
  state: Readonly<OrgListState> = {
    modal: false,
  };

  componentDidMount() {
    this.toggle();
  }
  
  @autobind
  toggle() {
    const newModalState = !this.state.modal;
    this.setState({
      modal: newModalState,
    });
  }
  render() {
    return (
      <Modal
        isOpen={this.state.modal}
        autoFocus={true}
        toggle={this.toggle}
        scrollable={false}
        centered={true}
        role="document"
        backdrop="static"
        size="sm"
      >
        <ModalHeader toggle={this.toggle}>Select Organization</ModalHeader>
        <ModalBody role="document">
          <ListGroup>
          {
            this.props.organizations.map((v, i) => {
              return (
                <ListGroupItem
                  className="pointer"
                  key={i}
                  onClick={(e: React.MouseEvent) => { this.props.selectOrgFn(v.orgID); }}
                  action={true}
                >
                  {v.name}
                </ListGroupItem>
              );
            })
          }
          </ListGroup>
        </ModalBody>
      </Modal>
    );
  }
}

const OrgList = connect(
  mapStateToProps,
  mapDispatchToProps,
)(UnconnectedOrgList);

export default OrgList;
