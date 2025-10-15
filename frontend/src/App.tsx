import { useState, useEffect } from 'react';
import SignIn from './pages/SignIn';
import SignUp from './pages/SignUp';

function App() {
  const [currentPath, setCurrentPath] = useState(window.location.pathname);

  useEffect(() => {
    const handleLocationChange = () => {
      setCurrentPath(window.location.pathname);
    };

    window.addEventListener('popstate', handleLocationChange);

    const originalPushState = window.history.pushState;
    window.history.pushState = function(...args) {
      originalPushState.apply(window.history, args);
      handleLocationChange();
    };

    return () => {
      window.removeEventListener('popstate', handleLocationChange);
      window.history.pushState = originalPushState;
    };
  }, []);

  switch (currentPath) {
    case '/signin':
      return <SignIn />;
    case '/signup':
      return <SignUp />;
    default:
      return <SignIn />;
  }
}

export default App;
