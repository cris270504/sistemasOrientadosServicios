"""Verifica la migración real en un esquema aislado y revierte todo al terminar.

Requiere el servicio postgres de Docker Compose. No modifica tablas públicas.
Ejecutar: python scripts/test_tipo_migration.py
"""
from pathlib import Path
import secrets
import subprocess


def main():
    migration = (Path(__file__).resolve().parents[1]
                 / "src/main/resources/migrations/001-articulo-tipo.sql").read_text(encoding="utf-8")
    schema = "test_tipo_" + secrets.token_hex(6)
    sql = f"""BEGIN;
CREATE SCHEMA {schema};
SET LOCAL search_path TO {schema};
CREATE TABLE tipo_articulo (id_tipo INTEGER PRIMARY KEY, descripcion VARCHAR(100) NOT NULL);
CREATE TABLE articulo (id_articulo INTEGER PRIMARY KEY, descripcion VARCHAR(150) NOT NULL,
                       precio DECIMAL(9,2) NOT NULL);
INSERT INTO tipo_articulo VALUES (1, 'Tipo existente');
INSERT INTO articulo VALUES (101, 'Artículo existente', 12.34);
{migration}
DO $$ BEGIN
    ASSERT (SELECT count(*) = 1 FROM articulo), 'Cambió el número de artículos';
    ASSERT (SELECT descripcion = 'Artículo existente' AND precio = 12.34 AND id_tipo = 9
            FROM articulo WHERE id_articulo = 101), 'No se conservó el artículo';
    BEGIN
        UPDATE articulo SET id_tipo = 99999 WHERE id_articulo = 101;
        RAISE EXCEPTION 'Se aceptó un tipo inexistente';
    EXCEPTION WHEN foreign_key_violation THEN NULL;
    END;
    BEGIN
        UPDATE articulo SET id_tipo = NULL WHERE id_articulo = 101;
        RAISE EXCEPTION 'Se aceptó un tipo nulo';
    EXCEPTION WHEN not_null_violation THEN NULL;
    END;
END $$;
UPDATE articulo SET id_tipo = 1 WHERE id_articulo = 101;
{migration}
DO $$ BEGIN
    ASSERT (SELECT id_tipo = 1 FROM articulo WHERE id_articulo = 101),
           'La segunda ejecución sobrescribió la clasificación';
    ASSERT (SELECT count(*) = 2 FROM tipo_articulo), 'Se duplicaron los tipos';
END $$;
ROLLBACK;
"""
    subprocess.run(["docker", "compose", "exec", "-T", "postgres", "psql", "-U", "postgres",
                    "-d", "abastecimiento", "-v", "ON_ERROR_STOP=1"],
                   input=sql, text=True, encoding="utf-8", check=True)
    print("OK: migración conserva datos, exige un tipo válido y puede repetirse sin sobrescribirlo.")


if __name__ == "__main__":
    main()
