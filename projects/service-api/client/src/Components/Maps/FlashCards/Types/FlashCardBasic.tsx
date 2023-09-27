import autobind from 'autobind-decorator';
import React, { Component, useState } from 'react';
import {
  Button,
  ModalHeader,
  ModalBody,
  ModalFooter,
  Tooltip,
  Nav,
  NavItem,
  NavLink,
  TabContent,
  TabPane,
  Row,
  Col,
  Spinner,
} from 'reactstrap';
import { FlashCardProps } from '../FlashCard';
import {
  CardAnswer,
  CardPost,
  FlashcardTypeID,
  MapID,
  SegmentID,
  NodeID,
  CardBriefEdit,
  AnswerChoiceBriefEdit,
} from '../../../../api/models';
import * as helpers from '../../../../utils/helpers';
import _ from 'underscore';
import { AxiosResponse } from 'axios';
import classnames from 'classnames';
import AudioDetails from 'src/Components/AudioRecorder/AudioDetails';
import Recorder from 'src/Components/AudioRecorder/Recorder';
import SimpleMDE from 'react-simplemde-editor';
import 'easymde/dist/easymde.min.css';
import { StoreImageFileResponse } from 'src/types';

export type ChoiceType = 'single' | 'multiple';
export interface AnswerChoiceEdit {
  id: number;
  isTrue: boolean;
  toolTip: boolean;
  answerText: string;
}

interface CardAnswerEditProps {
  initialChoiceType: ChoiceType;
  initialMultiChoiceState: AnswerChoiceEdit[];
  initialAnswerText: string;
  liftMultiChoiceStateFunction: (a: AnswerChoiceEdit[]) => void;
  liftAnswerTextStateFunction: (a: string) => void;
  liftChoiceTypeFunction: (a: ChoiceType) => void;
}

