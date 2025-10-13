import {
  Camera,
  Headphones,
  Laptop,
  Smartphone,
  Watch,
  Tv,
} from 'lucide-react';
import { ComponentType, useState } from 'react';

interface Category {
  name: string;
  icon: ComponentType<{ size?: number | string; className?: string }>;
}

const categories: Category[] = [
  { name: 'Смартфони', icon: Smartphone },
  { name: 'Ноутбуки', icon: Laptop },
  { name: 'Телевізори', icon: Tv },
  { name: 'Навушники', icon: Headphones },
  { name: 'Годинники', icon: Watch },
  { name: 'Камери', icon: Camera },
];

const CategoryFilter = () => {
  const [selectedCategory, setSelectedCategory] = useState<string | null>(
    'Смартфони'
  );

  const handleCategoryClick = (categoryName: string) => {
    if (selectedCategory === categoryName) {
      setSelectedCategory(null); // Deselect if clicked again
    } else {
      setSelectedCategory(categoryName);
    }
  };

  return (
    <div className="bg-gray-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-start space-x-2 sm:space-x-4 overflow-x-auto pb-4 -mb-4">
          {categories.map(category => {
            const isSelected = selectedCategory === category.name;
            return (
              <div
                key={category.name}
                className="flex flex-col items-center space-y-2 flex-shrink-0 cursor-pointer"
                onClick={() => handleCategoryClick(category.name)}
              >
                <button
                  className={`flex items-center justify-center w-20 h-20 rounded-full transition-colors duration-200 focus:outline-none ${
                    isSelected
                      ? 'bg-blue-100'
                      : 'bg-gray-100 hover:bg-gray-200'
                  }`}
                >
                  <category.icon
                    size={32}
                    className={
                      isSelected ? 'text-blue-600' : 'text-gray-600'
                    }
                  />
                </button>
                <span className="text-sm font-medium text-gray-700">
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
