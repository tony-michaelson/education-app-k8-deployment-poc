import React from 'react';
import ReactDOM from 'react-dom';
import registerServiceWorker from './registerServiceWorker';
import Routes from './utils/Routes';
import { Provider } from 'react-redux';
import { createStore } from 'redux';
import { rootReducer } from './redux';
import './index.css';
import 'bootstrap/dist/css/bootstrap.css';

export const store = createStore(rootReducer);

const content = (
  <Provider store={store}>
    <Routes />
  </Provider>
);

ReactDOM.render(content, document.getElementById('root') as HTMLElement);
registerServiceWorker();
