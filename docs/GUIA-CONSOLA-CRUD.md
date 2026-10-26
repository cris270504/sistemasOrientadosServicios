# Guía completa de CRUD desde F12 → Console

Ejecuta las operaciones reales de la API de Abastecimiento: proveedores, medicamentos, relaciones, catálogos y consultas. Incluye los casos de éxito y las familias de error implementadas. No supone que existan rutas que el código no ofrece.

## Cómo usarla

1. Inicia la aplicación y abre **http://localhost:8080/api**.
2. Presiona **F12 → Console**. Estos bloques son JavaScript del navegador, no PowerShell ni SQL de pgAdmin.
3. Pega los bloques de las secciones 1 y 2; después continúa en orden. Cada bloque puede pegarse completo y ejecutarse con Enter.
4. Los bloques de error son intencionales: comprueba el HTTP esperado y continúa. Termina con la sección 12 para borrar tus registros Demo.

Los cambios se guardan en la base conectada a la aplicación. La guía crea sus propios IDs y RUC libres; no borres ni reasignes registros originales. Si otra sesión crea simultáneamente un ID reservado, el POST devolverá 409: reserva un ID diferente antes de continuar. No vuelvas a ejecutar los CREATE exitosos sin limpiar primero.

## Índice

- Preparación y catálogos: secciones 1–2.
- CRUD y ofertas de medicamentos por proveedor: secciones 3–5.
- Validaciones de proveedores y medicamentos: secciones 6–7.
- IDs, registros inexistentes, JSON, métodos y rutas: secciones 8–9.
- Consultas especiales: sección 10.
- Reasignación y eliminación: secciones 11–12.

## Qué operaciones existen

| Recurso | Crear | Listar / consultar | Actualizar | Eliminar |
|---|---|---|---|---|
| Proveedores | POST `/api/proveedores` | GET lista y `/{id}` | PUT `/{id}` | DELETE `/{id}` |
| Medicamentos / artículos | POST `/api/articulos` | GET lista y `/{id}` | PUT `/{id}` | DELETE `/{id}` |
| Artículos de un proveedor | POST `/api/articulos` con `idProveedor` | GET `/api/proveedores/{id}/articulos` | PUT `/api/articulos/{id}` | DELETE `/api/articulos/{id}` |
| Unidades de medida | Sin endpoint | GET `/api/unidades-medida` | Sin endpoint | Sin endpoint |
| Tipos de artículo | Sin endpoint | GET `/api/tipos-articulo` | Sin endpoint | Sin endpoint |

POST/PUT reciben un objeto JSON. GET de listas devuelve `[{...}, {...}]`; GET por ID devuelve `{...}`. DELETE exitoso devuelve HTTP 204 sin cuerpo. Los ID son enteros positivos de hasta 2147483647; son manuales y no cambian mediante PUT. Los ejemplos usan `var` para poder reutilizar variables en la consola.

## 1. Preparación

### Pega primero esta función

```javascript
var api = async (ruta, metodo = 'GET', datos) => {
  var respuesta = await fetch('/api/' + ruta, {
    method: metodo,
    headers: datos === undefined ? {} : {'Content-Type': 'application/json'},
    body: datos === undefined ? undefined : JSON.stringify(datos)
  });
  var texto = await respuesta.text();
  var contenido = texto ? JSON.parse(texto) : null;
  console.log(metodo, ruta, 'HTTP', respuesta.status, contenido);
  if (!respuesta.ok) console.error(contenido?.detail || 'La petición falló');
  return {estado: respuesta.status, datos: contenido};
};
```

La función muestra el estado y el cuerpo. Los errores HTTP se muestran y se devuelven para poder estudiarlos; no detienen los siguientes casos. Un error de conexión sí rechaza la llamada. DELETE 204 se lee como null y no intenta interpretar JSON vacío.

### Reservar identificadores de demostración sin tocar registros existentes

