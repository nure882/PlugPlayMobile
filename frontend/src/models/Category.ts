interface Subcategory {
  id: number;
  name: string;
}

interface Category {
  id: number;
  name: string;
  icon: any;
  subcategories: Subcategory[];
}

export default Category;