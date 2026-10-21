package pe.unp.abastecimiento.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.unp.abastecimiento.model.UnidadMedida;

public interface UnidadMedidaRepository extends JpaRepository<UnidadMedida, Integer> {
}
