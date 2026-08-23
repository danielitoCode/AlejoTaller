/**
 * Domain — Category
 *
 * Maps to the `category` collection in Appwrite.
 * Only active categories should be exposed to customers.
 */
export type CategoryStatus = "active" | "inactive";

export interface Category {
  id: string;
  name: string;
  description: string;
  photoUrl: string | null;
  status: CategoryStatus;
}
