import autobind from 'autobind-decorator';
import React, { Component } from 'react';
import { RouteComponentProps } from 'react-router';
import {
  Modal,
  ModalHeader,
  ModalBody,
  ModalFooter,
  Button,
  FormGroup,
  Input,
  Label,
  Form,
  Container,
  Col,
  FormText,
  Spinner,
} from 'reactstrap';
import { MapID, MindMap, MapMode } from 'src/api/models';
import { FaBrain } from 'react-icons/fa';

export interface Props {
  mapID: MapID;
}

interface State {
  mapID: MapID;
  settings: MindMap;
  modal: boolean;
  thumbnail?: string;
  image?: File;
  loading: boolean;
  saving: boolean;
  error: boolean;
}

export default class MapSettings extends Component<
  Props & RouteComponentProps,
  {}
> {
  state: Readonly<State> = {
    settings: {
      id: '',
      name: '',
      description: '',
      published: false,
      orgID: '',
      mode: 'MAP',
    },
    mapID: this.props.mapID,
    modal: false,
    loading: true,
    saving: false,
    error: false,
  };

  componentDidMount() {
    window.mpio.getMapSettings(
      this.props.mapID,
      (response) => {
        this.setState({
          settings: response,
          thumbnail: response.icon,
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
  patchMapSettings() {
    window.mpio.patchMapSettings(
      this.props.mapID,
      {
        name: this.state.settings.name,
        description: this.state.settings.description,
        mode: this.state.settings.mode,
      },
      () => {
        this.exit();
      },
      (error) => {
        this.setState({ saving: false });
        // TODO - handle error
      },
    );
  }

  @autobind
  saveChanges() {
    this.setState({ saving: true });
    if (this.state.image) {
      window.mpio.setMapIcon(
        this.props.mapID, this.state.image,
        () => {
          this.patchMapSettings();
        },
        (notifier, error) => {
          this.setState({ saving: false });
          notifier('message', 'Error Uploading Icon');
        });
    } else {
      this.patchMapSettings();
    }
  }

  @autobind
  handleFileSelection() {
    return (e: React.ChangeEvent<HTMLInputElement>) => {
      const file = e.target.files?.[0];
      if (file) {
        const previewURL = URL.createObjectURL(file);
        this.setState({ thumbnail: previewURL, image: file });
      } else {
        this.setState({
          thumbnail: undefined,
          image: undefined,
        });
      }
    };
  }

  @autobind
  isChecked(mode: MapMode) {
    return this.state.settings.mode === mode ? true : false;
  }

  @autobind
  updateSetting(field: string) {
    return (e: React.ChangeEvent<HTMLInputElement>) => {
      const newSettings = this.state.settings;
      newSettings[field] = e.currentTarget.value;
      this.setState({ settings: newSettings });
    };
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
          role="document"
          backdrop="static"
          size="lg"
        >
          <ModalHeader toggle={this.toggle}>Course Settings</ModalHeader>
          <ModalBody role="document" />
          <Container>
            <Form>
              <FormGroup row={true}>
                <Label className="formLabelBoldRight" sm={2} for="name">
                  Name
                </Label>
                <Col sm={10}>
                  <Input
                    type="text"
                    name="name"
                    value={this.state.settings.name}
                    onChange={this.updateSetting('name')}
                    id="name"
                    placeholder="Name"
                  />
                </Col>
              </FormGroup>
              <FormGroup row={true}>
                <Label className="formLabelBoldRight" sm={2} for="description">
                  Description
                </Label>
                <Col sm={10}>
                  <Input
                    type="textarea"
                    name="description"
                    value={this.state.settings.description || ''}
                    onChange={this.updateSetting('description')}
                    id="description"
                    placeholder="Description"
                  />
                </Col>
              </FormGroup>
              <FormGroup row={true}>
                <Label
                  onClick={(e) => e.preventDefault()}
                  className="formLabelBoldRight"
                  sm={2}
                  for="file"
                >
                  Thumbnail
                </Label>
                <Col sm={10}>
                  <Input
                    onChange={this.handleFileSelection()}
                    type="file"
                    name="file"
                    id="file"
                  />
                  {this.state.thumbnail && (
                    <img
                      className="p-3"
                      height="150px"
                      width="150px"
                      src={this.state.thumbnail}
                    />
                  )}
                  {!this.state.thumbnail && (
                    <FaBrain color="orange" size="150" />
                  )}
                  <FormText color="muted">
                    Thumbnail must be <b>150px x 150px</b> and will be shown as
                    the primary image in the directory (if published).{' '}
                  </FormText>
                </Col>
              </FormGroup>
              <FormGroup row={true}>
                <Label className="formLabelBoldRight" sm={2} for="description">
                  Format
                </Label>
                <Col sm={10}>
                  <FormGroup className="mt-2" check={true} inline={true}>
                    <Label check={true}>
                      <Input
                        type="radio"
                        name="mapMode"
                        value="MAP"
                        onChange={this.updateSetting('mode')}
                        checked={this.isChecked('MAP')}
                      />
                      Mindmap
                    </Label>
                  </FormGroup>
                  <FormGroup check={true} inline={true}>
                    <Label check={true}>
                      <Input
                        type="radio"
                        name="mapMode"
                        value="LIST"
                        onChange={this.updateSetting('mode')}
                        checked={this.isChecked('LIST')}
                      />
                      Flashcard List
                    </Label>
                  </FormGroup>
                  <FormGroup check={true} inline={true}>
                    <Label check={true}>
                      <Input
                        type="radio"
                        name="mapMode"
                        value="DOCUMENT"
                        onChange={this.updateSetting('mode')}
                        checked={this.isChecked('DOCUMENT')}
                      />
                      Document
                    </Label>
                  </FormGroup>
                </Col>
              </FormGroup>
            </Form>
          </Container>
          <ModalFooter>
            {this.state.saving && <Spinner color="primary" />}
            {!this.state.saving && (
              <Button color="primary" onClick={this.saveChanges}>
                Save Changes
              </Button>
            )}
            <Button color="danger" onClick={this.toggle}>
              Cancel
            </Button>
          </ModalFooter>
        </Modal>
      );
    }
  }
}
