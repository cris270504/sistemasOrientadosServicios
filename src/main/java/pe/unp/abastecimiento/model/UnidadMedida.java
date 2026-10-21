package pe.unp.abastecimiento.model;

import jakarta.persistence.*;

@Entity
@Table(name = "unidad_medida")
public class UnidadMedida {

    @Id
    @Column(name = "id_unidad")
    private Integer idUnidad;

    @Column(name = "descripcion", nullable = false, length = 50)
    private String descripcion;

    public Integer getIdUnidad() { return idUnidad; }
    public String getDescripcion() { return descripcion; }
}
