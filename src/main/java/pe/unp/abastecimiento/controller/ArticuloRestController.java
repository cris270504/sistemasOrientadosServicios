package pe.unp.abastecimiento.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pe.unp.abastecimiento.dto.ArticuloRequest;
import pe.unp.abastecimiento.model.Articulo;
import pe.unp.abastecimiento.service.CrudService;
import java.util.List;

@RestController
@RequestMapping("/api/articulos")
public class ArticuloRestController {
    private final CrudService service;

    public ArticuloRestController(CrudService service) {
        this.service = service;
    }

    /** READ: GET /api/articulos — lista todos los medicamentos. */
    @GetMapping
    public List<Articulo> listar() {
        return service.listarArticulos();
    }

    /** READ: GET /api/articulos/{id} — consulta un medicamento. */
    @GetMapping("/{id}")
    public Articulo obtener(@PathVariable Integer id) {
        return service.obtenerArticulo(id);
    }

    /** CREATE: POST /api/articulos — crea y devuelve HTTP 201 con Location. */
    @PostMapping
    public ResponseEntity<Articulo> crear(@Valid @RequestBody ArticuloRequest datos) {
        Articulo creado = service.crearArticulo(datos);
        var location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(creado.getIdArticulo()).toUri();
        return ResponseEntity.created(location).body(creado);
    }

    /** UPDATE: PUT /api/articulos/{id} — reemplaza los datos editables. */
    @PutMapping("/{id}")
    public Articulo actualizar(@PathVariable Integer id, @Valid @RequestBody ArticuloRequest datos) {
        return service.actualizarArticulo(id, datos);
    }

    /** DELETE: DELETE /api/articulos/{id} — elimina y devuelve HTTP 204. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        service.eliminarArticulo(id);
        return ResponseEntity.noContent().build();
    }
}