```javascript
var reservados = new Set();
var idLibre = async recurso => {
  for (var intento = 0; intento < 30; intento++) {
    var candidato = 1000000000 + Math.floor(Math.random() * 900000000);
    var clave = recurso + '/' + candidato;
    if (reservados.has(clave)) continue;
    var r = await fetch('/api/' + clave);
    if (r.status === 404) { reservados.add(clave); return candidato; }
    if (!r.ok) throw new Error('No se pudo comprobar si el ID está libre');
  }
  throw new Error('No se encontró un ID libre');
};
var proveedoresActuales = (await api('proveedores')).datos;
var rucsUsados = new Set(proveedoresActuales.map(p => p.ruc));
var rucLibre = () => {
  var ruc;
  do { ruc = String(90000000000 + Math.floor(Math.random() * 999999999)); }
  while (rucsUsados.has(ruc));
  rucsUsados.add(ruc);
  return ruc;
};
var idPA = await idLibre('proveedores');
var idPB = await idLibre('proveedores');
var idMA = await idLibre('articulos');
var idMB = await idLibre('articulos');
var proveedorAusente = await idLibre('proveedores');
var articuloAusente = await idLibre('articulos');
var proveedorA = {idProveedor: idPA, ruc: rucLibre(), nombre: 'Proveedor Demo A'};
var proveedorB = {idProveedor: idPB, ruc: rucLibre(), nombre: 'Proveedor Demo B'};
console.table({proveedorA: idPA, proveedorB: idPB, medicamentoA: idMA, medicamentoB: idMB});
```

Ejecuta esta preparación una sola vez por demostración. No recargues la página durante la secuencia: perderías las variables. Los IDs son manuales; esta reserva comprueba que estén libres, pero otra persona podría ocuparlos antes del POST: en ese caso la API responderá 409.

## 2. Catálogos y listas

### Consultar presentaciones y tipos

```javascript
var unidades = (await api('unidades-medida')).datos;
var tipos = (await api('tipos-articulo')).datos;
console.table(unidades);
console.table(tipos);
if (!unidades.length || !tipos.length) throw new Error('Los catálogos deben tener datos');
var idUnidadDemo = unidades[0].idUnidad;
var idTipoDemo = (tipos.find(t => t.descripcion === 'Sin clasificar') || tipos[0]).idTipo;
var unidadAusente = 2147483647;
while (unidades.some(u => u.idUnidad === unidadAusente)) unidadAusente--;
var tipoAusente = 2147483647;
while (tipos.some(t => t.idTipo === tipoAusente)) tipoAusente--;
```

**Esperado:** HTTP **200** en las 2 llamadas.

Se usan IDs del catálogo real; no se presupone que un número corresponda a una presentación o categoría clínica.

### Listar todos los proveedores y artículos

```javascript
console.table((await api('proveedores')).datos);
console.table((await api('articulos')).datos);
```

**Esperado:** HTTP **200** en las 2 llamadas.

Las dos respuestas son listas JSON. Una lista sin registros se devuelve como [], con HTTP 200.

## 3. CRUD de proveedores

### CREATE: registrar dos proveedores

```javascript
await api('proveedores', 'POST', proveedorA);
await api('proveedores', 'POST', proveedorB);
```

**Esperado:** HTTP **201** en las 2 llamadas.

Cada respuesta contiene el objeto creado. El encabezado Location apunta a /api/proveedores/{id}. El RUC es texto de once dígitos.

### READ: consultar un proveedor por ID

```javascript
var resultadoProveedor = await api('proveedores/' + idPA);
console.log(resultadoProveedor.datos);
```

**Esperado:** HTTP **200**.

Devuelve un objeto JSON, no una lista.

### READ: proveedor existente sin medicamentos

```javascript
await api('proveedores/' + idPA + '/articulos');
```

**Esperado:** HTTP **200**.

Devuelve [] porque el proveedor existe, pero todavía no tiene artículos.

### UPDATE: cambiar nombre y RUC

```javascript
proveedorA = {...proveedorA, nombre: 'Proveedor Demo A Actualizado', ruc: rucLibre()};
await api('proveedores/' + idPA, 'PUT', {nombre: proveedorA.nombre, ruc: proveedorA.ruc});
```

**Esperado:** HTTP **200**.

PUT exige nombre y RUC. Se puede conservar el mismo RUC del proveedor; solo se rechaza si pertenece a otro.

### UPDATE: ID en URL y cuerpo coincidentes

```javascript
await api('proveedores/' + idPA, 'PUT', proveedorA);
```

**Esperado:** HTTP **200**.

El ID del cuerpo puede omitirse en PUT; si se envía, debe coincidir con el de la URL. PUT no cambia la clave primaria.

## 4. CRUD de medicamentos

### CREATE: agregar medicamento al proveedor A

