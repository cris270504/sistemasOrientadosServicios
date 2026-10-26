"""Comprueba listas y contadores con ofertas de dos proveedores, sin tocar datos previos.

Ejecutar con el servidor iniciado: python scripts/test_proveedor_medicamentos.py
"""
import secrets
import test_crud as api


def main():
    proveedores = []
    articulos = []
    initial_checks = api.checks
    try:
        for i in range(2):
            ident = api.unused_id("proveedores")
            datos = {"idProveedor": ident, "nombre": f"Proveedor prueba asociación {i}",
                     "ruc": str(80_000_000_000 + secrets.randbelow(999_999_999))}
            api.expect("POST", "/api/proveedores", 201, datos)
            proveedores.append(datos)
        p1, p2 = proveedores

        def comprobar(proveedor, ids):
            lista, _ = api.expect("GET", f'/api/proveedores/{proveedor["idProveedor"]}/articulos', 200)
            assert {a["idArticulo"] for a in lista} == set(ids), lista
            assert all(a["proveedor"]["idProveedor"] == proveedor["idProveedor"] for a in lista)
            contactos, _ = api.expect("GET", f'/api/servicios/proveedores/buscar?q={proveedor["ruc"]}', 200)
            assert len(contactos) == 1 and contactos[0]["medicamentosQueAbastece"] == len(lista), contactos

        comprobar(p1, [])
        comprobar(p2, [])
        missing = api.unused_id("proveedores")
        api.expect("GET", f"/api/proveedores/{missing}/articulos", 404)
        api.expect("GET", "/api/proveedores/0/articulos", 400)
        unidades, _ = api.expect("GET", "/api/unidades-medida", 200)
        tipos, _ = api.expect("GET", "/api/tipos-articulo", 200)
        for precio, proveedor in [(1.20, p1), (1.50, p2)]:
            datos = {"idArticulo": api.unused_id("articulos"), "descripcion": "Medicamento común de prueba",
                     "precio": precio, "idUnidad": unidades[0]["idUnidad"], "idTipo": tipos[0]["idTipo"],
                     "idProveedor": proveedor["idProveedor"]}
            creado, _ = api.expect("POST", "/api/articulos", 201, datos)
            articulos.append(datos)
            assert creado["precio"] == precio
        a1, a2 = articulos
        comprobar(p1, [a1["idArticulo"]])
        comprobar(p2, [a2["idArticulo"]])
        api.expect("POST", "/api/articulos", 409, a1)
        comprobar(p1, [a1["idArticulo"]])
        api.expect("PUT", f'/api/articulos/{a1["idArticulo"]}', 400, {**a1, "idProveedor": missing})
        comprobar(p1, [a1["idArticulo"]])
        api.expect("PUT", f'/api/articulos/{a1["idArticulo"]}', 200, {**a1, "idProveedor": p2["idProveedor"]})
        comprobar(p1, [])
        comprobar(p2, [a1["idArticulo"], a2["idArticulo"]])
        api.expect("DELETE", f'/api/proveedores/{p2["idProveedor"]}', 409)
        for articulo in list(articulos):
            api.expect("DELETE", f'/api/articulos/{articulo["idArticulo"]}', 204)
            articulos.remove(articulo)
        comprobar(p2, [])
        print(f"OK: {api.checks - initial_checks} comprobaciones de asociación, aislamiento y contadores.")
    finally:
        for articulo in articulos:
            api.expect("DELETE", f'/api/articulos/{articulo["idArticulo"]}', 204)
        for proveedor in proveedores:
            api.expect("DELETE", f'/api/proveedores/{proveedor["idProveedor"]}', 204)


if __name__ == "__main__":
    main()
