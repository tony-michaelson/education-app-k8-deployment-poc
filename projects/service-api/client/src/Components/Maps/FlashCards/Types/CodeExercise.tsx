import autobind from 'autobind-decorator';
import React, { Component } from 'react';
import {
  Button,
  Media,
  ModalHeader,
  ModalBody,
  ModalFooter,
  Nav,
  NavItem,
  NavLink,
  Row,
  Col,
  TabContent,
  TabPane,
  Spinner,
} from 'reactstrap';
import ReactMde from 'react-mde';
import * as Showdown from 'showdown';
import 'react-mde/lib/styles/css/react-mde-all.css';
import classnames from 'classnames';
import { FlashCardProps } from '../FlashCard';
// **** ACE ****
import AceEditor from 'react-ace';
import { config as aceConfig } from 'ace-builds';
aceConfig.set(
  'basePath',
  'https://cdn.jsdelivr.net/npm/ace-builds@1.4.12/src-noconflict/'
);
aceConfig.setModuleUrl(
   'ace/mode/javascript_worker',
   'https://cdn.jsdelivr.net/npm/ace-builds@1.4.12/src-noconflict/worker-javascript.js'
);
import 'ace-builds/src-noconflict/mode-scala';
import 'ace-builds/src-noconflict/mode-javascript';
import 'ace-builds/src-noconflict/theme-monokai';
import 'ace-builds/src-noconflict/theme-terminal';
// **** END ****
import {
  MapID,
  SegmentID,
  NodeID,
  CodeExerciseBriefEdit,
  Exercise,
} from '../../../../api/models';
import { getFlashCardIconByCardTypeName } from '../FlashCardTypeSelector';

const cardProperties = {
  code_exercise_scala: { lang: 'scala', name: 'Scala' },
  code_exercise_nodejs: { lang: 'javascript', name: 'Javascript' },
};

const getCardTypeLogo = (name: string) => {
  return '/assets/images/icons/' + getFlashCardIconByCardTypeName[name];
};

const converter = new Showdown.Converter({
  tables: true,
  simplifiedAutoLink: true,
  strikethrough: true,
  tasklists: true,
});

interface EasyMDEProps {
  value: string;
  height: number;
  liftMdeValue: (value: string) => void;
}

const EasyMDE = ({ value, height, liftMdeValue }: EasyMDEProps) => {
  const [selectedTab, setSelectedTab] = React.useState<'write' | 'preview'>(
    'write',
  );
  return (
    <div className="container">
      <ReactMde
        initialEditorHeight={height}
        value={value}
        onChange={liftMdeValue}
        selectedTab={selectedTab}
        onTabChange={setSelectedTab}
        generateMarkdownPreview={(markdown) =>
          Promise.resolve(converter.makeHtml(markdown))
        }
      />
    </div>
  );
};

interface CodeExerciseState {
  title: string;
  mapID: MapID;
  segmentID: SegmentID;
  nodeID: NodeID;
  toggleFunction: () => void;
  data: CodeExerciseBriefEdit | null;
  loading: boolean;
  activeTab: string;
  mdeValue: string;
  codeTest: string;
  codeSolution: string;
  codeTemplate: string;
  cliOutput: string;
  valid: boolean;
  validationRunning: boolean;
  saveBtnText: string;
}

export class CodeExercise extends Component<FlashCardProps, {}> {
  state: Readonly<CodeExerciseState> = {
    title: '',
    mapID: this.props.mapID,
    segmentID: this.props.segmentID,
    nodeID: this.props.nodeID,
    toggleFunction: this.props.toggleFunction,
    data: null,
    loading: true,
    activeTab: '1',
    mdeValue: '',
    codeTest: '',
    codeSolution: '',
    codeTemplate: '',
    cliOutput: '',
    valid: false,
    validationRunning: false,
    saveBtnText: 'Save Flashcard',
  };

