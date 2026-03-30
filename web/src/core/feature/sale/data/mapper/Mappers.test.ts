import { describe, expect, it } from "vitest";
import { saleFromDTO, saleToDTO } from "./Mappers";
import { BuyState, DeliveryType } from "../../domain/entity/enums";

describe("sale mappers", () => {
    it("serializa la direccion de entrega cuando la venta es a domicilio", () => {
        const dto = saleToDTO({
            id: "sale-1",
            date: "2026-03-29",
            amount: 40,
            verified: BuyState.UNVERIFIED,
            userId: "user-1",
            deliveryType: DeliveryType.DELIVERY,
            deliveryAddress: {
                province: "La Habana",
                municipality: "Playa",
                mainStreet: "42",
                betweenStreets: "1ra y 3ra",
                phone: "55555555",
                houseNumber: "1204",
                referenceName: "Leo"
            },
            products: []
        });

        expect(dto.delivery_address).toContain("\"province\":\"La Habana\"");
        expect(dto.delivery_type).toBe(DeliveryType.DELIVERY);
    });

    it("hidrata la direccion de entrega desde el dto", () => {
        const sale = saleFromDTO({
            $id: "sale-1",
            $sequence: 1,
            $createdAt: "",
            $updatedAt: "",
            $databaseId: "",
            $collectionId: "",
            $permissions: [],
            date: "2026-03-29",
            amount: 40,
            buy_state: BuyState.UNVERIFIED,
            products: "[]",
            user_id: "user-1",
            delivery_type: DeliveryType.DELIVERY,
            delivery_address: JSON.stringify({
                province: "La Habana",
                municipality: "Playa",
                mainStreet: "42",
                betweenStreets: null,
                phone: "55555555",
                houseNumber: "1204",
                referenceName: null
            })
        });

        expect(sale.deliveryAddress?.municipality).toBe("Playa");
        expect(sale.deliveryAddress?.houseNumber).toBe("1204");
    });
});