```javascript
var medicamentoA = {
  idArticulo: idMA, descripcion: 'Medicamento Demo Consola', precio: 12.50,
  idUnidad: idUnidadDemo, idTipo: idTipoDemo, idProveedor: idPA
};
await api('articulos', 'POST', medicamentoA);
```

**Esperado:** HTTP **201**.

idProveedor establece la relación. La unidad, el tipo y el proveedor deben existir. No se envían objetos anidados ni un contador.

### READ: consultar medicamento y sus relaciones

```javascript
var resultadoMedicamento = await api('articulos/' + idMA);
console.log(resultadoMedicamento.datos.proveedor);
console.log(resultadoMedicamento.datos.unidadMedida);
console.log(resultadoMedicamento.datos.tipoArticulo);
```

**Esperado:** HTTP **200**.

GET devuelve objetos relacionados. POST/PUT reciben sus IDs: idProveedor, idUnidad e idTipo.

### READ: lista y contador del proveedor A

```javascript
console.table((await api('proveedores/' + idPA + '/articulos')).datos);
await api('servicios/proveedores/buscar?q=' + encodeURIComponent(proveedorA.ruc));
```

**Esperado:** HTTP **200** en las 2 llamadas.

La lista tiene un artículo y medicamentosQueAbastece vale 1. El contador representa registros asociados, no stock.

### UPDATE: modificar descripción, precio, presentación y tipo

```javascript
medicamentoA = {...medicamentoA, descripcion: 'Medicamento Demo Consola Actualizado', precio: 8.50};
await api('articulos/' + idMA, 'PUT', {
  descripcion: medicamentoA.descripcion, precio: medicamentoA.precio,
  idUnidad: medicamentoA.idUnidad, idTipo: medicamentoA.idTipo, idProveedor: medicamentoA.idProveedor
});
```

**Esperado:** HTTP **200**.

Para cambiar presentación o tipo, reemplaza su ID por uno del catálogo. PUT requiere todos los campos editables, aunque solo quieras cambiar el precio.

### UPDATE: el precio cero es válido

```javascript
await api('articulos/' + idMA, 'PUT', {...medicamentoA, precio: 0});
await api('articulos/' + idMA, 'PUT', medicamentoA);
```

**Esperado:** HTTP **200** en las 2 llamadas.

Primero establece cero y luego restaura 8.50. El precio admite hasta 7 dígitos enteros y 2 decimales; el máximo es 9999999.99.

## 5. Un medicamento ofrecido por varios proveedores

### CREATE: misma descripción con proveedor y precio diferentes

```javascript
var medicamentoB = {
  ...medicamentoA, idArticulo: idMB, idProveedor: idPB, precio: 10.00
};
await api('articulos', 'POST', medicamentoB);
await api('proveedores/' + idPA + '/articulos');
await api('proveedores/' + idPB + '/articulos');
```

**Esperado:** 201 → 200 → 200, en el orden de las llamadas.

Hay dos ofertas distintas: cada una tiene ID propio. A y B tienen un artículo cada uno. Crear B no mueve ni sobrescribe A.

### READ: comparar las dos ofertas

```javascript
console.table((await api('servicios/articulos/mejor-precio?q=' + encodeURIComponent(medicamentoA.descripcion))).datos);
```

**Esperado:** HTTP **200**.

La lista está ordenada por precio ascendente. Deben verse las ofertas Demo de 8.50 y 10.00; la búsqueda es parcial y no distingue mayúsculas.

## 6. Errores de proveedores

### CREATE: ID ya utilizado

```javascript
await api('proveedores', 'POST', {...proveedorA, nombre: 'No debe sobrescribir'});
```

**Esperado:** HTTP **409**.

El proveedor A conserva sus datos. Para actualizar se utiliza PUT, no POST.

### CREATE y UPDATE: RUC que ya pertenece a otro proveedor

```javascript
await api('proveedores', 'POST', {...proveedorA, idProveedor: proveedorAusente});
await api('proveedores/' + idPB, 'PUT', {...proveedorB, ruc: proveedorA.ruc});
```

**Esperado:** HTTP **409** en las 2 llamadas.

La primera usa un ID libre con el RUC de A. La segunda intenta poner el RUC de A en B. Ninguna operación debe modificar registros.

### CREATE: ID ausente

```javascript
await api('proveedores', 'POST', {nombre: 'Sin ID', ruc: rucLibre()});
```

**Esperado:** HTTP **400**.

