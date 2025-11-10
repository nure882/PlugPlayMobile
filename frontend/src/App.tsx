import {BrowserRouter, Routes, Route, Outlet, useSearchParams} from 'react-router-dom';
import {useState, useEffect} from 'react';
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

const MainLayout = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  
  const categoryParam = searchParams.get('category');
  const [selectedCategory, setSelectedCategory] = useState<number | null>(
    categoryParam ? parseInt(categoryParam, 10) : null
  );

  useEffect(() => {
    const params = new URLSearchParams(searchParams);
    
    if (selectedCategory !== null) {
      params.set('category', selectedCategory.toString());
    } else {
      params.delete('category');
    }
    
    setSearchParams(params, { replace: true });
  }, [selectedCategory]);

  const handleCategorySelect = (categoryId: number | null) => {
    setSelectedCategory(prev => prev === categoryId ? null : categoryId);
  };

  return (
    <>
      <Header onCategorySelect={handleCategorySelect}/>
      <main>
        <Outlet context={{selectedCategory, onCategorySelect: handleCategorySelect}}/>
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
