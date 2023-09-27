import autobind from 'autobind-decorator';
import React, { Component } from 'react';
import { RouteComponentProps } from 'react-router';
import {
  Modal,
  ModalHeader,
  ModalBody,
  ModalFooter,
  Button,
} from 'reactstrap';
import { MapID } from 'src/api/models';
import { LoadingSpinner } from '../Animations/LoadingSpinner';

export interface Props {
  mapID: MapID;
}

interface State {
  mapID: MapID;
  blogPage: string;
  modal: boolean;
  loading: boolean;
  saving: boolean;
  error: boolean;
}

export default class MapPreview extends Component<
  Props & RouteComponentProps,
  {}
> {
  state: Readonly<State> = {
    mapID: this.props.mapID,
    blogPage: '',
    modal: false,
    loading: true,
    saving: false,
    error: false,
  };

  componentDidMount() {
    window.mpio.getBlogPostPreview(
      this.props.mapID,
      (response) => {
        this.setState({
          blogPage: response.html,
          loading: false,
        });
      },
      (notifier, error) => {
        if (error?.status === 404) {
          this.setState({
            loading: false,
          });
        } else {
          // TODO - handle errors
          this.setState({
            loading: false,
            error: true,
          });
        }
      },
    );
    this.toggle();
  }

  @autobind
  toggle() {
    const newModalState = !this.state.modal;
    this.setState({
      modal: newModalState,
    });
  }

  @autobind
  exit() {
    this.props.history.goBack();
  }

  render() {
    if (this.state.loading === true) {
      return <LoadingSpinner/>;
    } else {
      return (
        <Modal
          isOpen={this.state.modal}
          autoFocus={true}
          toggle={this.toggle}
          scrollable={false}
          centered={true}
          onClosed={() => this.exit()}
          role="document"
          backdrop="static"
          size="xl"
        >
          <ModalHeader toggle={this.toggle}>Publish Preview</ModalHeader>
          <ModalBody role="document" dangerouslySetInnerHTML={{__html: this.state.blogPage}}/>
          <ModalFooter>
            <Button color="danger" onClick={this.toggle}>
              Close
            </Button>
          </ModalFooter>
        </Modal>
      );
    }
  }
}
