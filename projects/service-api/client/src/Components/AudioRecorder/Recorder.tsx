import React, { Component } from 'react';
const microphone = require('./images/microphone.png');
const stopIcon = require('./images/stop.png');
import './Recorder.css';
import autobind from 'autobind-decorator';

declare global {
  interface Window {
    webkitAudioContext: typeof AudioContext;
  }
}

export interface AudioDetails {
  url: string;
  blob: Blob;
  duration: {
    h: number;
    m: number;
    s: number;
  };
}

interface Props {
  handleReset: (state: State) => void;
  handleAudioStop: (state: AudioDetails) => void;
  showUIAudio: boolean;
  title: string;
  audioURL?: string;
  bufferSize?: number;
}

interface State {
  time: {
    h: number;
    m: number;
    s: number;
  };
  seconds: number;
  isPaused: boolean;
  recording: boolean;
  micOpen: boolean;
  mediaNotFound: boolean;
  audioURL: string;
  audioBlob: Blob;
  bufferSize: number;
  browserName: 'ie' | 'edge' | 'firefox' | 'chrome' | 'opera' | 'safari' | null;
}

export default class Recorder extends Component<Props, {}> {
  state: Readonly<State> = {
    time: {
      h: 0,
      m: 0,
      s: 0,
    },
    seconds: 0,
    isPaused: false,
    recording: false,
    micOpen: false,
    mediaNotFound: false,
    audioURL: '',
    audioBlob: new Blob(),
    bufferSize: this.props.bufferSize || 4096,
    browserName: null,
  };
  recordedData: Float32Array[];
  recordingLength: number;
  audioContext: AudioContext;
  audioNode: ScriptProcessorNode;
  audioInput: MediaStreamAudioSourceNode;
  stream: MediaStream;
  timer: NodeJS.Timeout;

  componentDidMount() {
    // Get the user-agent string
    const userAgentString = navigator.userAgent;

    // Detect Chrome
    let chromeAgent = userAgentString.indexOf('Chrome') > -1;

    // Detect Internet Explorer
    let IExplorerAgent =
      userAgentString.indexOf('MSIE') > -1 ||
      userAgentString.indexOf('rv:') > -1;

    // Detect Firefox
    let firefoxAgent = userAgentString.indexOf('Firefox') > -1;

    // Detect Safari
    let safariAgent = userAgentString.indexOf('Safari') > -1;

    // Discard Safari since it also matches Chrome
    if (chromeAgent && safariAgent) {
      safariAgent = false;
    }

    // Detect Opera
    let operaAgent = userAgentString.indexOf('OP') > -1;

    // Discard Chrome since it also matches Opera
    if (chromeAgent && operaAgent) {
      chromeAgent = false;
    }

    if (IExplorerAgent) {
      this.setState({ browserName: 'ie' });
    }
    if (chromeAgent) {
      this.setState({ browserName: 'chrome' });
    }
    if (firefoxAgent) {
      this.setState({ browserName: 'firefox' });
    }
    if (safariAgent) {
      this.setState({ browserName: 'safari' });
    }
    if (operaAgent) {
      this.setState({ browserName: 'opera' });
    }
  }

  @autobind
  startRecording(e: React.MouseEvent<HTMLElement>) {
    e.preventDefault();
    this.resetState();

    if (!this.audioContext) {
      const audioCtx =
        this.state.browserName === 'safari'
          ? new window.webkitAudioContext()
          : new AudioContext();
      this.audioContext = audioCtx;
    }
    const audioNode = this.audioContext.createScriptProcessor(
      this.state.bufferSize,
      1,
      1,
    );
    audioNode.connect(this.audioContext.destination);

    this.audioNode = audioNode;
    this.recordingLength = 0;

    if (navigator.mediaDevices) {
      navigator.mediaDevices
        .getUserMedia({ audio: true })
        .then(this.onMicrophoneCaptured)
        .catch(this.onMicrophoneError);
    } else {
      this.setState({ mediaNotFound: true });
      // console.log('Media Devices will work only with SSL.....');
    }
  }

  onMicrophoneError(e: Error) {
    alert('Unable to access the microphone.');
  }

  @autobind
  onMicrophoneCaptured(stream: MediaStream) {
    this.stream = stream;
    const audioInput = this.audioContext.createMediaStreamSource(stream);
    audioInput.connect(this.audioNode);
    this.audioInput = audioInput;
    this.setState({ recording: true });
    this.audioNode.onaudioprocess = this.onAudioProcess;
  }

  @autobind
  onAudioProcess(e: AudioProcessingEvent) {
    if (!this.state.recording) {
      return;
    }
    if (!this.state.micOpen) {
      this.startTimer();
      this.setState({ micOpen: true });
    }
    this.recordedData.push(new Float32Array(e.inputBuffer.getChannelData(0)));
    this.recordingLength += this.state.bufferSize;
  }

  @autobind
  stopRecording(e: React.MouseEvent<HTMLElement>) {
    e.preventDefault();
    clearInterval(this.timer);

    // to make sure onaudioprocess stops firing
    this.stream.getTracks().forEach((track) => {
      track.stop();
    });
    this.audioInput.disconnect();
    this.audioNode.disconnect();

    this.setState({
      time: {},
      recording: false,
      micOpen: false,
    });
    this.saveAudio();
  }

  @autobind
  resetState() {
    this.setState({
      time: {},
      seconds: 0,
      isPaused: false,
      recording: false,
      micOpen: false,
      mediaNotFound: false,
      audioURL: '',
      audioBlob: null,
    });
    clearInterval(this.timer);
    this.recordedData = [];
  }

