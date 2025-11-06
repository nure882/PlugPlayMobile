
import { Loader2 } from 'lucide-react';

export default function LoadingMessage(loadingItemName: string)
{
  return (
    <div className="bg-gray-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="flex items-center justify-center min-h-[400px]">
          <div className="flex flex-col items-center gap-4">
            <Loader2 className="w-8 h-8 animate-spin text-blue-600"/>
            <p className="text-gray-600">Loading {loadingItemName}...</p>
          </div>
        </div>
      </div>
    </div>
  );
}
  