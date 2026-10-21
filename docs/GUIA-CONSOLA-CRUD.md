# CRUD desde la consola del navegador

Abre **http://localhost:8080/api**, presiona **F12 → Console** y ejecuta cada bloque con **Enter**. Esta es la consola que aparece en tu captura; no es PowerShell.

Los ejemplos trabajan con registros de demostración: **ID 101** y **RUC 20999999999**, libres al revisar la base. Si ya los creaste, continúa con consultar/actualizar/eliminar; no vuelvas a crear el mismo ID.

## 1. CREATE: insertar — POST

Primero crea el proveedor:

```javascript
var r = await fetch('/api/proveedores', {
  method: 'POST',
  headers: {'Content-Type': 'application/json'},
  body: JSON.stringify({
    idProveedor: 101, ruc: '20999999999', nombre: 'Proveedor Demo'
  })
});
console.log(r.status, await r.json());
```

Después crea su medicamento; la unidad **1** corresponde a **Tableta**:

```javascript
var r = await fetch('/api/articulos', {
  method: 'POST',
  headers: {'Content-Type': 'application/json'},
  body: JSON.stringify({
    idArticulo: 101, descripcion: 'Medicamento Demo', precio: 2.50,
    idUnidad: 1, idProveedor: 101
  })
});
console.log(r.status, await r.json());
```

**Resultado esperado: 201** y el registro creado. `JSON.stringify` convierte los datos de JavaScript en el JSON que recibe Java.

## 2. READ: consultar — GET

Ver todos los proveedores y medicamentos:

```javascript
console.table(await (await fetch('/api/proveedores')).json());
console.table(await (await fetch('/api/articulos')).json());
```

Ver solamente los registros que acabas de crear:

```javascript
console.log(await (await fetch('/api/proveedores/101')).json());
console.log(await (await fetch('/api/articulos/101')).json());
```

**Resultado esperado: 200** con los datos. Para un medicamento, despliega `proveedor` y `unidadMedida` para ver sus relaciones.

## 3. UPDATE: actualizar — PUT

Actualiza el proveedor:

```javascript
var r = await fetch('/api/proveedores/101', {
  method: 'PUT',
  headers: {'Content-Type': 'application/json'},
  body: JSON.stringify({ruc: '20999999999', nombre: 'Proveedor Demo Actualizado'})
});
console.log(r.status, await r.json());
```

Actualiza el medicamento (precio de **2.50** a **3.00**):

```javascript
var r = await fetch('/api/articulos/101', {
  method: 'PUT',
  headers: {'Content-Type': 'application/json'},
  body: JSON.stringify({
    descripcion: 'Medicamento Demo Actualizado', precio: 3.00,
    idUnidad: 1, idProveedor: 101
  })
});
console.log(r.status, await r.json());
```

**Resultado esperado: 200** con los cambios. En PUT envía todos los campos editables; el ID está en la URL.

## 4. DELETE: eliminar — DELETE

Elimina **primero el medicamento y después el proveedor**:

```javascript
var r = await fetch('/api/articulos/101', {method: 'DELETE'});
console.log(r.status); // 204: medicamento eliminado

var r = await fetch('/api/proveedores/101', {method: 'DELETE'});
console.log(r.status); // 204: proveedor eliminado
```

En DELETE no uses `r.json()`: una respuesta **204** no tiene cuerpo. Un GET posterior al registro eliminado devuelve **404**.

## Si aparece un error

| Estado | Qué significa |
|---|---|
| 400 | Datos inválidos: revisa `detail` y `campos` de la respuesta. |
| 404 | El ID no existe o ya fue eliminado. |
| 409 | ID/RUC repetido, o proveedor que todavía tiene medicamentos. |

Se usa `var` para que puedas repetir los bloques en la misma consola sin el error de variable ya declarada. `await` espera la respuesta del servidor.

**Frase para explicar:** «Uso fetch para llamar a mi API REST. POST crea, GET consulta, PUT actualiza y DELETE elimina. Los controladores Java reciben la petición y el servicio ejecuta la operación en PostgreSQL».

## Ver los datos de las tablas

En el navegador puedes consultar `/api/articulos`, `/api/proveedores` y `/api/unidades-medida`. El catálogo `tipo_articulo` no tiene una ruta propia.

Los datos de ejemplo que se cargan al iniciar están en [data.sql](../src/main/resources/data.sql). Para ver el contenido actual directamente en PostgreSQL, usa las instrucciones del [README](../README.md#visualizar-las-tablas-en-postgresql). Los comandos de esta guía se dejaron como ejemplos; no se ejecutaron contra tus datos al escribirla.