Crear exige un ID entero positivo, aunque en PUT pueda omitirse.

### UPDATE: nombre y RUC inválidos

```javascript
var erroresProveedor = [
  {caso: 'Nombre vacío', cambios: {nombre: ''}},
  {caso: 'Nombre solo espacios', cambios: {nombre: '   '}},
  {caso: 'Nombre nulo', cambios: {nombre: null}},
  {caso: 'Nombre mayor a 150 caracteres', cambios: {nombre: 'A'.repeat(151)}},
  {caso: 'RUC vacío', cambios: {ruc: ''}},
  {caso: 'RUC nulo', cambios: {ruc: null}},
  {caso: 'RUC con 10 dígitos', cambios: {ruc: '1234567890'}},
  {caso: 'RUC con 12 dígitos', cambios: {ruc: '123456789012'}},
  {caso: 'RUC con letras', cambios: {ruc: 'ABCDEFGHIJK'}}
];
for (var caso of erroresProveedor) {
  console.log(caso.caso);
  await api('proveedores/' + idPA, 'PUT', {...proveedorA, ...caso.cambios});
}
```

**Esperado:** HTTP **400** en las 9 llamadas.

Cada fila responde 400 y conserva el proveedor. El proyecto verifica formato y unicidad del RUC; no realiza una consulta externa de identidad.

### UPDATE: campos omitidos, ID distinto y PATCH no implementado

```javascript
await api('proveedores/' + idPA, 'PUT', {nombre: 'Falta RUC'});
await api('proveedores/' + idPA, 'PUT', {ruc: proveedorA.ruc});
await api('proveedores/' + idPA, 'PUT', {...proveedorA, idProveedor: idPB});
await api('proveedores/' + idPA, 'PATCH', {nombre: 'Cambio parcial'});
```

**Esperado:** 400 → 400 → 400 → 405, en el orden de las llamadas.

PUT no es una actualización parcial. PATCH no está implementado; devuelve 405, no actualiza.

## 7. Errores de medicamentos

### CREATE: ID duplicado o ausente

```javascript
await api('articulos', 'POST', {...medicamentoA, descripcion: 'No debe sobrescribir'});
var sinIdArticulo = {...medicamentoA};
delete sinIdArticulo.idArticulo;
await api('articulos', 'POST', sinIdArticulo);
```

**Esperado:** 409 → 400, en el orden de las llamadas.

Un ID duplicado se rechaza con 409. Sin ID al crear, la respuesta es 400.

### UPDATE: descripción, precio y relaciones inválidos

```javascript
var erroresMedicamento = [
  {caso: 'Descripción vacía', cambios: {descripcion: ''}},
  {caso: 'Descripción solo espacios', cambios: {descripcion: '   '}},
  {caso: 'Descripción nula', cambios: {descripcion: null}},
  {caso: 'Descripción mayor a 150 caracteres', cambios: {descripcion: 'A'.repeat(151)}},
  {caso: 'Precio negativo', cambios: {precio: -1}},
  {caso: 'Precio con tres decimales', cambios: {precio: 1.234}},
  {caso: 'Precio mayor al máximo', cambios: {precio: 10000000}},
  {caso: 'Precio nulo', cambios: {precio: null}},
  {caso: 'Unidad inexistente', cambios: {idUnidad: unidadAusente}},
  {caso: 'Unidad nula', cambios: {idUnidad: null}},
  {caso: 'Unidad cero', cambios: {idUnidad: 0}},
  {caso: 'Proveedor inexistente', cambios: {idProveedor: proveedorAusente}},
  {caso: 'Proveedor nulo', cambios: {idProveedor: null}},
  {caso: 'Proveedor negativo', cambios: {idProveedor: -1}},
  {caso: 'Tipo inexistente', cambios: {idTipo: tipoAusente}},
  {caso: 'Tipo nulo', cambios: {idTipo: null}},
  {caso: 'Tipo cero', cambios: {idTipo: 0}}
];
for (var caso of erroresMedicamento) {
  console.log(caso.caso);
  await api('articulos/' + idMA, 'PUT', {...medicamentoA, ...caso.cambios});
}
```

**Esperado:** HTTP **400** en las 17 llamadas.

Las relaciones inexistentes responden 400 al crear/actualizar un artículo. 404 corresponde a que no exista el recurso objetivo de la URL. Ninguna de estas peticiones debe guardar cambios.

