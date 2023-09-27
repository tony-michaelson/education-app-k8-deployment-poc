import React, { CSSProperties } from 'react';
const loadingSpinner = require('src/resources/loadingSpinner.svg');

const style: CSSProperties = {
  position: 'fixed',
  display: 'flex',
  justifyContent: 'center',
  height: '100vh',
  width: '100vw',
  top: 0,
  bottom: 0,
  left: 0,
  right: 0,
  zIndex: 999999,
};

export const LoadingSpinner = () => {
  return (
    <div style={style} className="modal-backdrop fade show">
      <img
        style={{ position: 'absolute', top: '30%' }}
        width="250px"
        height="250px"
        src={loadingSpinner}
        alt="loading"
      />
    </div>
  );
};
