-- BASE DE DATOS ABASTECIMIENTO (medicamentos para centros de salud SIS)

CREATE TABLE IF NOT EXISTS tipo_articulo (
    id_tipo      INTEGER      PRIMARY KEY,
    descripcion  VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS unidad_medida (
    id_unidad    INTEGER     PRIMARY KEY,
    descripcion  VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS proveedor (
    id_proveedor INTEGER      PRIMARY KEY,
    ruc          CHAR(11)     NOT NULL UNIQUE,
    nombre       VARCHAR(150) NOT NULL
);

CREATE TABLE IF NOT EXISTS articulo (
    id_articulo   INTEGER       PRIMARY KEY,
    descripcion   VARCHAR(150)  NOT NULL,
    precio        DECIMAL(9,2)  NOT NULL CHECK (precio >= 0),
    unidad_medida INTEGER       NOT NULL REFERENCES unidad_medida(id_unidad),
    id_proveedor  INTEGER       NOT NULL REFERENCES proveedor(id_proveedor),
    id_tipo       INTEGER       NOT NULL CONSTRAINT fk_articulo_tipo REFERENCES tipo_articulo(id_tipo)
);
