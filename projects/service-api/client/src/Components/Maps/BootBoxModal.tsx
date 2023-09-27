import autobind from 'autobind-decorator';
import React, { Component } from 'react';
import { Button, ModalHeader, ModalBody, ModalFooter } from 'reactstrap';
import { RouteComponentProps } from 'react-router';
import { Modal } from 'reactstrap';

export interface BootBoxModalProps {
    message: string;
    header: string;
}

export default class BootBoxModal extends Component<BootBoxModalProps & RouteComponentProps, {}> {
  state = {
    message: '',
    header: this.props.header,
    modal: false,
    loading: true
  };

  componentDidMount() {
    // tslint:disable-next-line: no-any
    const { message } = this.props.match.params as any;
    this.setState({
        message: message,
        'loading': false
    });
    this.toggle();
  }

  @autobind
  toggle() {
    const newModalState = !this.state.modal;
    this.setState({
      modal: newModalState
    });
  }

  @autobind
  exit() {
    this.props.history.goBack();
  }

  render() {
    if (this.state.loading === true) {
      return (<></>);
    } else {
      return (
          <Modal 
            isOpen={this.state.modal}
            autoFocus={true}
            toggle={this.toggle}
            scrollable={false}
            centered={true}
            onClosed={() => this.exit()}
            role="dialog"
            backdrop="static"
            size="sm"
          >
            <ModalHeader toggle={this.toggle}>
                {this.state.header}
            </ModalHeader>
            <ModalBody role="document">
                <p>{this.state.message}</p>
            </ModalBody>
            <ModalFooter>
                <Button color="primary" onClick={this.toggle}>Ok</Button>
            </ModalFooter>
          </Modal>
      );
    }
  }
}