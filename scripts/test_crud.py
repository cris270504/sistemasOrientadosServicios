"""Prueba HTTP del CRUD real con registros propios y limpieza al finalizar.

Ejecutar con la aplicación en localhost:8080: python scripts/test_crud.py
No modifica ni elimina registros preexistentes.
"""
import json
import secrets
from urllib.error import HTTPError
from urllib.request import Request, urlopen

BASE = "http://localhost:8080"
checks = 0


def request(method, path, body=None):
    payload = json.dumps(body).encode() if body is not None else None
    req = Request(BASE + path, data=payload, method=method,
                  headers={"Content-Type": "application/json"} if payload else {})
    try:
        response = urlopen(req, timeout=15)
    except HTTPError as error:
        response = error
    with response:
        raw = response.read().decode()
        return response.status, json.loads(raw) if raw else None, response.headers


def expect(method, path, status, body=None):
    global checks
    actual, data, headers = request(method, path, body)
    assert actual == status, (method, path, status, actual, data)
    checks += 1
    return data, headers


def unused_id(resource):
    for _ in range(10):
        candidate = 1_000_000_000 + secrets.randbelow(900_000_000)
        status, _, _ = request("GET", f"/api/{resource}/{candidate}")
        if status == 404:
            return candidate
    raise RuntimeError("No se encontró un ID libre para pruebas.")


