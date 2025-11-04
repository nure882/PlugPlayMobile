import {ChevronDown, ChevronUp} from 'lucide-react';
import {useState, useEffect} from 'react';
import AttributeGroup from "../../models/AttributeGroup.ts";

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
}

const sortOptions: SortOption[] = [
  {value: 'price-asc', label: 'Price (Low to High)'},
  {value: 'price-desc', label: 'Price (High to Low)'},
  {value: 'newest', label: 'Newest'},
];

export default function DynamicFiltersSidebar({
                                                filters,
                                                setFilters,
                                                priceRange,
                                                setPriceRange,
                                                sortOption,
                                                setSortOption,
                                                attributeGroups,
                                              }: DynamicFiltersSidebarProps) {
  const [expandedSections, setExpandedSections] = useState<Set<number>>(new Set());
  const [minInput, setMinInput] = useState<string>(priceRange.min?.toString() ?? '0');
  const [maxInput, setMaxInput] = useState<string>(priceRange.max?.toString() ?? '5000');

  useEffect(() => {
    setMinInput(priceRange.min?.toString() ?? '0');
    setMaxInput(priceRange.max?.toString() ?? '5000');
  }, [priceRange.min, priceRange.max]);

  const commitPriceRange = () => {
    const min = Number(minInput) || 0;
    const max = Number(maxInput) || 0;
    setPriceRange(prev => (prev.min === min && prev.max === max) ? prev : { min, max });
  };

  const hasProducts = attributeGroups.length > 0;

  useEffect(() => {
    if (attributeGroups.length > 0) {
      setExpandedSections(new Set(attributeGroups.map((g) => g.id)));
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
    const updated = existing.includes(value) ? existing.filter((v) => v !== value) : [...existing, value];
    setFilters({
      ...filters,
      [key]: updated,
    });
  };

  const clearAllFilters = () => {
    setFilters({});
    setPriceRange({min: 0, max: 5000});
    setSortOption(sortOptions[0]);
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
        if (pa.attributeId === 2) {
          console.log("pa.numValue")
          console.log(pa);
        }

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
    <div className="h-full bg-white">
      <div className="sticky top-0 bg-white border-b border-gray-200 px-6 py-4">
        <h2 className="text-lg font-semibold text-gray-900 mb-3">Filters</h2>
        <div className="flex items-center gap-2">
          <select
            value={sortOption.value}
            onChange={(e) => setSortOption(sortOptions.find((o) => o.value === e.target.value) || sortOptions[0])}
            className="border border-gray-300 rounded px-2 py-1 text-sm flex-1"
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
              className="px-3 py-1.5 bg-blue-600 text-white text-sm font-medium rounded hover:bg-blue-700 transition-colors whitespace-nowrap"
            >
              Clear all
            </button>
          )}
        </div>
      </div>

      <div className="px-6 py-4 space-y-6">
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
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-sm text-gray-600 mb-1">From</label>
                  <div className="relative">
                    <span className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500">$</span>
                    <input
                    type="number"
                    value={minInput}
                    onChange={(e) => setMinInput(e.target.value)}
                    onBlur={commitPriceRange}
                    onKeyDown={(e) => { if (e.key === 'Enter') commitPriceRange(); }}
                    className="w-full pl-7 pr-3 py-2 border border-gray-300 rounded focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500"
                    placeholder="0"
                  />
                  </div>
                </div>

                <div>
                  <label className="block text-sm text-gray-600 mb-1">To</label>
                  <div className="relative">
                    <span className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500">$</span>
                    <input
                      type="number"
                      value={maxInput}
                      onChange={(e) => setMaxInput(e.target.value)}
                      onBlur={commitPriceRange}
                      onKeyDown={(e) => { if (e.key === 'Enter') commitPriceRange(); }}
                      className="w-full pl-7 pr-3 py-2 border border-gray-300 rounded focus:outline-none focus:ring-1 focus:ring-blue-500 focus:border-blue-500"
                      placeholder="5000"
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
                <h3 className="text-base font-medium text-gray-900">{group.name}</h3>
                {isExpanded ? (
                  <ChevronUp className="w-5 h-5 text-gray-500"/>
                ) : (
                  <ChevronDown className="w-5 h-5 text-gray-500"/>
                )}
              </button>

              {isExpanded && (
                <div className="space-y-3">
                  {group.dataType === 'bool' ? (
                    <label className="flex items-center gap-3 cursor-pointer group">
                      <div className="relative">
                        <input
                          type="checkbox"
                          checked={selectedValues.includes('true')}
                          onChange={() => handleFilterChange(group.id, 'true')}
                          className="w-5 h-5 text-blue-600 border-gray-300 rounded focus:ring-blue-500 cursor-pointer"
                        />
                      </div>
                      <span className="text-sm text-gray-700 group-hover:text-gray-900">Yes</span>
                    </label>
                  ) : (group.dataType === 'decimal' || group.dataType === 'num') && uniqueValues.length <= 6 ? (
                    <div className="grid grid-cols-4 gap-2">
                      {uniqueValues.map((value) => (
                        <button
                          key={value}
                          onClick={() => handleFilterChange(group.id, value)}
                          type="button"
                          className={`px-3 py-2 text-sm border rounded transition-colors ${
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
                      <label key={value} className="flex items-center gap-3 cursor-pointer group">
                        <div className="relative">
                          <input
                            type="checkbox"
                            checked={selectedValues.includes(value)}
                            onChange={() => handleFilterChange(group.id, value)}
                            className="w-5 h-5 text-blue-600 border-gray-300 rounded focus:ring-blue-500 cursor-pointer"
                          />
                        </div>
                        <span className="text-sm text-gray-700 group-hover:text-gray-900">
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
  );
}
