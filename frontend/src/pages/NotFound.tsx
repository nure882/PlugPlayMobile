import { Link } from 'react-router-dom';
import { Home, ArrowLeft } from 'lucide-react';
import Header from '../components/Header';

export default function NotFound() {
  return (
    <div className="min-h-screen bg-white">
      <Header />
      <div className="flex items-center justify-center px-4 py-20">
        <div className="text-center max-w-md">
          <div className="mb-8">
            <h1 className="text-9xl font-bold text-gray-200">404</h1>
            <h2 className="text-2xl font-semibold text-black mb-4">Page Not Found</h2>
            <p className="text-gray-600 mb-8">
              The page you're looking for doesn't exist or has been moved.
            </p>
          </div>
          
          <div className="space-y-4">
            <Link
              to="/"
              className="inline-flex items-center gap-2 bg-blue-600 text-white px-6 py-3 rounded-lg hover:bg-blue-700 transition-colors font-medium"
            >
              <Home className="w-5 h-5" />
              Go Home
            </Link>
            
            <div>
              <button
                onClick={() => window.history.back()}
                className="inline-flex items-center gap-2 text-gray-600 hover:text-black transition-colors font-medium"
              >
                <ArrowLeft className="w-4 h-4" />
                Go Back
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
