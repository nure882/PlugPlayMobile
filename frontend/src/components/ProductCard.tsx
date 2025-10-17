import { Heart } from 'lucide-react';

interface ProductCardProps {
  id: string;
  name: string;
  price: number;
  originalPrice?: number;
  rating: number;
  reviewCount: number;
  image: string;
  badge?: 'NEW' | 'SALE';
  isFavorite: boolean;
  onToggleFavorite: (id: string) => void;
  onClick?: (id: string) => void;
}

const ProductCard = ({
  id,
  name,
  price,
  originalPrice,
  rating,
  reviewCount,
  image,
  badge,
  isFavorite,
  onToggleFavorite,
  onClick
}: ProductCardProps) => {
  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('uk-UA').format(price);
  };

  const renderStars = (rating: number) => {
    return Array.from({ length: 5 }, (_, i) => (
      <span
        key={i}
        className={`text-sm ${
          i < Math.floor(rating) ? 'text-yellow-400' : 'text-gray-300'
        }`}
      >
        ★
      </span>
    ));
  };

  const getBadgeColor = (badge: string) => {
    switch (badge) {
      case 'NEW':
        return 'bg-green-500';
      case 'SALE':
        return 'bg-red-500';
      default:
        return 'bg-gray-500';
    }
  };

  const handleCardClick = (e: React.MouseEvent) => {
    // Если клик был на кнопке избранного, не переходим на страницу товара
    if ((e.target as HTMLElement).closest('button')) {
      return;
    }
    if (onClick) {
      onClick(id);
    }
  };

  return (
    <div 
      className="bg-white rounded-lg shadow-lg border-2 border-gray-200 overflow-hidden hover:shadow-xl hover:border-gray-300 transition-all duration-300 cursor-pointer group"
      onClick={handleCardClick}
    >
      <div className="relative aspect-square">
        <img
          src={image}
          alt={name}
          className="w-full h-full object-cover"
        />
        
        {badge && (
          <div className={`absolute top-2 left-2 px-3 py-1 rounded-md text-white text-xs font-bold shadow-md ${getBadgeColor(badge)}`}>
            {badge}
          </div>
        )}
        
        <button
          onClick={(e) => {
            e.stopPropagation();
            onToggleFavorite(id);
          }}
          className={`absolute top-2 right-2 p-2 rounded-full transition-all duration-200 opacity-0 group-hover:opacity-100 shadow-md ${
            isFavorite
              ? 'bg-red-500 text-white opacity-100'
              : 'bg-white text-gray-600 hover:bg-gray-50 border border-gray-200'
          }`}
        >
          <Heart size={16} fill={isFavorite ? 'currentColor' : 'none'} />
        </button>
      </div>
      
      <div className="p-4">
        <h3 className="font-medium text-gray-900 mb-2 line-clamp-2">
          {name}
        </h3>
        
        <div className="flex items-center gap-1 mb-2">
          <div className="flex items-center">
            {renderStars(rating)}
          </div>
          <span className="text-sm text-gray-600 ml-1">
            {rating} ({reviewCount})
          </span>
        </div>
        
        <div className="flex items-center gap-2">
          <span className="text-lg font-bold text-gray-900">
            {formatPrice(price)} ₴
          </span>
          {originalPrice && (
            <span className="text-sm text-gray-500 line-through">
              {formatPrice(originalPrice)} ₴
            </span>
          )}
        </div>
      </div>
    </div>
  );
};

export default ProductCard;
