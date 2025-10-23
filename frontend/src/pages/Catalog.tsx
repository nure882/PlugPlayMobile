import { useState, useMemo } from 'react';
import { Filter } from 'lucide-react';
import ProductCard from '../components/ProductCard';
import Header from '../components/Header';
import CategoryFilter from '../components/CategoryFilter';
import { mockProducts } from '../data/products';
import FiltersSidebar, {
  Filters,
  SortOption,
} from '../components/FiltersSidebar';

const Catalog = () => {
  const [favoriteIds, setFavoriteIds] = useState<Set<string>>(new Set());
  const [isSidebarOpen, setSidebarOpen] = useState(false);



  // const maxPrice = useMemo(
  //   () => Math.max(...mockProducts.map(p => p.price)),
  //   []
  // );

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

  // const filteredAndSortedProducts = useMemo(() => {
  //   let filtered = [...mockProducts];

   
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

  const handleToggleFavorite = (productId: string) => {
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

  const handleProductClick = (productId: string) => {
    console.log('Clicked product:', productId);
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
          {mockProducts.map(product => (
            <ProductCard
              key={product.id}
              id={product.id}
              name={product.name}
              price={product.price}
              rating={0}
              reviewCount={10} 
              image=""
              isFavorite={favoriteIds.has(product.id)}
              onToggleFavorite={handleToggleFavorite}
              onClick={handleProductClick}
            />
          ))}
        </div>

        
        <div className="flex justify-center mt-12">
          <button className="px-8 py-3 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors duration-200 font-medium">
            Show more
          </button>
        </div>
      </div>
    </div>
  );
};

export default Catalog;