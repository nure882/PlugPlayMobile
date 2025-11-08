import {useNavigate} from 'react-router-dom';
import {useState, useEffect} from 'react';
import ProductCard from '../components/products/ProductCard';
import DynamicFiltersSidebar, {
  DynamicFilters,
  PriceRange,
  SortOption,
} from '../components/products/DynamicFilterSidebar';
import {Loader2} from 'lucide-react';
import {useProductsService} from '../features/products/ProductsService.ts';
import { useAppDispatch, useAppSelector } from '../app/configureStore.ts';
import { setAttributeFilters, setPriceRange, setSortOption } from '../app/slices/filterSlice.ts';


const Catalog = () => {
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  
  // Get filter state from Redux
  const {
    selectedCategory,
    priceRange = { min: 0, max: 5000 },
    attributeFilters = {},
    sortOption = { value: 'price-asc', label: 'Price (Low to High)' },
  } = useAppSelector((state) => state.filter || {});

  const [favoriteIds, setFavoriteIds] = useState<Set<number>>(new Set());
  const [visibleCount, setVisibleCount] = useState(20);

  const {products, attributeGroups, isLoading, isError, refetch} = useProductsService({
    categoryId: selectedCategory,
    minPrice: priceRange.min,
    maxPrice: priceRange.max,
    attributeFilters: attributeFilters,
    sort: sortOption.value,
    page: 1,
    pageSize: 100,
  });

  useEffect(() => {
    if (typeof refetch === 'function') {
      refetch();
    }
  }, [sortOption.value, attributeFilters, priceRange, refetch]);
  
  // useEffect(() => {
  //   if (products && products.length > 0) {
  //     const sortedProducts = [...products].sort((a, b) => a.price - b.price);
  //     const newMin = sortedProducts[0]?.price || 0;
  //     const newMax = sortedProducts[sortedProducts.length - 1]?.price || 5000;
      
  //     // Only update if significantly different to avoid infinite loops
  //     if (Math.abs(priceRange.min - newMin) > 0.01 || Math.abs(priceRange.max - newMax) > 0.01) {
  //       dispatch(setPriceRange({ min: newMin, max: newMax }));
  //     }
  //   }
  // }, [products]);

  useEffect(() => {
    setVisibleCount(20);
  }, [selectedCategory]);

  const handleSetFilters = (filters: DynamicFilters) => {
    dispatch(setAttributeFilters(filters));
  };

  const handleSetPriceRange = (range: PriceRange) => {
    dispatch(setPriceRange(range));
  };

  const handleSetSortOption = (option: SortOption) => {
    dispatch(setSortOption(option));
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50">
        <div className="flex items-center justify-center min-h-[400px]">
          <div className="flex flex-col items-center gap-4">
            <Loader2 className="w-8 h-8 animate-spin text-blue-600"/>
            <p className="text-gray-600">Loading catalog...</p>
          </div>
        </div>
      </div>
    );
  }

  if (isError) {
    return (
      <div className="min-h-screen bg-gray-50">
        <div className="flex items-center justify-center min-h-[400px]">
          <div className="text-center">
            <h2 className="text-2xl font-bold text-gray-900 mb-2">Products not found</h2>
            <p className="text-gray-600 mb-4">Error loading product catalog</p>
            <button
              onClick={() => window.history.back()}
              className="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700 transition-colors"
            >
              Go Back
            </button>
          </div>
        </div>
      </div>
    );
  }

  const visibleProducts = products.slice(0, visibleCount);

  const handleToggleFavorite = (productId: number) => {
    setFavoriteIds(prev => {
      const newFavorites = new Set(prev);
      if (newFavorites.has(productId)) {
        newFavorites.delete(productId);
      } else {
        newFavorites.add(productId);
      }
      return newFavorites;
    });
  };

  const handleProductClick = (productId: number) => {
    navigate(`/product/${productId}`);
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="flex">
        <aside className="w-80 flex-shrink-0 bg-white border-r border-gray-200 sticky top-0 h-screen overflow-y-auto">
          <DynamicFiltersSidebar
            isOpen={true}
            onClose={() => {}}
            filters={attributeFilters}
            setFilters={handleSetFilters}
            priceRange={priceRange}
            setPriceRange={handleSetPriceRange}
            sortOption={sortOption}
            setSortOption={handleSetSortOption}
            attributeGroups={attributeGroups}
          />
        </aside>

        <div className="flex-1 overflow-x-hidden">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
              {visibleProducts.map(product => (
                <ProductCard
                  key={product.id}
                  id={product.id}
                  name={product.name}
                  price={product.price}
                  rating={0}
                  reviewCount={10}
                  image={product.pictureUrls[0]}
                  isFavorite={favoriteIds.has(product.id)}
                  onToggleFavorite={handleToggleFavorite}
                  onClick={handleProductClick}
                />
              ))}
            </div>

            {visibleCount < products.length && (
              <div className="flex justify-center mt-12">
                <button
                  className="px-8 py-3 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors duration-200 font-medium"
                  onClick={() => setVisibleCount(prev => prev + 4)}
                >
                  Show more
                </button>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Catalog;
