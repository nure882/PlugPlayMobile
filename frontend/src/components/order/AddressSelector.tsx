import { Address } from '../../models/Address';

interface AddressSelectorProps {
  addresses: Address[];
  selectedId?: number | null;
  onSelect: (address: Address) => void;
}

export function AddressSelector({
  addresses,
  selectedId,
  onSelect,
}: AddressSelectorProps) {
  return (
    <select
      className="p-3 rounded-lg bg-gray-100 w-full"
      value={selectedId ?? ""}
      onChange={(e) => {
        const selected = addresses.find((a) => a.id === Number(e.target.value));
        if (selected) onSelect(selected);
      }}
    >
      <option value="" disabled>
        Select address
      </option>

      {addresses.map((addr) => (
        <option key={addr.id} value={addr.id}>
          {addr.city}, {addr.street} {addr.house}
          {addr.apartments && `, Apt ${addr.apartments}`}
        </option>
      ))}
    </select>
  );
}

export default AddressSelector;