const CardAnswerEdit = ({
  initialChoiceType,
  initialMultiChoiceState,
  initialAnswerText,
  liftMultiChoiceStateFunction,
  liftAnswerTextStateFunction,
  liftChoiceTypeFunction,
}: CardAnswerEditProps) => {
  function getRandomInt() {
    return Math.floor(Math.random() * 10000000000);
  }

  const [answerTypeButtonToolTip, setAnswerTypeButtonToolTip] = useState<
    boolean
  >(false);
  const [choiceType, setChoiceType] = useState<ChoiceType>(initialChoiceType);
  const [multiChoice, setMultiChoice] = useState<AnswerChoiceEdit[]>(
    initialMultiChoiceState,
  );
  const [answerText, setAnswerText] = useState<string>(initialAnswerText);

  function updateMultiChoice(multiChoiceData: AnswerChoiceEdit[]) {
    setMultiChoice(multiChoiceData);
    liftMultiChoiceStateFunction(multiChoiceData);
  }

  function toggleChoiceType() {
    if (choiceType === 'single') {
      setChoiceType('multiple');
      liftChoiceTypeFunction('multiple');
    } else {
      setChoiceType('single');
      liftChoiceTypeFunction('single');
    }
  }

  function removeAnswerChoice(choiceID: number) {
    updateMultiChoice(
      _.reject(multiChoice, function (choice: AnswerChoiceEdit) {
        return choice.id === choiceID;
      }),
    );
    return undefined;
  }

  function addAnswerChoice() {
    const newID: number = getRandomInt();
    const newChoice: AnswerChoiceEdit = {
      id: newID,
      isTrue: false,
      toolTip: false,
      answerText: '',
    };
    const newList: AnswerChoiceEdit[] = [...multiChoice, newChoice];
    updateMultiChoice(newList);
    return undefined;
  }

  function updateChoiceFieldByID(
    choiceID: number,
    fieldName: string,
    value: boolean | string,
  ) {
    return _.map(multiChoice, function (choice: AnswerChoiceEdit) {
      if (choice.id === choiceID) {
        choice[fieldName] = value;
        return choice;
      } else {
        return choice;
      }
    });
  }

  function setCheckBoxToolTipOpen(choiceID: number, open: boolean) {
    updateMultiChoice(updateChoiceFieldByID(choiceID, 'toolTip', open));
  }

  function toggleCorrect(choiceID: number, checked: boolean) {
    updateMultiChoice(updateChoiceFieldByID(choiceID, 'isTrue', checked));
  }

  function updateAnswerText(choiceID: number, value: string) {
    updateMultiChoice(updateChoiceFieldByID(choiceID, 'answerText', value));
  }

  function updateSingleAnswerText(text: string) {
    setAnswerText(text);
    liftAnswerTextStateFunction(text);
  }

  if (choiceType === 'multiple') {
    return (
      <div className="form-group hidden flashCardAnswerBox rounded">
        <div className="row" style={{ marginBottom: '10px' }}>
          <div className="col">
            <Button
              id="switchAnswerTypeButton"
              onClick={toggleChoiceType}
              type="button"
              className="btn btn-info"
            >
              Back (multiple choice)
            </Button>
            <Tooltip
              placement="top"
              isOpen={answerTypeButtonToolTip}
              target="switchAnswerTypeButton"
              toggle={() =>
                setAnswerTypeButtonToolTip(!answerTypeButtonToolTip)
              }
            >
              Click for plain text
            </Tooltip>
            &nbsp;
            <Button
              className="btn btn-success"
              onClick={addAnswerChoice}
              type="button"
            >
              +
            </Button>
          </div>
        </div>
        <div>
          <table className="flashCard_answerTable">
            <tbody>
              {multiChoice.map((answerChoice: AnswerChoiceEdit) => (
                <tr key={answerChoice.id}>
                  <td className="text-center" style={{ width: '5%' }}>
                    <input
                      id={'answerTrue_' + answerChoice.id}
                      type="checkbox"
                      checked={answerChoice.isTrue}
                      onChange={(e) =>
                        toggleCorrect(answerChoice.id, e.target.checked)
                      }
                    />
                    <Tooltip
                      id={'answerTip_' + answerChoice.id}
                      placement="top"
                      isOpen={answerChoice.toolTip}
                      target={'answerTrue_' + answerChoice.id}
                      toggle={() =>
                        setCheckBoxToolTipOpen(
                          answerChoice.id,
                          !answerChoice.toolTip,
                        )
                      }
                    >
                      Check if option is True
                    </Tooltip>
                  </td>
                  <td style={{ width: '90%' }}>
                    <input
                      className="form-control"
                      type="text"
                      placeholder="Answer choice"
                      value={answerChoice.answerText}
                      onChange={(e) =>
                        updateAnswerText(answerChoice.id, e.target.value)
                      }
                    />
                  </td>
                  <td className="text-center ml-2" style={{ width: '5%' }}>
                    <Button
                      className="btn btn-danger"
                      type="button"
                      onClick={() => removeAnswerChoice(answerChoice.id)}
                    >
                      -
                    </Button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    );
  } else {
    return (
      <div className="form-group flashCardAnswerBox rounded">
        <div className="row" style={{ marginBottom: '10px' }}>
          <div className="col">
            <Button
              id="switchAnswerTypeButton"
              onClick={toggleChoiceType}
              type="button"
              className="btn btn-info"
            >
              Back (plain text)
            </Button>
            <Tooltip
              placement="top"
              isOpen={answerTypeButtonToolTip}
              target="switchAnswerTypeButton"
              toggle={() =>
                setAnswerTypeButtonToolTip(!answerTypeButtonToolTip)
              }
            >
              Click for multiple choice
            </Tooltip>
          </div>
        </div>
        <textarea
          className="form-control"
          rows={5}
          id="answer_primary"
          name="answer_primary"
          defaultValue={answerText}
          onChange={(e) => updateSingleAnswerText(e.target.value)}
        />
      </div>
    );
  }
};

interface FlashCardBasicState {
  activeTabQ: 'plain' | 'markdown' | 'audio';
  activeTabA: 'plain' | 'markdown' | 'audio';
  audioDetails?: AudioDetails;
  title: string;
  mapID: MapID;
  segmentID: SegmentID;
  nodeID: NodeID;
  questionText: string;
  markdown: string;
  answerText: string;
  choiceType: ChoiceType;
  multiChoice: AnswerChoiceEdit[];
  toggleFunction: () => void;
  data: CardBriefEdit | null;
  loading: boolean;
  pendingResponse: boolean;
}

export class FlashCardBasic extends Component<FlashCardProps, {}> {
  state: Readonly<FlashCardBasicState> = {
    title: '',
    mapID: this.props.mapID,
    segmentID: this.props.segmentID,
    nodeID: this.props.nodeID,
    questionText: '',
    markdown: '',
    answerText: '',
    choiceType: 'single',
    activeTabQ: 'plain',
    activeTabA: 'plain',
    multiChoice: [
      {
        id: Math.floor(Math.random() * 10000000000),
        isTrue: false,
        toolTip: false,
        answerText: '',
      },
    ],
    toggleFunction: this.props.toggleFunction,
    data: null,
    loading: true,
    pendingResponse: false,
  };

  componentDidMount() {
    window.mpio.getFlashcard<CardBriefEdit>(
      this.props.mapID,
      this.props.segmentID,
      this.props.nodeID,
      (json: CardBriefEdit) => {
        if (json.choices.length > 1) {
          const multiChoice: AnswerChoiceEdit[] = _.map(json.choices, function (
            choice: AnswerChoiceBriefEdit,
          ) {
            return {
              id: Math.floor(Math.random() * 10000000000),
              isTrue: choice.correct,
              toolTip: false,
              answerText: choice.answer,
            };
          });
          this.setState({
            title: json.name,
            data: json,
            questionText: json.question,
            markdown: json.markdown,
            activeTabQ: json.markdown ? 'markdown' : 'plain',
            choiceType: 'multiple',
            multiChoice: multiChoice,
            loading: false,
          });
        } else {
          this.setState({
            title: json.name,
            data: json,
            questionText: json.question,
            markdown: json.markdown,
            activeTabQ: json.markdown ? 'markdown' : 'plain',
            answerText: helpers.getOrElse(json.choices, ['0', 'answer'], ''),
            loading: false,
          });
        }
      },
      (notifier, error: AxiosResponse) => {
        if (error?.status === 404) {
          this.setState({ title: 'New Flashcard', loading: false });
        }
      },
    );
  }

  @autobind
  answersToJSON() {
    let answers: { answer: string; correct: boolean }[] = [];
    if (this.state.choiceType === 'multiple') {
      this.state.multiChoice.forEach((answer: AnswerChoiceEdit) => {
        answers.push({
          answer: answer.answerText,
          correct: answer.isTrue,
        });
      });
    } else {
      answers = [
        {
          answer: this.state.answerText,
          correct: true,
        },
      ];
    }
    return answers;
  }

  @autobind
  liftMultiChoice(multiChoice: AnswerChoiceEdit[]) {
    this.setState({ multiChoice: multiChoice });
  }

  @autobind
  liftChoiceType(choiceType: ChoiceType) {
    this.setState({ choiceType: choiceType });
  }

  @autobind
  updateQuestionText(questionText: string) {
    this.setState({ questionText: questionText });
  }

  @autobind
  updateAnswerText(answerText: string) {
    this.setState({ answerText: answerText });
  }

  @autobind
  toggleFunction() {
    this.props.toggleFunction();
  }

  @autobind
  activeTabQ(tab: string) {
    if (this.state.activeTabQ !== tab) {
      this.setState({ activeTabQ: tab });
    }
  }

  @autobind
  activeTabA(tab: string) {
    if (this.state.activeTabA !== tab) {
      this.setState({ activeTabA: tab });
    }
  }

  @autobind
  saveMarkdown(value: string) {
    this.setState({ markdown: value });
  }

  @autobind
  createFlashcardJSON(
    fcTypeID: FlashcardTypeID,
    question: string,
    markdown: string,
    answers: CardAnswer[],
  ) {
    const card: CardPost = {
      flashcardTypeID: fcTypeID,
      question: question,
      markdown: markdown,
      audioURL:
        this.state.audioDetails?.url || this.state.data?.audio || undefined,
      answers: answers,
    };
    return card;
  }

  @autobind
  createFlashcard() {
    this.setState({ pendingResponse: true });
    const cardData: CardPost = this.createFlashcardJSON(
      '8e5918f8-89bf-4371-9999-3856d63700ac',
      this.state.questionText,
      this.state.markdown,
      this.answersToJSON(),
    );
    window.mpio.createFlashcard(
      cardData,
      this.state.mapID,
      this.state.segmentID,
      this.state.nodeID,
      'basic',
      () => {
        this.saveAudio();
      },
    );
  }

  saveFlashcard() {
    this.setState({ pendingResponse: true });
    const cardData: CardPost = this.createFlashcardJSON(
      '8e5918f8-89bf-4371-9999-3856d63700ac',
      this.state.questionText,
      this.state.markdown,
      this.answersToJSON(),
    );
    window.mpio.updateFlashcard(
      cardData,
      this.state.mapID,
      this.state.segmentID,
      this.state.nodeID,
      'basic',
      () => {
        this.saveAudio();
      },
    );
  }

  @autobind
  saveAudio() {
    if (this.state.audioDetails) {
      window.mpio.storeFlashcardAudio(
        this.state.audioDetails.blob,
        this.state.mapID,
        this.state.nodeID,
        () => {
          this.setState({ pendingResponse: false });
          this.toggleFunction();
        },
      );
    } else {
      this.setState({ pendingResponse: false });
      this.toggleFunction();
    }
  }

  @autobind
  handleAudioStop(data: AudioDetails) {
    // console.log(data);
    this.setState({ audioDetails: data });
  }

  @autobind
  handleRest() {
    this.setState({ audioDetails: undefined });
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

  render() {
    if (this.state.loading === true) {
      return <></>;
    } else {
      return (
        <>
          <ModalHeader toggle={this.toggleFunction}>
            {this.state.title}
          </ModalHeader>
          <ModalBody role="document">
            <div className="form-group flashCardQuestionBox rounded">
              <Nav tabs={true}>
                <NavItem>
                  <NavLink
                    className={classnames({
                      'nav-tab-active': this.state.activeTabQ === 'plain',
                      'nav-tab': true,
                    })}
                    onClick={() => {
                      this.activeTabQ('plain');
                    }}
                  >
                    Plain Text
                  </NavLink>
                </NavItem>
                <NavItem>
                  <NavLink
                    className={classnames({
                      'nav-tab-active': this.state.activeTabQ === 'markdown',
                      'nav-tab': true,
                    })}
                    onClick={() => {
                      this.activeTabQ('markdown');
                    }}
                  >
                    Markdown
                  </NavLink>
                </NavItem>
                <NavItem>
                  <NavLink
                    className={classnames({
                      'nav-tab-active': this.state.activeTabQ === 'audio',
                      'nav-tab': true,
                    })}
                    onClick={() => {
                      this.activeTabQ('audio');
                    }}
                  >
                    Audio
                  </NavLink>
                </NavItem>
              </Nav>
              <TabContent activeTab={this.state.activeTabQ}>
                <TabPane tabId="plain">
                  <Row>
                    <Col sm="12">
                      <textarea
                        className="form-control"
                        rows={5}
                        id="question"
                        name="question"
                        defaultValue={this.state.questionText}
                        onChange={(e) =>
                          this.updateQuestionText(e.target.value)
                        }
                      />
                    </Col>
                  </Row>
                </TabPane>
                <TabPane tabId="markdown">
                  <Row>
                    <Col sm="12">
                      <SimpleMDE
                        value={this.state.markdown}
                        onChange={(value) => this.saveMarkdown(value)}
                        options={{
                          uploadImage: true,
                          imageUploadFunction: this.imageUploadFunction,
                          autofocus: true,
                          initialValue: this.state.markdown,
                        }}
                      />
                    </Col>
                  </Row>
                </TabPane>
                <TabPane tabId="audio">
                  <Recorder
                    title={'New recording'}
                    showUIAudio={true}
                    handleAudioStop={(data) => this.handleAudioStop(data)}
                    handleReset={() => this.handleRest()}
                    audioURL={
                      this.state.audioDetails?.url || this.state.data?.audio
                    }
                  />
                </TabPane>
              </TabContent>
            </div>
            <div className="border-top my-3" />
            <CardAnswerEdit
              initialChoiceType={this.state.choiceType}
              initialMultiChoiceState={this.state.multiChoice}
              initialAnswerText={this.state.answerText}
              liftMultiChoiceStateFunction={this.liftMultiChoice}
              liftAnswerTextStateFunction={this.updateAnswerText}
              liftChoiceTypeFunction={this.liftChoiceType}
            />
          </ModalBody>
          <ModalFooter>
            {this.state.pendingResponse ? (
              <Spinner color="primary" />
            ) : this.state.data === null ? (
              <Button color="primary" onClick={() => this.createFlashcard()}>
                Create Flashcard
              </Button>
            ) : (
              <Button color="primary" onClick={() => this.saveFlashcard()}>
                Save Flashcard
              </Button>
            )}
            <Button color="danger" onClick={this.toggleFunction}>
              Cancel
            </Button>
          </ModalFooter>
        </>
      );
    }
  }
}
