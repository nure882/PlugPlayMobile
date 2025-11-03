import {useEffect, useState} from 'react';
import AccordionSection from '../components/profile/AccordionSection.tsx';
import {PlusCircle, Trash2, Pencil, Check, X} from 'lucide-react';
import {Address} from '../models/Address.ts';
import {validateName, validateEmail, validatePhone} from '../utils/validation.ts';
import {useGetUserByTokenQuery,  useUpdateUserByTokenMutation} from '../api/userInfoApi.ts';
import {storage} from '../utils/StorageService';
import LoadingMessage from '../components/common/LoadingMessage.tsx';
import ErrorMessage from '../components/common/ErrorMessage.tsx';

type Errors = {
  firstName: string;
  lastName: string;
  phone: string;
  email: string;
  address: {
    city: string;
    street: string;
    house: string;
  };
};

const initialErrors: Errors = {
  firstName: '', lastName: '', phone: '', email: '',
  address: {city: '', street: '', house: ''}
};

export default function Profile() {
  // initialize empty; will be populated from token endpoint if available
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [phone, setPhone] = useState('');
  const [email, setEmail] = useState('');

  const [addresses, setAddresses] = useState<Address[]>([
    {city: '', street: '', house: '', apartments: ''}
  ]);
  const [newAddress, setNewAddress] = useState<Address>({
    id: undefined, city: '', street: '', house: '', apartments: ''
  });

  const [addressEditIndex, setEditIndex] = useState<number | null>(null);
  const [editedAddress, setEditedAddress] = useState<any>(null);

  const [isEditing, setIsEditing] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [errors, setErrors] = useState<Errors>(initialErrors);
  const [addressEditErrors, setAddressEditErrors] = useState<{ [key: string]: string }>({});

  const [initialData, setInitialData] = useState({firstName: '', lastName: '', phone, email});

  const token = storage.getAccessToken();

  const {data: tokenUser, isLoading, isError, refetch}  = useGetUserByTokenQuery(token ?? '', {skip: !token});
  const [updateUserByToken] = useUpdateUserByTokenMutation();


  // o(tokenUser as object);
  useEffect(() => {
    // populate first/last from token endpoint on load, but don't overwrite while editing
    if (tokenUser && !isEditing) {
      console.log(tokenUser);

      setFirstName(tokenUser.firstName);
      setLastName(tokenUser.lastName);
      setEmail(tokenUser.email);
      setPhone(tokenUser.phoneNumber)
      setAddresses(tokenUser.addresses)
      setInitialData(prev => ({...prev, firstName: tokenUser.firstName, lastName: tokenUser.lastName}));
    }
  }, [tokenUser, isEditing]);

  const validateField = (fieldName: keyof Omit<Errors, 'address'>, value: string) => {
    let errorMessage = '';
    switch (fieldName) {
      case 'firstName':
        if (!validateName(value)) errorMessage = 'Only Latin/Cyrillic letters and numbers, 2-30 characters';
        break;
      case 'lastName':
        if (!validateName(value)) errorMessage = 'Only Latin/Cyrillic letters and numbers, 2-30 characters';
        break;
      case 'phone':
        if (!validatePhone(value)) errorMessage = 'International format: +country code + number';
        break;
      case 'email':
        if (!validateEmail(value)) errorMessage = 'Invalid email format';
        break;
    }
    setErrors(prev => ({...prev, [fieldName]: errorMessage}));
  };

  const handleChange = (setter: React.Dispatch<React.SetStateAction<string>>, fieldName: keyof Omit<Errors, 'address'>) => (e: React.ChangeEvent<HTMLInputElement>) => {
    const {value} = e.target;
    setter(value);
    validateField(fieldName, value);
  };

  const validateForm = (): boolean => {
    const newErrors: Errors = {...initialErrors, address: errors.address};

    if (!validateName(firstName)) newErrors.firstName = 'Only Latin/Cyrillic letters and numbers, 2-30 characters';
    if (!validateName(lastName)) newErrors.lastName = 'Only Latin/Cyrillic letters and numbers, 2-30 characters';
    if (!validatePhone(phone)) newErrors.phone = 'International format: +country code + number';
    if (!validateEmail(email)) newErrors.email = 'Invalid email format';

    setErrors(newErrors);

    return !Object.values(newErrors).some(error => typeof error === 'string' && error.length > 0);
  };

  const handleStartEdit = () => {
    setInitialData({firstName, lastName, phone, email});
    setIsEditing(true);
  };

  const handleSave = async () => {
    if (!validateForm()) return;

    setIsSaving(true);
    try {
      const updated = [...addresses];
      updated[addressEditIndex!] = editedAddress;
      setAddresses(updated);
      setEditIndex(null);

      await updateUserByToken({
        token : token ?? '',
        firstName : firstName,
        lastName : lastName,
        email : email,
        phoneNumber : phone,
        addresses : addresses
      })
      setIsEditing(false);
      setErrors(initialErrors);

      refetch();
    } catch (err) {
      console.error('Failed to save profile:', err);
    } finally {
      setIsSaving(false);
    }
  };

  const handleCancel = () => {
    setFirstName(initialData.firstName);
    setLastName(initialData.lastName);
    setPhone(initialData.phone);
    setEmail(initialData.email);

    setEditIndex(null);
    setEditedAddress(null);

    setIsEditing(false);
    setErrors(initialErrors);
  };

  const handleAddAddress = () => {
    const addressErrors = {city: '', street: '', house: ''};
    let isValid = true;
    if (!newAddress.city) {
      addressErrors.city = 'City is required';
      isValid = false;
    }
    if (!newAddress.street) {
      addressErrors.street = 'Street is required';
      isValid = false;
    }
    if (!newAddress.house) {
      addressErrors.house = 'House number is required';
      isValid = false;
    }

    if (!isValid) {
      setErrors(prev => ({...prev, address: addressErrors}));
      return;
    }

    setAddresses(prev => [...prev, newAddress]);
    setNewAddress({city: '', street: '', house: '', apartments: ''});
    setErrors(prev => ({...prev, address: initialErrors.address}));
  };

  const handleDeleteAddress = (index: number) => {
    setAddresses(prev => prev.filter((_, i) => i !== index));
  };

   const handleEditAddress = (index: number) => {
    setEditIndex(index);
    setEditedAddress({ ...addresses[index] }); 
    setAddressEditErrors({});
  };

   const handleAddressCancelEdit = () => {
    setEditIndex(null);
    setEditedAddress(null);
     setAddressEditErrors({});
  };

  const handleAddressSaveEdit = () => {
    if (addressEditIndex === null || !editedAddress)
    {
      return; 
    } 

    if (!validateAddress(editedAddress))
    {
      return;
    } 
    const updated = [...addresses];
    updated[addressEditIndex] = editedAddress;
    setAddresses(updated);
    setEditIndex(null);
    setEditedAddress(null);
  };

  const validateAddress = (address: Address): boolean => {
    const newErrors: { [key: string]: string } = {};

    if (!address.city?.trim()) newErrors.city = "City is required.";
    if (!address.street?.trim()) newErrors.street = "Street is required.";
    if (!address.house?.trim()) newErrors.house = "House number is required.";

    setAddressEditErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  if (isLoading) {
    return LoadingMessage("profile page");
  } 

  if(isError) {
    return ErrorMessage("error loading personal page", "couldn't retrieve data from the database")
  }

  return (
    <div className="min-h-screen bg-gray-50">
      
      <div className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm mb-6">
          <h1 className="text-xl font-semibold text-black">Personal Information</h1>
          <p className="text-sm text-gray-500 mt-1">Manage your personal details, delivery addresses, and account
            preferences</p>
        </div>

        <div className="bg-white border border-gray-200 rounded-lg shadow-sm overflow-hidden">
          <AccordionSection title="My Account" subtitle="Account information and login credentials" defaultOpen>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-black mb-1">First Name</label>
                {isEditing ? (
                  <>
                    <input value={firstName} onChange={handleChange(setFirstName, 'firstName')}
                           className="w-full px-3 py-2 border rounded-lg border-gray-300"/>
                    {errors.firstName && <p className="text-red-500 text-xs mt-1">{errors.firstName}</p>}
                  </>
                ) : <div className="text-sm text-gray-700">{firstName}</div>}
              </div>
              <div>
                <label className="block text-sm font-medium text-black mb-1">Last Name</label>
                {isEditing ? (
                  <>
                    <input value={lastName} onChange={handleChange(setLastName, 'lastName')}
                           className="w-full px-3 py-2 border rounded-lg border-gray-300"/>
                    {errors.lastName && <p className="text-red-500 text-xs mt-1">{errors.lastName}</p>}
                  </>
                ) : <div className="text-sm text-gray-700">{lastName}</div>}
              </div>
            </div>
          </AccordionSection>

          <AccordionSection title="Contacts" subtitle="Email addresses and phone numbers">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-black mb-1">Phone</label>
                {isEditing ? (
                  <>
                    <input value={phone} onChange={handleChange(setPhone, 'phone')}
                           className="w-full px-3 py-2 border rounded-lg border-gray-300"/>
                    {errors.phone && <p className="text-red-500 text-xs mt-1">{errors.phone}</p>}
                  </>
                ) : <div className="text-sm text-gray-700">{phone}</div>}
              </div>
              <div>
                <label className="block text-sm font-medium text-black mb-1">Email</label>
                {isEditing ? (
                  <>
                    <input value={email} onChange={handleChange(setEmail, 'email')}
                           className="w-full px-3 py-2 border rounded-lg border-gray-300"/>
                    {errors.email && <p className="text-red-500 text-xs mt-1">{errors.email}</p>}
                  </>
                ) : <div className="text-sm text-gray-700">{email}</div>}
              </div>
            </div>
          </AccordionSection>

          <AccordionSection title="Delivery Addresses" subtitle="Saved delivery addresses">
            <div className="space-y-6">
               <div className="space-y-6">
                {(addresses ?? []).map((address, index) => (
                  <div key={index} className="p-4 border border-gray-200 rounded-lg">
                    {addressEditIndex === index && editedAddress ? (
                      <>
                        <h4 className="text-sm font-medium text-black mb-4">
                          Edit Address
                        </h4>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                          <div>
                            <label className="block text-sm font-medium text-black mb-1">
                              City
                            </label>
                            <input
                              value={editedAddress.city}
                              onChange={(e) =>
                                setEditedAddress({
                                  ...editedAddress,
                                  city: e.target.value,
                                })
                              }
                              className={`w-full px-3 py-2 border rounded-lg ${
                                addressEditErrors.city ? "border-red-500" : "border-gray-300"
                              }`}
                              placeholder="Enter city"
                            />
                            {addressEditErrors.city && (
                              <p className="text-red-500 text-xs mt-1">{addressEditErrors.city}</p>
                            )}
                          </div>

                          <div>
                            <label className="block text-sm font-medium text-black mb-1">
                              Street
                            </label>
                            <input
                              value={editedAddress.street}
                              onChange={(e) =>
                                setEditedAddress({
                                  ...editedAddress,
                                  street: e.target.value,
                                })
                              }
                              className={`w-full px-3 py-2 border rounded-lg ${
                                addressEditErrors.street ? "border-red-500" : "border-gray-300"
                              }`}
                              placeholder="Enter street"
                            />
                            {addressEditErrors.street && (
                              <p className="text-red-500 text-xs mt-1">{addressEditErrors.street}</p>
                            )}
                          </div>

                          <div>
                            <label className="block text-sm font-medium text-black mb-1">
                              House
                            </label>
                            <input
                              value={editedAddress.house}
                              onChange={(e) =>
                                setEditedAddress({
                                  ...editedAddress,
                                  house: e.target.value,
                                })
                              }
                              className={`w-full px-3 py-2 border rounded-lg ${
                                addressEditErrors.house ? "border-red-500" : "border-gray-300"
                              }`}
                              placeholder="Enter house number"
                            />
                            {addressEditErrors.house && (
                              <p className="text-red-500 text-xs mt-1">{addressEditErrors.house}</p>
                            )}
                          </div>

                          <div>
                            <label className="block text-sm font-medium text-black mb-1">
                              Apartment (optional)
                            </label>
                            <input
                              value={editedAddress.apartments ?? ""}
                              onChange={(e) =>
                                setEditedAddress({
                                  ...editedAddress,
                                  apartments: e.target.value,
                                })
                              }
                              className="w-full px-3 py-2 border rounded-lg border-gray-300"
                              placeholder="Enter apartment number"
                            />
                          </div>
                        </div>

                        <div className="flex space-x-3 mt-4">
                          <button
                            onClick={handleAddressSaveEdit}
                            className="text-green-600 hover:text-green-700"
                            aria-label="Save address"
                          >
                            <Check className="w-4 h-4 inline-block mr-1" /> Save
                          </button>
                          <button
                            onClick={handleAddressCancelEdit}
                            className="text-gray-600 hover:text-gray-700"
                            aria-label="Cancel editing"
                          >
                            <X className="w-4 h-4 inline-block mr-1" /> Cancel
                          </button>
                        </div>
                      </>
                    ) : (
                      <div className="flex items-start justify-between">
                        <div className="space-y-1">
                          <div className="text-sm font-medium text-gray-900">
                            {address.street}, {address.house}
                            {address.apartments ? `, apt ${address.apartments}` : ""}
                          </div>
                          <div className="text-sm text-gray-500">{address.city}</div>
                        </div>

                        {isEditing && (
                          <div className="flex items-center space-x-3">
                            <button
                              onClick={() => handleEditAddress(index)}
                              className="text-blue-600 hover:text-blue-700"
                              aria-label="Edit address"
                            >
                              <Pencil className="w-4 h-4" />
                            </button>
                            <button
                              onClick={() => handleDeleteAddress(index)}
                              className="text-red-600 hover:text-red-700"
                              aria-label="Delete address"
                            >
                              <Trash2 className="w-4 h-4" />
                            </button>
                          </div>
                        )}
                      </div>
                    )}
                  </div>
                ))}
              </div>
            {isEditing && (
              <div className="mt-4 p-4 border border-gray-200 rounded-lg">
                <h4 className="text-sm font-medium text-black mb-4">Add New Address</h4>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-black mb-1">City</label>
                    <input
                      value={newAddress.city}
                      onChange={e => setNewAddress(prev => ({...prev, city: e.target.value}))}
                      className="w-full px-3 py-2 border rounded-lg border-gray-300"
                      placeholder="Enter city"
                    />
                    {errors.address.city && <p className="text-red-500 text-xs mt-1">{errors.address.city}</p>}
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-black mb-1">Street</label>
                    <input
                      value={newAddress.street}
                      onChange={e => setNewAddress(prev => ({...prev, street: e.target.value}))}
                      className="w-full px-3 py-2 border rounded-lg border-gray-300"
                      placeholder="Enter street"
                    />
                    {errors.address.street && <p className="text-red-500 text-xs mt-1">{errors.address.street}</p>}
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-black mb-1">House</label>
                    <input
                      value={newAddress.house}
                      onChange={e => setNewAddress(prev => ({...prev, house: e.target.value}))}
                      className="w-full px-3 py-2 border rounded-lg border-gray-300"
                      placeholder="Enter house number"
                    />
                    {errors.address.house && <p className="text-red-500 text-xs mt-1">{errors.address.house}</p>}
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-black mb-1">Apartment (optional)</label>
                    <input
                      value={newAddress.apartments}
                      onChange={e => setNewAddress(prev => ({...prev, apartments: e.target.value}))}
                      className="w-full px-3 py-2 border rounded-lg border-gray-300"
                      placeholder="Enter apartment number"
                    />
                  </div>
                </div>
                <button
                  onClick={handleAddAddress}
                  className="mt-4 flex items-center gap-2 px-4 py-2 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 text-sm font-medium"
                >
                  <PlusCircle className="w-4 h-4"/>
                  <span>Add Address</span>
                </button>
              </div>
            )}
          </div>
          </AccordionSection>

          <AccordionSection title="My Orders" subtitle="Your order history">
            <p className="text-sm text-gray-500">Your orders will appear here</p>
          </AccordionSection>
        </div>

        <div className="flex justify-end gap-3 mt-6">
          {isEditing ? (
            <>
              <button onClick={handleCancel}
                      className="px-4 py-2 bg-white border border-gray-300 rounded-lg hover:bg-gray-50">Cancel
              </button>
              <button onClick={handleSave} disabled={isSaving}
                      className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50">
                {isSaving ? 'Saving...' : 'Save Changes'}
              </button>
            </>
          ) : (
            <button onClick={handleStartEdit}
                    className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700">Edit</button>
          )}
        </div>
      </div>
    </div>
  );
}