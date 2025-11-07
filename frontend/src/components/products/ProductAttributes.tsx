import React, {useEffect, useState} from 'react';
import AttributeGroup from '../../models/AttributeGroup';
import {useGetAttributeGroupsMutation} from '../../api/productsApi';

interface Props {
  categoryId: number;
  productId: number;
  onSelectionChange?: (selection: Record<number, string[]>) => void;
}

const pillBase = 'inline-flex items-center gap-2 px-3 py-1.5 rounded-lg border cursor-pointer text-sm transition-colors focus:outline-none';
const pillSelected = 'bg-white text-blue-600 border-blue-600 font-medium';
const pillUnselected = 'bg-white text-gray-700 border-gray-200 hover:border-gray-300';

const ProductAttributes: React.FC<Props> = ({categoryId, productId, onSelectionChange}) => {
  const [getAttributeGroups, {data, isLoading, isError}] = useGetAttributeGroupsMutation();

  useEffect(() => {
    if (categoryId && productId) {
      void getAttributeGroups({categoryId, productIds: [productId]});
    }
  }, [categoryId, productId, getAttributeGroups]);

  const groups: AttributeGroup[] = (data ?? []) as AttributeGroup[];

  const [selections, setSelections] = useState<Record<number, Set<string>>>({});

  useEffect(() => {
    if (groups && groups.length) {
      setSelections(prev => {
        const next = {...prev};
        for (const g of groups) {
          if (!next[g.id]) next[g.id] = new Set<string>();
        }
        return next;
      });
    }
  }, [groups]);

  const notifyChange = (next: Record<number, Set<string>>) => {
    if (onSelectionChange) {
      const obj: Record<number, string[]> = {};
      for (const k of Object.keys(next)) {
        obj[Number(k)] = Array.from(next[Number(k)]);
      }
      onSelectionChange(obj);
    }
  };

  const toggleOption = (groupId: number, option: string) => {
    setSelections(prev => {
      const next = {...prev};
      const prevSet = prev[groupId] ?? new Set<string>();
      const newSet = new Set<string>(prevSet);
      if (newSet.has(option)) newSet.delete(option);
      else newSet.add(option);
      next[groupId] = newSet;
      notifyChange(next);
      return next;
    });
  };

  const getOptionLabel = (g: AttributeGroup, dto: any) => {
    if (g.dataType && g.dataType.toLowerCase().includes('num')) {
      const v = dto.numValue ?? dto.strValue;
      return v === undefined || v === null ? '' : `${v}${g.unit ? ' ' + g.unit : ''}`;
    }

    const s = dto.strValue ?? dto.numValue;
    return s === undefined || s === null ? '' : `${s}${g.unit ? ' ' + g.unit : ''}`;
  };

  const renderGroup = (g: AttributeGroup) => {
    const set = new Map<string, any>();
    for (const dto of g.productAttributeDtos ?? []) {
      const label = getOptionLabel(g, dto);
      if (label && !set.has(label)) set.set(label, dto);
    }
    const options = Array.from(set.keys());

    const selectedSet = selections[g.id] ?? new Set<string>();

    return (
      <div key={g.id} className="mb-4">
        <div className="text-sm font-medium text-gray-700 mb-2">{g.name}</div>
        <div className="flex flex-wrap gap-2">
          {options.map((opt) => {
            const isSelected = selectedSet.has(opt);
            return (
              <button
                key={opt}
                type="button"
                onClick={() => toggleOption(g.id, opt)}
                className={`${pillBase} ${isSelected ? pillSelected : pillUnselected}`}
                aria-pressed={isSelected}
              >
                <span>{opt}</span>
              </button>
            );
          })}
          {(!options || options.length === 0) && (
            <div className="text-sm text-gray-500">No options</div>
          )}
        </div>
      </div>
    );
  };

  if (!categoryId || !productId) return null;

  if (isLoading) {
    return <div className="py-2">Loading attributes...</div>;
  }

  if (isError) {
    return <div className="py-2 text-sm text-red-600">Failed to load attributes</div>;
  }

  if (!groups || groups.length === 0) return null;

  return (
    <div className="bg-white border border-gray-100 rounded-lg p-4">
      {groups.map(g => renderGroup(g))}
    </div>
  );
};

export default ProductAttributes;