  componentDidMount() {
    window.mpio.getFlashcard<CodeExerciseBriefEdit>(
      this.props.mapID,
      this.props.segmentID,
      this.props.nodeID,
      (response) => {
        const title = this.props.cardType.commonName;
        this.setState({
          data: response,
          title: title,
          mdeValue: response.explanation,
          codeSolution: response.solution,
          codeTest: response.test,
          codeTemplate: response.template,
          valid: true,
          loading: false,
        });
      },
      (notifier, error) => {
        if (error?.status === 404) {
          const title = this.props.cardType.commonName;
          this.setState({ title: title, loading: false });
        } else {
          // TODO - handle error
        }
      },
    );
  }

  @autobind
  toggle(tab: string) {
    if (this.state.activeTab !== tab) {
      this.setState({ activeTab: tab });
    }
  }

  @autobind
  toggleFunction() {
    this.props.toggleFunction();
  }

  @autobind
  validateSolution() {
    this.setState({ validationRunning: true });
    const data: Exercise = this.createExerciseDTO();
    window.mpio.validateFlashcard(
      data,
      this.props.mapID,
      this.props.cardType.cardType,
      (response) => {
        this.setState({
          valid: true,
          cliOutput: response.output,
          activeTab: '3',
          saveBtnText: 'Test Passed - Save Flashcard',
          validationRunning: false,
        });
      },
      (notifier, error) => {
        this.toggle('3');
        this.setState({
          valid: false,
          cliOutput: error?.data.output,
          activeTab: '3',
          validationRunning: false,
        });
      },
    );
  }

  @autobind
  createFlashcard() {
    const data: Exercise = this.createExerciseDTO();
    window.mpio.createFlashcard(
      data,
      this.props.mapID,
      this.props.segmentID,
      this.props.nodeID,
      this.props.cardType.cardType,
      () => {
        this.toggleFunction();
      },
      (notifier, error) => {
        alert(error?.data); // TODO - handle error
      },
    );
  }

  @autobind
  saveFlashcard() {
    const data: Exercise = this.createExerciseDTO();
    window.mpio.updateFlashcard(
      data,
      this.props.mapID,
      this.props.segmentID,
      this.props.nodeID,
      this.props.cardType.cardType,
      () => {
        this.toggleFunction();
      },
      (notifier, error) => {
        alert(error?.data); // TODO - handle error
      },
    );
  }

  @autobind
  updateMdeValue(value: string) {
    this.setState({ mdeValue: value });
  }

  @autobind
  updateCodeTest(value: string) {
    this.setState({ codeTest: value, valid: false });
  }

  @autobind
  updateCodeSolution(value: string) {
    this.setState({ codeSolution: value, valid: false });
  }

  @autobind
  updateCodeTemplate(value: string) {
    this.setState({ codeTemplate: value });
  }

