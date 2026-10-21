package pe.unp.abastecimiento.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "articulo")
public class Articulo {

    @Id
    @Column(name = "id_articulo")
    private Integer idArticulo;

    @Column(name = "descripcion", nullable = false, length = 150)
    private String descripcion;

    @Column(name = "precio", nullable = false, precision = 9, scale = 2)
    private BigDecimal precio;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "unidad_medida", nullable = false)
    private UnidadMedida unidadMedida;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_proveedor", nullable = false)
    private Proveedor proveedor;

    protected Articulo() { }

    public Articulo(Integer idArticulo, String descripcion, BigDecimal precio, UnidadMedida unidadMedida, Proveedor proveedor) {
        this.idArticulo = idArticulo;
        actualizar(descripcion, precio, unidadMedida, proveedor);
    }

    public void actualizar(String descripcion, BigDecimal precio, UnidadMedida unidadMedida, Proveedor proveedor) {
        this.descripcion = descripcion;
        this.precio = precio;
        this.unidadMedida = unidadMedida;
        this.proveedor = proveedor;
    }

    public Integer getIdArticulo() { return idArticulo; }
    public String getDescripcion() { return descripcion; }
    public BigDecimal getPrecio() { return precio; }
    public UnidadMedida getUnidadMedida() { return unidadMedida; }
    public Proveedor getProveedor() { return proveedor; }
}
