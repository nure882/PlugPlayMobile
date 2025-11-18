interface AttributeGroup {
  id: number;
  name: string;
  dataType: string;
  unit?: string;
  productAttributeDtos: Array<{
    id: number;
    attributeId: number;
    productId: number;
    strValue?: string;
    numValue?: number;
  }>;
}

export default AttributeGroup;
