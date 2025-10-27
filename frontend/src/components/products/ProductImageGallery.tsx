import React, { useEffect, useState } from 'react';

type Props = {
  images: string[];
  initialIndex?: number;
  className?: string;
  altPrefix?: string;
};

const ProductImageGallery: React.FC<Props> = ({ images, initialIndex = 0, className = '', altPrefix = 'Product image' }) => {
  const [current, setCurrent] = useState<number>(Math.min(Math.max(0, initialIndex), Math.max(0, images.length - 1)));
  const [isOpen, setIsOpen] = useState(false);

  useEffect(() => {
    setCurrent(Math.min(Math.max(0, initialIndex), Math.max(0, images.length - 1)));
  }, [initialIndex, images.length]);

  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'ArrowRight') next();
      if (e.key === 'ArrowLeft') prev();
      if (e.key === 'Escape') setIsOpen(false);
    };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [current, images.length]);

  const prev = () => setCurrent((c) => (c <= 0 ? images.length - 1 : c - 1));
  const next = () => setCurrent((c) => (c >= images.length - 1 ? 0 : c + 1));
  const open = (idx: number) => {
    setCurrent(idx);
    setIsOpen(true);
  };

if (!images || images.length === 0) {
  return (
    <div className={`w-full ${className}`}>
      <div className="relative bg-gray-100 rounded-lg overflow-hidden flex items-center justify-center min-h-[520px] text-gray-500 w-full">
        No images available
      </div>
    </div>
  );
}

  return (
    <div className={`w-full ${className}`}>
      {/* Main image */}
      <div className="relative bg-white rounded-lg overflow-hidden flex items-center justify-center">
        <button
          aria-label="Previous image"
          onClick={prev}
          className="absolute left-2 top-1/2 -translate-y-1/2 z-10 bg-white/90 p-1 rounded-full shadow hover:bg-white"
        >
          ‹
        </button>

        <img
          src={images[current]}
          alt={`${altPrefix} ${current + 1}`}
          onClick={() => open(current)}
          className="w-full max-h-[520px] object-contain cursor-zoom-in"
        />

        <button
          aria-label="Next image"
          onClick={next}
          className="absolute right-2 top-1/2 -translate-y-1/2 z-10 bg-white/90 p-1 rounded-full shadow hover:bg-white"
        >
          ›
        </button>

        <div className="absolute right-3 bottom-3 bg-black/60 text-white text-xs px-2 py-1 rounded">
          {current + 1} / {images.length}
        </div>
      </div>

      {/* Thumbnails */}
      <div className="mt-3 flex items-center gap-2 overflow-x-auto pb-2">
        {images.map((src, idx) => (
          <button
            key={src + idx}
            onClick={() => setCurrent(idx)}
            className={`flex-shrink-0 w-20 h-20 rounded-md overflow-hidden border-2 ${
              idx === current ? 'border-blue-600' : 'border-transparent'
            }`}
            aria-label={`Show image ${idx + 1}`}
          >
            <img src={src} alt={`${altPrefix} thumb ${idx + 1}`} className="w-full h-full object-cover" />
          </button>
        ))}
      </div>

      {/* Lightbox modal */}
      {isOpen && (
        <div
          role="dialog"
          aria-modal="true"
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 p-4"
          onClick={() => setIsOpen(false)}
        >
          <div className="relative max-w-[95%] max-h-[95%]" onClick={(e) => e.stopPropagation()}>
            <button
              aria-label="Close"
              onClick={() => setIsOpen(false)}
              className="absolute right-2 top-2 z-20 bg-white/90 rounded-full p-1"
            >
              ✕
            </button>

            <button
              aria-label="Previous"
              onClick={prev}
              className="absolute left-2 top-1/2 -translate-y-1/2 z-20 bg-white/90 rounded-full p-1"
            >
              ‹
            </button>

            <img src={images[current]} alt={`${altPrefix} fullscreen ${current + 1}`} className="w-full h-auto max-h-[90vh] object-contain" />

            <button
              aria-label="Next"
              onClick={next}
              className="absolute right-2 top-1/2 -translate-y-1/2 z-20 bg-white/90 rounded-full p-1"
            >
              ›
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default ProductImageGallery;
