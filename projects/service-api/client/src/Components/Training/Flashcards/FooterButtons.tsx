import React from 'react';
import { ModalFooter, Button, Spinner } from 'reactstrap';
import { MapID, NodeID } from 'src/api/models';
import { connect } from 'react-redux';
import { RootState } from 'src/redux';
import { setLoadingLogResponse } from 'src/redux/modules/training';
import Sound from 'react-sound';
import './FooterButtons.css';

const mapStateToProps = (state: RootState) => ({
  loadingLogResponse: state.training.loadingLogResponse,
});

const mapDispatchToProps = { setLoadingLogResponse };

type Props = ReturnType<typeof mapStateToProps> &
  typeof mapDispatchToProps &
  FooterButtonsProps;

interface FooterButtonsProps {
  mapID: MapID;
  nodeID: NodeID;
  firstSeen: boolean;
  submissionCorrect?: boolean;
  tryAgainButton?: boolean;
  showQualityRating: boolean;
  checkAnswer: Function;
  nextCard: Function;
  loading: boolean;
}

const UnconnectedFooterButtons: React.FunctionComponent<Props> = (props) => {
  function logAnswer(quality: number) {
    props.setLoadingLogResponse(true);
    window.mpio.logFlashcardQuality(
      { quality: quality },
      props.mapID,
      props.nodeID,
      () => {
        if (
          props.submissionCorrect &&
          typeof props.submissionCorrect === 'boolean'
        ) {
          props.nextCard(props.submissionCorrect);
          props.setLoadingLogResponse(false);
        } else {
          const correct = quality <= 3 ? false : true;
          props.nextCard(correct);
          props.setLoadingLogResponse(false);
        }
      },
    );
  }

  const wrongAnswerJSX = (
    <>
      {props.tryAgainButton ? (
        props.loading ? (
          <Spinner color="primary" />
        ) : (
          <>
            <Button onClick={() => props.checkAnswer()} color="primary">
              Try Again
            </Button>
            <Button onClick={() => props.nextCard(props.submissionCorrect)} color="danger">
              Next Card
            </Button>
          </>
        )
      ) : (
        <Button onClick={() => props.nextCard(props.submissionCorrect)} color="danger">
          Next Card
        </Button>
      )}
    </>
  );

  const qualityRatingButtonsJSXLearning =
    props.submissionCorrect !== true ? (
      <>
        <Button onClick={() => logAnswer(0)} color="danger">
          I'm learning this
        </Button>
        <Button onClick={() => logAnswer(4)} color="success">
          I know this
        </Button>
      </>
    ) : (
      <Button onClick={() => logAnswer(4)} color="success">
        Next Card
      </Button>
    );

  const qualityRatingButtonsJSXRecalling = (
    <>
      {props.submissionCorrect !== true && (
        <Button onClick={() => logAnswer(0)} color="danger">
          I forgot
        </Button>
      )}
      <Button onClick={() => logAnswer(3)} color="success">
        Hard
      </Button>
      <Button onClick={() => logAnswer(4)} color="success">
        Easy
      </Button>
      <Button onClick={() => logAnswer(5)} color="success">
        Very Easy
      </Button>
    </>
  );

  const playCorrectAnswerSound = (
    <Sound
      url="/assets/audio/correct.mp3"
      playStatus="PLAYING"
      autoLoad={true}
    />
  );

  const playIncorrectAnswerSound = (
    <Sound
      url="/assets/audio/incorrect.mp3"
      playStatus="PLAYING"
      autoLoad={true}
    />
  );

  const answerCorrectBgColor = props.submissionCorrect === undefined ? '' : (
    props.submissionCorrect ? 'greenBackground' : 'redBackground'
  );

  return (
    <>
    <ModalFooter className="text-center" style={{ display: 'block'! }}>
      {props.submissionCorrect === false && wrongAnswerJSX}
      {props.submissionCorrect === false && playIncorrectAnswerSound}
      {props.submissionCorrect === true &&
        !props.loadingLogResponse &&
        playCorrectAnswerSound}
      {!props.showQualityRating &&
        props.submissionCorrect === undefined &&
        (props.loading ? (
          <Spinner color="primary" />
        ) : (
          <Button onClick={() => props.checkAnswer()} color="primary">
            Check Answer
          </Button>
        ))}
      {props.showQualityRating &&
        (props.loadingLogResponse ? (
          <Spinner color="success" />
        ) : props.firstSeen ? (
          qualityRatingButtonsJSXLearning
        ) : (
          qualityRatingButtonsJSXRecalling
            ))}
    </ModalFooter>
    <div className={answerCorrectBgColor + ' answerFeedback'}/>
    </>
  );
};

const FooterButtons = connect(
  mapStateToProps,
  mapDispatchToProps,
)(UnconnectedFooterButtons);

export default FooterButtons;
