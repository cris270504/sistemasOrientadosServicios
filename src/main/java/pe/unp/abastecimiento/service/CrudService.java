package pe.unp.abastecimiento.service;

import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pe.unp.abastecimiento.dto.ArticuloRequest;
import pe.unp.abastecimiento.dto.ProveedorRequest;
import pe.unp.abastecimiento.model.*;
import pe.unp.abastecimiento.repository.*;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CrudService {
    private final ArticuloRepository articulos;
    private final ProveedorRepository proveedores;
    private final UnidadMedidaRepository unidades;
    private final TipoArticuloRepository tipos;
    private final EntityManager entityManager;

    public CrudService(ArticuloRepository articulos, ProveedorRepository proveedores,
                       UnidadMedidaRepository unidades, TipoArticuloRepository tipos, EntityManager entityManager) {
        this.articulos = articulos;
        this.proveedores = proveedores;
        this.unidades = unidades;
        this.tipos = tipos;
        this.entityManager = entityManager;
    }

    public List<Articulo> listarArticulos() {
        return articulos.findAll(Sort.by("idArticulo"));
    }

    public Articulo obtenerArticulo(Integer id) {
        validarId(id);
        return articulos.findById(id).orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Medicamento no encontrado."));
    }

    public List<Articulo> listarArticulosProveedor(Integer idProveedor) {
        obtenerProveedor(idProveedor);
        return articulos.findByProveedorIdProveedorOrderByDescripcionAscIdArticuloAsc(idProveedor);
    }

    @Transactional
    public Articulo crearArticulo(ArticuloRequest datos) {
        validarId(datos.idArticulo());
        if (articulos.existsById(datos.idArticulo())) {
            throw error(HttpStatus.CONFLICT, "Ya existe un medicamento con ese ID.");
        }
        Articulo articulo = new Articulo(datos.idArticulo(), datos.descripcion().trim(), datos.precio(),
                buscarUnidad(datos.idUnidad()), buscarProveedorRelacionado(datos.idProveedor()), buscarTipo(datos.idTipo()));
        entityManager.persist(articulo);
        entityManager.flush();
        return articulo;
    }

    @Transactional
    public Articulo actualizarArticulo(Integer id, ArticuloRequest datos) {
        Articulo articulo = obtenerArticulo(id);
        validarCoincidenciaId(id, datos.idArticulo());
        articulo.actualizar(datos.descripcion().trim(), datos.precio(), buscarUnidad(datos.idUnidad()),
                buscarProveedorRelacionado(datos.idProveedor()), buscarTipo(datos.idTipo()));
        return articulos.saveAndFlush(articulo);
    }

    @Transactional
    public void eliminarArticulo(Integer id) {
        articulos.delete(obtenerArticulo(id));
        articulos.flush();
    }

    public List<Proveedor> listarProveedores() {
        return proveedores.findAll(Sort.by("idProveedor"));
    }

    public Proveedor obtenerProveedor(Integer id) {
        validarId(id);
        return proveedores.findById(id).orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Proveedor no encontrado."));
    }

    @Transactional
    public Proveedor crearProveedor(ProveedorRequest datos) {
        validarId(datos.idProveedor());
        if (proveedores.existsById(datos.idProveedor())) {
            throw error(HttpStatus.CONFLICT, "Ya existe un proveedor con ese ID.");
        }
        validarRucDisponible(datos.ruc(), datos.idProveedor());
        Proveedor proveedor = new Proveedor(datos.idProveedor(), datos.ruc(), datos.nombre().trim());
        entityManager.persist(proveedor);
        entityManager.flush();
        return proveedor;
    }

    @Transactional
    public Proveedor actualizarProveedor(Integer id, ProveedorRequest datos) {
        Proveedor proveedor = obtenerProveedor(id);
        validarCoincidenciaId(id, datos.idProveedor());
        validarRucDisponible(datos.ruc(), id);
        proveedor.actualizar(datos.ruc(), datos.nombre().trim());
        return proveedores.saveAndFlush(proveedor);
    }

    @Transactional
    public void eliminarProveedor(Integer id) {
        Proveedor proveedor = obtenerProveedor(id);
        if (articulos.countByProveedorIdProveedor(id) > 0) {
            throw error(HttpStatus.CONFLICT, "No se puede eliminar el proveedor: tiene medicamentos asociados. Reasígnalos o elimínalos primero.");
        }
        proveedores.delete(proveedor);
        proveedores.flush();
    }

    public List<UnidadMedida> listarUnidades() {
        return unidades.findAll(Sort.by("idUnidad"));
    }

    public List<TipoArticulo> listarTipos() {
        return tipos.findAll(Sort.by("idTipo"));
    }

    private TipoArticulo buscarTipo(Integer id) {
        return tipos.findById(id).orElseThrow(() -> error(HttpStatus.BAD_REQUEST, "El tipo de artículo indicado no existe."));
    }

    private UnidadMedida buscarUnidad(Integer id) {
        return unidades.findById(id).orElseThrow(() -> error(HttpStatus.BAD_REQUEST, "La unidad de medida indicada no existe."));
    }

    private Proveedor buscarProveedorRelacionado(Integer id) {
        return proveedores.findById(id).orElseThrow(() -> error(HttpStatus.BAD_REQUEST, "El proveedor indicado no existe."));
    }

    private void validarRucDisponible(String ruc, Integer id) {
        if (proveedores.existsByRucAndIdProveedorNot(ruc, id)) {
            throw error(HttpStatus.CONFLICT, "El RUC ya está registrado para otro proveedor.");
        }
    }

    private void validarId(Integer id) {
        if (id == null || id <= 0) {
            throw error(HttpStatus.BAD_REQUEST, "Debes indicar un ID entero positivo.");
        }
    }

    private void validarCoincidenciaId(Integer idUrl, Integer idBody) {
        if (idBody != null && !idUrl.equals(idBody)) {
            throw error(HttpStatus.BAD_REQUEST, "El ID del cuerpo debe coincidir con el de la URL.");
        }
    }

    private ResponseStatusException error(HttpStatus status, String mensaje) {
        return new ResponseStatusException(status, mensaje);
    }
}
