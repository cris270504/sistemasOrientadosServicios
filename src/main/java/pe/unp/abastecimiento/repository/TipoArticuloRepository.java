package pe.unp.abastecimiento.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.unp.abastecimiento.model.TipoArticulo;

public interface TipoArticuloRepository extends JpaRepository<TipoArticulo, Integer> {
}
