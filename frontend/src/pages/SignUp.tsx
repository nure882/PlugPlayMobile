import {useState} from 'react';
import {useNavigate, Link} from 'react-router-dom';
import {Loader2} from 'lucide-react';
import Header from '../components/Header';
import { useRegisterMutation} from '../lib/redux/authApi.ts';
import { API_BASE_URL } from '../lib/redux/baseApi.ts';
import {GoogleLogin} from "@react-oauth/google";
import {storage} from "../lib/utils/StorageService.ts";
import {validateName, validateEmail, validatePhone, validatePassword} from '../lib/validation';
import { useAuth } from '../lib/context/AuthContext.tsx';


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
  const { setUser } = useAuth();


  const [fieldErrors, setFieldErrors] = useState({
    firstName: '',
    lastName: '',
    phone: '',
    email: '',
    password: '',
    confirmPassword: ''
  });

  const [register, {isLoading: isRegistering}] = useRegisterMutation();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess(false);
    setIsLoading(true);

    const newFieldErrors = {
      firstName: validateName(firstName) ? '' : 'Only Latin/Cyrillic letters and numbers, 2-30 characters',
      lastName: validateName(lastName) ? '' : 'Only Latin/Cyrillic letters and numbers, 2-30 characters',
      phone: validatePhone(phone) ? '' : 'International format: +country code + number',
      email: validateEmail(email) ? '' : 'Invalid email format',
      password: validatePassword(password) ? '' : 'Min 8 chars: digit, lowercase, uppercase, special',
      confirmPassword: password === confirmPassword ? '' : 'Passwords do not match',
    };
    setFieldErrors(newFieldErrors);

    // required fields
    if (!firstName || !lastName || !phone || !email || !password || !confirmPassword) {
      setError('All fields are required');
      setIsLoading(false);
      return;
    }

    // per-field validations (keep existing messages)
    if (!validateName(firstName)) {
      setError('First name must contain only Latin/Cyrillic letters and numbers, 2-30 characters');
      setIsLoading(false);
      return;
    }

    if (!validateName(lastName)) {
      setError('Last name must contain only Latin/Cyrillic letters and numbers, 2-30 characters');
      setIsLoading(false);
      return;
    }

    if (!validatePhone(phone)) {
      setError('Please enter a valid international phone number (e.g., +380123456789)');
      setIsLoading(false);
      return;
    }

    if (!validateEmail(email)) {
      setError('Please enter a valid email address');
      setIsLoading(false);
      return;
    }

    if (!validatePassword(password)) {
      setError('Password must contain at least 8 characters including: 1 digit, 1 lowercase letter, 1 uppercase letter, 1 special character, and at least 1 unique character');
      setIsLoading(false);
      return;
    }

    if (password !== confirmPassword) {
      setError('Passwords do not match');
      setIsLoading(false);
      return;
    }

    const isValid = Object.values(newFieldErrors).every(err => err === '');
    if (!isValid) {
      setError('Please fix validation errors');
      setIsLoading(false);
      return;
    }

    try {
      const userData = {
        firstName,
        lastName,
        email,
        password,
        phoneNumber: phone
      };

      await register(userData).unwrap();

      setSuccess(true);
      setFirstName('');
      setLastName('');
      setPhone('');
      setEmail('');
      setPassword('');
      setConfirmPassword('');
      setFieldErrors({
        firstName: '',
        lastName: '',
        phone: '',
        email: '',
        password: '',
        confirmPassword: ''
      });

      setTimeout(() => {
        navigate('/signin');
      }, 1500);
    } catch (err: any) {
      const msg =
        err?.data?.message ||
        err?.data?.error ||
        err?.error ||
        (err instanceof Error ? err.message : null) ||
        'Registration failed. Please try again.';
      setError(msg);
    } finally {
      setIsLoading(false);
    }
  };

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
    setFieldErrors(prev => ({...prev, [fieldName]: errorMessage}));
  };


  const handleChange = (setter: React.Dispatch<React.SetStateAction<string>>, fieldName: keyof typeof fieldErrors) => (e: React.ChangeEvent<HTMLInputElement>) => {
    const {value} = e.target;
    setter(value);
    validateField(fieldName, value);

    if (fieldName === 'password' && confirmPassword) {
      validateField('confirmPassword', confirmPassword);
    }
  };

  const handleGoogleSuccess = async (credentialResponse: any) => {
    try {
      const response = await fetch(
        `${API_BASE_URL}/api/auth/google`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            idToken: credentialResponse.credential
          })
        });

      const data = await response.json();
      console.log(data);

      storage.setTokens(data.token, data.refreshToken);
      setUser(data.user);


      console.log('Login successful:', data.user);
    } catch (error) {
      console.error('Login failed:', error);
    }
  };

  const handleGoogleError = () => {
    console.error('Google Sign-In failed');
  };

  return (
    <div className="min-h-screen bg-white">
      <Header/>
      <div className="flex items-center justify-center px-4">
        <div className="w-full max-w-md py-10">
          <div className="text-center mb-8">
            <p className="text-2xl md:text-3xl font-semibold text-black">Create your account</p>
          </div>

          <div className="bg-white border border-gray-200 rounded-lg p-8 shadow-sm">
            {error && (
              <div className="mb-4 p-3 bg-red-50 border border-red-200 text-red-700 rounded text-sm">
                {error}
              </div>
            )}

            {success && (
              <div className="mb-4 p-3 bg-green-50 border border-green-200 text-green-700 rounded text-sm">
                Account created successfully! Redirecting to sign in...
              </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-4" noValidate>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label htmlFor="firstName" className="block text-sm font-medium text-black mb-1">First Name</label>
                  <input id="firstName" type="text" value={firstName} onChange={handleChange(setFirstName, 'firstName')}
                         className={`w-full px-4 py-2 border rounded-lg ${fieldErrors.firstName ? 'border-red-300' : 'border-gray-300'}`}
                         placeholder="John"/>
                  {fieldErrors.firstName && <p className="text-red-500 text-xs mt-1">{fieldErrors.firstName}</p>}
                </div>
                <div>
                  <label htmlFor="lastName" className="block text-sm font-medium text-black mb-1">Last Name</label>
                  <input id="lastName" type="text" value={lastName} onChange={handleChange(setLastName, 'lastName')}
                         className={`w-full px-4 py-2 border rounded-lg ${fieldErrors.lastName ? 'border-red-300' : 'border-gray-300'}`}
                         placeholder="Doe"/>
                  {fieldErrors.lastName && <p className="text-red-500 text-xs mt-1">{fieldErrors.lastName}</p>}
                </div>
              </div>

              <div>
                <label htmlFor="phone" className="block text-sm font-medium text-black mb-1">Phone</label>
                <input id="phone" type="tel" value={phone} onChange={handleChange(setPhone, 'phone')}
                       className={`w-full px-4 py-2 border rounded-lg ${fieldErrors.phone ? 'border-red-300' : 'border-gray-300'}`}
                       placeholder="+380123456789"/>
                {fieldErrors.phone && <p className="text-red-500 text-xs mt-1">{fieldErrors.phone}</p>}
              </div>
              <div>
                <label htmlFor="email" className="block text-sm font-medium text-black mb-1">Email</label>
                <input id="email" type="email" value={email} onChange={handleChange(setEmail, 'email')}
                       className={`w-full px-4 py-2 border rounded-lg ${fieldErrors.email ? 'border-red-300' : 'border-gray-300'}`}
                       placeholder="john@example.com"/>
                {fieldErrors.email && <p className="text-red-500 text-xs mt-1">{fieldErrors.email}</p>}
              </div>
              <div>
                <label htmlFor="password" className="block text-sm font-medium text-black mb-1">Password</label>
                <input id="password" type="password" value={password} onChange={handleChange(setPassword, 'password')}
                       className={`w-full px-4 py-2 border rounded-lg ${fieldErrors.password ? 'border-red-300' : 'border-gray-300'}`}
                       placeholder="••••••••"/>
                {fieldErrors.password && <p className="text-red-500 text-xs mt-1">{fieldErrors.password}</p>}
              </div>
              <div>
                <label htmlFor="confirmPassword" className="block text-sm font-medium text-black mb-1">Confirm
                  Password</label>
                <input id="confirmPassword" type="password" value={confirmPassword}
                       onChange={handleChange(setConfirmPassword, 'confirmPassword')}
                       className={`w-full px-4 py-2 border rounded-lg ${fieldErrors.confirmPassword ? 'border-red-300' : 'border-gray-300'}`}
                       placeholder="••••••••"/>
                {fieldErrors.confirmPassword &&
                  <p className="text-red-500 text-xs mt-1">{fieldErrors.confirmPassword}</p>}
              </div>
              <button type="submit" disabled={isLoading}
                      className="w-full bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700 transition-colors font-medium disabled:opacity-50 flex items-center justify-center gap-2">
                {isLoading ? (<><Loader2 className="w-4 h-4 animate-spin"/>Registering...</>) : 'Sign Up'}
              </button>
            </form>

            <div className="relative my-6">
              <div className="absolute inset-0 flex items-center">
                <div className="w-full border-t border-gray-300"></div>
              </div>
              <div className="relative flex justify-center text-sm">
                <span className="px-2 bg-white text-gray-500">or</span>
              </div>
            </div>

            <div>
              <GoogleLogin
                onSuccess={handleGoogleSuccess}
                onError={handleGoogleError}
                text={"signup_with"}
              />
            </div>
            <p className="text-center text-sm text-gray-600 mt-6">
              Already have an account?{' '}
              <Link to="/signin" className="text-blue-600 hover:text-blue-700 font-medium">
                Sign In
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
