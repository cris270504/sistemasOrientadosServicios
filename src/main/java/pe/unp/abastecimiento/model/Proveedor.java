package pe.unp.abastecimiento.model;

import jakarta.persistence.*;

@Entity
@Table(name = "proveedor")
public class Proveedor {

    @Id
    @Column(name = "id_proveedor")
    private Integer idProveedor;

    @Column(name = "ruc", nullable = false, length = 11, unique = true)
    private String ruc;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    protected Proveedor() { }

    public Proveedor(Integer idProveedor, String ruc, String nombre) {
        this.idProveedor = idProveedor;
        actualizar(ruc, nombre);
    }

    public void actualizar(String ruc, String nombre) {
        this.ruc = ruc;
        this.nombre = nombre;
    }

    public Integer getIdProveedor() { return idProveedor; }
    public String getRuc() { return ruc; }
    public String getNombre() { return nombre; }
}
