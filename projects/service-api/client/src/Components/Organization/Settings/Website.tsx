import React, { Component } from 'react';
import {
  Alert,
  Button,
  Col,
  Form,
  FormGroup,
  FormText,
  Input,
  InputGroup,
  InputGroupAddon,
  InputGroupText,
  Label,
  Spinner,
} from 'reactstrap';

interface WebsiteState {
  domain: string;
  theme: string;
  name: string;
  title: string;
  description: string;
  siteLogo?: string;
  image?: File;
  ssl: boolean;
  exists: boolean;
  loading: boolean;
  loadingCreateBlog: boolean;
  loadingBuildBlog: boolean;
  loadingPublishBlog: boolean;
  loadingDeleteBlog: boolean;
  loadingUpdateBlog: boolean;
  deleteSiteName: string;
}

class Website extends Component {
  state: Readonly<WebsiteState> = {
    domain: '',
    theme: '',
    name: '',
    title: '',
    description: '',
    ssl: false,
    exists: false,
    loading: true,
    loadingCreateBlog: false,
    loadingBuildBlog: false,
    loadingPublishBlog: false,
    loadingDeleteBlog: false,
    loadingUpdateBlog: false,
    deleteSiteName: '',
  };

  getBlogSiteDetails() {
    window.mpio.getBlogSettings(
      (response) => {
        this.setState({
          deleteSiteName: '',
          name: response.name,
          title: response.title,
          description: response.description,
          siteLogo: response.logo,
          domain: response.domain,
          theme: response.theme,
          ssl: response.ssl,
          exists: true,
          loading: false,
          loadingCreateBlog: false,
        });
      },
      (notifier, error) => {
        if (error?.status === 404) {
          this.setState({ exists: false, loading: false });
        } else {
          // TODO - handle error
        }
      },
    );
  }

  handleFileSelection() {
    return (e: React.ChangeEvent<HTMLInputElement>) => {
      const file = e.target.files?.[0];
      if (file) {
        const previewURL = URL.createObjectURL(file);
        this.setState({ siteLogo: previewURL, image: file });
      } else {
        this.setState({
          siteLogo: undefined,
          image: undefined,
        });
      }
    };
  }

  componentDidMount() {
    this.getBlogSiteDetails();
  }

  updateDomainName(name: string) {
    this.setState({ domain: name });
  }

  isAnythingElseLoading() {
    return (
      this.state.loadingBuildBlog ||
      this.state.loadingCreateBlog ||
      this.state.loadingDeleteBlog ||
      this.state.loadingPublishBlog
    );
  }

  isTheme(name: string) {
    return name === this.state.theme;
  }

  createBlogSite() {
    this.toggleLoadingCreateBlog(true);
    window.mpio.createBlog({ subDomain: this.state.domain }, () => {
      this.toggleLoadingCreateBlog(false);
      this.getBlogSiteDetails();
    });
  }

  saveBlog() {
    this.toggleLoadingUpdateBlog(true);
    if (this.state.image) {
      window.mpio.setSiteLogo(
        this.state.image,
        () => {
          this.patchBlogSettings();
        },
        (notifier, error) => {
          this.setState({ saving: false });
          notifier('message', 'Error Uploading Icon');
        },
      );
    } else {
      this.patchBlogSettings();
    }
  }

  patchBlogSettings() {
    window.mpio.patchBlogSettings(
      {
        theme: this.state.theme,
        name: this.state.name,
        description: this.state.description,
      },
      () => {
        this.toggleLoadingUpdateBlog(false);
      },
    );
  }

  deleteBlogSite() {
    if (this.state.deleteSiteName === this.state.domain) {
      this.toggleLoadingDeleteBlog(true);
      window.mpio.deleteBlog(() => {
        this.toggleLoadingDeleteBlog(false);
        this.getBlogSiteDetails();
      });
    } else {
      alert('Site name does not match, please correct and re-submit.');
    }
  }

  buildBlog() {
    this.toggleLoadingBuildBlog(true);
    window.mpio.buildBlog(
      () => {
        this.toggleLoadingBuildBlog(false);
      },
      (notifier, error) => {
        this.toggleLoadingBuildBlog(false);
        notifier('message', 'Error occurred while trying to build site.');
      },
    );
  }

  publishBlog() {
    this.toggleLoadingPublishBlog(true);
    window.mpio.publishBlog(
      () => {
        this.toggleLoadingPublishBlog(false);
      },
      (notifier, error) => {
        this.toggleLoadingPublishBlog(false);
        notifier('message', 'Error occurred while trying to publish site.');
      },
    );
  }

  toggleLoadingCreateBlog(loading: boolean) {
    this.setState({ loadingCreateBlog: loading });
  }

  toggleLoadingBuildBlog(loading: boolean) {
    this.setState({ loadingBuildBlog: loading });
  }

  toggleLoadingPublishBlog(loading: boolean) {
    this.setState({ loadingPublishBlog: loading });
  }

  toggleLoadingDeleteBlog(loading: boolean) {
    this.setState({ loadingDeleteBlog: loading });
  }

