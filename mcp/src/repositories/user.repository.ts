import type { User, UserProfileUpdate } from "../domain/user.js";

/**
 * Repository interface — User
 *
 * Implementations: AppwriteUserRepository, MockUserRepository (tests)
 * The MCP server uses the server-side Appwrite SDK (node-appwrite),
 * not the client SDK, so we can look up users by userId via the Users API.
 */
export interface IUserRepository {
  /**
   * Get a user's profile by their Appwrite userId.
   * Used internally by services after auth resolution.
   */
  getById(userId: string): Promise<User>;

  /**
   * Update allowed profile fields for a customer.
   * Phone is stored in Appwrite prefs (not the account phone field).
   */
  updateProfile(userId: string, update: UserProfileUpdate): Promise<User>;
}
