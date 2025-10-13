import { useState } from 'react';
import { Filter } from 'lucide-react';
import ProductCard from '../components/ProductCard';
import Header from '../components/Header';
import { mockProducts, Product } from '../data/products';

const Catalog = () => {
  const [products, setProducts] = useState<Product[]>(mockProducts);

  const handleToggleFavorite = (productId: string) => {
    setProducts(prevProducts =>
      prevProducts.map(product =>
        product.id === productId
          ? { ...product, isFavorite: !product.isFavorite }
          : product
      )
    );
  };

  const handleProductClick = (productId: string) => {
    // Здесь можно добавить логику перехода на страницу товара
    console.log('Clicked product:', productId);
    // Например: navigate(`/product/${productId}`);
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Header */}
        <div className="flex justify-between items-center mb-8">
          <h1 className="text-2xl font-bold text-gray-900">Для тебе</h1>
          <button className="flex items-center gap-2 px-4 py-2 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors duration-200">
            <Filter size={16} />
            <span>Фільтри</span>
          </button>
        </div>

        {/* Products Grid */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          {products.map(product => (
            <ProductCard
              key={product.id}
              {...product}
              onToggleFavorite={handleToggleFavorite}
              onClick={handleProductClick}
            />
          ))}
        </div>

        {/* Load More Button */}
        <div className="flex justify-center mt-12">
          <button className="px-8 py-3 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors duration-200 font-medium">
            Показати більше
          </button>
        </div>
      </div>
    </div>
  );
};

export default Catalog;
