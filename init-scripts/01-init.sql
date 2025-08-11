-- Script de inicialización de la base de datos para microservicios
-- Este script crea las tablas necesarias para ambos microservicios

-- Crear la base de datos si no existe
-- CREATE DATABASE microservicios_db;

-- Conectar a la base de datos
-- \c microservicios_db;

-- Crear extensión para UUID si es necesaria
-- CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Tabla de productos (microservicio de productos)
CREATE TABLE IF NOT EXISTS productos (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL UNIQUE,
    precio DECIMAL(10,2) NOT NULL CHECK (precio > 0),
    descripcion TEXT,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0
);

-- Tabla de inventario (microservicio de inventario)
CREATE TABLE IF NOT EXISTS inventario (
    id BIGSERIAL PRIMARY KEY,
    producto_id BIGINT NOT NULL UNIQUE REFERENCES productos(id) ON DELETE CASCADE,
    cantidad INTEGER NOT NULL DEFAULT 0 CHECK (cantidad >= 0),
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0
);

-- Tabla de historial de compras (microservicio de inventario)
CREATE TABLE IF NOT EXISTS historial_compras (
    id BIGSERIAL PRIMARY KEY,
    producto_id BIGINT NOT NULL REFERENCES productos(id) ON DELETE CASCADE,
    cantidad INTEGER NOT NULL CHECK (cantidad > 0),
    precio_unitario DECIMAL(10,2) NOT NULL CHECK (precio_unitario > 0),
    precio_total DECIMAL(10,2) NOT NULL CHECK (precio_total > 0),
    fecha_compra TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    nombre_producto VARCHAR(255)
);

-- Crear índices para mejorar el rendimiento
CREATE INDEX IF NOT EXISTS idx_productos_nombre ON productos(nombre);
CREATE INDEX IF NOT EXISTS idx_productos_precio ON productos(precio);
CREATE INDEX IF NOT EXISTS idx_productos_fecha_creacion ON productos(fecha_creacion);

CREATE INDEX IF NOT EXISTS idx_inventario_producto_id ON inventario(producto_id);
CREATE INDEX IF NOT EXISTS idx_inventario_cantidad ON inventario(cantidad);
CREATE INDEX IF NOT EXISTS idx_inventario_fecha_actualizacion ON inventario(fecha_actualizacion);

CREATE INDEX IF NOT EXISTS idx_historial_compras_producto_id ON historial_compras(producto_id);
CREATE INDEX IF NOT EXISTS idx_historial_compras_fecha_compra ON historial_compras(fecha_compra);
CREATE INDEX IF NOT EXISTS idx_historial_compras_precio_total ON historial_compras(precio_total);

-- Crear función para actualizar automáticamente fecha_actualizacion
CREATE OR REPLACE FUNCTION update_fecha_actualizacion()
RETURNS TRIGGER AS $$
BEGIN
    NEW.fecha_actualizacion = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Crear triggers para actualizar automáticamente fecha_actualizacion
CREATE TRIGGER update_productos_fecha_actualizacion
    BEFORE UPDATE ON productos
    FOR EACH ROW
    EXECUTE FUNCTION update_fecha_actualizacion();

CREATE TRIGGER update_inventario_fecha_actualizacion
    BEFORE UPDATE ON inventario
    FOR EACH ROW
    EXECUTE FUNCTION update_fecha_actualizacion();

-- Insertar datos de ejemplo
INSERT INTO productos (nombre, precio, descripcion) VALUES
    ('Laptop Gaming ASUS ROG', 1299.99, 'Laptop para gaming de alto rendimiento con RTX 4060'),
    ('iPhone 15 Pro', 999.99, 'Smartphone Apple con chip A17 Pro y cámara triple'),
    ('Samsung Galaxy S24', 899.99, 'Smartphone Android con IA integrada'),
    ('MacBook Air M2', 1199.99, 'Laptop Apple con chip M2 y 13 pulgadas'),
    ('iPad Pro 12.9"', 1099.99, 'Tablet profesional con chip M2 y Apple Pencil'),
    ('AirPods Pro', 249.99, 'Auriculares inalámbricos con cancelación de ruido'),
    ('Apple Watch Series 9', 399.99, 'Reloj inteligente con monitor cardíaco'),
    ('Sony WH-1000XM5', 349.99, 'Auriculares over-ear con cancelación de ruido'),
    ('Nintendo Switch OLED', 349.99, 'Consola de videojuegos portátil'),
    ('PlayStation 5', 499.99, 'Consola de videojuegos de nueva generación')
