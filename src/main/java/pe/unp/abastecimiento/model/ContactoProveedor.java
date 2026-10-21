package pe.unp.abastecimiento.model;

/** Respuesta del servicio "buscar contacto": proveedor + cuantos medicamentos abastece. */
public record ContactoProveedor(Integer idProveedor, String ruc, String nombre, long medicamentosQueAbastece) {

    public static ContactoProveedor of(Proveedor p, long total) {
        return new ContactoProveedor(p.getIdProveedor(), p.getRuc(), p.getNombre(), total);
    }
}
