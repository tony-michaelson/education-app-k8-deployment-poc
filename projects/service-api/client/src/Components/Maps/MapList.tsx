import React, { Component } from 'react';
import MapMedia from './MapMedia';
import { MindMap } from 'src/api/models';
import { Container, Nav, NavItem, NavLink } from 'reactstrap';
import { MdAddCircle } from 'react-icons/md';
import { LoadingSpinner } from '../Animations/LoadingSpinner';

export interface MapListProps {}

export default class MapList extends Component<MapListProps, {}> {
  maps: MindMap[] = [];

  state = {
    maps: this.maps,
    loading: true,
    error: false,
  };

  componentDidMount() {
    window.mpio.getMyMaps(
      (maps) => {
        this.setState({
          maps: maps,
          loading: false,
        });
      },
      (error) => {
        this.setState({
          loading: false,
          error: true,
        });
      },
    );
  }

  render() {
    const { maps, loading, error } = this.state;
    const newMapButton = (
      <Nav className="mt-3" pills={true} style={{ height: '55px' }}>
        <NavItem>
          <NavLink
            href="/maps/new"
            style={{ backgroundColor: 'orange', color: 'white' }}
          >
            <MdAddCircle className="mr-1" size="25" color="white" />
            <span className="align-middle">New Map</span>
          </NavLink>
        </NavItem>
      </Nav>
    );

    if (loading && !error) {
      return <LoadingSpinner />;
    } else if (error) {
      // TODO - handle errors
      return (
        <div className="jumbotron">
          <div className="container">
            <p>An error occurred</p>
          </div>
        </div>
      );
    }

    if (maps.length === 0) {
      return (
        <div className="jumbotron">
          <div className="container">
            <p>You don't have any maps, click here to get started.</p>
            {newMapButton}
          </div>
        </div>
      );
    } else {
      return (
        <Container fluid="lg">
          {newMapButton}
          {maps.map((m) => (
            <MapMedia
              key={m.id}
              id={m.id}
              name={m.name}
              icon={m.icon}
              description={m.description}
            />
          ))}
        </Container>
      );
    }
  }
}