### UPDATE: cada campo obligatorio omitido

```javascript
for (var campo of ['descripcion', 'precio', 'idUnidad', 'idProveedor', 'idTipo']) {
  var incompleto = {...medicamentoA};
  delete incompleto[campo];
  console.log('Falta', campo);
  await api('articulos/' + idMA, 'PUT', incompleto);
}
```

**Esperado:** HTTP **400** en las 5 llamadas.

Cada caso responde 400. idArticulo es el único campo del cuerpo que puede omitirse en PUT.

### UPDATE: ID del cuerpo distinto y PATCH no implementado

```javascript
await api('articulos/' + idMA, 'PUT', {...medicamentoA, idArticulo: idMB});
await api('articulos/' + idMA, 'PATCH', {precio: 5});
```

**Esperado:** 400 → 405, en el orden de las llamadas.

El primer caso no cambia la clave primaria. El segundo no se admite porque la API ofrece PUT, no PATCH.

## 8. IDs y registros inexistentes

### READ: IDs inválidos para ambos recursos

```javascript
for (var recurso of ['proveedores', 'articulos']) {
  for (var idInvalido of ['0', '-1', '1.5', 'abc', '2147483648']) {
    await api(recurso + '/' + idInvalido);
  }
}
```

**Esperado:** HTTP **400** en las 10 llamadas.

IDs cero, negativos, decimales, texto y valores fuera del rango de Integer se rechazan con 400.

### READ/UPDATE/DELETE: recursos que no existen

```javascript
await api('proveedores/' + proveedorAusente);
await api('proveedores/' + proveedorAusente, 'PUT', {ruc: rucLibre(), nombre: 'No existe'});
await api('proveedores/' + proveedorAusente, 'DELETE');
await api('proveedores/' + proveedorAusente + '/articulos');
await api('articulos/' + articuloAusente);
await api('articulos/' + articuloAusente, 'PUT', {...medicamentoA, idArticulo: articuloAusente});
await api('articulos/' + articuloAusente, 'DELETE');
```

**Esperado:** HTTP **404** en las 7 llamadas.

El proveedor ausente no devuelve lista vacía: devuelve 404. No se crea un registro nuevo mediante PUT.

## 9. JSON, métodos y rutas

### JSON mal escrito

```javascript
var r = await fetch('/api/articulos', {
  method: 'POST', headers: {'Content-Type': 'application/json'},
  body: '{"descripcion":'
});
console.log(r.status, await r.json());
```

**Esperado:** HTTP **400**.

La coma entre URL y opciones de fetch es obligatoria. Este ejemplo tiene un JSON inválido intencionalmente; la sintaxis JavaScript sí es correcta.

### Tipo de contenido incorrecto

```javascript
var r = await fetch('/api/articulos', {
  method: 'POST', headers: {'Content-Type': 'text/plain'},
  body: JSON.stringify(medicamentoA)
});
console.log(r.status, await r.text());
```

**Esperado:** HTTP **415**.

Para POST/PUT con estos DTO envía Content-Type: application/json. No se garantiza que todos los errores de infraestructura tengan el mismo cuerpo JSON.

### Catálogos: solo lectura en esta API

```javascript
await api('unidades-medida', 'POST', {idUnidad: 999, descripcion: 'No se creará'});
await api('tipos-articulo', 'POST', {idTipo: 999, descripcion: 'No se creará'});
```

**Esperado:** HTTP **405** en las 2 llamadas.

Hay GET de listas de unidades y tipos. No hay CRUD público ni GET por ID de esos catálogos. No hay POST /proveedores/{id}/articulos: crear una oferta usa POST /articulos con idProveedor.

## 10. Consultas especiales

### Buscar proveedores por nombre, RUC o sin filtro

```javascript
await api('servicios/proveedores/buscar?q=' + encodeURIComponent('Proveedor Demo'));
await api('servicios/proveedores/buscar?q=' + encodeURIComponent(proveedorA.ruc));
await api('servicios/proveedores/buscar');
```

**Esperado:** HTTP **200** en las 3 llamadas.

Todas devuelven listas. La búsqueda por nombre es parcial y no distingue mayúsculas; el RUC admite coincidencia parcial. Sin q se listan todos con su contador.

### Artículo de mayor costo y comparación de precios

