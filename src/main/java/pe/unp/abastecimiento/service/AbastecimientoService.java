package pe.unp.abastecimiento.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.unp.abastecimiento.model.Articulo;
import pe.unp.abastecimiento.model.ContactoProveedor;
import pe.unp.abastecimiento.repository.ArticuloRepository;
import pe.unp.abastecimiento.repository.ProveedorRepository;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class AbastecimientoService {

    private final ArticuloRepository articuloRepository;
    private final ProveedorRepository proveedorRepository;

    public AbastecimientoService(ArticuloRepository articuloRepository, ProveedorRepository proveedorRepository) {
        this.articuloRepository = articuloRepository;
        this.proveedorRepository = proveedorRepository;
    }

    public Optional<Articulo> medicamentoMayorCosto() {
        return articuloRepository.findFirstByOrderByPrecioDesc();
    }

    public List<ContactoProveedor> buscarContactoProveedor(String criterio) {
        String q = criterio == null ? "" : criterio.trim();
        return proveedorRepository.findByNombreContainingIgnoreCaseOrRucContainingOrderByNombreAsc(q, q).stream()
                .map(p -> ContactoProveedor.of(p, articuloRepository.countByProveedorIdProveedor(p.getIdProveedor())))
                .toList();
    }

    public List<Articulo> mejorPrecio(String medicamento) {
        String q = medicamento == null ? "" : medicamento.trim();
        return articuloRepository.findByDescripcionContainingIgnoreCaseOrderByPrecioAsc(q);
    }
}
