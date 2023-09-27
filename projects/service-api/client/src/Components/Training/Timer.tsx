import { Component } from 'react';
import React from 'react';
import { RootState } from 'src/redux';
import {
  setTrainingTimeStart,
  setTrainingTimeElapsed,
  trainingClearState,
} from 'src/redux/modules/training';
import { connect } from 'react-redux';

const mapStateToProps = (state: RootState) => ({
  timeStart: state.training.timeStart,
  timeElapsed: state.training.timeElapsed,
  timeElapsedMin: state.training.timeElapsedMin,
  timeElapsedSec: state.training.timeElapsedSec,
  currentCardElapsedTime: state.training.currentCardElapsedTime,
});

const mapDispatchToProps = {
  setTrainingTimeStart,
  setTrainingTimeElapsed,
  trainingClearState,
};

type Props = ReturnType<typeof mapStateToProps> & typeof mapDispatchToProps;

interface State {
  interval: NodeJS.Timeout;
}

class UnconnectedTimer extends Component<Props> {
  state: Readonly<State> = {
    interval: setInterval(() => this.updateTime(), 1000),
  };

  componentDidMount() {
    this.props.setTrainingTimeStart(new Date().getTime());
  }

  componentWillUnmount() {
    this.props.trainingClearState();
    clearInterval(this.state.interval);
  }

  updateTime() {
    const newTimeElapsed = this.props.timeElapsed + 1;
    const currentCardElapsedTime = this.props.currentCardElapsedTime + 1;
    const minutes = parseInt((newTimeElapsed / 60).toString(), 10);
    const seconds = newTimeElapsed - minutes * 60;
    this.props.setTrainingTimeElapsed({
      timeElapsed: newTimeElapsed,
      timeElapsedMin: minutes,
      timeElapsedSec: seconds,
      currentCardElapsedTime: currentCardElapsedTime,
    });
  }

  render() {
    return (
      <span style={{ display: 'inline' }}>
        {this.props.timeElapsedMin} minutes, {this.props.timeElapsedSec} seconds
      </span>
    );
  }
}

const Timer = connect(mapStateToProps, mapDispatchToProps)(UnconnectedTimer);

export default Timer;
