import React, { Component } from 'react';
import { Media, ButtonGroup, Col, Row } from 'reactstrap';
import { Link } from 'react-router-dom';
import { FaBrain } from 'react-icons/fa';

export interface MapCardProps {
  id: string;
  icon?: string;
  name: string;
  description?: string;
}
export default class Home extends Component<MapCardProps, {}> {
  render() {
    return (
      <div className="p-2">
        <Media>
          <Media left={true} href="#">
            {this.props.icon && (
              <img
                className="p-1"
                height="150px"
                width="150px"
                src={this.props.icon}
              />
            )}
            {!this.props.icon && (
              <FaBrain className="p-1" color="orange" size="150" />
            )}
          </Media>
          <Media className="ml-4" body={true}>
            <Col>
              <Row>
                <Media heading={true}>{this.props.name}</Media>
              </Row>
              <Row className="overflow-hidden" style={{ height: '70px' }}>
                {this.props.description}
              </Row>
              <Row>
                <ButtonGroup className="mt-2">
                  <Link
                    to={
                      '/maps/' +
                      this.props.id +
                      '/' +
                      this.props.id +
                      '/#/train'
                    }
                    className="btn btn-sm btn-outline-secondary"
                  >
                    Start Training
                  </Link>
                  <Link
                    to={
                      '/maps/' +
                      this.props.id +
                      '/' +
                      this.props.id +
                      '/#/editor'
                    }
                    className="btn btn-sm btn-outline-secondary"
                  >
                    Edit
                  </Link>
                </ButtonGroup>
              </Row>
            </Col>
          </Media>
        </Media>
        <hr />
      </div>
    );
  }
}

//  <div className="col-md-4">
//    <div className="card mb-4 shadow-sm">
//      <div className="card-body">
//        <p className="card-text">{this.props.name}</p>
//        <p className="card-text">{this.props.description}</p>
//        <div className="d-flex justify-content-between align-items-center">
//          <div className="btn-group">
//            <Link
//              to={
//                '/maps/' + this.props.id + '/' + this.props.id + '/#/editor'
//              }
//              className="btn btn-sm btn-outline-secondary"
//            >
//              View
//            </Link>
//            <button
//              type="button"
//              className="btn btn-sm btn-outline-secondary"
//            >
//              Edit
//            </button>
//          </div>
//          <small className="text-muted">9 mins</small>
//        </div>
//      </div>
//    </div>
//  </div>;
