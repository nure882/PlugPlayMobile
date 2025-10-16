/**
 * Validates first or last name.
 * Allows Latin/Cyrillic letters and numbers, 2-30 characters.
 */
export const validateName = (name: string): boolean => {
  const nameRegex = /^[a-zA-Zа-яА-ЯёЁ0-9]{2,30}$/;
  return nameRegex.test(name);
};

/**
 * Validates email address.
 */
export const validateEmail = (email: string): boolean => {
  const emailRegex = /^[a-zA-Z0-9]([a-zA-Z0-9._+-]*[a-zA-Z0-9])?@[a-zA-Z0-9]([a-zA-Z0-9-]*[a-zA-Z0-9])?(\.[a-zA-Z0-9]([a-zA-Z0-9-]*[a-zA-Z0-9])?)*\.[a-zA-Z]{2,}$/;
  return emailRegex.test(email) && !email.includes('..');
};

/**
 * Validates international phone number.
 * Must start with '+' and be between 10 and 15 digits long.
 */
export const validatePhone = (phone: string): boolean => {
  const cleanPhone = phone.replace(/[^\d+]/g, '');
  const phoneRegex = /^\+[1-9]\d{1,14}$/;
  return phoneRegex.test(cleanPhone) && cleanPhone.length >= 10 && cleanPhone.length <= 15;
};

/**
 * Validates password strength.
 * - At least 8 characters
 * - At least one digit
 * - At least one lowercase letter
 * - At least one uppercase letter
 * - At least one special character
 * - More than one unique character
 */
export const validatePassword = (password: string): boolean => {
  if (password.length < 8) return false;
  
  const hasDigit = /\d/.test(password);
  const hasLowercase = /[a-zа-яё]/.test(password);
  const hasUppercase = /[A-ZА-ЯЁ]/.test(password);
  const hasNonAlphanumeric = /[^a-zA-Zа-яА-ЯёЁ0-9]/.test(password);
  const uniqueChars = new Set(password).size;
  const hasUniqueChar = uniqueChars > 1;

  return hasDigit && hasLowercase && hasUppercase && hasNonAlphanumeric && hasUniqueChar;
};
