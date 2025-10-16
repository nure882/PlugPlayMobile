import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Chrome, Loader2 } from 'lucide-react';
import Header from '../components/Header';
import { registerUser } from '../lib/api';
import { validateName, validateEmail, validatePhone, validatePassword } from '../lib/validation';

export default function SignUp() {
  const navigate = useNavigate();
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [phone, setPhone] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  
  const [fieldErrors, setFieldErrors] = useState({
    firstName: '', lastName: '', phone: '', email: '', password: '', confirmPassword: ''
  });


  const validateField = (fieldName: keyof typeof fieldErrors, value: string) => {
    let errorMessage = '';
    switch (fieldName) {
      case 'firstName':
        if (value && !validateName(value)) errorMessage = 'Only Latin/Cyrillic letters and numbers, 2-30 characters';
        break;
      case 'lastName':
        if (value && !validateName(value)) errorMessage = 'Only Latin/Cyrillic letters and numbers, 2-30 characters';
        break;
      case 'phone':
        if (value && !validatePhone(value)) errorMessage = 'International format: +country code + number';
        break;
      case 'email':
        if (value && !validateEmail(value)) errorMessage = 'Invalid email format';
        break;
      case 'password':
        if (value && !validatePassword(value)) errorMessage = 'Min 8 chars: digit, lowercase, uppercase, special';
        break;
      case 'confirmPassword':
        if (value && value !== password) errorMessage = 'Passwords do not match';
        break;
    }
    setFieldErrors(prev => ({ ...prev, [fieldName]: errorMessage }));
  };

  
  const handleChange = (setter: React.Dispatch<React.SetStateAction<string>>, fieldName: keyof typeof fieldErrors) => (e: React.ChangeEvent<HTMLInputElement>) => {
    const { value } = e.target;
    setter(value);
    validateField(fieldName, value);
   
    if (fieldName === 'password' && confirmPassword) {
        validateField('confirmPassword', confirmPassword);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess(false);

    
    const newFieldErrors = {
      firstName: validateName(firstName) ? '' : 'Only Latin/Cyrillic letters and numbers, 2-30 characters',
      lastName: validateName(lastName) ? '' : 'Only Latin/Cyrillic letters and numbers, 2-30 characters',
      phone: validatePhone(phone) ? '' : 'International format: +country code + number',
      email: validateEmail(email) ? '' : 'Invalid email format',
      password: validatePassword(password) ? '' : 'Min 8 chars: digit, lowercase, uppercase, special',
      confirmPassword: password === confirmPassword ? '' : 'Passwords do not match',
    };
    setFieldErrors(newFieldErrors);

    const isValid = Object.values(newFieldErrors).every(err => err === '');
    if (!isValid) return;

    setIsLoading(true);
    try {
      const userData = { firstName, lastName, email, password, phoneNumber: phone };
      const response = await registerUser(userData);
      
      if (response.success) {
        setSuccess(true);
        setFirstName(''); setLastName(''); setPhone(''); setEmail(''); setPassword(''); setConfirmPassword('');
        setFieldErrors({ firstName: '', lastName: '', phone: '', email: '', password: '', confirmPassword: '' });
        setTimeout(() => navigate('/signin'), 1500);
      } else {
        setError(response.message || 'Registration failed');
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Registration failed. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-white">
      <Header />
      <div className="flex items-center justify-center px-4">
        <div className="w-full max-w-md py-10">
          <div className="text-center mb-8">
            <p className="text-2xl md:text-3xl font-semibold text-black">Create your account</p>
          </div>

          <div className="bg-white border border-gray-200 rounded-lg p-8 shadow-sm">
            {error && <div className="mb-4 p-3 bg-red-50 border border-red-200 text-red-700 rounded text-sm">{error}</div>}
            {success && <div className="mb-4 p-3 bg-green-50 border border-green-200 text-green-700 rounded text-sm">Account created! Redirecting...</div>}

            <form onSubmit={handleSubmit} className="space-y-4" noValidate>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label htmlFor="firstName" className="block text-sm font-medium text-black mb-1">First Name</label>
                  <input id="firstName" type="text" value={firstName} onChange={handleChange(setFirstName, 'firstName')} className={`w-full px-4 py-2 border rounded-lg ${fieldErrors.firstName ? 'border-red-300' : 'border-gray-300'}`} placeholder="John" />
                  {fieldErrors.firstName && <p className="text-red-500 text-xs mt-1">{fieldErrors.firstName}</p>}
                </div>
                <div>
                  <label htmlFor="lastName" className="block text-sm font-medium text-black mb-1">Last Name</label>
                  <input id="lastName" type="text" value={lastName} onChange={handleChange(setLastName, 'lastName')} className={`w-full px-4 py-2 border rounded-lg ${fieldErrors.lastName ? 'border-red-300' : 'border-gray-300'}`} placeholder="Doe" />
                  {fieldErrors.lastName && <p className="text-red-500 text-xs mt-1">{fieldErrors.lastName}</p>}
                </div>
              </div>

              <div>
                <label htmlFor="phone" className="block text-sm font-medium text-black mb-1">Phone</label>
                <input id="phone" type="tel" value={phone} onChange={handleChange(setPhone, 'phone')} className={`w-full px-4 py-2 border rounded-lg ${fieldErrors.phone ? 'border-red-300' : 'border-gray-300'}`} placeholder="+380123456789" />
                {fieldErrors.phone && <p className="text-red-500 text-xs mt-1">{fieldErrors.phone}</p>}
              </div>
              <div>
                <label htmlFor="email" className="block text-sm font-medium text-black mb-1">Email</label>
                <input id="email" type="email" value={email} onChange={handleChange(setEmail, 'email')} className={`w-full px-4 py-2 border rounded-lg ${fieldErrors.email ? 'border-red-300' : 'border-gray-300'}`} placeholder="john@example.com" />
                {fieldErrors.email && <p className="text-red-500 text-xs mt-1">{fieldErrors.email}</p>}
              </div>
              <div>
                <label htmlFor="password" className="block text-sm font-medium text-black mb-1">Password</label>
                <input id="password" type="password" value={password} onChange={handleChange(setPassword, 'password')} className={`w-full px-4 py-2 border rounded-lg ${fieldErrors.password ? 'border-red-300' : 'border-gray-300'}`} placeholder="••••••••" />
                {fieldErrors.password && <p className="text-red-500 text-xs mt-1">{fieldErrors.password}</p>}
              </div>
              <div>
                <label htmlFor="confirmPassword" className="block text-sm font-medium text-black mb-1">Confirm Password</label>
                <input id="confirmPassword" type="password" value={confirmPassword} onChange={handleChange(setConfirmPassword, 'confirmPassword')} className={`w-full px-4 py-2 border rounded-lg ${fieldErrors.confirmPassword ? 'border-red-300' : 'border-gray-300'}`} placeholder="••••••••" />
                {fieldErrors.confirmPassword && <p className="text-red-500 text-xs mt-1">{fieldErrors.confirmPassword}</p>}
              </div>
              <button type="submit" disabled={isLoading} className="w-full bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700 transition-colors font-medium disabled:opacity-50 flex items-center justify-center gap-2">
                {isLoading ? (<><Loader2 className="w-4 h-4 animate-spin" />Registering...</>) : 'Sign Up'}
              </button>
            </form>

            <div className="relative my-6">
                <div className="absolute inset-0 flex items-center"><div className="w-full border-t border-gray-300"></div></div>
                <div className="relative flex justify-center text-sm"><span className="px-2 bg-white text-gray-500">or</span></div>
            </div>
            <button className="w-full flex items-center justify-center gap-3 border border-gray-300 bg-white text-black py-2 rounded-lg hover:bg-gray-50 transition-colors font-medium">
                <Chrome className="w-5 h-5" />
                Continue with Google
            </button>
            <p className="text-center text-sm text-gray-600 mt-6">
                Already have an account?{' '}
                <Link to="/signin" className="text-blue-600 hover:text-blue-700 font-medium">Sign In</Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}

