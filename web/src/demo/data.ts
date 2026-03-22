export type Category = {
  id: string
  name: string
}

export type Product = {
  id: string
  name: string
  categoryId: string
  price: number
  stock: number
  description: string
  accent: string
  oldPrice: number
  rating: string
  recommendation: string
  reviews: string
}

export type Metric = {
  label: string
  value: string
  detail: string
}

export type Promotion = {
  id: string
  title: string
  message: string
  oldPrice: string
  currentPrice: string
  accent: string
}

export type CartItem = {
  productId: string
  quantity: number
}

export type Reservation = {
  id: string
  date: string
  amount: string
  status: 'Pendiente' | 'Listo' | 'Cancelada'
  delivery: 'Recogida' | 'Entrega'
}

export type Profile = {
  name: string
  email: string
  phone: string
  photoInitials: string
  accountType: string
}

export type SettingOption = {
  id: string
  title: string
  description: string
  enabled: boolean
}

export const categories: Category[] = [
  { id: 'all', name: 'Todas' },
  { id: 'audio', name: 'Audio' },
  { id: 'energia', name: 'Energia' },
  { id: 'accesorios', name: 'Accesorios' },
  { id: 'ofertas', name: 'Ofertas' },
]

export const products: Product[] = [
  {
    id: 'p1',
    name: 'EchoFlow Mini',
    categoryId: 'audio',
    price: 149.9,
    stock: 12,
    description: 'Parlante compacto con perfil de venta rapida y ficha limpia.',
    accent: 'aurora',
    oldPrice: 180,
    rating: '4.8',
    recommendation: '98%',
    reviews: '124 reviews',
  },
  {
    id: 'p2',
    name: 'PowerCore 90W',
    categoryId: 'energia',
    price: 89.5,
    stock: 7,
    description: 'Cargador de alto rendimiento para mostrador y entregas.',
    accent: 'forest',
    oldPrice: 110,
    rating: '4.7',
    recommendation: '96%',
    reviews: '87 reviews',
  },
  {
    id: 'p3',
    name: 'Clip Mount Pro',
    categoryId: 'accesorios',
    price: 24.99,
    stock: 19,
    description: 'Accesorio ligero para ventas recurrentes y reposicion rapida.',
    accent: 'mist',
    oldPrice: 32,
    rating: '4.6',
    recommendation: '94%',
    reviews: '53 reviews',
  },
  {
    id: 'p4',
    name: 'Studio Beam',
    categoryId: 'ofertas',
    price: 199,
    stock: 4,
    description: 'Producto en promo para empujar conversion y ticket medio.',
    accent: 'ember',
    oldPrice: 249,
    rating: '4.9',
    recommendation: '99%',
    reviews: '211 reviews',
  },
]

export const promotions: Promotion[] = [
  {
    id: 'promo-1',
    title: 'Hasta 50% en dispositivos seleccionados',
    message: 'Promocion especial para mover inventario esta semana.',
    oldPrice: '$130.00',
    currentPrice: '$89.50',
    accent: 'forest',
  },
  {
    id: 'promo-2',
    title: 'Liquidacion de accesorios',
    message: 'Descuento por tiempo limitado en productos de mostrador.',
    oldPrice: '$32.00',
    currentPrice: '$24.99',
    accent: 'aurora',
  },
]

export const metrics: Metric[] = [
  { label: 'Ventas del dia', value: '$1,248', detail: '18 operaciones sincronizadas' },
  { label: 'Pendientes offline', value: '03', detail: 'Listas para enviar a Appwrite' },
  { label: 'Alertas', value: '02', detail: 'Una promo y un soporte abierto' },
]

export const cartItems: CartItem[] = [
  { productId: 'p1', quantity: 2 },
  { productId: 'p2', quantity: 1 },
]

export const reservations: Reservation[] = [
  { id: 'rv-1204', date: '2026-03-22', amount: '$210.00', status: 'Pendiente', delivery: 'Entrega' },
  { id: 'rv-1207', date: '2026-03-20', amount: '$89.50', status: 'Listo', delivery: 'Recogida' },
  { id: 'rv-1212', date: '2026-03-18', amount: '$149.90', status: 'Cancelada', delivery: 'Entrega' },
]

export const profile: Profile = {
  name: 'Alejo Diaz',
  email: 'admin@talleralejo.dev',
  phone: '+53 555 0101',
  photoInitials: 'AD',
  accountType: 'Cuenta estandar',
}

export const settingsOptions: SettingOption[] = [
  {
    id: 'dark',
    title: 'Tema oscuro',
    description: 'Controla el tema general de toda la app.',
    enabled: false,
  },
  {
    id: 'notifications',
    title: 'Notificaciones',
    description: 'Recibe alertas sobre pedidos y novedades.',
    enabled: true,
  },
  {
    id: 'haptics',
    title: 'Vibracion',
    description: 'Activa respuesta tactil en interacciones.',
    enabled: true,
  },
]

export function getProduct(productId: string) {
  return products.find((product) => product.id === productId) ?? products[0]
}

export function getPromotion(promotionId: string) {
  return promotions.find((promotion) => promotion.id === promotionId) ?? promotions[0]
}
