import React, { Component } from 'react';
import { RootState } from 'src/redux';
import {
  toggleNotifier,
  clearNotifierCallback,
} from 'src/redux/modules/notifier';
import { connect } from 'react-redux';
import { Modal, ModalHeader, ModalBody, ModalFooter, Button } from 'reactstrap';
import autobind from 'autobind-decorator';

const mapStateToProps = (state: RootState) => ({
  toggle: state.notifier.toggle,
  text: state.notifier.text,
  component: state.notifier.component,
  callback: state.notifier.callback,
  logout: state.member.logout,
});

const mapDispatchToProps = { toggleNotifier, clearNotifierCallback };

type Props = ReturnType<typeof mapStateToProps> & typeof mapDispatchToProps;

class UnconnectedNotifier extends Component<Props> {
  @autobind
  toggleFunction() {
    const toggle = !this.props.toggle;
    this.props.toggleNotifier(toggle);
  }

  @autobind
  refreshSession() {
    if (this.props.callback) {
      this.props.callback();
      this.props.clearNotifierCallback();
    }
    this.toggleFunction();
  }

  @autobind
  logout() {
    if (this.props.logout) {
      this.props.logout();
    } else {
      this.toggleFunction();
    }
  }

  @autobind
  buttons() {
    if (this.props.component === 'tokenRefresh') {
      return (
        <>
          <Button color="primary" onClick={this.refreshSession}>
            Stay Logged In
          </Button>
          <Button color="danger" onClick={this.logout}>
            Log Out
          </Button>
        </>
      );
    } else {
      return (
        <Button color="primary" onClick={this.toggleFunction}>
          Ok
        </Button>
      );
    }
  }

  render() {
    return (
      <Modal
        isOpen={this.props.toggle}
        autoFocus={true}
        toggle={this.toggleFunction}
        scrollable={false}
        centered={true}
        role="document"
        backdrop="static"
        size="lg"
        zIndex="9999999"
      >
        <ModalHeader toggle={this.toggleFunction}>Notification</ModalHeader>
        <ModalBody role="document">{this.props.text}</ModalBody>
        <ModalFooter>{this.buttons()}</ModalFooter>
      </Modal>
    );
  }
}

const Notifier = connect(
  mapStateToProps,
  mapDispatchToProps,
)(UnconnectedNotifier);

export default Notifier;
