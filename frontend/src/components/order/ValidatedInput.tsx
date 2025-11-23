import React, { useState } from "react";

interface ValidatedInputProps {
  label?: string;
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
  disabled?: boolean;
  validate?: (value: string) => boolean; 
  errorMessage?: string
  className?: string;
}

export const ValidatedInput: React.FC<ValidatedInputProps> = ({
  label,
  value,
  onChange,
  placeholder,
  disabled = false,
  validate,
  errorMessage = "Wrong value!",
  className = "",
}) => {
  const [error, setError] = useState<string | null>(null);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const v = e.target.value;
    onChange(v);

    if (validate && !validate(v)) {
      setError(errorMessage);
    }
    else {
      setError(null);
    }
  };

  return (
    <div className="flex flex-col space-y-1 w-full">
      {label && <label className="text-sm font-medium text-gray-700">{label}</label>}

      <input
        placeholder={placeholder}
        value={value}
        disabled={disabled}
        onChange={handleChange}
        className={`p-3 rounded-lg bg-gray-100 disabled:bg-gray-300 border 
          ${error ? "border-red-500" : "border-transparent"} ${className}`}
      />

      {error && <p className="text-sm text-red-500">{error}</p>}
    </div>
  );
};
