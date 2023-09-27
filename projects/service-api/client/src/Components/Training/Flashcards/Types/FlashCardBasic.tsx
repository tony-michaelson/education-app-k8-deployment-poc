import autobind from 'autobind-decorator';
import React, { Component } from 'react';
import { ModalHeader, ModalBody, FormGroup, Label, Input } from 'reactstrap';
import { FlashCardProps } from '../FlashCard';
import {
  MapID,
  NodeID,
  CardBriefTest,
  AnswerChoiceBriefTest,
  AnswerChoiceID,
  TestAnswer,
  AnswerChoice,
} from 'src/api/models';
import * as helpers from 'src/utils/helpers';
import _ from 'underscore';
import { AxiosResponse } from 'axios';
import FooterButtons from 'src/Components/Training/Flashcards/FooterButtons';
import { store } from 'src/index';
import { BsCheckBox, BsFillXSquareFill } from 'react-icons/bs';
import 'easymde/dist/easymde.min.css';

export type ChoiceType = 'single' | 'multiple';

export interface AnswerChoiceTest {
  id: AnswerChoiceID;
  answerText: string;
}

interface FlashCardBasicTestState {
  title: string;
  mapID: MapID;
  nodeID: NodeID;
  questionText: string;
  markdown_html: string;
  answerText: string;
  showAnswer: boolean;
  showQualityRating: boolean;
  choiceType: ChoiceType;
  multiChoice: AnswerChoiceTest[];
  selectedAnswers: string[];
  correctAnswers: string[];
  submissionCorrect?: boolean;
  toggleFunction: () => void;
  data: CardBriefTest | null;
  radio: boolean;
  loading: boolean;
  verificationRunning: boolean;
}

