import autobind from 'autobind-decorator';
import React, { Component } from 'react';
import { RouteComponentProps } from 'react-router';
import { Modal, ModalHeader, ModalFooter, ModalBody, Button } from 'reactstrap';
import SimpleMDE from 'react-simplemde-editor';
import 'easymde/dist/easymde.min.css';
import { MapID, SegmentID, NodeID, Post, PostMarkdown } from 'src/api/models';
import { StoreImageFileResponse } from 'src/types';

export interface ContentModalProps {
  mapID: MapID;
  segmentID: SegmentID;
  nodeID: NodeID;
  // tslint:disable-next-line: no-any
  toggleFunction: () => void;
}

interface ContentModalState {
  mapID: MapID;
  segmentID: SegmentID;
  nodeID: NodeID;
  markdown: string;
  modal: boolean;
  loading: boolean;
  error: boolean;
  data?: Post;
}

export default class ContentModal extends Component<
  ContentModalProps & RouteComponentProps,
  {}
> {
  state: Readonly<ContentModalState> = {
    mapID: this.props.mapID,
    segmentID: this.props.segmentID,
    nodeID: '',
    markdown: '',
    modal: false,
    loading: true,
    error: false,
  };

  componentDidMount() {
    // tslint:disable-next-line: no-any
    const { nodeID } = this.props.match.params as any;

    window.mpio.getBlogPost(
      this.state.mapID,
      this.state.segmentID,
      nodeID,
      (response) => {
        this.setState({
          nodeID: nodeID,
          data: response,
          markdown: response.markdown,
          loading: false,
        });
      },
      (notifier, error) => {
        this.setState({
          nodeID: nodeID,
          loading: false,
          error: true,
        });
      },
    );
    this.toggle();
  }

  savePost() {
    const data: PostMarkdown = {
      markdown: this.state.markdown,
    };
    if (this.state.data) {
      // save
      window.mpio.updateBlogPost(
        data,
        this.state.mapID,
        this.state.segmentID,
        this.state.nodeID,
        () => {
          this.exit();
        },
      );
    } else {
      // create
      window.mpio.createBlogPost(
        data,
        this.state.mapID,
        this.state.segmentID,
        this.state.nodeID,
        () => {
          this.exit();
        },
      );
    }
  }

  @autobind
  imageUploadFunction(
    file: File,
    onSuccess: (url: string) => void,
    onError: (error: string) => void,
  ) {
    window.mpio.storeImageFile(
      file,
      this.state.mapID,
      (response: StoreImageFileResponse) => {
        if (response.url) {
          onSuccess(response.url);
        } else if (response.message) {
          onError(response.message);
        } else {
          onError('Unknown Error Uploading to Server');
        }
      },
    );
  }

  @autobind
  saveMarkdown(value: string) {
    this.setState({ markdown: value });
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
          size="xl"
        >
          <ModalHeader toggle={this.toggle}>Instructional Content</ModalHeader>
          <ModalBody role="document">
            <SimpleMDE
              value={this.state.markdown}
              onChange={(value) => this.saveMarkdown(value)}
              options={{
                uploadImage: true,
                imageUploadFunction: this.imageUploadFunction,
              }}
            />
          </ModalBody>
          <ModalFooter>
            <Button onClick={() => this.savePost()} color="primary">
              Save
            </Button>
            <Button onClick={() => this.exit()} color="danger">
              Cancel
            </Button>
          </ModalFooter>
        </Modal>
      );
    }
  }
}