  toggleLoadingUpdateBlog(loading: boolean) {
    this.setState({ loadingUpdateBlog: loading });
  }

  render() {
    if (this.state.loading) {
      return <></>;
    } else {
      if (this.state.exists) {
        const siteAddress =
          (this.state.ssl ? 'https' : 'http') + '://' + this.state.domain;
        const siteStagingAddress =
          (this.state.ssl ? 'https' : 'http') +
          '://' +
          'staging.' +
          this.state.domain;
        return (
          <>
            <Alert color="secondary">
              <Form>
                <FormGroup row={true}>
                  <Label for="name" sm={2}>
                    Site Name
                  </Label>
                  <Col sm={10}>
                    <Input
                      onChange={(e) => {
                        this.setState({ name: e.currentTarget.value });
                      }}
                      type="text"
                      value={this.state.name}
                    />
                  </Col>
                </FormGroup>
                <FormGroup row={true}>
                  <Label for="name" sm={2}>
                    Description
                  </Label>
                  <Col sm={10}>
                    <Input
                      onChange={(e) => {
                        this.setState({ description: e.currentTarget.value });
                      }}
                      type="text"
                      value={this.state.description}
                    />
                  </Col>
                </FormGroup>
                <FormGroup row={true}>
                  <Label for="theme" sm={2}>
                    Theme
                  </Label>
                  <Col sm={10}>
                    <Input
                      onChange={(e) => {
                        this.setState({ theme: e.currentTarget.value });
                      }}
                      type="select"
                    >
                      <option
                        selected={this.isTheme('memoirs')}
                        value="memoirs"
                      >
                        Gallery & Sidebar Menu
                      </option>
                    </Input>
                  </Col>
                </FormGroup>
                <FormGroup row={true}>
                  <Label onClick={(e) => e.preventDefault()} sm={2} for="file">
                    Site Logo
                  </Label>
                  <Col sm={10}>
                    <Input
                      onChange={this.handleFileSelection()}
                      type="file"
                      name="file"
                      id="file"
                    />
                    {this.state.siteLogo && (
                      <img className="p-3" src={this.state.siteLogo} />
                    )}
                    <FormText color="muted">
                      Logo will not be resized. Please upload the exact size you
                      want to appear on your home page.
                    </FormText>
                  </Col>
                </FormGroup>
              </Form>
              {this.state.loadingUpdateBlog ? (
                <Spinner color="primary" />
              ) : (
                <Button
                  disabled={this.isAnythingElseLoading()}
                  onClick={() => this.saveBlog()}
                  color="primary"
                >
                  Save Site Settings
                </Button>
              )}
            </Alert>
            <Alert color="warning">
              Your staging site address is:{' '}
              <a href={siteStagingAddress} target="_blank">
                {siteStagingAddress}
              </a>
              <br />
              <br />
              Anytime you build your site, the changes are available in your
              staging site before they are publically visible.
              <br />
              <br />
              {this.state.loadingBuildBlog ? (
                <Spinner color="primary" />
              ) : (
                <Button
                  disabled={this.isAnythingElseLoading()}
                  onClick={() => this.buildBlog()}
                  color="primary"
                >
                  Build Site
                </Button>
              )}
            </Alert>
            <Alert color="success">
              Your public site address is:{' '}
              <a href={siteAddress} target="_blank">
                {siteAddress}
              </a>
              <br />
              <br />
              You must publish your staging site for it to become publically
              visible.
              <br />
              <br />
              {this.state.loadingPublishBlog ? (
                <Spinner color="primary" />
              ) : (
                <Button
                  disabled={this.isAnythingElseLoading()}
                  onClick={() => this.publishBlog()}
                  color="primary"
                >
                  Publish Site
                </Button>
              )}
            </Alert>
            <Alert color="danger">
              To delete your site, type "{this.state.domain}" in the field below
              and click "Delete Site"
              <br />
              <br />
              <FormGroup>
                <Label for="examplePassword">Domain:</Label>
                <Input
                  onChange={(e) => {
                    this.setState({ deleteSiteName: e.currentTarget.value });
                  }}
                />
              </FormGroup>
              {this.state.loadingDeleteBlog ? (
                <Spinner color="danger" />
              ) : (
                <Button
                  disabled={this.isAnythingElseLoading()}
                  onClick={() => this.deleteBlogSite()}
                  color="danger"
                >
                  Delete Site
                </Button>
              )}
            </Alert>
          </>
        );
      } else {
        return (
          <Alert color="info">
            Your site has not been created yet.
            <br />
            <br />
            <p>Start by Choosing a MasteryPath Address:</p>
            <InputGroup>
              <Input
                onChange={(e) => {
                  this.updateDomainName(e.currentTarget.value);
                }}
              />
              <InputGroupAddon addonType="append">
                <InputGroupText>.masterypath.net</InputGroupText>
              </InputGroupAddon>
            </InputGroup>
            <br />
            {this.state.loadingCreateBlog ? (
              <Spinner color="primary" />
            ) : (
              <Button onClick={() => this.createBlogSite()} color="primary">
                Create Site
              </Button>
            )}
          </Alert>
        );
      }
    }
  }
}

export default Website;
