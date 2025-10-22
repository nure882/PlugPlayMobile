import {BrowserRouter, Routes, Route} from 'react-router-dom';
import SignIn from './pages/SignIn';
import SignUp from './pages/SignUp';
import Catalog from './pages/Catalog';
import Profile from './pages/Profile';
import NotFound from './pages/NotFound';
import {GoogleOAuthProvider} from "@react-oauth/google";
import ProtectedRoute from './components/ProtectedRoute';

function App() {
  return (
    <GoogleOAuthProvider clientId="103104858818-t81fh5qs9t74135p630o4kkd7nbkiaj4.apps.googleusercontent.com">
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Catalog />} />
          <Route path="/signin" element={<SignIn />} />
          <Route path="/signup" element={<SignUp />} />
          <Route path="/profile" element={
            <ProtectedRoute>
              <Profile />
            </ProtectedRoute>
          } />
          <Route path="*" element={<NotFound />} />
        </Routes>
      </BrowserRouter>
    </GoogleOAuthProvider>
  );
}

export default App;