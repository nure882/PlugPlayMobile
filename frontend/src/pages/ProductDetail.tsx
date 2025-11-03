import {useState} from 'react';
import {useParams} from 'react-router-dom';
import {Heart, ShoppingCart, Loader2, Package, Truck, Shield, RotateCcw} from 'lucide-react';
import {useGetProductByIdQuery} from '../api/productsApi.ts';
import ProductImageGallery from "../components/products/ProductImageGallery.tsx";

const ProductDetail = () => {
  const {id} = useParams<{ id: string }>();
  const productId = id ? parseInt(id, 10) : 0;

  const {
    data,
    isLoading,
    error,
    isError
  } = useGetProductByIdQuery(productId, {
    skip: !productId || isNaN(productId)
  });

  const [isFavorite, setIsFavorite] = useState(false);

  const product = data;

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="flex items-center justify-center min-h-[400px]">
            <div className="flex flex-col items-center gap-4">
              <Loader2 className="w-8 h-8 animate-spin text-blue-600"/>
              <p className="text-gray-600">Loading product...</p>
            </div>
          </div>
        </div>
      </div>
    );
  }

  if (isError || !product) {
    return (
      <div className="min-h-screen bg-gray-50">
        
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="flex items-center justify-center min-h-[400px]">
            <div className="text-center">
              <h2 className="text-2xl font-bold text-gray-900 mb-2">Product not found</h2>
              <p className="text-gray-600 mb-4">
                {error ? 'Error loading product' : 'The product you are looking for does not exist.'}
              </p>
              <button
                onClick={() => window.history.back()}
                className="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700 transition-colors"
              >
                Go Back
              </button>
            </div>
          </div>
        </div>
      </div>
    );
  }

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('uk-UA').format(price);
  };

  const handleBuy = () => {
    console.log('Buy product:', {
      productId: product.id,
    });
  };

  const handleAddToCart = () => {
    console.log('Add to cart:', {
      productId: product.id,
    });
  };

  // Mock delivery options
  const deliveryOptions = [
    {
      icon: 'Truck',
      title: 'Fast delivery',
      description: 'Delivery to Kyiv on the next day',
    },
    {
      icon: 'Shield',
      title: '1 year warranty',
      description: 'Official manufacturer warranty',
    },
    {
      icon: 'RotateCcw',
      title: 'Return within 14 days',
      description: 'Ability to return the product',
    },
    {
      icon: 'Package',
      title: 'Safe packaging',
      description: 'Reliable protection during delivery',
    },
  ];

  const iconMap = {
    Truck,
    Shield,
    RotateCcw,
    Package,
  };

  return (
    <div className="min-h-screen bg-gray-50">
      

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-12">
          {/* Product Image Gallery */}
          <div className="bg-white rounded-lg p-8">
            <ProductImageGallery
              images={product.pictureUrls ?? []}
              initialIndex={0}
              altPrefix={product.name}
              className=""
            />
          </div>

          {/* Product Info */}
          <div className="flex flex-col gap-6">
            <div>
              <h1 className="text-3xl font-bold text-gray-900 mb-3">
                {product.name}
              </h1>

              {product.category && (
                <p className="text-sm text-gray-600 mb-2">
                  Category: {product.category.name}
                </p>
              )}
            </div>

            <div className="flex items-baseline gap-3">
                <span className="text-4xl font-bold text-gray-900">
                  {formatPrice(product.price)} ₴
                </span>
            </div>

            <div className="flex items-center gap-2">
              <div
                className={`w-2 h-2 rounded-full ${product.stockQuantity > 0 ? 'bg-green-500' : 'bg-red-500'}`}></div>
              <span className="text-sm text-gray-700 font-medium">
                  {product.stockQuantity > 0 ? `In stock (${product.stockQuantity} items)` : 'Out of stock'}
                </span>
            </div>

            <div className="flex gap-3">
              <button
                onClick={handleBuy}
                disabled={product.stockQuantity === 0}
                className="flex-1 bg-blue-600 text-white px-6 py-3 rounded-lg hover:bg-blue-700 transition-colors font-semibold flex items-center justify-center gap-2 disabled:bg-gray-400 disabled:cursor-not-allowed"
              >
                <ShoppingCart className="w-5 h-5"/>
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
              disabled={product.stockQuantity === 0}
              className="w-full bg-white text-gray-900 px-6 py-3 rounded-lg border-2 border-gray-300 hover:bg-gray-50 transition-colors font-semibold disabled:bg-gray-100 disabled:text-gray-400 disabled:cursor-not-allowed"
            >
              Add to cart
            </button>

            {/* Delivery Info */}
            <div className="bg-white border border-gray-200 rounded-lg p-6">
              <h3 className="text-lg font-semibold text-gray-900 mb-4">
                Delivery and warranty
              </h3>

              <div className="space-y-4">
                {deliveryOptions.map((option, index) => {
                  const IconComponent = iconMap[option.icon as keyof typeof iconMap];

                  return (
                    <div key={index} className="flex items-start gap-3">
                      <div className="flex-shrink-0 w-10 h-10 bg-blue-50 rounded-lg flex items-center justify-center">
                        <IconComponent className="w-5 h-5 text-blue-600"/>
                      </div>

                      <div>
                        <h4 className="font-medium text-gray-900 text-sm">
                          {option.title}
                        </h4>
                        <p className="text-sm text-gray-600 mt-0.5">
                          {option.description}
                        </p>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          </div>
        </div>

        {/* Description */}
        <div className="bg-white border border-gray-200 rounded-lg p-8">
          <h2 className="text-2xl font-bold text-gray-900 mb-6">
            Description
          </h2>

          <p className="text-gray-700 leading-relaxed">
            {product.description}
          </p>
        </div>
      </div>
    </div>
  );
};

export default ProductDetail;