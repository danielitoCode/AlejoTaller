import type { Category } from "../../domain/entity/Category";

export const categoryFixtures: Category[] = [
    {
        id: "cat-all",
        name: "Todas",
        description: "Categoria virtual para mostrar todos los productos",
        photoUrl: null,
        status: "active"
    },
    {
        id: "cat-audio",
        name: "Audio",
        description: "Equipos y accesorios de audio",
        photoUrl: null,
        status: "active"
    },
    {
        id: "cat-power",
        name: "Energia",
        description: "Fuentes, baterias y cargadores",
        photoUrl: null,
        status: "active"
    },
    {
        id: "cat-accessories",
        name: "Accesorios",
        description: "Accesorios de mostrador",
        photoUrl: null,
        status: "active"
    }
];
