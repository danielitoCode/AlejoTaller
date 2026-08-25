import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { z } from "zod";
import type { ProductService } from "../../services/product.service.js";
import type { CategoryService } from "../../services/category.service.js";
import { okJson, failText, runTool } from "./barrel.js";

/**
 * Product & Category tools — catálogo público (sin auth).
 */
export function registerProductTools(
  server: McpServer,
  productService: ProductService,
  categoryService: CategoryService
): void {
  server.tool(
    "list_products",
    "Consulta el catálogo de productos (precio, disponibilidad = existence − reserved, categoría).",
    {
      categoryId: z
        .string()
        .min(1)
        .optional()
        .describe("Filtrar productos por ID de categoría"),
    },
    async (args, extra) =>
      runTool("list_products", "Listar productos", extra, null, async () => {
        const products = args.categoryId
          ? await productService.listByCategory(args.categoryId)
          : await productService.listProducts();
        return okJson(products);
      })
  );

  server.tool(
    "get_product",
    "Obtiene los detalles de un producto por su ID.",
    {
      productId: z.string().min(1).describe("ID único del producto"),
    },
    async (args, extra) =>
      runTool("get_product", "Obtener producto", extra, null, async () => {
        const product = await productService.getProduct(args.productId);
        if (!product) {
          return failText(`Producto no encontrado: ${args.productId}`);
        }
        return okJson(product);
      })
  );

  server.tool(
    "list_categories",
    "Lista las categorías activas de productos y servicios de AlejoTaller.",
    {},
    async (_args, extra) =>
      runTool("list_categories", "Listar categorías", extra, null, async () => {
        const categories = await categoryService.listCategories();
        return okJson(categories);
      })
  );

  server.tool(
    "get_category",
    "Obtiene una categoría por su ID.",
    {
      categoryId: z.string().min(1).describe("ID de la categoría"),
    },
    async (args, extra) =>
      runTool("get_category", "Obtener categoría", extra, null, async () => {
        const category = await categoryService.getCategory(args.categoryId);
        if (!category) {
          return failText(`Categoría no encontrada: ${args.categoryId}`);
        }
        return okJson(category);
      })
  );
}
