import React, { useEffect, useState } from "react";

const ErrorPopup = ({ error }) => {
  const [visible, setVisible] = useState(!!error);

  useEffect(() => {
    if (error) {
      setVisible(true);
      const timer = setTimeout(() => setVisible(false), 5000);
      return () => clearTimeout(timer);
    }
  }, [error]);

  if (!visible || !error) return null;

  return (
    <div className="popup-error">
      <div className="popup-content">
        <span className="close" onClick={() => setVisible(false)}>
          &times;
        </span>
        <p>{error}</p>
      </div>
    </div>
  );
};

export default ErrorPopup;
