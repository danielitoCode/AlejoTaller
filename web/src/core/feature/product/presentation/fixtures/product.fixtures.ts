import type { Product } from "../../domain/entity/Product";

export type ProductCardUi = {
    product: Product;
    accent: "aurora" | "forest" | "mist" | "ember";
    oldPrice: number;
    recommendation: string;
    reviews: string;
};

export const productFixtures: ProductCardUi[] = [
    {
        product: {
            id: "p1",
            name: "EchoFlow Mini",
            description: "Parlante compacto con perfil de venta rapida.",
            price: 149.9,
            photoUrl: "",
            categoryId: "cat-audio",
            rating: 4.8
        },
        accent: "aurora",
        oldPrice: 180,
        recommendation: "98%",
        reviews: "124 reviews"
    },
    {
        product: {
            id: "p2",
            name: "PowerCore 90W",
            description: "Cargador de alto rendimiento para mostrador.",
            price: 89.5,
            photoUrl: "",
            categoryId: "cat-power",
            rating: 4.7
        },
        accent: "forest",
        oldPrice: 110,
        recommendation: "96%",
        reviews: "87 reviews"
    },
    {
        product: {
            id: "p3",
            name: "Clip Mount Pro",
            description: "Accesorio ligero para ventas recurrentes.",
            price: 24.99,
            photoUrl: "",
            categoryId: "cat-accessories",
            rating: 4.6
        },
        accent: "mist",
        oldPrice: 32,
        recommendation: "94%",
        reviews: "53 reviews"
    }
];
