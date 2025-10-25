import { useState} from 'react';
import { Filter, Loader2 } from 'lucide-react';
import ProductCard from '../components/products/ProductCard.tsx';
import Header from '../components/common/Header.tsx';
import CategoryFilter from '../components/products/CategoryFilter.tsx';
import FiltersSidebar, {
  Filters,
  SortOption,
} from '../components/products/FiltersSidebar.tsx';
import { useGetAvailableProductsQuery } from '../api/productsApi.ts';
import { useNavigate } from 'react-router-dom';

const Catalog = () => {
  const navigate = useNavigate();

  const [favoriteIds, setFavoriteIds] = useState<Set<number>>(new Set());
  const [isSidebarOpen, setSidebarOpen] = useState(false);

  const [visibleCount, setVisibleCount] = useState(4);

  const { data: backendProducts = [], isLoading, isError } = useGetAvailableProductsQuery();
  
  const products = backendProducts//?.map(mapBackendProductToDetail) ?? [];

  // const maxPrice = useMemo(
  //   () => Math.max(...mockProducts.map(p => p.price)),
  //   []
  // );

  // const filteredAndSortedProducts = useMemo(() => {
  //   let filtered = [...mockProducts];

  const [filters, setFilters] = useState<Filters>({
     priceRange: [0, 10000],
     rating: 0,
     condition: { new: true, used: true },
     badges: { new: false, sale: false },
   });

  const [sortOption, setSortOption] = useState<SortOption>({
     value: 'rating-desc',
     label: 'Rating (High to Low)',
   });

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Header />
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="flex items-center justify-center min-h-[400px]">
            <div className="flex flex-col items-center gap-4">
              <Loader2 className="w-8 h-8 animate-spin text-blue-600" />
              <p className="text-gray-600">Loading catalog...</p>
            </div>
          </div>
        </div>
      </div>
    );
  }

  if (isError) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Header />
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="flex items-center justify-center min-h-[400px]">
            <div className="text-center">
              <h2 className="text-2xl font-bold text-gray-900 mb-2">Products not found</h2>
              <p className="text-gray-600 mb-4">
                Error loading product catalog
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

  if (products.length === 0) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Header />
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="flex items-center justify-center min-h-[400px]">
            <div className="text-center">
              <h2 className="text-2xl font-bold text-gray-900 mb-2">The catalog is empty</h2>
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

  
  const visibleProducts = products.slice(0, visibleCount);

  //   filtered = filtered.filter(p => p.price <= filters.priceRange[1]);
  //   filtered = filtered.filter(p => p.rating >= filters.rating);
    
   
  //   if (!filters.condition.new || !filters.condition.used) {
  //     if (filters.condition.new) {
  //       filtered = filtered.filter(p => p.condition === 'new');
  //     } else if (filters.condition.used) {
  //       filtered = filtered.filter(p => p.condition === 'used');
  //     } else {
  //       return []; 
  //     }
  //   }

    
  //   if (filters.badges.new || filters.badges.sale) {
  //     filtered = filtered.filter(p => {
  //       if (filters.badges.new && p.badge === 'new') return true;
  //       if (filters.badges.sale && p.badge === 'sale') return true;
  //       return false;
  //     });
  //   }

    
  //   switch (sortOption.value) {
  //     case 'rating-desc':
  //       filtered.sort((a, b) => b.rating - a.rating);
  //       break;
  //     case 'rating-asc':
  //       filtered.sort((a, b) => a.rating - b.rating);
  //       break;
  //     case 'price-desc':
  //       filtered.sort((a, b) => b.price - a.price);
  //       break;
  //     case 'price-asc':
  //       filtered.sort((a, b) => a.price - b.price);
  //       break;
  //   }

  //   return filtered;
  // }, [filters, sortOption]);

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
      <Header />

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        
        <div className="flex justify-between items-center mb-8">
          <CategoryFilter />
          <div className="relative">
            <button
              id="filter-button"
              onClick={() => setSidebarOpen(!isSidebarOpen)}
              className="flex items-center gap-2 px-4 py-2 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors duration-200"
            >
              <Filter size={16} />
              <span>Filters</span>
            </button>
            {isSidebarOpen && (

              <div id="filter-sidebar">
                <FiltersSidebar
                  isOpen={isSidebarOpen}
                  onClose={() => setSidebarOpen(false)}
                  filters={filters}
                  setFilters={setFilters}
                  sortOption={sortOption}
                  setSortOption={setSortOption}
                  maxPrice={10000}
                />
              </div>
            )}
          </div>
        </div>

        
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
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
          <button className="px-8 py-3 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors duration-200 font-medium"
          onClick={() => setVisibleCount(prev => prev + 4)}>
            Show more
             
          </button>
        </div>)}
      </div>
    </div>
  );
};

export default Catalog;
