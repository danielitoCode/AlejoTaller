-- =============================================================================
-- AlejoTaller / dash_alejo_taller — Turso (libSQL / SQLite)
-- Alineado a entidades de dominio Core1–Core5 + Auth0 subject como user_id
-- Ejecutar en consola Turso o: turso db shell <db> < TURSO_SCHEMA.sql
-- =============================================================================

PRAGMA foreign_keys = ON;

-- ---------------------------------------------------------------------------
-- 0. profiles (opcional, NO auth — Auth0 es fuente de identidad)
-- id = Auth0 user.sub (google-oauth2|… | auth0|…)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS profiles (
    id              TEXT PRIMARY KEY,          -- Auth0 sub
    email           TEXT,
    display_name    TEXT,
    picture_url     TEXT,
    phone           TEXT,
    role_override   TEXT,                      -- opcional si roles no van solo en Auth0 claim
    created_at      TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at      TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE INDEX IF NOT EXISTS idx_profiles_email ON profiles(email);

-- ---------------------------------------------------------------------------
-- 1. categories
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS categories (
    id              TEXT PRIMARY KEY,
    name            TEXT NOT NULL,
    description     TEXT NOT NULL DEFAULT '',
    photo_url       TEXT,
    status          TEXT NOT NULL DEFAULT 'active'
        CHECK (status IN ('active', 'inactive')),
    created_at      TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at      TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE INDEX IF NOT EXISTS idx_categories_status ON categories(status);

-- ---------------------------------------------------------------------------
-- 2. products  (web + dash; dash añade last_unit_cost, status, price protection)
-- available = max(0, existence - reserved)  → NO se persiste
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS products (
    id                          TEXT PRIMARY KEY,
    name                        TEXT NOT NULL,
    description                 TEXT NOT NULL DEFAULT '',
    existence                   INTEGER NOT NULL DEFAULT 0 CHECK (existence >= 0),
    reserved                    INTEGER NOT NULL DEFAULT 0 CHECK (reserved >= 0),
    price                       REAL NOT NULL DEFAULT 0 CHECK (price >= 0),  -- USD
    photo_url                   TEXT NOT NULL DEFAULT '',                   -- URL o JSON array string
    category_id                 TEXT NOT NULL REFERENCES categories(id),
    status                      TEXT NOT NULL DEFAULT 'active'
        CHECK (status IN ('active', 'inactive')),
    rating                      REAL DEFAULT 0,
    last_unit_cost              REAL,                                      -- Core2 COGS base, USD
    price_protected_at          TEXT,                                      -- Core3
    price_protection_entry_id   TEXT,                                      -- Core3 → purchase_entries
    created_at                  TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at                  TEXT NOT NULL DEFAULT (datetime('now')),
    CHECK (existence >= reserved)
);

CREATE INDEX IF NOT EXISTS idx_products_category ON products(category_id);
CREATE INDEX IF NOT EXISTS idx_products_status ON products(status);
CREATE INDEX IF NOT EXISTS idx_products_created ON products(created_at);

-- ---------------------------------------------------------------------------
-- 3. sales + sale_items  (items normalizados; JSON opcional de respaldo)
-- user_id = Auth0 sub del cliente
-- verified: UNVERIFIED | VERIFIED | DELETED
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sales (
    id                  TEXT PRIMARY KEY,
    date_iso            TEXT NOT NULL,                     -- Sale.date
    amount              REAL NOT NULL DEFAULT 0 CHECK (amount >= 0),
    verified            TEXT NOT NULL DEFAULT 'UNVERIFIED'
        CHECK (verified IN ('UNVERIFIED', 'VERIFIED', 'DELETED')),
    currency            TEXT NOT NULL DEFAULT 'USD'
        CHECK (currency IN ('CUP', 'USD', 'MLC')),
    user_id             TEXT NOT NULL,                     -- Auth0 sub
    delivery_type       TEXT
        CHECK (delivery_type IS NULL OR delivery_type IN ('PICKUP', 'DELIVERY')),
    -- DeliveryAddress aplanado (nullable si PICKUP)
    delivery_province       TEXT,
    delivery_municipality   TEXT,
    delivery_main_street    TEXT,
    delivery_between_streets TEXT,
    delivery_phone          TEXT,
    delivery_house_number   TEXT,
    delivery_reference_name TEXT,
    sale_type           TEXT
        CHECK (sale_type IS NULL OR sale_type IN ('NORMAL', 'DISCOUNT', 'GIFT')),
    stock_hold_applied  INTEGER NOT NULL DEFAULT 0 CHECK (stock_hold_applied IN (0, 1)),
    -- respaldo JSON del array products legacy (opcional)
    products_json       TEXT,
    created_at          TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at          TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE INDEX IF NOT EXISTS idx_sales_user ON sales(user_id);
CREATE INDEX IF NOT EXISTS idx_sales_verified ON sales(verified);
CREATE INDEX IF NOT EXISTS idx_sales_date ON sales(date_iso);
CREATE INDEX IF NOT EXISTS idx_sales_user_verified ON sales(user_id, verified);

CREATE TABLE IF NOT EXISTS sale_items (
    id                  TEXT PRIMARY KEY,
    sale_id             TEXT NOT NULL REFERENCES sales(id) ON DELETE CASCADE,
    product_id          TEXT NOT NULL REFERENCES products(id),
    product_name        TEXT,
    quantity            INTEGER NOT NULL CHECK (quantity > 0),
    unit_price          REAL,                              -- efectivo
    list_unit_price     REAL,                              -- lista auditoría
    price_legacy        REAL,                              -- deprecated SaleItem.price
    line_order          INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_sale_items_sale ON sale_items(sale_id);
CREATE INDEX IF NOT EXISTS idx_sale_items_product ON sale_items(product_id);

-- ---------------------------------------------------------------------------
-- 4. suppliers + purchase_entries + purchase_entry_lines  (Core2/3 dash)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS suppliers (
    id          TEXT PRIMARY KEY,
    name        TEXT NOT NULL,
    contact     TEXT NOT NULL DEFAULT '',
    notes       TEXT,
    created_at  TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at  TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS purchase_entries (
    id                      TEXT PRIMARY KEY,
    supplier_id             TEXT REFERENCES suppliers(id),
    reference               TEXT,
    entry_date_iso          TEXT NOT NULL,
    total_cost              REAL NOT NULL DEFAULT 0 CHECK (total_cost >= 0),
    currency                TEXT NOT NULL DEFAULT 'USD'
        CHECK (currency IN ('USD', 'CUP')),
    user_id                 TEXT NOT NULL,                 -- Auth0 sub operador
    notes                   TEXT,
    line_count              INTEGER NOT NULL DEFAULT 0 CHECK (line_count >= 0),
    status                  TEXT NOT NULL DEFAULT 'ACTIVE'
        CHECK (status IN ('ACTIVE', 'CANCELLED')),
    exchange_rate           REAL,                          -- CUP por 1 USD si currency=CUP
    exchange_rate_at        TEXT,
    exchange_rate_source    TEXT
        CHECK (exchange_rate_source IS NULL OR exchange_rate_source IN ('DIRECTORIO_CUBANO', 'manual')),
    created_at              TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at              TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE INDEX IF NOT EXISTS idx_purchase_entries_supplier ON purchase_entries(supplier_id);
CREATE INDEX IF NOT EXISTS idx_purchase_entries_date ON purchase_entries(entry_date_iso);
CREATE INDEX IF NOT EXISTS idx_purchase_entries_status ON purchase_entries(status);
CREATE INDEX IF NOT EXISTS idx_purchase_entries_user ON purchase_entries(user_id);

CREATE TABLE IF NOT EXISTS purchase_entry_lines (
    id              TEXT PRIMARY KEY,
    entry_id        TEXT NOT NULL REFERENCES purchase_entries(id) ON DELETE CASCADE,
    product_id      TEXT NOT NULL REFERENCES products(id),
    quantity        INTEGER NOT NULL CHECK (quantity > 0),
    unit_cost       REAL NOT NULL DEFAULT 0 CHECK (unit_cost >= 0),
    concept         TEXT NOT NULL DEFAULT 'purchase'
        CHECK (concept IN ('purchase', 'royalty', 'other')),
    line_cost       REAL NOT NULL DEFAULT 0 CHECK (line_cost >= 0),
    line_order      INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_purchase_lines_entry ON purchase_entry_lines(entry_id);
CREATE INDEX IF NOT EXISTS idx_purchase_lines_product ON purchase_entry_lines(product_id);

-- ---------------------------------------------------------------------------
-- 5. stock_movements  (Core2)
-- type: entrada | salida_venta | ajuste | devolucion
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS stock_movements (
    id              TEXT PRIMARY KEY,
    product_id      TEXT NOT NULL REFERENCES products(id),
    type            TEXT NOT NULL
        CHECK (type IN ('entrada', 'salida_venta', 'ajuste', 'devolucion')),
    quantity        INTEGER NOT NULL CHECK (quantity > 0),
    balance_after   INTEGER NOT NULL CHECK (balance_after >= 0),
    reason          TEXT NOT NULL,
    user_id         TEXT NOT NULL,                         -- Auth0 sub
    sale_id         TEXT,                                  -- FK lógica sales
    entry_id        TEXT,                                  -- FK lógica purchase_entries
    created_at      TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE INDEX IF NOT EXISTS idx_stock_movements_product ON stock_movements(product_id);
CREATE INDEX IF NOT EXISTS idx_stock_movements_sale ON stock_movements(sale_id);
CREATE INDEX IF NOT EXISTS idx_stock_movements_entry ON stock_movements(entry_id);
CREATE INDEX IF NOT EXISTS idx_stock_movements_created ON stock_movements(created_at);
CREATE INDEX IF NOT EXISTS idx_stock_movements_type ON stock_movements(type);

-- ---------------------------------------------------------------------------
-- 6. sale_finance_events + lines  (Core2/4)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sale_finance_events (
    id          TEXT PRIMARY KEY,
    sale_id     TEXT NOT NULL REFERENCES sales(id),
    revenue     REAL NOT NULL DEFAULT 0 CHECK (revenue >= 0),
    cogs        REAL NOT NULL DEFAULT 0 CHECK (cogs >= 0),
    margin      REAL NOT NULL DEFAULT 0,
    user_id     TEXT NOT NULL,
    at_iso      TEXT NOT NULL,
    currency    TEXT,
    created_at  TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_sale_finance_sale ON sale_finance_events(sale_id);
CREATE INDEX IF NOT EXISTS idx_sale_finance_at ON sale_finance_events(at_iso);

CREATE TABLE IF NOT EXISTS sale_finance_lines (
    id                  TEXT PRIMARY KEY,
    event_id            TEXT NOT NULL REFERENCES sale_finance_events(id) ON DELETE CASCADE,
    product_id          TEXT NOT NULL REFERENCES products(id),
    quantity            INTEGER NOT NULL CHECK (quantity > 0),
    unit_price          REAL NOT NULL DEFAULT 0 CHECK (unit_price >= 0),
    unit_cost_snapshot  REAL NOT NULL DEFAULT 0 CHECK (unit_cost_snapshot >= 0),
    line_revenue        REAL NOT NULL DEFAULT 0,
    line_cogs           REAL NOT NULL DEFAULT 0,
    line_margin         REAL NOT NULL DEFAULT 0,
    line_order          INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_sale_finance_lines_event ON sale_finance_lines(event_id);
CREATE INDEX IF NOT EXISTS idx_sale_finance_lines_product ON sale_finance_lines(product_id);

-- ---------------------------------------------------------------------------
-- 7. promotions  (web + dash)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS promotions (
    id                      TEXT PRIMARY KEY,
    product_id              TEXT REFERENCES products(id),
    title                   TEXT NOT NULL,
    message                 TEXT NOT NULL DEFAULT '',
    image_url               TEXT,
    old_price               REAL,
    current_price           REAL,
    valid_from_epoch_ms     INTEGER NOT NULL,
    valid_until_epoch_ms    INTEGER NOT NULL,
    source                  TEXT DEFAULT 'manual'
        CHECK (source IS NULL OR source IN ('automatic', 'manual')),
    kind                    TEXT DEFAULT 'product_discount'
        CHECK (kind IS NULL OR kind IN ('product_discount', 'banner')),
    status                  TEXT DEFAULT 'draft'
        CHECK (status IS NULL OR status IN ('draft', 'active', 'ended', 'cancelled')),
    created_at              TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at              TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE INDEX IF NOT EXISTS idx_promotions_product ON promotions(product_id);
CREATE INDEX IF NOT EXISTS idx_promotions_status ON promotions(status);
CREATE INDEX IF NOT EXISTS idx_promotions_window ON promotions(valid_from_epoch_ms, valid_until_epoch_ms);

-- ---------------------------------------------------------------------------
-- 8. workshop_reservations  (Core2 B5 / Core6 taller — no afecta stock producto)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS workshop_reservations (
    id                  TEXT PRIMARY KEY,
    client_name         TEXT NOT NULL,
    client_phone        TEXT,
    client_user_id      TEXT,                              -- Auth0 sub cliente opcional
    equipment           TEXT NOT NULL,
    service_type        TEXT NOT NULL,
    status              TEXT NOT NULL DEFAULT 'requested',
    scheduled_at_iso    TEXT NOT NULL,
    duration_minutes    INTEGER CHECK (duration_minutes IS NULL OR duration_minutes >= 15),
    notes               TEXT,
    staff_user_id       TEXT,
    created_by          TEXT NOT NULL,                     -- Auth0 sub
    channel             TEXT NOT NULL DEFAULT 'dash',
    created_at          TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at          TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE INDEX IF NOT EXISTS idx_workshop_status ON workshop_reservations(status);
CREATE INDEX IF NOT EXISTS idx_workshop_scheduled ON workshop_reservations(scheduled_at_iso);
CREATE INDEX IF NOT EXISTS idx_workshop_client_user ON workshop_reservations(client_user_id);

-- ---------------------------------------------------------------------------
-- 9. cup_exchanges  (tasa de cambio cacheable)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS cup_exchanges (
    id              TEXT PRIMARY KEY,
    usd_reference   REAL NOT NULL CHECK (usd_reference > 0),
    euro_reference  REAL NOT NULL CHECK (euro_reference > 0),
    updated_at      TEXT NOT NULL,
    source          TEXT NOT NULL DEFAULT 'DIRECTORIO_CUBANO'
);

CREATE INDEX IF NOT EXISTS idx_cup_exchanges_updated ON cup_exchanges(updated_at);

-- ---------------------------------------------------------------------------
-- 10. support_messages (mínimo — ampliar cuando migréis soporte)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS support_threads (
    id              TEXT PRIMARY KEY,
    user_id         TEXT NOT NULL,                         -- Auth0 sub cliente
    subject         TEXT,
    status          TEXT NOT NULL DEFAULT 'open',
    unread_user     INTEGER NOT NULL DEFAULT 0,
    unread_staff    INTEGER NOT NULL DEFAULT 0,
    created_at      TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at      TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE INDEX IF NOT EXISTS idx_support_threads_user ON support_threads(user_id);

CREATE TABLE IF NOT EXISTS support_messages (
    id              TEXT PRIMARY KEY,
    thread_id       TEXT NOT NULL REFERENCES support_threads(id) ON DELETE CASCADE,
    sender_id       TEXT NOT NULL,                         -- Auth0 sub
    body            TEXT NOT NULL,
    is_staff        INTEGER NOT NULL DEFAULT 0 CHECK (is_staff IN (0, 1)),
    created_at      TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE INDEX IF NOT EXISTS idx_support_messages_thread ON support_messages(thread_id);

-- =============================================================================
-- Vistas útiles (equivalentes a cálculos de dominio)
-- =============================================================================
CREATE VIEW IF NOT EXISTS v_products_available AS
SELECT
    p.*,
    MAX(0, p.existence - p.reserved) AS available
FROM products p;

CREATE VIEW IF NOT EXISTS v_sales_pending AS
SELECT * FROM sales WHERE verified = 'UNVERIFIED';

-- =============================================================================
-- Notas de alineación dominio ↔ SQL
-- =============================================================================
-- profiles.id / sales.user_id / stock_movements.user_id / … = Auth0 sub
-- Product.photoUrl puede ser string URL o JSON de varias URLs → photo_url TEXT
-- Sale.products[] → sale_items (+ products_json opcional para migración)
-- SaleFinanceEvent.lines[] → sale_finance_lines
-- No hay tabla de passwords: Auth0
-- Soft-hold: reserved en products; stock_hold_applied en sales
