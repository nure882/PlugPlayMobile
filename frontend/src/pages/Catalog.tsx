import {useState, useEffect} from 'react';
import {useNavigate} from 'react-router-dom';
import {Loader2} from 'lucide-react';
import ProductCard from '../components/products/ProductCard';
import DynamicFiltersSidebar, {
  DynamicFilters,
  PriceRange,
  SortOption,
} from '../components/products/DynamicFilterSidebar';
import {useProductsService} from '../features/products/ProductsService';
import {useAppDispatch, useAppSelector} from '../app/configureStore';
import {
  setAttributeFilters,
  setPriceRange,
  setSortOption,
} from '../app/slices/filterSlice';

const Catalog = () => {
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  const [isMobileFilterOpen, setIsMobileFilterOpen] = useState(false);

  const {
    selectedCategory,
    priceRange = {min: 0, max: 5000},
    attributeFilters = {},
    sortOption = {value: 'price-asc', label: 'Price (Low to High)'},
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

  const currentMin = products.length > 0 ? Math.min(...products.map(p => p.price)) : 0;
  const currentMax = products.length > 0 ? Math.max(...products.map(p => p.price)) : 5000;

  useEffect(() => {
    if (typeof refetch === 'function') {
      refetch();
    }
  }, [sortOption.value, attributeFilters, priceRange, selectedCategory, refetch]);

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
    setFavoriteIds((prev) => {
      const newSet = new Set(prev);
      if (newSet.has(productId)) {
        newSet.delete(productId);
      } else {
        newSet.add(productId);
      }
      return newSet;
    });
  };

  const handleProductClick = (productId: number) => {
    navigate(`/product/${productId}`);
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="flex">
        {/* Desktop Sidebar - hidden on mobile, visible on desktop */}
        <aside
          className="hidden lg:block lg:w-80 flex-shrink-0 bg-white border-r border-gray-200 sticky top-0 h-screen overflow-y-auto">
          <DynamicFiltersSidebar
            isOpen={true}
            onClose={() => {
            }}
            filters={attributeFilters}
            setFilters={handleSetFilters}
            priceRange={priceRange}
            setPriceRange={handleSetPriceRange}
            sortOption={sortOption}
            setSortOption={handleSetSortOption}
            attributeGroups={attributeGroups}
            currentMin={currentMin}
            currentMax={currentMax}
          />
        </aside>

        {/* Mobile sidebar - shows as overlay */}
        <div className="lg:hidden">
          <DynamicFiltersSidebar
            isOpen={isMobileFilterOpen}
            onClose={() => setIsMobileFilterOpen(false)}
            filters={attributeFilters}
            setFilters={handleSetFilters}
            priceRange={priceRange}
            setPriceRange={handleSetPriceRange}
            sortOption={sortOption}
            setSortOption={handleSetSortOption}
            attributeGroups={attributeGroups}
            currentMin={currentMin}
            currentMax={currentMax}
          />
        </div>

        <div className="flex-1 overflow-x-hidden">
          {/* Mobile filter button */}
          <div className="lg:hidden sticky top-0 z-30 bg-white border-b border-gray-200 px-4 py-3">
            <button
              onClick={() => setIsMobileFilterOpen(true)}
              className="w-full flex items-center justify-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
            >
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                      d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2.586a1 1 0 01-.293.707l-6.414 6.414a1 1 0 00-.293.707V17l-4 4v-6.586a1 1 0 00-.293-.707L3.293 7.293A1 1 0 013 6.586V4z"/>
              </svg>
              <span className="font-medium">Filters & Sort</span>
            </button>
          </div>

          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4 sm:py-8">
            {products.length === 0 ? (
              <div className="flex flex-col items-center justify-center min-h-[400px] text-center">
                <p className="text-xl text-gray-600 mb-2">No products found</p>
                <p className="text-sm text-gray-500">Try adjusting your filters</p>
              </div>
            ) : (
              <>
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4 sm:gap-6">
                  {visibleProducts.map((product) => (
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
                  <div className="flex justify-center mt-8 sm:mt-12">
                    <button
                      className="w-full sm:w-auto px-6 sm:px-8 py-3 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors duration-200 font-medium text-sm sm:text-base"
                      onClick={() => setVisibleCount((prev) => prev + 20)}
                    >
                      Show more
                    </button>
                  </div>
                )}
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Catalog;
