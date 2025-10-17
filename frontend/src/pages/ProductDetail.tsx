import { useState } from 'react';
import { useParams } from 'react-router-dom';
import { Heart, ShoppingCart } from 'lucide-react';
import Header from '../components/Header';
import ProductGallery from '../components/ProductGallery';
import DeliveryInfo from '../components/DeliveryInfo';
import { getProductById } from '../data/products';

const ProductDetail = () => {
  const { id } = useParams<{ id: string }>();
  const product = id ? getProductById(id) : undefined;

  const [selectedColor, setSelectedColor] = useState(0);
  const [selectedMemory, setSelectedMemory] = useState(0);
  const [isFavorite, setIsFavorite] = useState(false);

  if (!product) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Header />
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <p className="text-center text-gray-600">Product not found</p>
        </div>
      </div>
    );
  }

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('uk-UA').format(price);
  };

  const calculateDiscount = () => {
    if (!product.originalPrice) return 0;
    return Math.round(((product.originalPrice - product.price) / product.originalPrice) * 100);
  };

  const renderStars = (rating: number) => {
    return Array.from({ length: 5 }, (_, i) => (
      <span
        key={i}
        className={`text-lg ${
          i < Math.floor(rating) ? 'text-yellow-400' : 'text-gray-300'
        }`}
      >
        ★
      </span>
    ));
  };

  const handleBuy = () => {
    console.log('Buy product:', {
      productId: product.id,
      color: product.colors?.[selectedColor],
      memory: product.memory?.[selectedMemory],
    });
  };

  const handleAddToCart = () => {
    console.log('Add to cart:', {
      productId: product.id,
      color: product.colors?.[selectedColor],
      memory: product.memory?.[selectedMemory],
    });
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-12">
          <div>
            <ProductGallery images={product.images} productName={product.name} />
          </div>

          <div className="flex flex-col gap-6">
            <div>
              <h1 className="text-3xl font-bold text-gray-900 mb-3">
                {product.name}
              </h1>

              <div className="flex items-center gap-2 mb-2">
                <div className="flex items-center">
                  {renderStars(product.rating)}
                </div>
                <span className="text-gray-600">
                  {product.rating} ({product.reviewCount} reviews)
                </span>
              </div>

              <p className="text-sm text-gray-600">
                Product code: {product.productCode}
              </p>
            </div>

            <div className="flex items-baseline gap-3">
              <span className="text-4xl font-bold text-gray-900">
                {formatPrice(product.price)} ₴
              </span>
              {product.originalPrice && (
                <>
                  <span className="text-xl text-gray-500 line-through">
                    {formatPrice(product.originalPrice)} ₴
                  </span>
                  <span className="bg-red-500 text-white px-2 py-1 rounded text-sm font-bold">
                    -{calculateDiscount()}%
                  </span>
                </>
              )}
            </div>

            {product.inStock && (
              <div className="flex items-center gap-2">
                <div className="w-2 h-2 bg-green-500 rounded-full"></div>
                <span className="text-sm text-gray-700 font-medium">In stock</span>
              </div>
            )}

            {product.colors && product.colors.length > 0 && (
              <div>
                <label className="block text-sm font-medium text-gray-900 mb-2">
                  Color: {product.colors[selectedColor].label}
                </label>
                <div className="flex gap-2">
                  {product.colors.map((color, index) => (
                    <button
                      key={index}
                      onClick={() => setSelectedColor(index)}
                      className={`w-12 h-12 rounded-full border-2 transition-all ${
                        selectedColor === index
                          ? 'border-blue-600 ring-2 ring-blue-200'
                          : 'border-gray-300 hover:border-gray-400'
                      }`}
                      style={{ backgroundColor: color.hex }}
                      title={color.label}
                    />
                  ))}
                </div>
              </div>
            )}

            {product.memory && product.memory.length > 0 && (
              <div>
                <label className="block text-sm font-medium text-gray-900 mb-2">
                  Memory
                </label>
                <div className="flex gap-2">
                  {product.memory.map((mem, index) => (
                    <button
                      key={index}
                      onClick={() => setSelectedMemory(index)}
                      className={`px-6 py-2 rounded-lg border-2 font-medium transition-all ${
                        selectedMemory === index
                          ? 'border-blue-600 bg-blue-50 text-blue-700'
                          : 'border-gray-300 bg-white text-gray-700 hover:border-gray-400'
                      }`}
                    >
                      {mem.label}
                    </button>
                  ))}
                </div>
              </div>
            )}

            <div className="flex gap-3">
              <button
                onClick={handleBuy}
                className="flex-1 bg-blue-600 text-white px-6 py-3 rounded-lg hover:bg-blue-700 transition-colors font-semibold flex items-center justify-center gap-2"
              >
                <ShoppingCart className="w-5 h-5" />
                Buy
              </button>
              
              <button
                onClick={() => setIsFavorite(!isFavorite)}
                className={`p-3 rounded-lg border-2 transition-all ${
                  isFavorite
                    ? 'bg-red-50 border-red-500 text-red-500'
                    : 'bg-white border-gray-300 text-gray-700 hover:border-gray-400'
                }`}
              >
                <Heart
                  className="w-6 h-6"
                  fill={isFavorite ? 'currentColor' : 'none'}
                />
              </button>
            </div>

            <button
              onClick={handleAddToCart}
              className="w-full bg-white text-gray-900 px-6 py-3 rounded-lg border-2 border-gray-300 hover:bg-gray-50 transition-colors font-semibold"
            >
              Add to cart
            </button>

            <DeliveryInfo deliveryOptions={product.deliveryOptions} />
          </div>
        </div>

        <div className="bg-white border border-gray-200 rounded-lg p-8">
          <h2 className="text-2xl font-bold text-gray-900 mb-6">
            Product description
          </h2>
          
          <p className="text-gray-700 leading-relaxed mb-8">
            {product.description}
          </p>

          <h3 className="text-xl font-semibold text-gray-900 mb-4">
            Key specifications:
          </h3>
          
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {Object.entries(product.specifications).map(([key, value]) => (
              <div
                key={key}
                className="flex justify-between items-center py-3 px-4 bg-gray-50 rounded-lg"
              >
                <span className="font-medium text-gray-700">{key}:</span>
                <span className="text-gray-900">{value}</span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

export default ProductDetail;