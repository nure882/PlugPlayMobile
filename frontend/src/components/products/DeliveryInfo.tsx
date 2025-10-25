import { Truck, Shield, RotateCcw, Package } from 'lucide-react';
import {DeliveryOption} from "../../models/Address.ts";

interface DeliveryInfoProps {
  deliveryOptions: DeliveryOption[];
}

const iconMap = {
  Truck,
  Shield,
  RotateCcw,
  Package,
};

const DeliveryInfo = ({ deliveryOptions }: DeliveryInfoProps) => {
  return (
    <div className="bg-white border border-gray-200 rounded-lg p-6">
      <h3 className="text-lg font-semibold text-gray-900 mb-4">
        Delivery and warranty
      </h3>
      
      <div className="space-y-4">
        {deliveryOptions.map((option, index) => {
          const IconComponent = iconMap[option.icon as keyof typeof iconMap];
          
          return (
            <div key={index} className="flex items-start gap-3">
              <div className="flex-shrink-0 w-10 h-10 bg-blue-50 rounded-lg flex items-center justify-center">
                <IconComponent className="w-5 h-5 text-blue-600" />
              </div>
              
              <div>
                <h4 className="font-medium text-gray-900 text-sm">
                  {option.title}
                </h4>
                <p className="text-sm text-gray-600 mt-0.5">
                  {option.description}
                </p>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default DeliveryInfo;