import autobind from 'autobind-decorator';
import React, { Component } from 'react';
import {
  Media,
  ModalHeader,
  ModalBody,
  Nav,
  NavItem,
  NavLink,
  Row,
  Col,
  TabContent,
  TabPane,
} from 'reactstrap';
import 'react-mde/lib/styles/css/react-mde-all.css';
import classnames from 'classnames';
import { FlashCardProps } from 'src/Components/Training/Flashcards/FlashCard';
import AceEditor from 'react-ace';
import 'ace-builds/src-noconflict/mode-scala';
import 'ace-builds/src-noconflict/mode-javascript';
import 'ace-builds/src-noconflict/theme-monokai';
import 'ace-builds/src-noconflict/theme-terminal';
import {
  MapID,
  SegmentID,
  NodeID,
  CodeExerciseBriefEdit,
  CodeExerciseBriefTest,
  ExerciseAnswer,
  CardGradeAnswer,
} from 'src/api/models';
import { getFlashCardIconByCardTypeName } from 'src/Components/Maps/FlashCards/FlashCardTypeSelector';
import FooterButtons from 'src/Components/Training/Flashcards/FooterButtons';
import { store } from 'src/index';

const cardProperties = {
  code_exercise_scala: { lang: 'scala', name: 'Scala' },
  code_exercise_nodejs: { lang: 'javascript', name: 'Javascript' },
};

const getCardTypeLogo = (name: string) => {
  return '/assets/images/icons/' + getFlashCardIconByCardTypeName[name];
};

interface CodeExerciseTestState {
  title: string;
  mapID: MapID;
  segmentID: SegmentID;
  nodeID: NodeID;
  toggleFunction: () => void;
  data: CodeExerciseBriefEdit | null;
  loading: boolean;
  activeTab: string;
  explanation: string;
  codeSolution: string;
  testCode: string;
  cliOutput: string;
  submissionCorrect?: boolean;
  showQualityRating: boolean;
  verificationRunning: boolean;
}

export class CodeExercise extends Component<FlashCardProps, {}> {
  state: Readonly<CodeExerciseTestState> = {
    title: '',
    mapID: this.props.mapID,
    segmentID: this.props.segmentID,
    nodeID: this.props.nodeID,
    toggleFunction: this.props.toggleFunction,
    data: null,
    loading: true,
    activeTab: '1',
    explanation: '',
    codeSolution: '',
    testCode: '',
    cliOutput: '',
    showQualityRating: false,
    verificationRunning: false,
  };

  componentDidMount() {
    this.getFlashcardData();
  }

  componentDidUpdate(prevProps: FlashCardProps) {
    if (
      prevProps.nodeID !== this.props.nodeID ||
      prevProps.lastAnswer !== this.props.lastAnswer
    ) {
      this.setState({
        title: '',
        mapID: this.props.mapID,
        segmentID: this.props.segmentID,
        nodeID: this.props.nodeID,
        loading: true,
        data: null,
        activeTab: '1',
        explanation: '',
        codeSolution: '',
        testCode: '',
        cliOutput: '',
        showQualityRating: false,
        verificationRunning: false,
        submissionCorrect: undefined,
      });
      this.getFlashcardData();
    }
  }

  @autobind
  getFlashcardData() {
    window.mpio.getFlashcardTest<CodeExerciseBriefTest>(
      this.props.mapID,
      this.props.nodeID,
      (response) => {
        const title = this.props.cardType.commonName;
        this.setState({
          data: response,
          title: title,
          explanation: response.explanation,
          codeSolution: response.template,
          testCode: response.test,
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
  updateCodeSolution(value: string) {
    this.setState({ codeSolution: value, valid: false });
  }

  @autobind
  handleWrongAnswer() {
    window.mpio.logFlashcardQuality(
      { quality: 0 },
      this.props.mapID,
      this.props.nodeID,
      () => {
        this.setState({
          submissionCorrect: false,
          showQualityRating: false,
        });
        this.toggle('3');
      },
    );
  }

  @autobind
  handleCorrectAnswer() {
    this.setState({ showQualityRating: true, submissionCorrect: true });
  }

  @autobind
  checkAnswer() {
    this.setState({ verificationRunning: true, submissionCorrect: undefined });
    const data: ExerciseAnswer = this.createExerciseDTO();
    window.mpio.gradeFlashcard(
      data,
      this.props.mapID,
      this.props.nodeID,
      this.props.cardType.cardType,
      (json: CardGradeAnswer) => {
        this.setState({
          cliOutput: json.message,
          activeTab: '3',
          verificationRunning: false,
        });
        if (json.correct === true) {
          this.handleCorrectAnswer();
        } else {
          this.handleWrongAnswer();
        }
      },
    );
  }

  @autobind
  nextCard(correct: boolean) {
    if (typeof this.props.nextCard === 'function') {
      this.props.nextCard(correct);
    }
  }

  @autobind
  toggleFunction() {
    this.props.toggleFunction();
  }

  @autobind
  toggle(tab: string) {
    if (this.state.activeTab !== tab) {
      this.setState({ activeTab: tab });
    }
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
                  Exercise
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
                  Test Code
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
                    <div
                      dangerouslySetInnerHTML={{
                        __html: this.state.explanation,
                      }}
                    />
                  </Col>
                  <Col xs="6">
                    <AceEditor
                      placeholder="Solution Code"
                      mode={cardProperties[this.props.cardType.name].lang}
                      theme="monokai"
                      name="template"
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
                  <Col>
                    <AceEditor
                      placeholder="Test Code"
                      mode={cardProperties[this.props.cardType.name].lang}
                      theme="monokai"
                      name="testCode"
                      fontSize={14}
                      width="100%"
                      height={editorHeightPx}
                      showPrintMargin={true}
                      showGutter={true}
                      highlightActiveLine={true}
                      onChange={this.updateCodeSolution}
                      value={this.state.testCode}
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
          <FooterButtons
            mapID={this.props.mapID}
            nodeID={this.props.nodeID}
            firstSeen={this.props.lastAnswer === 0 ? true : false}
            submissionCorrect={this.state.submissionCorrect}
            tryAgainButton={true}
            showQualityRating={this.state.showQualityRating}
            checkAnswer={this.checkAnswer}
            nextCard={this.nextCard}
            loading={this.state.verificationRunning}
          />
        </>
      );
    }
  }

  private createExerciseDTO(): ExerciseAnswer {
    return {
      code: this.state.codeSolution,
      seconds: store.getState().training.currentCardElapsedTime,
    };
  }
}
