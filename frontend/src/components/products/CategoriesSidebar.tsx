import { X, ChevronRight, Smartphone, Laptop, Headphones, Tv, Camera, Home, Network, Projector, Watch, Car, Box } from 'lucide-react';
import React, { useState } from 'react';

interface CategoriesSidebarProps {
  isOpen: boolean;
  onClose: () => void;
  onCategorySelect: (categoryId: number | null) => void;
  selectedCategoryId?: number | null;
}

interface Subcategory {
  id: number;
  name: string;
}

interface CategoryWithSubcategories {
  id: number;
  name: string;
  icon: any;
  subcategories: Subcategory[];
}

export const categories: CategoryWithSubcategories[] = [
  {
    id: 1,
    name: 'Computers & Laptops',
    icon: Laptop,
    subcategories: [
      { id: 2, name: 'Laptops' },
      { id: 3, name: 'Desktops' },
      { id: 4, name: 'Motherboards' },
      { id: 5, name: 'Processors (CPUs)' },
      { id: 6, name: 'Graphics Cards (GPUs)' },
      { id: 7, name: 'RAM (Memory)' },
      { id: 8, name: 'Storage (HDD, SSD, NVMe)' },
      { id: 9, name: 'Power Supplies (PSUs)' },
      { id: 10, name: 'Cooling & Fans' },
      { id: 11, name: 'Computer Cases' },
      { id: 12, name: 'Monitors' },
      { id: 13, name: 'Chargers & Adapters' },
      { id: 14, name: 'Bags & Sleeves' },
      { id: 15, name: 'Web-cameras' },
      { id: 16, name: 'Mouses' },
      { id: 17, name: 'Keyboards' },
      { id: 18, name: 'Docking Stations' },
      { id: 19, name: 'Stands & Cooling Pads' }
    ]
  },
  {
    id: 22,
    name: 'Mobile Phones & Tablets',
    icon: Smartphone,
    subcategories: [
      { id: 23, name: 'Smartphones' },
      { id: 24, name: 'Feature Phones' },
      { id: 25, name: 'Tablets' },
      { id: 26, name: 'E-Readers' },
      { id: 27, name: 'Chargers & Cables' },
      { id: 28, name: 'Phone Cases' },
      { id: 29, name: 'Screen Protectors' },
      { id: 30, name: 'Power Banks' },
      { id: 31, name: 'Earphones & Headsets' },
      { id: 32, name: 'Memory Cards' }
    ]
  },
  {
    id: 34,
    name: 'Audio & Music',
    icon: Headphones,
    subcategories: [
      { id: 35, name: 'Headphones & Earbuds' },
      { id: 36, name: 'Bluetooth Speakers' },
      { id: 37, name: 'Soundbars' },
      { id: 38, name: 'Home Audio Systems' },
      { id: 39, name: 'Turntables & Vinyl Players' },
      { id: 40, name: 'Microphones' },
      { id: 41, name: 'Studio Equipment' },
      { id: 42, name: 'Musical Instruments (Electronic)' }
    ]
  },
  {
    id: 43,
    name: 'TVs & Home Entertainment',
    icon: Tv,
    subcategories: [
      { id: 44, name: 'Smart TVs' },
      { id: 45, name: 'LED / OLED / QLED TVs' },
      { id: 46, name: 'Projectors' },
      { id: 47, name: 'Streaming Devices' },
      { id: 48, name: 'Mounts & Stands' },
      { id: 49, name: 'Remotes & Controllers' },
      { id: 50, name: 'Cables (HDMI, Optical, etc.)' }
    ]
  },
  {
    id: 52,
    name: 'Cameras & Photography',
    icon: Camera,
    subcategories: [
      { id: 53, name: 'DSLR Cameras' },
      { id: 54, name: 'Mirrorless Cameras' },
      { id: 55, name: 'Compact Cameras' },
      { id: 56, name: 'Action Cameras' },
      { id: 57, name: 'Drones' },
      { id: 58, name: 'Camera Lenses' },
      { id: 59, name: 'Tripods & Mounts' },
      { id: 60, name: 'Lighting Equipment' },
      { id: 61, name: 'Memory Cards & Storage' }
    ]
  },
  {
    id: 62,
    name: 'Smart Home',
    icon: Home,
    subcategories: [
      { id: 63, name: 'Smart Lights' },
      { id: 64, name: 'Smart Plugs' },
      { id: 65, name: 'Smart Speakers' },
      { id: 66, name: 'Smart Cameras' },
      { id: 67, name: 'Smart Thermostats' },
      { id: 68, name: 'Smart Locks & Security Systems' },
      { id: 69, name: 'Robot Vacuums' }
    ]
  },
  {
    id: 70,
    name: 'Networking & Internet',
    icon: Network,
    subcategories: [
      { id: 71, name: 'Wi-Fi Routers' },
      { id: 72, name: 'Range Extenders' },
      { id: 73, name: 'Network Switches' },
      { id: 74, name: 'Modems' },
      { id: 75, name: 'Cables & Adapters' },
      { id: 76, name: 'Network Storage (NAS)' }
    ]
  },
  {
    id: 77,
    name: 'Office Equipment',
    icon: Projector,
    subcategories: [
      { id: 78, name: 'Printers & Scanners' },
      { id: 79, name: 'Projectors' },
      { id: 80, name: 'Fax Machines' },
      { id: 81, name: 'Office Phones' },
      { id: 82, name: 'Paper Shredders' },
      { id: 83, name: 'Presentation Tools' }
    ]
  },
  {
    id: 84,
    name: 'Wearables',
    icon: Watch,
    subcategories: [
      { id: 85, name: 'Smartwatches' },
      { id: 86, name: 'Fitness Bands' },
      { id: 87, name: 'VR & AR Devices' },
      { id: 88, name: 'Smart Glasses' }
    ]
  },
  {
    id: 89,
    name: 'Car Electronics',
    icon: Car,
    subcategories: [
      { id: 90, name: 'Dash Cameras' },
      { id: 91, name: 'Car Audio Systems' },
      { id: 92, name: 'Car Chargers' },
      { id: 93, name: 'GPS & Navigation' },
      { id: 94, name: 'Parking Sensors' },
      { id: 95, name: 'Car Accessories' }
    ]
  },
  {
    id: 96,
    name: 'Tools & Components',
    icon: Box,
    subcategories: [
      { id: 97, name: 'Soldering Equipment' },
      { id: 98, name: 'Multimeters & Testers' },
      { id: 99, name: 'Wires & Connectors' },
      { id: 100, name: 'Batteries & Power Supplies' },
      { id: 101, name: 'Electronic Modules (Arduino, ESP32, etc.)' },
      { id: 102, name: 'Sensors & Actuators' },
      { id: 103, name: 'Breadboards & Prototyping Kits' }
    ]
  }
];

