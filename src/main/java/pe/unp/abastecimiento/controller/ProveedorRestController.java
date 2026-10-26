package pe.unp.abastecimiento.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pe.unp.abastecimiento.dto.ProveedorRequest;
import pe.unp.abastecimiento.model.Proveedor;
import pe.unp.abastecimiento.model.Articulo;
import pe.unp.abastecimiento.service.CrudService;
import java.util.List;

@RestController
@RequestMapping("/api/proveedores")
public class ProveedorRestController {
    private final CrudService service;

    public ProveedorRestController(CrudService service) {
        this.service = service;
    }

    /** READ: GET /api/proveedores — lista todos los proveedores. */
    @GetMapping
    public List<Proveedor> listar() {
        return service.listarProveedores();
    }

    /** READ: GET /api/proveedores/{id} — consulta un proveedor. */
    @GetMapping("/{id}")
    public Proveedor obtener(@PathVariable Integer id) {
        return service.obtenerProveedor(id);
    }

    @GetMapping("/{id}/articulos")
    public List<Articulo> medicamentos(@PathVariable Integer id) {
        return service.listarArticulosProveedor(id);
    }

    /** CREATE: POST /api/proveedores — crea y devuelve HTTP 201 con Location. */
    @PostMapping
    public ResponseEntity<Proveedor> crear(@Valid @RequestBody ProveedorRequest datos) {
        Proveedor creado = service.crearProveedor(datos);
        var location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(creado.getIdProveedor()).toUri();
        return ResponseEntity.created(location).body(creado);
    }

    /** UPDATE: PUT /api/proveedores/{id} — reemplaza los datos editables. */
    @PutMapping("/{id}")
    public Proveedor actualizar(@PathVariable Integer id, @Valid @RequestBody ProveedorRequest datos) {
        return service.actualizarProveedor(id, datos);
    }

    /** DELETE: DELETE /api/proveedores/{id} — elimina y devuelve HTTP 204. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        service.eliminarProveedor(id);
        return ResponseEntity.noContent().build();
    }
}
