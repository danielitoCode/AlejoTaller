import type { Users } from "node-appwrite";
import type { IUserRepository } from "../../../repositories/user.repository.js";
import type { User, UserProfileUpdate } from "../../../domain/user.js";

/**
 * Appwrite implementation of IUserRepository.
 *
 * Uses the node-appwrite Users API (server SDK) to look up customers.
 * Profile updates go through the Users.updatePrefs() method.
 */
export class AppwriteUserRepository implements IUserRepository {
  constructor(private readonly users: Users) {}

  async getById(userId: string): Promise<User> {
    const doc = await this.users.get(userId);

    const prefs = (doc.prefs ?? {}) as Record<string, unknown>;
    const labels = Array.isArray(doc.labels) ? (doc.labels as string[]) : [];

    const roleFromLabels = labels.length
      ? labels.includes("admin")
        ? "admin"
        : (labels[0] ?? null)
      : null;

    const role =
      roleFromLabels ??
      (typeof prefs["role"] === "string" ? prefs["role"] : null);

    const photoUrl =
      typeof prefs["photo_url"] === "string"
        ? prefs["photo_url"]
        : typeof prefs["photoUrl"] === "string"
          ? prefs["photoUrl"]
          : "";

    const phone =
      typeof prefs["phone"] === "string"
        ? prefs["phone"]
        : (doc.phone ?? "");

    return {
      id: doc.$id,
      name: doc.name,
      email: doc.email,
      phone,
      photoUrl,
      role,
      verified: doc.emailVerification,
      labels,
    };
  }

  async updateProfile(userId: string, update: UserProfileUpdate): Promise<User> {
    const prefsUpdate: Record<string, unknown> = {};

    if (update.name !== undefined) {
      await this.users.updateName(userId, update.name);
    }

    if (update.phone !== undefined) {
      prefsUpdate["phone"] = update.phone;
    }

    if (update.photoUrl !== undefined) {
      prefsUpdate["photo_url"] = update.photoUrl;
      prefsUpdate["photoUrl"] = update.photoUrl;
    }

    if (Object.keys(prefsUpdate).length > 0) {
      const current = await this.users.get(userId);
      const existingPrefs = (current.prefs ?? {}) as Record<string, unknown>;
      await this.users.updatePrefs(userId, { ...existingPrefs, ...prefsUpdate });
    }

    // Return the updated user
    return this.getById(userId);
  }
}
