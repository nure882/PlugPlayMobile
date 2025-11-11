import {BrowserRouter, Routes, Route, Outlet } from 'react-router-dom';
import SignIn from './pages/SignIn';
import SignUp from './pages/SignUp';
import Catalog from './pages/Catalog';
import Profile from './pages/Profile';
import ProductDetail from './pages/ProductDetail';
import NotFound from './pages/NotFound';
import { GoogleOAuthProvider } from "@react-oauth/google";
import ProtectedRoute from './components/common/ProtectedRoute.tsx';
import Header from './components/common/Header.tsx';
import {ShoppingCartWrapper} from './components/common/ShoppingCartWrapper.tsx';
import { useAppDispatch } from './app/configureStore';
import { setSelectedCategory } from './app/slices/filterSlice';

const MainLayout = () => {
  const dispatch = useAppDispatch();

  const handleCategorySelect = (categoryId: number | null) => {
    dispatch(setSelectedCategory(categoryId));
  };

  return (
    <>
      <Header onCategorySelect={handleCategorySelect}/>
      <main>
        <Outlet />
      </main>
    </>
  );
};

function App() {
  return (
    <GoogleOAuthProvider clientId="103104858818-t81fh5qs9t74135p630o4kkd7nbkiaj4.apps.googleusercontent.com">
      <BrowserRouter>
        <ShoppingCartWrapper/>

        <Routes>
          <Route element={<MainLayout/>}>
            <Route path="/" element={<Catalog/>}/>
            <Route path="/product/:id" element={<ProductDetail/>}/>
            <Route path="/profile" element={
              <ProtectedRoute>
                <Profile/>
              </ProtectedRoute>
            }/>
            <Route path="/signin" element={<SignIn/>}/>
            <Route path="/signup" element={<SignUp/>}/>
            <Route path="*" element={<NotFound/>}/>
          </Route>
        </Routes>
      </BrowserRouter>
    </GoogleOAuthProvider>
  );
}

export default App;
