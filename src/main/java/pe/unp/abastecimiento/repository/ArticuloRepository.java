package pe.unp.abastecimiento.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.unp.abastecimiento.model.Articulo;

import java.util.List;
import java.util.Optional;

public interface ArticuloRepository extends JpaRepository<Articulo, Integer> {

    Optional<Articulo> findFirstByOrderByPrecioDesc();

    List<Articulo> findByDescripcionContainingIgnoreCaseOrderByPrecioAsc(String descripcion);

    long countByProveedorIdProveedor(Integer idProveedor);

    List<Articulo> findByProveedorIdProveedorOrderByDescripcionAscIdArticuloAsc(Integer idProveedor);
}
