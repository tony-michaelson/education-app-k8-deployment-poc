import autobind from 'autobind-decorator';
import React, { Component } from 'react';
import { RouteComponentProps } from 'react-router';
import {
  Button,
  Modal,
  ModalHeader,
  ModalBody,
  ModalFooter,
  Progress,
  Table,
} from 'reactstrap';
import {
  MapID,
  SegmentID,
  PostTimeRead,
  CardMetaData,
  CardsDue,
} from 'src/api/models';
import FlashCardModal from './Flashcards/FlashCardModal';
import PostView from '../Maps/Content/PostView';
import { LoadingSpinner } from 'src/Components/Animations/LoadingSpinner';
import { RootState } from 'src/redux';
import { requestMindMapReload } from 'src/redux/modules/mindmap';
import {
  setCurrentCardElapsedTime,
  setLoadingLogResponse,
} from 'src/redux/modules/training';
import { connect } from 'react-redux';
import Timer from './Timer';

const mapStateToProps = (state: RootState) => ({
  breadcrumb: state.mindmap.breadcrumb,
  reloadMap: state.mindmap.reloadMap,
});

const mapDispatchToProps = {
  requestMindMapReload,
  setCurrentCardElapsedTime,
  setLoadingLogResponse,
};

export interface TrainingScreenProps {
  mapID: MapID;
  segmentID: SegmentID;
  mapView: boolean;
}

interface TrainingScreenState {
  postsTimeRead: PostTimeRead[];
  cardsDue?: CardsDue;
  currentCard?: CardMetaData;
  currentCardIndex: number;
  totalCards: number;
  completedCards: number;
  postID: string;
  modal: boolean;
  requestMapReload: boolean;
  loading: boolean;
}

type Props = ReturnType<typeof mapStateToProps> & typeof mapDispatchToProps;

class UnconnectedTrainingScreen extends Component<
  Props & TrainingScreenProps & RouteComponentProps,
  {}
> {
  state: Readonly<TrainingScreenState> = {
    postsTimeRead: [],
    postID: '',
    currentCardIndex: 0,
    totalCards: 0,
    completedCards: 0,
    requestMapReload: false,
    modal: false,
    loading: true,
  };

  componentDidMount() {
    this.refreshDeck();
  }

  @autobind
  refreshDeck() {
    window.mpio.getMapPostsReadTimes(this.props.mapID, (postsTimes) => {
      window.mpio.getCards(
        this.props.mapID,
        this.props.segmentID,
        (cardsDue) => {
          const totalCards = this.state.totalCards + cardsDue.cards.length;
          this.setState({
            postsTimeRead: postsTimes,
            cardsDue: cardsDue,
            currentCard:
              cardsDue.cards.length > 0 ? cardsDue.cards[0] : undefined,
            currentCardIndex: 0,
            totalCards: totalCards,
            loading: false,
          });
          this.props.setLoadingLogResponse(false); // otherwise loading spinner hangs
          if (this.state.modal === false) {
            this.toggle();
          }
        },
      );
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
  checkForPost(postID: string): PostTimeRead | undefined {
    return this.state.postsTimeRead.find((post) => {
      if (post.postID === postID) {
        return true;
      } else {
        return false;
      }
    });
  }

  @autobind
  nextCard(lastSubmissionCorrect?: boolean) {
    if (lastSubmissionCorrect === false && this.state.currentCard) {
      const post = this.checkForPost(this.state.currentCard.parentID);
      if (post) {
        this.setState({ postID: post.postID });
      } else {
        this.pullNextCard();
      }
    } else {
      this.pullNextCard();
    }
  }

  @autobind
  acknowledgePost() {
    this.setState({ postID: '' });
    this.pullNextCard();
  }

  @autobind
  pullNextCard() {
    if (
      this.state.currentCard &&
      this.state.cardsDue &&
      this.state.cardsDue.cards.length > this.state.currentCardIndex + 1
    ) {
      const newCardIndex = this.state.currentCardIndex + 1;
      const completedCards = this.state.completedCards + 1;
      this.setState({
        requestMapReload: true,
        currentCard: this.state.cardsDue?.cards[newCardIndex],
        completedCards: completedCards,
        currentCardIndex: newCardIndex,
      });
      this.props.setCurrentCardElapsedTime(0);
    } else {
      const completedCards = this.state.completedCards + 1;
      this.props.setCurrentCardElapsedTime(0);
      this.setState({
        completedCards: completedCards,
      });
      this.refreshDeck();
    }
  }

  @autobind
  exit() {
    if (this.state.requestMapReload && this.props.mapView) {
      this.props.requestMindMapReload(true);
    }
    this.props.history.goBack();
  }

  @autobind
  progressBar() {
    const percentageCompleted =
      this.state.cardsDue?.cards.length === 0
        ? 100
        : (this.state.completedCards / this.state.totalCards) * 100;
    return (
      <Progress value={percentageCompleted}>
        <div style={{ float: 'left' }}>
          <span style={{ display: 'inline' }}>
            ({this.state.completedCards} / {this.state.totalCards}){' '}
            {percentageCompleted.toFixed(0)}%{'  -  '}
          </span>
          <Timer />
        </div>
      </Progress>
    );
  }

  render() {
    const informationBar = (
      <nav className="trainingInfoBar fixed-bottom navbar-light bg-light container-fluid">
        {this.progressBar()}
      </nav>
    );

    if (this.state.loading === true) {
      return <LoadingSpinner />;
    } else if (this.state.postID) {
      return (
        <>
          <PostView
            nodeID={this.state.postID}
            acknowledgePost={this.acknowledgePost}
            {...this.props}
          />
          {informationBar}
        </>
      );
    } else if (this.state.currentCard) {
      return (
        <>
          <FlashCardModal
            card={this.state.currentCard}
            segmentID={this.state.currentCard.segmentID}
            mapID={this.props.mapID}
            lastAnswer={this.state.currentCard.lastAnswer}
            history={this.props.history}
            location={this.props.location}
            match={this.props.match}
            nextCard={this.nextCard}
          />
          {informationBar}
        </>
      );
    } else {
      return (
        <>
          <Modal
            isOpen={this.state.modal}
            onClosed={() => this.exit()}
            autoFocus={true}
            toggle={this.toggle}
            scrollable={false}
            centered={true}
            role="dialog"
            backdrop="static"
          >
            <ModalHeader toggle={this.toggle}>Training Session</ModalHeader>
            <ModalBody role="document">
              <p>No Cards Due Today</p>
              <Table>
                <thead>
                  <th>Cards</th>
                  <th>Date</th>
                </thead>
                <tbody>
                  {this.state.cardsDue?.dueDates.slice(0, 5).map((v) => {
                    const cardCount = v[1];
                    const date = v[0].replace(/^\d+:/, '');
                    return (
                      <tr key={date}>
                        <td>{cardCount}</td>
                        <td>{date}</td>
                      </tr>
                    );
                  })}
                </tbody>
              </Table>
            </ModalBody>
            <ModalFooter>
              <Button color="primary" onClick={this.toggle}>
                Ok
              </Button>
            </ModalFooter>
          </Modal>
          {informationBar}
        </>
      );
    }
  }
}

const TrainingScreen = connect(
  mapStateToProps,
  mapDispatchToProps,
)(UnconnectedTrainingScreen);

export default TrainingScreen;
