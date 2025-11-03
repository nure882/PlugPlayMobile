import {ComponentType} from 'react';

interface Subcategory {
  id: number;
  name: string;
}

interface CategoryWithSubcategories {
  id: number;
  name: string;
  icon: ComponentType<{ size?: number | string; className?: string }>;
  subcategories: Subcategory[];
}

interface CategoryFilterProps {
  selectedCategoryId: number | null;
  onCategorySelect: (categoryId: number | null) => void;
}

// Import categories from CategoriesSidebar to keep them in sync
import { categories } from './CategoriesSidebar';

const CategoryFilter = ({ selectedCategoryId, onCategorySelect }: CategoryFilterProps) => {
  const handleCategoryClick = (categoryId: number) => {
    onCategorySelect(selectedCategoryId === categoryId ? null : categoryId);
  };

  return (
    <div className="bg-gray-50 w-full">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="w-full flex gap-2 sm:gap-3 py-3 -mb-3 overflow-x-auto items-center">
          {categories.map(category => {
            const isSelected = selectedCategoryId === category.id;
            const Icon = category.icon;
            return (
              <div
                key={category.id}
                className="flex flex-col items-center space-y-1 cursor-pointer flex-shrink-0"
                style={{minWidth: 64}}
              >
                <button
                  onClick={() => handleCategoryClick(category.id)}
                  aria-pressed={isSelected}
                  className={`flex items-center justify-center rounded-full transition-colors duration-200 focus:outline-none
                      ${isSelected ? 'bg-blue-100' : 'bg-gray-100 hover:bg-gray-200'}
                      w-8 sm:w-10 md:w-12 lg:w-14 h-8 sm:h-10 md:h-12 lg:h-14`}
                >
                  <Icon
                    className={`${isSelected ? 'text-blue-600' : 'text-gray-600'} w-4 sm:w-5 md:w-6 lg:w-8 h-auto`}
                  />
                </button>
                <span className="text-xs sm:text-xs text-center text-gray-700 truncate w-full px-1">
                    {category.name}
                  </span>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
};

export default CategoryFilter;