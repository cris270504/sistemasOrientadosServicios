package pe.unp.abastecimiento.dto;

import jakarta.validation.constraints.*;

/** El RUC es texto para conservar sus once dígitos, incluidos los ceros iniciales. */
public record ProveedorRequest(
        @Positive Integer idProveedor,
        @NotBlank @Pattern(regexp = "[0-9]{11}", message = "debe contener exactamente 11 dígitos") String ruc,
        @NotBlank @Size(max = 150) String nombre
) { }
