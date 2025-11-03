import { X, Star } from 'lucide-react';
import { Dispatch, SetStateAction } from 'react';

export interface Filters {
  priceRange: [number, number];
  rating: number;
  condition: {
    new: boolean;
    used: boolean;
  };
  badges: {
    new: boolean;
    sale: boolean;
  };
}

export interface SortOption {
  value: string;
  label: string;
}

interface FiltersSidebarProps {
  isOpen: boolean;
  onClose: () => void;
  filters: Filters;
  setFilters: Dispatch<SetStateAction<Filters>>;
  sortOption: SortOption;
  setSortOption: Dispatch<SetStateAction<SortOption>>;
  maxPrice: number;
}

const FiltersSidebar = ({
  isOpen,
  onClose,
  filters,
  setFilters,
  sortOption,
  setSortOption,
  maxPrice,
}: FiltersSidebarProps) => {
  const handlePriceChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = Number(e.target.value);
    setFilters(prev => ({ ...prev, priceRange: [prev.priceRange[0], value] }));
  };

  const handleRatingChange = (newRating: number) => {
    setFilters(prev => ({ ...prev, rating: newRating }));
  };

  const handleConditionChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, checked } = e.target;
    setFilters(prev => ({
      ...prev,
      condition: { ...prev.condition, [name]: checked },
    }));
  };

  if (!isOpen) {
    return null;
  }

  return (
    <div
      className="fixed top-[72px] right-4 w-80 bg-white shadow-xl rounded-lg z-50 p-6 overflow-y-auto"
      onClick={e => e.stopPropagation()}
    >
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-xl font-bold">Filters</h2>
        <button onClick={onClose} className="text-gray-500 hover:text-gray-700">
          <X size={24} />
        </button>
      </div>

      
      {/* <div className="mb-6">
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Sort by
        </label>
        <select
          value={sortOption.value}
          onChange={e => {
            const selected = sortOptions.find(o => o.value === e.target.value);
            if (selected) setSortOption(selected);
          }}
          className="w-full p-2 border border-gray-300 rounded-md"
        >
          {sortOptions.map(option => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
      </div>

      
      <div className="mb-6">
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Price Range
        </label>
        <input
          type="range"
          min="0"
          max={maxPrice}
          value={filters.priceRange[1]}
          onChange={handlePriceChange}
          className="w-full"
        />
        <div className="flex justify-between text-sm text-gray-600 mt-1">
          <span>0</span>
          <span>{filters.priceRange[1]}</span>
        </div>
      </div> */}

      
      <div className="mb-6">
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Minimum Rating
        </label>
        <div className="flex">
          {[1, 2, 3, 4, 5].map(star => (
            <button key={star} onClick={() => handleRatingChange(star)}>
              <Star
                size={24}
                className={
                  star <= filters.rating
                    ? 'text-yellow-400 fill-current'
                    : 'text-gray-300'
                }
              />
            </button>
          ))}
        </div>
      </div>

      
      {/* <div className="mb-6">
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Condition
        </label>
        <div className="space-y-2">
          <div className="flex items-center">
            <input
              type="checkbox"
              id="new"
              name="new"
              checked={filters.condition.new}
              onChange={handleConditionChange}
              className="h-4 w-4 text-blue-600 border-gray-300 rounded"
            />
            <label htmlFor="new" className="ml-2 text-sm text-gray-700">
              New
            </label>
          </div>
          <div className="flex items-center">
            <input
              type="checkbox"
              id="used"
              name="used"
              checked={filters.condition.used}
              onChange={handleConditionChange}
              className="h-4 w-4 text-blue-600 border-gray-300 rounded"
            />
            <label htmlFor="used" className="ml-2 text-sm text-gray-700">
              Used
            </label>
          </div>
        </div>
      </div> */}

      
      {/* <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Badges
        </label>
        <div className="space-y-2">
          <div className="flex items-center">
            <input
              type="checkbox"
              id="badge-new"
              name="new"
              checked={filters.badges.new}
              onChange={e => {
                const { name, checked } = e.target;
                setFilters(prev => ({
                  ...prev,
                  badges: { ...prev.badges, [name]: checked },
                }));
              }}
              className="h-4 w-4 text-blue-600 border-gray-300 rounded"
            />
            <label htmlFor="badge-new" className="ml-2 text-sm text-gray-700">
              New
            </label>
          </div>
          <div className="flex items-center">
            <input
              type="checkbox"
              id="badge-sale"
              name="sale"
              checked={filters.badges.sale}
              onChange={e => {
                const { name, checked } = e.target;
                setFilters(prev => ({
                  ...prev,
                  badges: { ...prev.badges, [name]: checked },
                }));
              }}
              className="h-4 w-4 text-blue-600 border-gray-300 rounded"
            />
            <label htmlFor="badge-sale" className="ml-2 text-sm text-gray-700">
              Sale
            </label>
          </div>
        </div>
      </div> */}
    </div>
  );
};

export default FiltersSidebar;