ON CONFLICT (nombre) DO NOTHING;

-- Insertar inventario inicial
INSERT INTO inventario (producto_id, cantidad) VALUES
    (1, 25),
    (2, 50),
    (3, 30),
    (4, 15),
    (5, 20),
    (6, 100),
    (7, 40),
    (8, 35),
    (9, 60),
    (10, 10)
ON CONFLICT (producto_id) DO NOTHING;

-- Crear vistas útiles
CREATE OR REPLACE VIEW vista_productos_inventario AS
SELECT 
    p.id,
    p.nombre,
    p.precio,
    p.descripcion,
    COALESCE(i.cantidad, 0) as cantidad_inventario,
    p.fecha_creacion,
    p.fecha_actualizacion
FROM productos p
LEFT JOIN inventario i ON p.id = i.producto_id
ORDER BY p.nombre;

CREATE OR REPLACE VIEW vista_productos_sin_stock AS
SELECT 
    p.id,
    p.nombre,
    p.precio,
    p.descripcion
FROM productos p
LEFT JOIN inventario i ON p.id = i.producto_id
WHERE i.cantidad IS NULL OR i.cantidad = 0
ORDER BY p.nombre;

CREATE OR REPLACE VIEW vista_productos_inventario_bajo AS
SELECT 
    p.id,
    p.nombre,
    p.precio,
    i.cantidad
FROM productos p
JOIN inventario i ON p.id = i.producto_id
WHERE i.cantidad <= 10
ORDER BY i.cantidad ASC;

-- Crear función para obtener estadísticas de ventas
CREATE OR REPLACE FUNCTION obtener_estadisticas_ventas(
    fecha_inicio TIMESTAMP DEFAULT CURRENT_DATE - INTERVAL '30 days',
    fecha_fin TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)
RETURNS TABLE (
    producto_id BIGINT,
    nombre_producto VARCHAR(255),
    total_compras BIGINT,
    cantidad_total BIGINT,
    precio_total DECIMAL(12,2),
    promedio_precio_unitario DECIMAL(10,2)
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        hc.producto_id,
        hc.nombre_producto,
        COUNT(*) as total_compras,
        SUM(hc.cantidad) as cantidad_total,
        SUM(hc.precio_total) as precio_total,
        AVG(hc.precio_unitario) as promedio_precio_unitario
    FROM historial_compras hc
    WHERE hc.fecha_compra BETWEEN fecha_inicio AND fecha_fin
    GROUP BY hc.producto_id, hc.nombre_producto
    ORDER BY precio_total DESC;
END;
$$ LANGUAGE plpgsql;

-- Comentarios sobre las tablas
COMMENT ON TABLE productos IS 'Tabla que almacena la información de productos';
COMMENT ON TABLE inventario IS 'Tabla que almacena el inventario de cada producto';
COMMENT ON TABLE historial_compras IS 'Tabla que registra el historial de todas las compras realizadas';

COMMENT ON COLUMN productos.id IS 'Identificador único del producto';
COMMENT ON COLUMN productos.nombre IS 'Nombre del producto (único)';
COMMENT ON COLUMN productos.precio IS 'Precio del producto (debe ser mayor a 0)';
COMMENT ON COLUMN productos.descripcion IS 'Descripción opcional del producto';

COMMENT ON COLUMN inventario.producto_id IS 'Referencia al producto (único por producto)';
COMMENT ON COLUMN inventario.cantidad IS 'Cantidad disponible en inventario (debe ser >= 0)';

COMMENT ON COLUMN historial_compras.producto_id IS 'Referencia al producto comprado';
COMMENT ON COLUMN historial_compras.cantidad IS 'Cantidad comprada (debe ser > 0)';
COMMENT ON COLUMN historial_compras.precio_unitario IS 'Precio por unidad al momento de la compra';
COMMENT ON COLUMN historial_compras.precio_total IS 'Precio total de la compra';
COMMENT ON COLUMN historial_compras.fecha_compra IS 'Fecha y hora de la compra';
COMMENT ON COLUMN historial_compras.nombre_producto IS 'Nombre del producto al momento de la compra (para auditoría)';