```javascript
await api('servicios/articulo-mayor-costo');
await api('servicios/articulos/mejor-precio?q=' + encodeURIComponent(medicamentoA.descripcion));
await api('servicios/articulos/mejor-precio');
```

**Esperado:** HTTP **200** en las 3 llamadas.

Mayor costo devuelve un objeto (404 si la tabla está vacía). Mejor precio devuelve lista ascendente; sin filtro lista todos los artículos por precio. No se elimina la base para demostrar el caso vacío.

### Búsquedas sin resultados

```javascript
await api('servicios/proveedores/buscar?q=' + encodeURIComponent('sin-coincidencia-' + idPA));
await api('servicios/articulos/mejor-precio?q=' + encodeURIComponent('sin-coincidencia-' + idMA));
```

**Esperado:** HTTP **200** en las 2 llamadas.

Ambas devuelven [], con HTTP 200. Una búsqueda sin coincidencias no es un error 404.

## 11. Reasignar un medicamento

### DELETE bloqueado: proveedor con medicamentos

```javascript
await api('proveedores/' + idPA, 'DELETE');
```

**Esperado:** HTTP **409**.

No se elimina en cascada. Primero hay que reasignar o eliminar sus medicamentos.

### UPDATE: mover la oferta A al proveedor B

```javascript
medicamentoA = {...medicamentoA, idProveedor: idPB};
await api('articulos/' + idMA, 'PUT', medicamentoA);
await api('proveedores/' + idPA + '/articulos');
await api('proveedores/' + idPB + '/articulos');
await api('servicios/proveedores/buscar?q=' + encodeURIComponent('Proveedor Demo'));
```

**Esperado:** HTTP **200** en las 4 llamadas.

A queda sin artículos y su contador es 0. B tiene dos registros y su contador es 2. Esto mueve la oferta existente; para conservar A y B se crean dos IDs, como en la sección 5.

## 12. Eliminar y limpiar la demostración

### DELETE: proveedor A después de reasignar

```javascript
await api('proveedores/' + idPA, 'DELETE');
await api('proveedores/' + idPA);
```

**Esperado:** 204 → 404, en el orden de las llamadas.

Ya no tiene medicamentos, por eso puede eliminarse. El GET posterior confirma 404.

### DELETE: medicamento A y contador de B

```javascript
await api('articulos/' + idMA, 'DELETE');
await api('proveedores/' + idPB + '/articulos');
await api('servicios/proveedores/buscar?q=' + encodeURIComponent(proveedorB.ruc));
```

**Esperado:** 204 → 200 → 200, en el orden de las llamadas.

B conserva solo el medicamento B y su contador pasa de 2 a 1. DELETE 204 no tiene cuerpo ni mensaje automático: la función api imprime null.

### READ/UPDATE/DELETE después de eliminar un medicamento

```javascript
await api('articulos/' + idMA);
await api('articulos/' + idMA, 'PUT', medicamentoA);
await api('articulos/' + idMA, 'DELETE');
```

**Esperado:** HTTP **404** en las 3 llamadas.

Repetir DELETE devuelve 404. El resultado final sigue siendo que el registro no existe.

### DELETE: terminar la limpieza

```javascript
await api('articulos/' + idMB, 'DELETE');
await api('proveedores/' + idPB + '/articulos');
await api('proveedores/' + idPB, 'DELETE');
await api('proveedores/' + idPB, 'DELETE');
```

**Esperado:** 204 → 200 → 204 → 404, en el orden de las llamadas.

Primero elimina la oferta restante; la lista de B queda vacía. Después elimina B. Solo se borran los IDs creados en esta demostración.

### Comprobar que no quedaron los cuatro registros

```javascript
await api('proveedores/' + idPA);
await api('proveedores/' + idPB);
await api('articulos/' + idMA);
await api('articulos/' + idMB);
```

**Esperado:** HTTP **404** en las 4 llamadas.

Los cuatro deben responder 404. Si interrumpiste la demostración, consulta primero esos IDs y elimina solamente tus artículos Demo antes de tus proveedores Demo.

## Errores y recuperación

