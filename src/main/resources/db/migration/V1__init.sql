-- Tabla de Productos
CREATE TABLE producto (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tipo VARCHAR(20) NOT NULL CHECK(tipo IN ('MAQUILLAJE','BOLSO','BISUTERIA')),
    nombre VARCHAR(255) NOT NULL,
    marca VARCHAR(100),
    codigo_producto VARCHAR(50) UNIQUE,
    costo DECIMAL(10,3) NOT NULL CHECK(costo >= 0),
    precio_venta DECIMAL(10,3) NOT NULL CHECK(precio_venta >= 0),
    stock_actual INTEGER NOT NULL DEFAULT 0 CHECK(stock_actual >= 0),
    stock_minimo INTEGER NOT NULL DEFAULT 0 CHECK(stock_minimo >= 0),
    activo BOOLEAN NOT NULL DEFAULT 1,
    tono_color VARCHAR(50),
    color VARCHAR(50),
    tamanio VARCHAR(50),
    material VARCHAR(100),
    talla VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Compras
CREATE TABLE compra (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    proveedor VARCHAR(255),
    fecha DATE NOT NULL,
    total_costo DECIMAL(10,3) NOT NULL DEFAULT 0 CHECK(total_costo >= 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Items de Compra
CREATE TABLE compra_item (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    compra_id INTEGER NOT NULL,
    producto_id INTEGER NOT NULL,
    cantidad INTEGER NOT NULL CHECK(cantidad > 0),
    costo_unitario DECIMAL(10,3) NOT NULL CHECK(costo_unitario >= 0),
    FOREIGN KEY (compra_id) REFERENCES compra(id) ON DELETE CASCADE,
    FOREIGN KEY (producto_id) REFERENCES producto(id)
);

-- Índices para mejorar búsquedas
CREATE INDEX idx_producto_nombre ON producto(nombre);
CREATE INDEX idx_producto_codigo ON producto(codigo_producto);
CREATE INDEX idx_producto_tipo ON producto(tipo);
CREATE INDEX idx_compra_fecha ON compra(fecha);

-- Datos seed (opcional - 5 productos de ejemplo)
INSERT INTO producto (tipo, nombre, marca, codigo_producto, costo, precio_venta, stock_actual, stock_minimo, activo, tono_color)
VALUES ('MAQUILLAJE', 'Labial Mate', 'MAC', 'MAC-LAB-001', 15.00, 30.00, 10, 3, 1, 'Ruby Woo');

INSERT INTO producto (tipo, nombre, marca, codigo_producto, costo, precio_venta, stock_actual, stock_minimo, activo, tono_color)
VALUES ('MAQUILLAJE', 'Base Líquida', 'Maybelline', 'MAY-BASE-002', 12.50, 25.00, 2, 5, 1, 'Ivory');

INSERT INTO producto (tipo, nombre, marca, codigo_producto, costo, precio_venta, stock_actual, stock_minimo, activo, color, material)
VALUES ('BOLSO', 'Bolso Tote Clásico', 'Local', 'BOLSO-001', 20.00, 45.00, 8, 2, 1, 'Negro', 'Cuero sintético');

INSERT INTO producto (tipo, nombre, marca, codigo_producto, costo, precio_venta, stock_actual, stock_minimo, activo, color, material)
VALUES ('BISUTERIA', 'Aretes Largos Dorados', NULL, 'ARET-001', 3.50, 10.00, 15, 5, 1, 'Dorado', 'Aleación');

INSERT INTO producto (tipo, nombre, marca, codigo_producto, costo, precio_venta, stock_actual, stock_minimo, activo, color, material)
VALUES ('BISUTERIA', 'Collar Perlas', NULL, 'COLLAR-001', 5.00, 15.00, 1, 3, 1, 'Blanco', 'Perlas sintéticas');

