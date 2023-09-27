import autobind from 'autobind-decorator';
import React, { Component } from 'react';
import { RouteComponentProps } from 'react-router';
import { Modal } from 'reactstrap';
import {
  SegmentID,
  MapID,
  NodeID,
  FlashcardTypeBrief,
  CardMetaData,
} from 'src/api/models';
import { setLoadingLogResponse } from 'src/redux/modules/training';
import { RootState } from 'src/redux';
import { connect } from 'react-redux';
import { FlashCard } from './FlashCard';

export interface FlashCardModalProps {
  mapID: MapID;
  segmentID: SegmentID;
  card: CardMetaData;
  nextCard: Function;
  lastAnswer: number;
}

const mapStateToProps = (state: RootState) => ({
  loadingLogResponse: state.training.loadingLogResponse,
});

const mapDispatchToProps = { setLoadingLogResponse };

type Props = ReturnType<typeof mapStateToProps> &
  typeof mapDispatchToProps &
  FlashCardModalProps;

interface FlashCardModalState {
  mapID: MapID;
  segmentID: SegmentID;
  nodeID: NodeID;
  modal: boolean;
  loading: boolean;
  error: boolean;
  cardType?: FlashcardTypeBrief;
}

class UnconnectedFlashCardModal extends Component<
  Props & RouteComponentProps,
  {}
> {
  state: Readonly<FlashCardModalState> = {
    mapID: this.props.mapID,
    segmentID: this.props.segmentID,
    nodeID: '',
    modal: false,
    loading: true,
    error: false,
  };

  componentDidMount() {
    this.toggle();
  }

  componentDidUpdate(prevProps: FlashCardModalProps) {
    if (
      this.props.card.nodeID &&
      prevProps.card.nodeID !== this.props.card.nodeID
    ) {
      this.setState({ loading: true });
    }
  }

  @autobind
  toggle() {
    const newModalState = !this.state.modal;
    this.setState({
      modal: newModalState,
    });
  }

  @autobind
  selectCardType(cardType: FlashcardTypeBrief) {
    this.setState({
      cardType: cardType,
      error: undefined,
    });
  }

  @autobind
  exit() {
    this.props.history.goBack();
  }

  render() {
    const modalSize =
      this.props.card.flashCardType.cardType === 'code_exercise' ? 'xl' : 'lg';
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
        size={modalSize}
      >
        <FlashCard
          mapID={this.state.mapID}
          segmentID={this.state.segmentID}
          nodeID={this.props.card.nodeID}
          toggleFunction={this.toggle}
          cardType={this.props.card.flashCardType}
          nextCard={this.props.nextCard}
          lastAnswer={this.props.lastAnswer}
        />
      </Modal>
    );
  }
}

const FlashCardModal = connect(
  mapStateToProps,
  mapDispatchToProps,
)(UnconnectedFlashCardModal);

export default FlashCardModal;
