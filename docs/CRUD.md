# Demostrar el CRUD de medicamentos y proveedores

Abre [http://localhost:8080/api](http://localhost:8080/api). La página permite ejecutar peticiones reales y muestra el método, la URL, el JSON y el estado HTTP.

## Demostración rápida

1. Selecciona **Proveedores → Crear**. Usa un ID nuevo, un RUC de once dígitos que no esté registrado y un nombre. Debes recibir **201**.
2. Selecciona **Consultar por ID**, conserva ese ID y ejecuta. Debes recibir **200** con el proveedor.
3. Cambia a **Actualizar**, modifica el nombre y ejecuta. Debes recibir **200** con el cambio.
4. Crea un medicamento con otro ID nuevo, descripción, precio, una unidad existente y el ID del proveedor creado. Debes recibir **201**.
5. Consulta y actualiza el medicamento. Debes recibir **200**.
6. Elimina el medicamento y después el proveedor, marcando la confirmación. Cada eliminación correcta devuelve **204**, sin cuerpo.

Usa los enlaces superiores para consultar los IDs existentes y el catálogo de unidades. No uses los IDs o RUC de los registros originales para la demostración de creación.

## Rutas REST

| Función | Método | Medicamentos | Proveedores |
|---|---|---|---|
| Crear | POST | `/api/articulos` | `/api/proveedores` |
| Listar | GET | `/api/articulos` | `/api/proveedores` |
| Consultar uno | GET | `/api/articulos/{id}` | `/api/proveedores/{id}` |
| Actualizar | PUT | `/api/articulos/{id}` | `/api/proveedores/{id}` |
| Eliminar | DELETE | `/api/articulos/{id}` | `/api/proveedores/{id}` |

La barra de direcciones permite demostrar GET. Para POST, PUT y DELETE usa el formulario de `/api`, que envía el método correspondiente mediante `fetch`.

## Código para mostrar al profesor

- `src/main/java/pe/unp/abastecimiento/controller/ArticuloRestController.java`: `listar`, `obtener`, `crear`, `actualizar` y `eliminar`, con sus anotaciones HTTP y comentarios CRUD.
- `src/main/java/pe/unp/abastecimiento/controller/ProveedorRestController.java`: las mismas operaciones para proveedores.
- `src/main/java/pe/unp/abastecimiento/service/CrudService.java`: consultas, inserciones, actualizaciones, eliminaciones y validaciones dentro de transacciones.
- `src/main/java/pe/unp/abastecimiento/dto/`: validaciones de los datos recibidos.

Para crear se envía `idArticulo` o `idProveedor`, según corresponda. En PUT se usa el ID de la URL; si también se manda en el cuerpo, debe coincidir.

Ejemplo del cuerpo de un medicamento (los IDs de proveedor y unidad deben existir):

```json
{
  "idArticulo": 101,
  "descripcion": "Medicamento de demostración",
  "precio": 3.50,
  "idUnidad": 1,
  "idProveedor": 1
}
```

## Respuestas y reglas

- **400**: campos inválidos, ID ausente al crear, JSON incorrecto o relación inexistente.
- **404**: el registro consultado, actualizado o eliminado no existe.
- **409**: ID o RUC duplicado, o proveedor con medicamentos asociados.
- Crear con un ID existente no sobrescribe el registro. Eliminar un proveedor no elimina sus medicamentos en cascada.

## Verificación

La compilación y el empaquetado se verifican con `mvnw.cmd -o package` si las dependencias ya están instaladas; omite `-o` cuando necesites descargarlas.

Con el servidor iniciado, `python scripts/test_crud.py` verifica las tres páginas HTML y 38 respuestas HTTP de los CRUD, sus validaciones y las consultas originales. El script crea sus propios registros y los elimina al finalizar; no modifica los registros preexistentes.

También se probó el ciclo completo de ambos recursos desde `/api`, incluida la protección 409 de proveedores con medicamentos. Las capturas de escritorio y móvil se generan en el entorno local bajo `output/verification/`; esa carpeta no se incluye en el repositorio.

## Límite de reversión

El CRUD usa dos controladores REST, sus DTO, `CrudService`, `ApiExceptionHandler`, `CatalogoRestController` y la vista `api.html`. El enlace del portal y la ruta de `PortalController` permiten acceder a esta vista. Los cambios de entidades agregan constructores y métodos de actualización; la consulta de RUC duplicado se agrega al repositorio de proveedores. No se alteró el esquema SQL ni las consultas originales. Estas piezas y el script de pruebas forman la unidad de cambio; los mockups y el rediseño previo del portal son independientes.
