package pe.unp.abastecimiento.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tipo_articulo")
public class TipoArticulo {

    @Id
    @Column(name = "id_tipo")
    private Integer idTipo;

    @Column(name = "descripcion", nullable = false, length = 100)
    private String descripcion;

    public Integer getIdTipo() { return idTipo; }
    public String getDescripcion() { return descripcion; }
}
