import { describe, it, expect, vi } from "vitest";
import { CustomerService } from "../../src/services/customer.service.js";
import type { IUserRepository } from "../../src/repositories/user.repository.js";
import type { User } from "../../src/domain/user.js";
import type { McpAuthContext } from "../../src/auth/context.js";

describe("CustomerService", () => {
  const mockUser: User = {
    id: "usr-123",
    name: "Alejo Test",
    email: "alejo@test.com",
    phone: "+5355555555",
    photoUrl: "https://example.com/photo.jpg",
    role: "customer",
    verified: true,
    labels: [],
  };

  const mockUserRepo: IUserRepository = {
    getById: vi.fn().mockResolvedValue(mockUser),
    updateProfile: vi.fn().mockImplementation(async (userId, update) => ({
      ...mockUser,
      ...update,
    })),
  };

  const service = new CustomerService(mockUserRepo);

  it("should get profile for authenticated user", async () => {
    const auth: McpAuthContext = {
      userId: "usr-123",
      userName: "Alejo Test",
      userEmail: "alejo@test.com",
    };

    const profile = await service.getMyProfile(auth);
    expect(profile.id).toBe("usr-123");
    expect(profile.name).toBe("Alejo Test");
    expect(mockUserRepo.getById).toHaveBeenCalledWith("usr-123");
  });

  it("should update profile for authenticated user", async () => {
    const auth: McpAuthContext = {
      userId: "usr-123",
      userName: "Alejo Test",
      userEmail: "alejo@test.com",
    };

    const updated = await service.updateMyProfile(auth, { name: "Nuevo Nombre" });
    expect(updated.name).toBe("Nuevo Nombre");
    expect(mockUserRepo.updateProfile).toHaveBeenCalledWith("usr-123", {
      name: "Nuevo Nombre",
    });
  });

  it("should fail if user is unauthenticated", async () => {
    const auth: McpAuthContext = {
      userId: "",
      userName: "",
      userEmail: "",
    };

    await expect(service.getMyProfile(auth)).rejects.toThrow("User is not authenticated");
  });
});