export default function CategoriesSidebar({ isOpen, onClose, onCategorySelect, selectedCategoryId }: CategoriesSidebarProps) {
  const [expandedId, setExpandedId] = useState<number | null>(null);

  const handleSelectCategory = (categoryId: number) => {
    const next = selectedCategoryId !== undefined
      ? (selectedCategoryId === categoryId ? null : categoryId)
      : categoryId;
    onCategorySelect(next);
    onClose();
  };

  const toggleExpand = (e: React.MouseEvent, categoryId: number) => {
    e.stopPropagation();
    setExpandedId(prev => (prev === categoryId ? null : categoryId));
  };

  if (!isOpen) {
    return null;
  }

  return (
    <>
      <div
        className="fixed inset-0 bg-black bg-opacity-50 z-40"
        onClick={onClose}
      />

      <div className="fixed left-0 top-0 h-full w-80 bg-white shadow-xl z-50 overflow-y-auto">
        <div className="sticky top-0 bg-white border-b border-gray-200 p-4 flex items-center justify-between">
          <h2 className="text-xl font-bold text-gray-900">Categories</h2>
          <button
            onClick={onClose}
            className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
          >
            <X size={20} />
          </button>
        </div>

        <div className="p-4">
          {categories.map((category) => {
            const Icon = category.icon;
            const isExpanded = expandedId === category.id;
            const isSelected = selectedCategoryId === category.id;
            return (
              <div key={category.id} className="mb-4">
                <div className="flex items-center">
                  <button
                    onClick={() => handleSelectCategory(category.id)}
                    className={`flex-1 flex items-center gap-3 p-3 rounded-lg transition-colors text-left font-medium ${isSelected ? 'bg-gray-100' : 'hover:bg-gray-50'} text-gray-900`}
                    aria-pressed={isSelected}
                  >
                    <Icon size={20} className="text-gray-600" />
                    <span className="flex-1">{category.name}</span>
                  </button>

                  <button
                    onClick={(e) => toggleExpand(e, category.id)}
                    aria-expanded={isExpanded}
                    className="p-3 rounded-lg hover:bg-gray-50 ml-2"
                    title={isExpanded ? 'Collapse' : 'Expand'}
                  >
                    <ChevronRight
                      size={16}
                      className={`text-gray-400 transform transition-transform ${isExpanded ? 'rotate-90' : 'rotate-0'}`}
                    />
                  </button>
                </div>

                <div
                  className={`ml-9 mt-2 space-y-1 overflow-hidden transition-[max-height,opacity] duration-200 ${isExpanded ? 'max-h-96 opacity-100' : 'max-h-0 opacity-0'}`}
                >
                  {category.subcategories.map((sub) => (
                    <button
                      key={sub.id}
                      onClick={() => { onCategorySelect(sub.id); onClose(); }}
                      className="w-full text-left px-3 py-2 text-sm text-gray-600 hover:text-gray-900 hover:bg-gray-50 rounded transition-colors"
                    >
                      {sub.name}
                    </button>
                  ))}
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </>
  );
}