  @autobind
  handleReset() {
    this.resetState();
    this.props.handleReset(this.state);
  }

  @autobind
  saveAudio() {
    function joinBuffers(channelBuffer: Float32Array[], count: number) {
      let result = new Float64Array(count);
      let offset = 0;
      let lng = channelBuffer.length;

      for (let i = 0; i < lng; i++) {
        let channelbuffer = channelBuffer[i];
        result.set(channelbuffer, offset);
        offset += channelbuffer.length;
      }

      return result;
    }

    function writeUTFBytes(
      dataview: DataView,
      offset: number,
      identifier: string,
    ) {
      let lng = identifier.length;
      for (let i = 0; i < lng; i++) {
        dataview.setUint8(offset + i, identifier.charCodeAt(i));
      }
    }

    const sampleRate = this.audioContext.sampleRate;
    const data = joinBuffers(this.recordedData, this.recordingLength);
    const dataLength = data.length;

    // create wav file
    const buffer = new ArrayBuffer(44 + dataLength * 2);
    const view = new DataView(buffer);

    writeUTFBytes(view, 0, 'RIFF'); // RIFF chunk descriptor/identifier
    view.setUint32(4, 44 + dataLength * 2, true); // RIFF chunk length
    writeUTFBytes(view, 8, 'WAVE'); // RIFF type
    writeUTFBytes(view, 12, 'fmt '); // format chunk identifier, FMT sub-chunk
    view.setUint32(16, 16, true); // format chunk length
    view.setUint16(20, 1, true); // sample format (raw)
    view.setUint16(22, 1, true); // mono (1 channel)
    view.setUint32(24, sampleRate, true); // sample rate
    view.setUint32(28, sampleRate * 2, true); // byte rate (sample rate * block align)
    view.setUint16(32, 2, true); // block align (channel count * bytes per sample)
    view.setUint16(34, 16, true); // bits per sample
    writeUTFBytes(view, 36, 'data'); // data sub-chunk identifier
    view.setUint32(40, dataLength * 2, true); // data chunk length

    // write the PCM samples
    let index = 44;
    for (let i = 0; i < dataLength; i++) {
      view.setInt16(index, data[i] * 0x7fff, true);
      index += 2;
    }

    const blob = new Blob([view], { type: 'audio/wav' });
    const audioURL = window.URL.createObjectURL(blob);
    this.setState({ audioURL: audioURL, audioBlob: blob });
    this.props.handleAudioStop({
      url: audioURL,
      blob: blob,
      duration: this.state.time,
    });
  }

  render() {
    const { time, mediaNotFound } = this.state;
    const { audioURL } = this.props;

    return (
      <div className="recorder_library_box">
        <div className="recorder_box">
          <div className="recorder_box_inner">
            {!mediaNotFound ? (
              <div className="record_section">
                <div className="duration_section">
                  <div className="duration">
                    <span className="mins">
                      {time.m !== undefined
                        ? `${time.m <= 9 ? '0' + time.m : time.m}`
                        : '00'}
                    </span>
                    <span className="divider">:</span>
                    <span className="secs">
                      {time.s !== undefined
                        ? `${time.s <= 9 ? '0' + time.s : time.s}`
                        : '00'}
                    </span>
                  </div>
                  {!this.state.recording ? (
                    <p className="help">Press the microphone to record</p>
                  ) : null}
                </div>
                {this.state.recording && this.state.micOpen && (
                  <div className="open_mic">Open Mic</div>
                )}
                {!this.state.recording ? (
                  <a
                    onClick={(e) => this.startRecording(e)}
                    href=" #"
                    className="mic_icon"
                  >
                    <img
                      src={microphone}
                      width={30}
                      height={30}
                      alt="Microphone icons"
                    />
                  </a>
                ) : (
                  <div className="record_controller">
                    <a
                      onClick={(e) => this.stopRecording(e)}
                      href=" #"
                      className={`$"icons" $"stop"`}
                    >
                      <img
                        src={stopIcon}
                        width={20}
                        height={20}
                        alt="Stop icons"
                      />
                    </a>
                  </div>
                )}
                <div className="audio_section">
                  {audioURL &&
                  this.props.showUIAudio &&
                  !this.state.recording ? (
                    <audio controls={true}>
                      <source src={audioURL} type="audio/wav" />
                    </audio>
                  ) : null}
                </div>
              </div>
            ) : (
              <p
                style={{
                  color: '#fff',
                  marginTop: 30,
                  fontSize: 25,
                }}
              >
                Seems the site is Non-SSL
              </p>
            )}
          </div>
        </div>
      </div>
    );
  }

  @autobind
  private startTimer() {
    this.timer = setInterval(this.countDown, 1000);
  }

  @autobind
  private countDown() {
    // Remove one second, set state so a re-render happens.
    let seconds = this.state.seconds + 1;
    this.setState({
      time: this.secondsToTime(seconds),
      seconds: seconds,
    });
  }

  @autobind
  private secondsToTime(secs: number) {
    let hours = Math.floor(secs / (60 * 60));

    let divisorForMin = secs % (60 * 60);
    let minutes = Math.floor(divisorForMin / 60);

    let divisorForSec = divisorForMin % 60;
    let seconds = Math.ceil(divisorForSec);

    let obj = {
      h: hours,
      m: minutes,
      s: seconds,
    };
    return obj;
  }
}
