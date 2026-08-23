import type { IUserRepository } from "../repositories/user.repository.js";
import type { User, UserProfileUpdate } from "../domain/user.js";
import type { McpAuthContext } from "../auth/context.js";
import { AuthenticationError } from "../auth/context.js";

/**
 * Service — Customer
 * Business logic for customer profile operations.
 */
export class CustomerService {
  constructor(private readonly userRepo: IUserRepository) {}

  /**
   * Get authenticated user's profile.
   */
  async getMyProfile(auth: McpAuthContext): Promise<User> {
    if (!auth.userId) {
      throw new AuthenticationError("User is not authenticated");
    }
    return await this.userRepo.getById(auth.userId);
  }

  /**
   * Update allowed profile fields for authenticated user.
   */
  async updateMyProfile(
    auth: McpAuthContext,
    update: UserProfileUpdate
  ): Promise<User> {
    if (!auth.userId) {
      throw new AuthenticationError("User is not authenticated");
    }
    return await this.userRepo.updateProfile(auth.userId, update);
  }
}
