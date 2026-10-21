package pe.unp.abastecimiento.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.unp.abastecimiento.model.UnidadMedida;
import pe.unp.abastecimiento.service.CrudService;
import java.util.List;

@RestController
public class CatalogoRestController {
    private final CrudService service;

    public CatalogoRestController(CrudService service) {
        this.service = service;
    }

    @GetMapping("/api/unidades-medida")
    public List<UnidadMedida> unidades() {
        return service.listarUnidades();
    }
}
