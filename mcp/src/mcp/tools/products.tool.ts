import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { z } from "zod";
import type { ProductService } from "../../services/product.service.js";
import type { CategoryService } from "../../services/category.service.js";

/**
 * Product & Category tools — Explore catalog.
 */
export function registerProductTools(
  server: McpServer,
  productService: ProductService,
  categoryService: CategoryService
): void {
  // ─── list_products ──────────────────────────────────────────────────────
  server.tool(
    "list_products",
    "Consulta el catálogo de productos disponibles en AlejoTaller (precios, disponibilidad, fotos y categoría).",
    {
      categoryId: z
        .string()
        .optional()
        .describe("Filtrar productos opcionalmente por ID de categoría"),
    },
    async (args, _extra) => {
      try {
        const products = args.categoryId
          ? await productService.listByCategory(args.categoryId)
          : await productService.listProducts();

        return {
          content: [
            {
              type: "text",
              text: JSON.stringify(products, null, 2),
            },
          ],
        };
      } catch (err: unknown) {
        const message = err instanceof Error ? err.message : String(err);
        return {
          isError: true,
          content: [{ type: "text", text: `Error al listar productos: ${message}` }],
        };
      }
    }
  );

  // ─── get_product ────────────────────────────────────────────────────────
  server.tool(
    "get_product",
    "Obtiene los detalles completos de un producto específico por su ID.",
    {
      productId: z.string().describe("ID único del producto"),
    },
    async (args, _extra) => {
      try {
        const product = await productService.getProduct(args.productId);
        if (!product) {
          return {
            isError: true,
            content: [{ type: "text", text: `Producto no encontrado: ${args.productId}` }],
          };
        }
        return {
          content: [
            {
              type: "text",
              text: JSON.stringify(product, null, 2),
            },
          ],
        };
      } catch (err: unknown) {
        const message = err instanceof Error ? err.message : String(err);
        return {
          isError: true,
          content: [{ type: "text", text: `Error al obtener producto: ${message}` }],
        };
      }
    }
  );

  // ─── list_categories ────────────────────────────────────────────────────
  server.tool(
    "list_categories",
    "Obtiene la lista de categorías activas de productos y servicios de AlejoTaller.",
    {},
    async (_args, _extra) => {
      try {
        const categories = await categoryService.listCategories();
        return {
          content: [
            {
              type: "text",
              text: JSON.stringify(categories, null, 2),
            },
          ],
        };
      } catch (err: unknown) {
        const message = err instanceof Error ? err.message : String(err);
        return {
          isError: true,
          content: [{ type: "text", text: `Error al listar categorías: ${message}` }],
        };
      }
    }
  );

  // ─── get_category ───────────────────────────────────────────────────────
  server.tool(
    "get_category",
    "Obtiene la información de una categoría específica por su ID.",
    {
      categoryId: z.string().describe("ID de la categoría"),
    },
    async (args, _extra) => {
      try {
        const category = await categoryService.getCategory(args.categoryId);
        if (!category) {
          return {
            isError: true,
            content: [{ type: "text", text: `Categoría no encontrada: ${args.categoryId}` }],
          };
        }
        return {
          content: [
            {
              type: "text",
              text: JSON.stringify(category, null, 2),
            },
          ],
        };
      } catch (err: unknown) {
        const message = err instanceof Error ? err.message : String(err);
        return {
          isError: true,
          content: [{ type: "text", text: `Error al obtener categoría: ${message}` }],
        };
      }
    }
  );
}
