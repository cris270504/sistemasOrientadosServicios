package pe.unp.abastecimiento.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.unp.abastecimiento.model.Proveedor;

import java.util.List;

public interface ProveedorRepository extends JpaRepository<Proveedor, Integer> {

    boolean existsByRucAndIdProveedorNot(String ruc, Integer idProveedor);

    List<Proveedor> findByNombreContainingIgnoreCaseOrRucContainingOrderByNombreAsc(String nombre, String ruc);
}
