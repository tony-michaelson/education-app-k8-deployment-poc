import autobind from 'autobind-decorator';
import React, { Component } from 'react';
import { RouteComponentProps } from 'react-router';
import { Modal } from 'reactstrap';
import FlashCardTypeSelector from './FlashCardTypeSelector';
import { SegmentID, MapID, NodeID, FlashcardTypeBrief } from 'src/api/models';
import { FlashCard } from './FlashCard';
import { LoadingSpinner } from 'src/Components/Animations/LoadingSpinner';
import { setLoadingLogResponse } from 'src/redux/modules/training';
import { RootState } from 'src/redux';
import { connect } from 'react-redux';

export interface FlashCardModalProps {
  mapID: MapID;
  segmentID: SegmentID;
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
    // tslint:disable-next-line: no-any
    const { nodeID } = this.props.match.params as any;
    this.getFlashcardData(nodeID);
    this.toggle();
  }

  @autobind
  getFlashcardData(flashCardID: string) {
    window.mpio.getFlashcardType(
      this.state.mapID,
      this.state.segmentID,
      flashCardID,
      (response) => {
        this.setState({
          nodeID: flashCardID,
          cardType: response,
          loading: false,
        });
      },
      (notifier, error) => {
        if (error?.status === 404) {
          this.setState({
            nodeID: flashCardID,
            loading: false,
          });
        } else {
          // TODO - handle errors
          this.setState({
            nodeID: flashCardID,
            loading: false,
            error: true,
          });
        }
      },
    );
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
    if (this.state.loading === true) {
      return <LoadingSpinner />;
    } else {
      if (this.state.cardType && this.state.cardType.name) {
        const modalSize =
          this.state.cardType.cardType === 'code_exercise' ? 'xl' : 'lg';
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
              nodeID={this.state.nodeID}
              toggleFunction={this.toggle}
              cardType={this.state.cardType}
            />
          </Modal>
        );
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
            size="lg"
          >
            <FlashCardTypeSelector
              toggleFunction={this.toggle}
              liftCardType={this.selectCardType}
            />
          </Modal>
        );
      }
    }
  }
}

const FlashCardModal = connect(
  mapStateToProps,
  mapDispatchToProps,
)(UnconnectedFlashCardModal);

export default FlashCardModal;
