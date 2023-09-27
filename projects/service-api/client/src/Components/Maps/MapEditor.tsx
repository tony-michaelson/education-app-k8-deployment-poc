import { Component } from 'react';
import * as React from 'react';
import { RouteComponentProps } from 'react-router';
import { HashRouter, Route } from 'react-router-dom';
import { API_CONFIG } from '../../api/ApiConfig';
import BootBoxModal from './BootBoxModal';
import FlashCardModal from './FlashCards/FlashCardModal';
import ContentModal from './Content/ContentModal';
import { PrivateRoute } from '../../utils/Routes';
import { RootState } from 'src/redux';
import {
  setMindMap,
  clearMindMap,
  requestMindMapReload,
} from 'src/redux/modules/mindmap';
import { connect } from 'react-redux';
import MapSettings from './MapSettings';
import TrainingScreen from '../Training/TrainingScreen';
import { Button, Breadcrumb, BreadcrumbItem } from 'reactstrap';
import autobind from 'autobind-decorator';
import { Node } from 'src/api/models';
import './Maps.css';
import { MapPermissions } from 'src/types';
import { MapRights } from './MapRights';
import MapPreview from './MapPreview';

declare global {
  interface Window {
    legacyInit: Function;
    runDeck: Function;
    deck_id: string;
    addNode: Function;
    mapC: Object;
  }
}

window.legacyInit = window.legacyInit || {};

const mapStateToProps = (state: RootState) => ({
  accessToken: state.member.accessToken,
  mapID: state.mindmap.mapID,
  segmentID: state.mindmap.segmentID,
  breadcrumb: state.mindmap.breadcrumb,
  reloadMap: state.mindmap.reloadMap,
});

const mapDispatchToProps = { setMindMap, clearMindMap, requestMindMapReload };

type Props = ReturnType<typeof mapStateToProps> & typeof mapDispatchToProps;

class UnconnectedMapEditor extends Component<Props & RouteComponentProps, {}> {
  mapJSON: string;

  componentDidMount() {
    // tslint:disable-next-line: no-any
    const { mapID, segmentID } = this.props.match.params as any;
    window.mpio.getMapBreadcrumb(mapID, segmentID, (nodes) => {
      window.mapC = this;
      window.legacyInit(
        mapID,
        segmentID,
        API_CONFIG,
        (permissions: MapPermissions) => {
          this.props.setMindMap(mapID, segmentID, nodes, permissions);
        },
      );
    });
  }

  componentDidUpdate(prevProps: Props) {
    if (this.props.reloadMap) {
      window.refreshMap();
      this.props.requestMindMapReload(false);
    }
  }

  componentWillUnmount() {
    this.props.clearMindMap();
  }

  @autobind
  openTrainingScreen() {
    location.href =
      '/maps/' + this.props.mapID + '/' + this.props.segmentID + '/#/train';
  }

  render() {
    // tslint:disable-next-line: no-any
    const { mapID, segmentID } = this.props.match.params as any;

    const breadcrumb = (() => {
      if (this.props.breadcrumb) {
        const breadcrumbItems = this.props.breadcrumb.map((node: Node) => {
          const link =
            '/maps/' + node.mapID + '/' + node.segmentID + '/#/editor';
          const active = node.segmentID === segmentID ? true : false;
          return (
            <BreadcrumbItem key={node.id} active={active}>
              {!active ? <a href={link}>{node.name}</a> : node.name}
            </BreadcrumbItem>
          );
        });
        return <Breadcrumb>{breadcrumbItems}</Breadcrumb>;
      } else {
        return <></>;
      }
    })();

    return (
      <HashRouter>
        <PrivateRoute path="/settings" component={MapSettings} mapID={mapID} />
        <PrivateRoute path="/rights" component={MapRights} mapID={mapID} />
        <PrivateRoute path="/preview" component={MapPreview} mapID={mapID} />
        <PrivateRoute
          path="/cards/:nodeID/edit"
          component={FlashCardModal}
          mode="edit"
          mapID={mapID}
          segmentID={segmentID}
        />
        <PrivateRoute
          path="/content/:nodeID/edit"
          mapID={mapID}
          segmentID={segmentID}
          component={ContentModal}
        />
        <PrivateRoute
          path="/train"
          mapID={mapID}
          segmentID={segmentID}
          component={TrainingScreen}
          mapView={true}
        />
        <PrivateRoute
          path="/bb/:message"
          header="Alert"
          component={BootBoxModal}
        />
        {breadcrumb}
        <div id="mapContainer" />
        <Route
          path="/editor"
          render={(props) => {
            return (
              <nav className="navbar fixed-bottom navbar-light bg-light container-fluid">
                <div>
                  <Button
                    onClick={() => this.openTrainingScreen()}
                    color="success"
                    type="button"
                    className="beginTrainingBtn"
                  >
                    Begin Training
                  </Button>
                </div>
                <div>
                  <Button
                    id="addCategoryBtn"
                    onClick={() => window.addNode('category')}
                    type="button"
                    className="addCategoryBtn"
                  >
                    Add Concept
                  </Button>
                  <Button
                    onClick={() => window.addNode('flashcard')}
                    type="button"
                    className="addFlashCardBtn"
                  >
                    Add Flashcard
                  </Button>
                </div>
              </nav>
            );
          }}
        />
      </HashRouter>
    );
  }
}

const MapEditor = connect(
  mapStateToProps,
  mapDispatchToProps,
)(UnconnectedMapEditor);

export default MapEditor;
