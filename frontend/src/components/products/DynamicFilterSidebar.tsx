import {ChevronDown, ChevronUp, X} from 'lucide-react';
import {useState, useEffect} from 'react';
import AttributeGroup from "../../models/AttributeGroup.ts";
import { resetFilters } from '../../app/slices/filterSlice.ts';
import { useDispatch } from 'react-redux';

export interface DynamicFilters {
  [attributeId: string]: string[];
}

export interface PriceRange {
  min: number;
  max: number;
}

export interface SortOption {
  value: string;
  label: string;
}

interface DynamicFiltersSidebarProps {
  isOpen: boolean;
  onClose: () => void;
  filters: DynamicFilters;
  setFilters: (filters: DynamicFilters) => void;
  priceRange: PriceRange;
  setPriceRange: (range: PriceRange) => void;
  sortOption: SortOption;
  setSortOption: (option: SortOption) => void;
  attributeGroups: AttributeGroup[];
  currentMin: number;
  currentMax: number;
}

const sortOptions: SortOption[] = [
  {value: 'price-asc', label: 'Price (Low to High)'},
  {value: 'price-desc', label: 'Price (High to Low)'},
  {value: 'newest', label: 'Newest'},
];

export default function DynamicFiltersSidebar({
                                                isOpen,
                                                onClose,
                                                filters,
                                                setFilters,
                                                priceRange,
                                                setPriceRange,
                                                sortOption,
                                                setSortOption,
                                                attributeGroups,
                                                currentMin,
                                                currentMax,
                                              }: DynamicFiltersSidebarProps) {
  const dispatch = useDispatch();
  const [expandedSections, setExpandedSections] = useState<Set<number>>(new Set([-1]));
  const [minInput, setMinInput] = useState<string>(currentMin.toString());
  const [maxInput, setMaxInput] = useState<string>(currentMax.toString());

  useEffect(() => {
    setMinInput(currentMin.toString());
    setMaxInput(currentMax.toString());
  }, [currentMin, currentMax]);

  const commitPriceRange = () => {
    const min = Number(minInput) || 0;
    const max = Number(maxInput) || 5000;
    
    if (min >= 0 && max > min) {
      setPriceRange({ min, max });
    }
  };

  const hasProducts = attributeGroups.length > 0;

  useEffect(() => {
    if (attributeGroups.length > 0) {
      setExpandedSections(new Set([-1, ...attributeGroups.map((g) => g.id)]));
    }
  }, [attributeGroups]);

  const toggleSection = (attributeId: number) => {
    setExpandedSections((prev) => {
      const newSet = new Set(prev);
      if (newSet.has(attributeId)) {
        newSet.delete(attributeId);
      } else {
        newSet.add(attributeId);
      }
      return newSet;
    });
  };

  const handleFilterChange = (attributeId: number, value: string) => {
    const key = attributeId.toString();
    const existing = filters[key] || [];
    const updated = existing.includes(value) 
      ? existing.filter((v) => v !== value) 
      : [...existing, value];
    
    setFilters({
      ...filters,
      [key]: updated,
    });
  };

  const clearAllFilters = () => {
    dispatch(resetFilters());
  };

  const hasActiveFilters =
    Object.keys(filters).some((key) => filters[key] && filters[key].length > 0) ||
    priceRange.min > 0 ||
    priceRange.max < 5000;

  const getUniqueValues = (group: AttributeGroup): string[] => {
    const values = group.productAttributeDtos.map(pa => {
      if (group.dataType === 'string' || group.dataType === 'str') {
        return pa.strValue;
      } else if (group.dataType === 'bool') {
        return pa.numValue === 1 ? 'true' : 'false';
      } else {
        return pa.numValue?.toString();
      }
    }).filter(v => v != null) as string[];
    
    return Array.from(new Set(values)).sort((a, b) => {
      if (group.dataType === 'decimal' || group.dataType === 'num') {
        return parseFloat(a) - parseFloat(b);
      }
      return a.localeCompare(b);
    });
  };

  return (
    <>
      {/* Mobile/Tablet Overlay */}
      {isOpen && (
        <div 
          className="fixed inset-0 bg-black bg-opacity-50 z-40 lg:hidden"
          onClick={onClose}
        />
      )}

      {/* Sidebar */}
      <div className={`
        fixed lg:sticky top-0 left-0 h-screen bg-white z-50 lg:z-auto
        transition-transform duration-300 ease-in-out
        w-80 sm:w-96 lg:w-80
        ${isOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'}
        overflow-y-auto
      `}>
        {/* Mobile Close Button */}
        <button
          onClick={onClose}
          className="lg:hidden absolute top-4 right-4 p-2 text-gray-500 hover:text-gray-700 z-10"
        >
          <X className="w-6 h-6" />
        </button>

        <div className="sticky top-0 bg-white border-b border-gray-200 px-4 sm:px-6 py-4 z-10">
          <h2 className="text-lg font-semibold text-gray-900 mb-3">Filters</h2>
          <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-2">
            <select
              value={sortOption.value}
              onChange={(e) => setSortOption(sortOptions.find((o) => o.value === e.target.value) || sortOptions[0])}
              className="border border-gray-300 rounded px-2 py-2 text-sm flex-1 min-w-0"
            >
              {sortOptions.map((opt) => (
                <option key={opt.value} value={opt.value}>
                  {opt.label}
                </option>
              ))}
            </select>
            {hasActiveFilters && (
              <button
                onClick={clearAllFilters}
                className="px-3 py-2 bg-blue-600 text-white text-sm font-medium rounded hover:bg-blue-700 transition-colors whitespace-nowrap"
              >
                Clear all
              </button>
            )}
          </div>
        </div>

        <div className="px-4 sm:px-6 py-4 space-y-6">
          <div className="border-b border-gray-200 pb-6">
            <button
              onClick={() => toggleSection(-1)}
              className="w-full flex items-center justify-between mb-4"
            >
              <h3 className="text-base font-medium text-gray-900">Price Range</h3>
              {expandedSections.has(-1) ? (
                <ChevronUp className="w-5 h-5 text-gray-500"/>
              ) : (
                <ChevronDown className="w-5 h-5 text-gray-500"/>
              )}
            </button>

            {(expandedSections.has(-1) || expandedSections.size === 0) && (
              <div className="space-y-3">
                <div className="grid grid-cols-2 gap-2 sm:gap-3">
                  <div>
                    <label className="block text-xs sm:text-sm text-gray-600 mb-1">From</label>
                    <div className="relative">
                      <input
                        type="number"
                        value={minInput}
                        onChange={(e) => setMinInput(e.target.value)}
                        onBlur={commitPriceRange}
                        onKeyDown={(e) => { if (e.key === 'Enter') commitPriceRange(); }}
                        className="w-full pl-3 sm:pl-4 pr-2 sm:pr-3 py-2 text-sm border border-gray-300 rounded focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500"
                        placeholder="0"
                        min="0"
                      />
                    </div>
                  </div>

                  <div>
                    <label className="block text-xs sm:text-sm text-gray-600 mb-1">To</label>
                    <div className="relative">
                      <input
                        type="number"
                        value={maxInput}
                        onChange={(e) => setMaxInput(e.target.value)}
                        onBlur={commitPriceRange}
                        onKeyDown={(e) => { if (e.key === 'Enter') commitPriceRange(); }}
                        className="w-full pl-3 sm:pl-4 pr-2 sm:pr-3 py-2 text-sm border border-gray-300 rounded focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500"
                        placeholder="5000"
                        min="0"
                      />
                    </div>
                  </div>
                </div>
              </div>
            )}
          </div>

          {hasProducts && attributeGroups.map((group) => {
            const isExpanded = expandedSections.has(group.id);
            const attributeKey = group.id.toString();
            const selectedValues = filters[attributeKey] || [];
            const uniqueValues = getUniqueValues(group);

            if (uniqueValues.length === 0) {
              return null;
            }

            return (
              <div key={group.id} className="border-b border-gray-200 pb-6">
                <button
                  onClick={() => toggleSection(group.id)}
                  className="w-full flex items-center justify-between mb-4"
                >
                  <h3 className="text-sm sm:text-base font-medium text-gray-900">{group.name}</h3>
                  {isExpanded ? (
                    <ChevronUp className="w-4 h-4 sm:w-5 sm:h-5 text-gray-500"/>
                  ) : (
                    <ChevronDown className="w-4 h-4 sm:w-5 sm:h-5 text-gray-500"/>
                  )}
                </button>

                {isExpanded && (
                  <div className="space-y-2 sm:space-y-3">
                    {group.dataType === 'bool' ? (
                      <label className="flex items-center gap-2 sm:gap-3 cursor-pointer group">
                        <div className="relative">
                          <input
                            type="checkbox"
                            checked={selectedValues.includes('true')}
                            onChange={() => handleFilterChange(group.id, 'true')}
                            className="w-4 h-4 sm:w-5 sm:h-5 text-blue-600 border-gray-300 rounded focus:ring-blue-500 cursor-pointer"
                          />
                        </div>
                        <span className="text-xs sm:text-sm text-gray-700 group-hover:text-gray-900">Yes</span>
                      </label>
                    ) : (group.dataType === 'decimal' || group.dataType === 'num') && uniqueValues.length <= 6 ? (
                      <div className="grid grid-cols-3 sm:grid-cols-4 gap-1.5 sm:gap-2">
                        {uniqueValues.map((value) => (
                          <button
                            key={value}
                            onClick={() => handleFilterChange(group.id, value)}
                            type="button"
                            className={`px-2 sm:px-3 py-1.5 sm:py-2 text-xs sm:text-sm border rounded transition-colors ${
                              selectedValues.includes(value)
                                ? 'border-blue-600 bg-blue-50 text-blue-600'
                                : 'border-gray-300 text-gray-700 hover:border-gray-400'
                            }`}
                          >
                            {value}
                            {group.unit && <span className="text-xs"> {group.unit}</span>}
                          </button>
                        ))}
                      </div>
                    ) : (
                      uniqueValues.map((value) => (
                        <label key={value} className="flex items-center gap-2 sm:gap-3 cursor-pointer group">
                          <div className="relative flex-shrink-0">
                            <input
                              type="checkbox"
                              checked={selectedValues.includes(value)}
                              onChange={() => handleFilterChange(group.id, value)}
                              className="w-4 h-4 sm:w-5 sm:h-5 text-blue-600 border-gray-300 rounded focus:ring-blue-500 cursor-pointer"
                            />
                          </div>
                          <span className="text-xs sm:text-sm text-gray-700 group-hover:text-gray-900 break-words">
                            {value}
                            {group.unit && ` ${group.unit}`}
                          </span>
                        </label>
                      ))
                    )}
                  </div>
                )}
              </div>
            );
          })}
        </div>
      </div>
    </>
  );
}
