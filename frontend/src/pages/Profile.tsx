import { useState } from 'react';
import Header from '../components/Header';
import AccordionSection from '../components/AccordionSection';
import { updateUserProfile } from '../lib/api';

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

export default function Profile() {
  const [firstName, setFirstName] = useState('John');
  const [lastName, setLastName] = useState('Doe');
  const [phone, setPhone] = useState('+380123456789');
  const [email, setEmail] = useState('john@example.com');
  const [isSaving, setIsSaving] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [errors, setErrors] = useState({ firstName: '', lastName: '', phone: '', email: '' });

  const validateAll = () => {
    const newErrors = { firstName: '', lastName: '', phone: '', email: '' };
    if (!firstName || !validateName(firstName)) newErrors.firstName = 'Only Latin/Cyrillic letters and numbers, 2-30 characters';
    if (!lastName || !validateName(lastName)) newErrors.lastName = 'Only Latin/Cyrillic letters and numbers, 2-30 characters';
    if (!phone || !validatePhone(phone)) newErrors.phone = 'International format: +country code + number';
    if (!email || !validateEmail(email)) newErrors.email = 'Invalid email format';
    setErrors(newErrors);
    return !Object.values(newErrors).some(Boolean);
  };

  const handleSave = async () => {
    if (!validateAll()) return;
    setIsSaving(true);
    const payload = { firstName, lastName, phone, email };
    try {
      await updateUserProfile(payload);
      setIsEditing(false);
    } catch (err) {
      console.error('Failed to save profile (placeholder):', err);
    } finally {
      setIsSaving(false);
    }
  };

  const handleCancel = () => {
    setIsEditing(false);
    setErrors({ firstName: '', lastName: '', phone: '', email: '' });
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

            <div className="mt-4 grid grid-cols-1 md:grid-cols-2 gap-4">
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

          <AccordionSection title="Personal Data" subtitle="Gender, date of birth and personal details">
            <p className="text-sm text-gray-500">Placeholder for personal data fields (DOB, gender, etc.)</p>
          </AccordionSection>

          <AccordionSection title="My Order Recipients" subtitle="Saved recipients for orders">
            <p className="text-sm text-gray-500">Placeholder for recipients</p>
          </AccordionSection>

          <AccordionSection title="Contacts" subtitle="Email addresses and phone numbers">
            <p className="text-sm text-gray-500">Placeholder for additional contacts</p>
          </AccordionSection>

          <AccordionSection title="Delivery Addresses" subtitle="Saved delivery addresses">
            <p className="text-sm text-gray-500">Placeholder for addresses</p>
          </AccordionSection>

          <AccordionSection title="Additional Information" subtitle="Preferences and additional notes">
            <p className="text-sm text-gray-500">Placeholder for preferences</p>
          </AccordionSection>
        </div>

        <div className="flex justify-end gap-3 mt-6">
          {isEditing ? (
            <>
              <button onClick={() => handleCancel()} className="px-4 py-2 bg-white border border-gray-300 rounded-lg">Cancel</button>
              <button onClick={() => handleSave()} disabled={isSaving} className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50">
                {isSaving ? 'Saving...' : 'Save Changes'}
              </button>
            </>
          ) : (
            <button onClick={() => setIsEditing(true)} className="px-4 py-2 bg-white border border-gray-300 rounded-lg">Edit</button>
          )}
        </div>
      </div>
    </div>
  );
}
