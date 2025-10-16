import { useState } from 'react';
import Header from '../components/Header';
import AccordionSection from '../components/AccordionSection';
import { updateUserProfile } from '../lib/api';
import { PlusCircle, Trash2 } from 'lucide-react';

const validateName = (name: string) => {
  const nameRegex = /^[a-zA-Zа-яА-ЯёЁ0-9]{2,30}$/;
  return nameRegex.test(name);
};

const validateEmail = (email: string) => {
  const emailRegex = /^[a-zA-Z0-9]([a-zA-Z0-9._+-]*[a-zA-Z0-9])?@[a-zA-Z0-9]([a-zA-Z0-9-]*[a-zA-Z0-9])?(\.[a-zA-Z0-9]([a-zA-Z0-9-]*[a-zA-Z0-9])?)*\.[a-zA-Z]{2,}$/;
  return emailRegex.test(email) && !email.includes('..');
};

const validatePhone = (phone: string) => {
  const cleanPhone = phone.replace(/[^\d+]/g, '');
  const phoneRegex = /^\+[1-9]\d{1,14}$/;
  return phoneRegex.test(cleanPhone) && cleanPhone.length >= 10 && cleanPhone.length <= 15;
};
const validatePassword = (password: string) => {
  if (password.length < 8) return false;
  const hasDigit = /\d/.test(password);
  const hasLowercase = /[a-zа-яё]/.test(password);
  const hasUppercase = /[A-ZА-ЯЁ]/.test(password);
  const hasNonAlphanumeric = /[^a-zA-Zа-яА-ЯёЁ0-9]/.test(password);
  const uniqueChars = new Set(password).size;
  const hasUniqueChar = uniqueChars > 1;
  return hasDigit && hasLowercase && hasUppercase && hasNonAlphanumeric && hasUniqueChar;
};

interface Address {
  city: string;
  street: string;
  house: string;
  apartments: string;
}

