import {ReactNode, useState} from 'react';
import {ChevronDown} from 'lucide-react';

type Props = {
  title: string;
  subtitle?: string;
  children?: ReactNode;
  defaultOpen?: boolean;
};

export default function AccordionSection({title, subtitle, children, defaultOpen = false}: Props) {
  const [open, setOpen] = useState(defaultOpen);

  return (
    <div className="border-b last:border-b-0">
      <button
        type="button"
        onClick={() => setOpen(prev => !prev)}
        className="w-full flex items-center justify-between px-4 py-4 bg-white hover:bg-gray-50"
        aria-expanded={open}
      >
        <div className="text-left">
          <div className="text-sm font-medium text-black">{title}</div>
          {subtitle && <div className="text-xs text-gray-500 mt-1">{subtitle}</div>}
        </div>

        <ChevronDown
          className={`w-5 h-5 text-gray-400 transform transition-transform ${open ? 'rotate-180' : 'rotate-0'}`}
        />
      </button>

      {open && <div className="px-4 pb-4 pt-0 bg-white">{children}</div>}
    </div>
  );
}