| Síntoma / HTTP | Significado | Qué hacer |
|---|---|---|
| 200 | Consulta o actualización correcta | Revisa el JSON devuelto. |
| 201 | Registro creado | Conserva su ID; Location indica su ruta. |
| 204 | Eliminado correctamente | No llames `r.json()`; no hay cuerpo. |
| 400 | Datos inválidos, incompletos, ID incorrecto o relación inexistente | Revisa `detail` y, si aparece, `campos`; corrige el cuerpo completo. |
| 404 | Recurso objetivo inexistente | Confirma el ID. Una búsqueda vacía normalmente devuelve 200 y []. |
| 405 | Método no implementado en esa ruta | Usa los métodos de la tabla de operaciones. |
| 409 | ID/RUC duplicado o proveedor con medicamentos | Cambia el ID/RUC o resuelve las relaciones antes de eliminar. |
| 415 | Content-Type incorrecto | Envía application/json con JSON.stringify. |
| `TypeError: Failed to fetch` | No se recibió una respuesta HTTP utilizable | Comprueba servidor, puerto y conexión. Si era POST, consulta el ID antes de reintentar: pudo haberse guardado. |
| `SyntaxError: missing ) after argument list` | JavaScript mal escrito, antes de llamar a la API | Revisa comas y comillas: `fetch('/api/articulos/101', {method: 'DELETE'})`. |
| Error al interpretar JSON después de DELETE | Se intentó leer un cuerpo vacío | Usa `r.status` o la función api de la sección 1. |
| `Identifier has already been declared` | Repetiste una declaración let/const en la consola | Usa los ejemplos con var o recarga y empieza una demostración nueva. |
| Puerto 8080 ocupado al iniciar | Hay otra aplicación escuchando | Identifica el proceso antes de detenerlo; no ejecutes dos instancias en el mismo puerto. |

Si ves 500 o un resultado distinto al esperado, consulta los registros del servidor. Los ejemplos no convierten un error inesperado en un éxito ni lo ignoran. No se simula una base caída, un catálogo vacío ni una tabla completamente vacía eliminando tus datos. La consulta de mayor costo devuelve 404 cuando no hay artículos, según el código; este caso debe probarse en una base aislada.

La pantalla del portal no se actualiza automáticamente si cambias datos desde otra consola: vuelve a buscar el proveedor para ver su contador actual. El contador no se edita directamente. Los cambios por SQL sobre la misma base también serán visibles al volver a consultar.

## Llamada directa sin la función api

La función inicial solo evita repetir fetch, los encabezados y la lectura del cuerpo; no agrega endpoints ni reglas al backend. Para consultar directamente:

```javascript
var r = await fetch('/api/articulos');
console.log(r.status);
console.table(await r.json());
```

Los ejemplos completos de CREATE y UPDATE están en las secciones 3 y 4, usando el helper cuya implementación se muestra íntegra. Para DELETE sin helper, toma uno de los IDs Demo aún existentes y escribe `fetch('/api/articulos/' + idMA, {method: 'DELETE'})`; no ejecutes esa eliminación fuera de orden si quieres seguir todos los casos.

## Código que respalda esta guía

- [ArticuloRestController.java](../src/main/java/pe/unp/abastecimiento/controller/ArticuloRestController.java): CRUD de artículos.
- [ProveedorRestController.java](../src/main/java/pe/unp/abastecimiento/controller/ProveedorRestController.java): CRUD de proveedores y su lista de medicamentos.
- [CatalogoRestController.java](../src/main/java/pe/unp/abastecimiento/controller/CatalogoRestController.java): catálogos de solo lectura.
- [ServicioRestController.java](../src/main/java/pe/unp/abastecimiento/controller/ServicioRestController.java): búsquedas, comparación y mayor costo.
- [CrudService.java](../src/main/java/pe/unp/abastecimiento/service/CrudService.java): persistencia y reglas de relaciones.
- [ArticuloRequest.java](../src/main/java/pe/unp/abastecimiento/dto/ArticuloRequest.java) y [ProveedorRequest.java](../src/main/java/pe/unp/abastecimiento/dto/ProveedorRequest.java): validaciones de campos.

Los campos adicionales desconocidos, el orden de registros empatados por precio y la validación externa del RUC no forman parte del contrato documentado. No se promete una validación que la aplicación no implementa.

## Verificación de los ejemplos

Se ejecutaron los 40 bloques principales de esta guía desde un navegador contra una base temporal local. Las 114 respuestas HTTP con expectativa explícita coincidieron con lo indicado. También se ejecutaron las consultas de preparación y el ejemplo final de fetch directo: 122 llamadas en total. Los registros de demostración se eliminaron y la base temporal se descartó; la validación no modificó los datos originales.
