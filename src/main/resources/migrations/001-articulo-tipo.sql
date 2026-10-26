-- Compatible con bases anteriores: conserva artículos y clasificaciones existentes.
-- No se infiere la categoría de un medicamento por su ID o descripción.
INSERT INTO tipo_articulo (id_tipo, descripcion)
VALUES (9, 'Sin clasificar')
ON CONFLICT (id_tipo) DO NOTHING;

ALTER TABLE articulo ADD COLUMN IF NOT EXISTS id_tipo INTEGER
    CONSTRAINT fk_articulo_tipo REFERENCES tipo_articulo(id_tipo);

UPDATE articulo SET id_tipo = 9 WHERE id_tipo IS NULL;

ALTER TABLE articulo ALTER COLUMN id_tipo SET NOT NULL;
