import autobind from 'autobind-decorator';
import React, { Component } from 'react';
import { RouteComponentProps } from 'react-router';
import { Modal, ModalHeader, ModalFooter, ModalBody, Button } from 'reactstrap';
import { MapID, NodeID, Post } from 'src/api/models';

export interface PostViewProps {
  mapID: MapID;
  nodeID: NodeID;
  acknowledgePost: Function;
}

interface PostViewState {
  html: string;
  modal: boolean;
  loading: boolean;
  error: boolean;
  data?: Post;
}

export default class PostView extends Component<
  PostViewProps & RouteComponentProps,
  {}
> {
  state: Readonly<PostViewState> = {
    html: '',
    modal: false,
    loading: true,
    error: false,
  };

  componentDidMount() {
    window.mpio.getBlogPostTest(
      this.props.mapID,
      this.props.nodeID,
      (json: Post) => {
        this.setState({ html: json.html, loading: false });
      },
    );
    this.toggle();
  }

  @autobind
  acknowledge() {
    window.mpio.markBlogPostRead(this.props.mapID, this.props.nodeID, () => {
      this.props.acknowledgePost();
    });
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
      return <></>;
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
          size="lg"
        >
          <ModalHeader toggle={this.toggle}>Instructional Content</ModalHeader>
          <ModalBody role="document">
            <div
              dangerouslySetInnerHTML={{
                __html: this.state.html,
              }}
            />
          </ModalBody>
          <ModalFooter>
            <Button onClick={() => this.acknowledge()} color="primary">
              Acknowledge
            </Button>
          </ModalFooter>
        </Modal>
      );
    }
  }
}
