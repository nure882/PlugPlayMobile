import React from "react";
import {X} from "lucide-react";

interface ModalProps {
  title: string;
  isOpen: boolean;
  onClose: () => void;
  children: React.ReactNode;
}

export default function Modal({title, isOpen, onClose, children}: ModalProps) {
  if (!isOpen) {
    return null;
  }

  return (
    <>
      <div
        className="fixed inset-0 bg-black/50 z-50"
        onClick={onClose}
      />

      <div className="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2
                      w-full max-w-2xl max-h-[90vh] bg-white rounded-2xl shadow-2xl
                      z-50 overflow-hidden flex flex-col">
        <div className="flex items-center justify-between px-6 py-4 border-b border-gray-200">
          <h2 className="text-2xl">{title}</h2>
          <button
            onClick={onClose}
            className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
          >
            <X className="w-6 h-6"/>
          </button>
        </div>

        <div className="flex-1 overflow-y-auto">{children}</div>
      </div>
    </>
  );
}
