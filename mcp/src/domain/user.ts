/**
 * Domain — User
 *
 * Represents an authenticated AlejoTaller customer.
 * Clean domain entity: no Appwrite annotations, no serialization concerns.
 */
export interface User {
  id: string;
  name: string;
  email: string;
  phone: string;
  photoUrl: string;
  role: string | null;
  verified: boolean;
  labels: string[];
}

/** Fields a customer is allowed to update on their own profile. */
export interface UserProfileUpdate {
  name?: string;
  phone?: string;
  photoUrl?: string;
}