export default function Profile() {
  const [firstName, setFirstName] = useState('John');
  const [lastName, setLastName] = useState('Doe');
  const [phone, setPhone] = useState('+380123456789');
  const [email, setEmail] = useState('john@example.com');
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [addresses, setAddresses] = useState<Address[]>([
    { city: 'Kyiv', street: 'Main St', house: '42', apartments: '1' }
  ]);
  const [newAddress, setNewAddress] = useState<Address>({
    city: '', street: '', house: '', apartments: ''
  });
  const [isSaving, setIsSaving] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [errors, setErrors] = useState({
    firstName: '', lastName: '', phone: '', email: '',
    currentPassword: '', newPassword: '', confirmPassword: '',
    address: { city: '', street: '', house: '', apartments: '' }
  });
  
  const [initialData, setInitialData] = useState({
    firstName, lastName, phone, email
  });

  const validateAll = () => {
    const newErrors = {
      firstName: '', lastName: '', phone: '', email: '',
      currentPassword: '', newPassword: '', confirmPassword: '',
      address: { city: '', street: '', house: '', apartments: '' }
    } as typeof errors;
    if (!firstName || !validateName(firstName)) newErrors.firstName = 'Only Latin/Cyrillic letters and numbers, 2-30 characters';
    if (!lastName || !validateName(lastName)) newErrors.lastName = 'Only Latin/Cyrillic letters and numbers, 2-30 characters';
    if (!phone || !validatePhone(phone)) newErrors.phone = 'International format: +country code + number';
    if (!email || !validateEmail(email)) newErrors.email = 'Invalid email format';
    if (currentPassword || newPassword || confirmPassword) {
      if (!validatePassword(newPassword)) {
        newErrors.newPassword = 'Password must contain at least 8 characters including: digit, lowercase, uppercase, special character';
      }
      if (newPassword !== confirmPassword) {
        newErrors.confirmPassword = 'Passwords do not match';
      }
    }
    setErrors(newErrors);
    return ![
      newErrors.firstName,
      newErrors.lastName,
      newErrors.phone,
      newErrors.email,
      newErrors.currentPassword,
      newErrors.newPassword,
      newErrors.confirmPassword,
      newErrors.address.city,
      newErrors.address.street,
      newErrors.address.house
    ].some(Boolean);
  };

  const handleStartEdit = () => {
    setInitialData({ firstName, lastName, phone, email });
    setIsEditing(true);
  };

  const handleSave = async () => {
    try {
      if (!validateAll()) return;
      setIsSaving(true);
      const payload = { firstName, lastName, phone, email };
      try {
        await updateUserProfile(payload);
        setIsEditing(false);
        setCurrentPassword('');
        setNewPassword('');
        setConfirmPassword('');
      } catch (err) {
        console.error('Failed to save profile (placeholder):', err);
      } finally {
        setIsSaving(false);
      }
    } catch (err) {
      console.error('Unexpected error during save:', err);
      setIsSaving(false);
    }
  };

  const handleCancel = () => {
    const { firstName, lastName, phone, email } = initialData;
    setFirstName(firstName);
    setLastName(lastName);
    setPhone(phone);
    setEmail(email);
    setCurrentPassword('');
    setNewPassword('');
    setConfirmPassword('');
    setIsEditing(false);
    setErrors({
      firstName: '', lastName: '', phone: '', email: '',
      currentPassword: '', newPassword: '', confirmPassword: '',
      address: { city: '', street: '', house: '', apartments: '' }
    });
  };

  const handleAddAddress = () => {
    if (!newAddress.city || !newAddress.street || !newAddress.house) {
      setErrors(prev => ({
        ...prev,
        address: {
          city: !newAddress.city ? 'City is required' : '',
          street: !newAddress.street ? 'Street is required' : '',
          house: !newAddress.house ? 'House number is required' : '',
          apartments: ''
        }
      }));
      return;
    }
    setAddresses(prev => [...prev, { ...newAddress }]);
    setNewAddress({ city: '', street: '', house: '', apartments: '' });
    setErrors(prev => ({
      ...prev,
      address: { city: '', street: '', house: '', apartments: '' }
    }));
  };

  const handleDeleteAddress = (index: number) => {
    setAddresses(prev => prev.filter((_, i) => i !== index));
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />

      <div className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm mb-6">
          <h1 className="text-xl font-semibold text-black">Personal Information</h1>
          <p className="text-sm text-gray-500 mt-1">Manage your personal details, delivery addresses, and account preferences</p>
        </div>

        <div className="bg-white border border-gray-200 rounded-lg shadow-sm overflow-hidden">
          <AccordionSection title="My Account" subtitle="Account information and login credentials">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-black mb-1">First Name</label>
                {isEditing ? (
                  <>
                    <input value={firstName} onChange={e => setFirstName(e.target.value)} className="w-full px-3 py-2 border rounded-lg border-gray-300" />
                    {errors.firstName && <p className="text-red-500 text-xs mt-1">{errors.firstName}</p>}
                  </>
                ) : (
                  <div className="text-sm text-gray-700">{firstName}</div>
                )}
              </div>
              <div>
                <label className="block text-sm font-medium text-black mb-1">Last Name</label>
                {isEditing ? (
                  <>
                    <input value={lastName} onChange={e => setLastName(e.target.value)} className="w-full px-3 py-2 border rounded-lg border-gray-300" />
                    {errors.lastName && <p className="text-red-500 text-xs mt-1">{errors.lastName}</p>}
                  </>
                ) : (
                  <div className="text-sm text-gray-700">{lastName}</div>
                )}
              </div>
            </div>

            
          </AccordionSection>

          <AccordionSection title="Change Password" subtitle="Update your password">
            {isEditing ? (
              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-black mb-1">Current Password</label>
                  <input
                    type="password"
                    value={currentPassword}
                    onChange={e => setCurrentPassword(e.target.value)}
                    className="w-full px-3 py-2 border rounded-lg border-gray-300"
                    placeholder="Enter current password"
                  />
                  {errors.currentPassword && <p className="text-red-500 text-xs mt-1">{errors.currentPassword}</p>}
                </div>
                <div>
                  <label className="block text-sm font-medium text-black mb-1">New Password</label>
                  <input
                    type="password"
                    value={newPassword}
                    onChange={e => setNewPassword(e.target.value)}
                    className="w-full px-3 py-2 border rounded-lg border-gray-300"
                    placeholder="Enter new password"
                  />
                  {errors.newPassword && <p className="text-red-500 text-xs mt-1">{errors.newPassword}</p>}
                </div>
                <div>
                  <label className="block text-sm font-medium text-black mb-1">Confirm New Password</label>
                  <input
                    type="password"
                    value={confirmPassword}
                    onChange={e => setConfirmPassword(e.target.value)}
                    className="w-full px-3 py-2 border rounded-lg border-gray-300"
                    placeholder="Confirm new password"
                  />
                  {errors.confirmPassword && <p className="text-red-500 text-xs mt-1">{errors.confirmPassword}</p>}
                </div>
              </div>
            ) : (
              <p className="text-sm text-gray-500">Enter the "Edit" button to change the password</p>
            )}
          </AccordionSection>

          <AccordionSection title="My Orders" subtitle="Your order history">
            <p className="text-sm text-gray-500">Your orders will appear here</p>
          </AccordionSection>

          <AccordionSection title="Contacts" subtitle="Email addresses and phone numbers">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-black mb-1">Phone</label>
                {isEditing ? (
                  <>
                    <input value={phone} onChange={e => setPhone(e.target.value)} className="w-full px-3 py-2 border rounded-lg border-gray-300" />
                    {errors.phone && <p className="text-red-500 text-xs mt-1">{errors.phone}</p>}
                  </>
                ) : (
                  <div className="text-sm text-gray-700">{phone}</div>
                )}
              </div>
              <div>
                <label className="block text-sm font-medium text-black mb-1">Email</label>
                {isEditing ? (
                  <>
                    <input value={email} onChange={e => setEmail(e.target.value)} className="w-full px-3 py-2 border rounded-lg border-gray-300" />
                    {errors.email && <p className="text-red-500 text-xs mt-1">{errors.email}</p>}
                  </>
                ) : (
                  <div className="text-sm text-gray-700">{email}</div>
                )}
              </div>
            </div>
          </AccordionSection>

          <AccordionSection title="Delivery Addresses" subtitle="Saved delivery addresses">
            <div className="space-y-6">
              {addresses.map((address, index) => (
                <div key={index} className="flex items-start justify-between p-4 bg-gray-50 rounded-lg">
                  <div className="space-y-1">
                    <div className="text-sm font-medium text-gray-900">
                      {address.street}, {address.house}{address.apartments ? `, apt ${address.apartments}` : ''}
                    </div>
                    <div className="text-sm text-gray-500">{address.city}</div>
                  </div>
                  {isEditing && (
                    <button
                      onClick={() => handleDeleteAddress(index)}
                      className="text-red-600 hover:text-red-700"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  )}
                </div>
              ))}

              {isEditing && (
                <div className="mt-4 p-4 border border-gray-200 rounded-lg">
                  <h4 className="text-sm font-medium text-black mb-4">Add New Address</h4>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium text-black mb-1">City</label>
                      <input
                        value={newAddress.city}
                        onChange={e => setNewAddress(prev => ({ ...prev, city: e.target.value }))}
                        className="w-full px-3 py-2 border rounded-lg border-gray-300"
                        placeholder="Enter city"
                      />
                      {errors.address.city && <p className="text-red-500 text-xs mt-1">{errors.address.city}</p>}
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-black mb-1">Street</label>
                      <input
                        value={newAddress.street}
                        onChange={e => setNewAddress(prev => ({ ...prev, street: e.target.value }))}
                        className="w-full px-3 py-2 border rounded-lg border-gray-300"
                        placeholder="Enter street"
                      />
                      {errors.address.street && <p className="text-red-500 text-xs mt-1">{errors.address.street}</p>}
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-black mb-1">House</label>
                      <input
                        value={newAddress.house}
                        onChange={e => setNewAddress(prev => ({ ...prev, house: e.target.value }))}
                        className="w-full px-3 py-2 border rounded-lg border-gray-300"
                        placeholder="Enter house number"
                      />
                      {errors.address.house && <p className="text-red-500 text-xs mt-1">{errors.address.house}</p>}
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-black mb-1">Apartment (optional)</label>
                      <input
                        value={newAddress.apartments}
                        onChange={e => setNewAddress(prev => ({ ...prev, apartments: e.target.value }))}
                        className="w-full px-3 py-2 border rounded-lg border-gray-300"
                        placeholder="Enter apartment number"
                      />
                    </div>
                  </div>
                  <button
                    onClick={handleAddAddress}
                    className="mt-4 flex items-center gap-2 px-4 py-2 bg-white border border-gray-300 rounded-lg hover:bg-gray-50"
                  >
                    <PlusCircle className="w-4 h-4" />
                    <span>Add Address</span>
                  </button>
                </div>
              )}
            </div>
          </AccordionSection>
        </div>

        <div className="flex justify-end gap-3 mt-6">
          {isEditing ? (
            <>
              <button onClick={handleCancel} className="px-4 py-2 bg-white border border-gray-300 rounded-lg">Cancel</button>
              <button onClick={handleSave} disabled={isSaving} className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50">
                {isSaving ? 'Saving...' : 'Save Changes'}
              </button>
            </>
          ) : (
            <button onClick={handleStartEdit} className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700">Edit</button>
          )}
        </div>
      </div>
    </div>
  );
}