  render() {
    if (this.state.loading === true) {
      return <></>;
    } else {
      const editorHeight = window.innerHeight - 275;
      const editorHeightPx = editorHeight + 'px';
      return (
        <>
          <ModalHeader
            className="modalHeaderHeight"
            toggle={this.toggleFunction}
          >
            <Media as="li">
              <img
                width={48}
                height={48}
                className="mr-3"
                src={getCardTypeLogo(this.props.cardType.name)}
              />
              <Media body={true} className="mt-2">
                <h5>{this.state.title}</h5>
              </Media>
            </Media>
          </ModalHeader>
          <ModalBody role="document">
            <Nav tabs={true}>
              <NavItem>
                <NavLink
                  className={classnames({
                    active: this.state.activeTab === '1',
                    'nav-tab': true,
                  })}
                  onClick={() => {
                    this.toggle('1');
                  }}
                >
                  Solution
                </NavLink>
              </NavItem>
              <NavItem>
                <NavLink
                  className={classnames({
                    active: this.state.activeTab === '2',
                    'nav-tab': true,
                  })}
                  onClick={() => {
                    this.toggle('2');
                  }}
                >
                  Exercise
                </NavLink>
              </NavItem>
              <NavItem>
                <NavLink
                  className={classnames({
                    active: this.state.activeTab === '3',
                    'nav-tab': true,
                  })}
                  onClick={() => {
                    this.toggle('3');
                  }}
                >
                  CLI Output
                </NavLink>
              </NavItem>
            </Nav>

            <TabContent activeTab={this.state.activeTab}>
              <TabPane tabId="1">
                <Row>
                  <Col xs="6">
                    <AceEditor
                      placeholder="Test Code"
                      mode={cardProperties[this.props.cardType.name].lang}
                      theme="monokai"
                      name="test"
                      fontSize={14}
                      width="100%"
                      height={editorHeightPx}
                      showPrintMargin={true}
                      showGutter={true}
                      highlightActiveLine={true}
                      onChange={this.updateCodeTest}
                      value={this.state.codeTest}
                      setOptions={{
                        showLineNumbers: true,
                        tabSize: 2,
                      }}
                    />
                  </Col>
                  <Col xs="6">
                    <AceEditor
                      placeholder="Test Solution"
                      mode={cardProperties[this.props.cardType.name].lang}
                      theme="monokai"
                      name="solution"
                      fontSize={14}
                      width="100%"
                      height={editorHeightPx}
                      showPrintMargin={true}
                      showGutter={true}
                      highlightActiveLine={true}
                      onChange={this.updateCodeSolution}
                      value={this.state.codeSolution}
                      setOptions={{
                        showLineNumbers: true,
                        tabSize: 2,
                      }}
                    />
                  </Col>
                </Row>
              </TabPane>
              <TabPane tabId="2">
                <Row>
                  <Col xs="6">
                    <EasyMDE
                      height={editorHeight - 115}
                      value={this.state.mdeValue}
                      liftMdeValue={this.updateMdeValue}
                    />
                  </Col>
                  <Col xs="6">
                    <AceEditor
                      placeholder="Exercise Code Template"
                      mode={cardProperties[this.props.cardType.name].lang}
                      theme="monokai"
                      name="template"
                      fontSize={14}
                      width="100%"
                      height={editorHeightPx}
                      showPrintMargin={true}
                      showGutter={true}
                      highlightActiveLine={true}
                      onChange={this.updateCodeTemplate}
                      value={this.state.codeTemplate}
                      setOptions={{
                        showLineNumbers: true,
                        tabSize: 2,
                      }}
                    />
                  </Col>
                </Row>
              </TabPane>
              <TabPane tabId="3">
                <Row>
                  <Col>
                    <AceEditor
                      placeholder="> "
                      mode="text"
                      theme="terminal"
                      name="template"
                      fontSize={14}
                      width="100%"
                      height={editorHeightPx}
                      showPrintMargin={true}
                      showGutter={false}
                      highlightActiveLine={false}
                      value={this.state.cliOutput}
                      readOnly={true}
                      setOptions={{
                        showLineNumbers: true,
                        tabSize: 2,
                      }}
                    />
                  </Col>
                </Row>
              </TabPane>
            </TabContent>
          </ModalBody>
          <ModalFooter>
            {this.state.valid === true ? (
              this.state.data === null ? (
                <Button color="success" onClick={this.createFlashcard}>
                  Test Passed - Create Flashcard
                </Button>
              ) : (
                <Button color="success" onClick={this.saveFlashcard}>
                  {this.state.saveBtnText}
                </Button>
              )
            ) : this.state.validationRunning ? (
              <Spinner color="primary" />
            ) : (
              <Button color="primary" onClick={this.validateSolution}>
                Validate Solution
              </Button>
            )}
          </ModalFooter>
        </>
      );
    }
  }

  private createExerciseDTO(): Exercise {
    return {
      flashcardTypeID: this.props.cardType.id,
      solution: this.state.codeSolution,
      test: this.state.codeTest,
      explanation: this.state.mdeValue,
      template: this.state.codeTemplate,
    };
  }
}
