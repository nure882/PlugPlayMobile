import { ShoppingCart, User } from 'lucide-react';
import logoUrl from '../lib/logo.svg';

export default function Header() {
  return (
    <header className="bg-white border-b border-gray-200 sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          <div className="flex items-center">
            <a href="/" className="flex items-center gap-2">
              <img src={logoUrl} alt="Plug&Play logo" className="h-8 w-8" />
              <span className="text-2xl font-bold text-black">Plug&Play</span>
            </a>
          </div>

          <nav className="hidden md:flex items-center space-x-8">
            <a href="/" className="text-gray-700 hover:text-black transition-colors">
              Home
            </a>
            <a href="/products" className="text-gray-700 hover:text-black transition-colors">
              Products
            </a>
            <a href="/about" className="text-gray-700 hover:text-black transition-colors">
              About
            </a>
            <a href="/contact" className="text-gray-700 hover:text-black transition-colors">
              Contact
            </a>
          </nav>

          <div className="flex items-center space-x-4">
            <button className="p-2 text-gray-700 hover:text-black transition-colors">
              <ShoppingCart className="w-6 h-6" />
            </button>

            <button className="p-2 text-gray-700 hover:text-black transition-colors">
              <User className="w-6 h-6" />
            </button>

            <a
              href="/signin"
              className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors font-medium"
            >
              Sign In
            </a>
          </div>
        </div>
      </div>
    </header>
  );
}