export class FlashCardBasic extends Component<FlashCardProps, {}> {
  state: Readonly<FlashCardBasicTestState> = {
    title: '',
    mapID: this.props.mapID,
    nodeID: this.props.nodeID,
    questionText: '',
    markdown_html: '',
    answerText: '',
    showAnswer: false,
    showQualityRating: false,
    choiceType: 'single',
    multiChoice: [],
    selectedAnswers: [],
    correctAnswers: [],
    toggleFunction: this.props.toggleFunction,
    data: null,
    radio: false,
    loading: true,
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
        nodeID: this.props.nodeID,
        questionText: '',
        markdown_html: '',
        answerText: '',
        showAnswer: false,
        showQualityRating: false,
        choiceType: 'single',
        multiChoice: [],
        submissionCorrect: undefined,
        selectedAnswers: [],
        correctAnswers: [],
        data: null,
        radio: false,
        verificationRunning: false,
      });
      this.getFlashcardData();
    }
  }

  @autobind
  getFlashcardData() {
    window.mpio.getFlashcardTest<CardBriefTest>(
      this.props.mapID,
      this.props.nodeID,
      (json: CardBriefTest) => {
        if (json.choices.length > 1) {
          const multiChoice: AnswerChoiceTest[] = _.map(json.choices, function (
            choice: AnswerChoiceBriefTest,
          ) {
            return {
              id: choice.id,
              answerText: choice.answer,
            };
          });
          this.setState({
            title: json.name,
            data: json,
            questionText: json.question,
            markdown_html: json.markdown_html,
            choiceType: 'multiple',
            multiChoice: multiChoice,
            radio: json.radio,
            loading: false,
          });
        } else {
          this.setState({
            title: json.name,
            data: json,
            questionText: json.question,
            markdown_html: json.markdown_html,
            radio: json.radio,
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
  toggleFunction() {
    this.props.toggleFunction();
  }

  @autobind
  handleWrongAnswer(answers: AnswerChoice[]) {
    window.mpio.logFlashcardQuality(
      { quality: 0 },
      this.props.mapID,
      this.props.nodeID,
      () => {
        if (answers) {
          const correctAnswers = answers.map((v: AnswerChoice) => {
            return v.id;
          });
          this.setState({
            correctAnswers: correctAnswers,
            submissionCorrect: false,
            showQualityRating: false,
          });
        }
      },
    );
  }

  @autobind
  handleCorrectAnswer() {
    this.setState({ showQualityRating: true, submissionCorrect: true });
  }

  @autobind
  checkAnswer() {
    this.setState({ verificationRunning: true });
    if (this.state.choiceType === 'multiple') {
      const answer: TestAnswer = {
        choices: this.state.selectedAnswers,
        seconds: store.getState().training.currentCardElapsedTime,
      };
      window.mpio.gradeFlashcard(
        answer,
        this.props.mapID,
        this.props.nodeID,
        this.props.cardType.cardType,
        (json) => {
          if (json.correct === true) {
            this.handleCorrectAnswer();
          } else {
            if (json.answers) {
              this.handleWrongAnswer(json.answers);
            }
          }
        },
      );
    } else {
      this.setState({
        showAnswer: true,
        showQualityRating: true,
        verificationRunning: false,
      });
    }
  }

  @autobind
  getRandomInt() {
    return Math.floor(Math.random() * 10000000000);
  }

  @autobind
  handleSelectedAnswer() {
    return (e: React.ChangeEvent<HTMLInputElement>) => {
      const value = e.currentTarget.value;
      if (e.currentTarget.checked) {
        const newSelected = this.state.selectedAnswers.concat(value);
        this.setState({ selectedAnswers: newSelected });
      } else {
        const newSelected = this.state.selectedAnswers.filter((v) => {
          if (v !== value) {
            return true;
          } else {
            return false;
          }
        });
        this.setState({ selectedAnswers: newSelected });
      }
    };
  }

  @autobind
  isChecked(id: AnswerChoiceID): boolean {
    const search = this.state.selectedAnswers.find((v) => {
      return v === id.toString() ? true : false;
    });
    return search ? true : false;
  }

  @autobind
  isCorrect(id: AnswerChoiceID): boolean {
    const search = this.state.correctAnswers.find((v) => {
      return v === id.toString() ? true : false;
    });
    return search ? true : false;
  }

  @autobind
  nextCard(correct: boolean) {
    if (typeof this.props.nextCard === 'function') {
      this.props.nextCard(correct);
    }
  }

  @autobind
  setSubmissionCorrect(correct: boolean) {
    this.setState({ submissionCorrect: correct });
  }

  @autobind
  selectJSX(selectType: 'radio' | 'checkbox') {
    let answerBoxClasses: string[] = ['flashCardAnswerSelectionBox'];
    if (this.state.submissionCorrect === true) {
      answerBoxClasses.push('correctAnswer');
    }
    if (this.state.submissionCorrect === false) {
      answerBoxClasses.push('wrongAnswer');
    }

    if (selectType === 'radio') {
      if (this.state.correctAnswers.length) {
        return (
          <FormGroup tag="fieldset">
            <hr />
            <div className={answerBoxClasses.join(' ')}>
              {this.state.multiChoice.map((choice: AnswerChoiceTest) => (
                <FormGroup key={this.getRandomInt()} check={true}>
                  <Label
                    check={true}
                    className={
                      this.isCorrect(choice.id) ? 'font-weight-bold' : ''
                    }
                  >
                    {this.isCorrect(choice.id) ? (
                      <BsCheckBox
                        className="correctAnswerCheckbox"
                        color="green"
                      />
                    ) : (
                      <BsFillXSquareFill
                        className="correctAnswerCheckbox"
                        color="red"
                      />
                    )}{' '}
                    {choice.answerText}
                  </Label>
                </FormGroup>
              ))}
            </div>
          </FormGroup>
        );
      } else {
        return (
          <FormGroup tag="fieldset">
            <hr />
            <div className={answerBoxClasses.join(' ')}>
              {this.state.multiChoice.map((choice: AnswerChoiceTest) => (
                <FormGroup key={this.getRandomInt()} check={true}>
                  <Label check={true}>
                    <Input
                      type="radio"
                      onChange={this.handleSelectedAnswer()}
                      name="radioAnswer"
                      value={choice.id.toString()}
                      checked={this.isChecked(choice.id)}
                    />{' '}
                    {choice.answerText}
                  </Label>
                </FormGroup>
              ))}
            </div>
          </FormGroup>
        );
      }
    } else {
      if (this.state.correctAnswers.length) {
        return (
          <FormGroup tag="fieldset">
            <hr />
            <div className={answerBoxClasses.join(' ')}>
              {this.state.multiChoice.map((choice: AnswerChoiceTest) => (
                <FormGroup key={this.getRandomInt()} check={true}>
                  <Label
                    check={true}
                    className={
                      this.isCorrect(choice.id) ? 'font-weight-bold' : ''
                    }
                  >
                    {this.isCorrect(choice.id) ? (
                      <BsCheckBox
                        className="correctAnswerCheckbox"
                        color="green"
                      />
                    ) : (
                      <BsFillXSquareFill
                        className="correctAnswerCheckbox"
                        color="red"
                      />
                    )}{' '}
                    {choice.answerText}
                  </Label>
                </FormGroup>
              ))}
            </div>
          </FormGroup>
        );
      } else {
        return (
          <FormGroup tag="fieldset">
            <hr />
            <div className={answerBoxClasses.join(' ')}>
              {this.state.multiChoice.map((choice: AnswerChoiceTest) => (
                <FormGroup key={this.getRandomInt()} check={true}>
                  <Label check={true}>
                    <Input
                      type="checkbox"
                      onChange={this.handleSelectedAnswer()}
                      name="checkboxAnswer"
                      value={choice.id.toString()}
                      checked={this.isChecked(choice.id)}
                    />{' '}
                    {choice.answerText}
                  </Label>
                </FormGroup>
              ))}
            </div>
          </FormGroup>
        );
      }
    }
  }

  render() {
    const answerJSX = (
      <>
        <hr />
        <div
          className="flashCardAnswerSelectionBox correctAnswer"
          dangerouslySetInnerHTML={{
            __html: helpers.convertNewlinesToBreaks(
              helpers.escapeHTML(this.state.answerText),
            ),
          }}
        />
      </>
    );

    if (this.state.loading === true) {
      return <></>;
    } else {
      return (
        <>
          <ModalHeader toggle={this.toggleFunction}>
            {this.state.title}
          </ModalHeader>
          <ModalBody role="document">
            <div
              dangerouslySetInnerHTML={{
                __html: helpers.convertNewlinesToBreaks(
                  this.state.markdown_html ||
                    helpers.escapeHTML(this.state.questionText),
                ),
              }}
            />
            {this.state.data?.audio && (
              <audio
                className="flashCardAudioPlayer"
                controls={true}
                autoPlay={true}
              >
                <source src={this.state.data.audio} type="audio/wav" />
              </audio>
            )}
            {this.state.choiceType === 'multiple' &&
              this.state.radio === false &&
              this.selectJSX('checkbox')}
            {this.state.choiceType === 'multiple' &&
              this.state.radio === true &&
              this.selectJSX('radio')}
            {this.state.showAnswer && answerJSX}
          </ModalBody>
          <FooterButtons
            mapID={this.props.mapID}
            nodeID={this.props.nodeID}
            firstSeen={this.props.lastAnswer === 0 ? true : false}
            submissionCorrect={this.state.submissionCorrect}
            showQualityRating={this.state.showQualityRating}
            checkAnswer={this.checkAnswer}
            nextCard={this.nextCard}
            loading={this.state.verificationRunning}
          />
        </>
      );
    }
  }
}
