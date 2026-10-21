package pe.unp.abastecimiento.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.unp.abastecimiento.model.Articulo;
import pe.unp.abastecimiento.model.ContactoProveedor;
import pe.unp.abastecimiento.service.AbastecimientoService;

import java.util.List;

@RestController
@RequestMapping("/api/servicios")
public class ServicioRestController {

    private final AbastecimientoService service;

    public ServicioRestController(AbastecimientoService service) {
        this.service = service;
    }

    @GetMapping("/articulo-mayor-costo")
    public ResponseEntity<Articulo> articuloMayorCosto() {
        return ResponseEntity.of(service.medicamentoMayorCosto());
    }

    @GetMapping("/proveedores/buscar")
    public List<ContactoProveedor> buscarContacto(@RequestParam(defaultValue = "") String q) {
        return service.buscarContactoProveedor(q);
    }

    @GetMapping("/articulos/mejor-precio")
    public List<Articulo> mejorPrecio(@RequestParam(defaultValue = "") String q) {
        return service.mejorPrecio(q);
    }
}
