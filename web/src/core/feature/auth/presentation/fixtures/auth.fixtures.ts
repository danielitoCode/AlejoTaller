import type { User } from "../../domain/entity/User";

export const profileFixture: User = {
    id: "user-1",
    name: "Alejo Diaz",
    email: "admin@talleralejo.dev",
    phone: "+53 555 0101",
    password: "",
    photo_url: "",
    sub: "",
    verification: true,
    role: "admin",
    status: true,
    labels: ["dashboard"]
};
