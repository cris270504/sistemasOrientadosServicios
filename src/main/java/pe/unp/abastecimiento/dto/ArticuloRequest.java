package pe.unp.abastecimiento.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/** El ID se indica al crear. En PUT puede omitirse, pues se usa el ID de la URL. */
public record ArticuloRequest(
        @Positive Integer idArticulo,
        @NotBlank @Size(max = 150) String descripcion,
        @NotNull @DecimalMin("0.00") @Digits(integer = 7, fraction = 2) BigDecimal precio,
        @NotNull @Positive Integer idUnidad,
        @NotNull @Positive Integer idProveedor,
        @NotNull @Positive Integer idTipo
) { }