def main():
    # Comprueba también que Thymeleaf renderiza las páginas completas.
    for page in ("/", "/api", "/api/"):
        with urlopen(BASE + page, timeout=15) as response:
            html = response.read().decode()
            assert response.status == 200 and "</html>" in html, page
    proveedor_id = unused_id("proveedores")
    articulo_id = unused_id("articulos")
    proveedor_created = articulo_created = False
    provider_path = f"/api/proveedores/{proveedor_id}"
    article_path = f"/api/articulos/{articulo_id}"
    initial_providers, _ = expect("GET", "/api/proveedores", 200)
    used_rucs = {p["ruc"] for p in initial_providers}
    while True:
        ruc = str(90_000_000_000 + secrets.randbelow(999_999_999))
        if ruc not in used_rucs:
            break
    proveedor = {"idProveedor": proveedor_id, "ruc": ruc, "nombre": "Proveedor de prueba CRUD"}
    unidades, _ = expect("GET", "/api/unidades-medida", 200)
    assert unidades, "Se requiere al menos una unidad de medida."
    tipos, _ = expect("GET", "/api/tipos-articulo", 200)
    assert len(tipos) >= 2, "Se requieren dos tipos para probar la reclasificación."
    articulo = {"idArticulo": articulo_id, "descripcion": "Medicamento de prueba CRUD", "precio": 12.34,
                "idUnidad": unidades[0]["idUnidad"], "idProveedor": proveedor_id, "idTipo": tipos[0]["idTipo"]}
    try:
        data, headers = expect("POST", "/api/proveedores", 201, proveedor)
        proveedor_created = True
        assert data["idProveedor"] == proveedor_id
        assert headers["Location"].endswith(provider_path)
        expect("POST", "/api/proveedores", 409, {**proveedor, "nombre": "No debe sobrescribir"})
        data, _ = expect("GET", provider_path, 200)
        assert data["nombre"] == proveedor["nombre"]
        expect("POST", "/api/proveedores", 409, {**proveedor, "idProveedor": proveedor_id + 1})
        expect("POST", "/api/proveedores", 400, {"ruc": "123", "nombre": ""})
        expect("POST", "/api/proveedores", 400, {"ruc": ruc, "nombre": "Sin ID"})
        data, _ = expect("PUT", provider_path, 200, {"ruc": ruc, "nombre": "Proveedor CRUD actualizado"})
        assert data["nombre"] == "Proveedor CRUD actualizado"
        expect("PUT", provider_path, 400, {**proveedor, "idProveedor": proveedor_id + 1})

        data, headers = expect("POST", "/api/articulos", 201, articulo)
        articulo_created = True
        assert headers["Location"].endswith(article_path)
        assert data["proveedor"]["idProveedor"] == proveedor_id
        assert data["tipoArticulo"]["idTipo"] == articulo["idTipo"]
        expect("POST", "/api/articulos", 409, {**articulo, "descripcion": "No debe sobrescribir"})
        data, _ = expect("GET", article_path, 200)
        assert data["descripcion"] == articulo["descripcion"]
        assert data["tipoArticulo"]["idTipo"] == articulo["idTipo"]
        expect("POST", "/api/articulos", 400, {**articulo, "precio": -1})
        expect("POST", "/api/articulos", 400, {**articulo, "precio": 1.234})
        expect("POST", "/api/articulos", 400, {**articulo, "descripcion": " "})
        expect("POST", "/api/articulos", 400, {**articulo, "idArticulo": None})
        expect("PUT", article_path, 400, {**articulo, "idProveedor": None})
        missing_supplier = unused_id("proveedores")
        expect("PUT", article_path, 400, {**articulo, "idProveedor": missing_supplier})
        expect("PUT", article_path, 400, {**articulo, "idUnidad": 2_000_000_000})
        expect("PUT", article_path, 400, {k: v for k, v in articulo.items() if k != "idTipo"})
        expect("PUT", article_path, 400, {**articulo, "idTipo": None})
        expect("PUT", article_path, 400, {**articulo, "idTipo": 0})
        expect("PUT", article_path, 400, {**articulo, "idTipo": 2_000_000_000})
        data, _ = expect("GET", article_path, 200)
        assert data["tipoArticulo"]["idTipo"] == articulo["idTipo"], "Un tipo inválido no debe alterar el artículo."
        expect("PUT", article_path, 400, {**articulo, "idArticulo": articulo_id + 1})
        expect("DELETE", provider_path, 409)
        data, _ = expect("PUT", article_path, 200, {**articulo, "descripcion": "Medicamento CRUD actualizado", "precio": 8.50, "idTipo": tipos[1]["idTipo"]})
        assert data["descripcion"] == "Medicamento CRUD actualizado" and data["precio"] == 8.5
        assert data["tipoArticulo"]["idTipo"] == tipos[1]["idTipo"]
        data, _ = expect("GET", article_path, 200)
        assert data["tipoArticulo"]["idTipo"] == tipos[1]["idTipo"], "El nuevo tipo debe persistir."
        listed, _ = expect("GET", "/api/articulos", 200)
        assert any(a["idArticulo"] == articulo_id for a in listed)
        listed, _ = expect("GET", "/api/proveedores", 200)
        assert any(p["idProveedor"] == proveedor_id for p in listed)
        expect("GET", "/api/articulos/0", 400)
        expect("GET", "/api/proveedores/no-numero", 400)
        expect("DELETE", article_path, 204)
        articulo_created = False
        expect("GET", article_path, 404)
        expect("PUT", article_path, 404, articulo)
        expect("DELETE", article_path, 404)
        expect("DELETE", provider_path, 204)
        proveedor_created = False
        expect("GET", provider_path, 404)
        expect("PUT", provider_path, 404, proveedor)
        expect("DELETE", provider_path, 404)
        expect("GET", "/api/servicios/articulo-mayor-costo", 200)
        expect("GET", "/api/servicios/proveedores/buscar?q=Andinos", 200)
        expect("GET", "/api/servicios/articulos/mejor-precio?q=Paracetamol", 200)
        print(f"OK: {checks} comprobaciones HTTP. Registros de prueba eliminados.")
    finally:
        # Solo se borran los registros cuya creación confirmó esta misma ejecución.
        if articulo_created:
            expect("DELETE", article_path, 204)
        if proveedor_created:
            expect("DELETE", provider_path, 204)


if __name__ == "__main__":
    main()
