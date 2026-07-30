package com.parfum.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() {}

    public record RegisterRequest(
            @NotBlank @Size(max = 80) String nombre,
            @Size(max = 80) String apellido,
            @NotBlank @Email @Size(max = 160) String email,
            @NotBlank @Size(min = 6, max = 72) String password,
            @Size(max = 30) String telefono) {}

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password) {}

    public record UsuarioResponse(Long id, String nombre, String apellido, String email,
                                  String telefono, String direccion, String rol, boolean activo) {}

    public record AuthResponse(String token, UsuarioResponse usuario) {}

    public record UpdateProfileRequest(
            @NotBlank @Size(max = 80) String nombre,
            @Size(max = 80) String apellido,
            @Size(max = 30) String telefono,
            @Size(max = 300) String direccion) {}

    public record ChangePasswordRequest(@NotBlank String actual, @NotBlank @Size(min = 6, max = 72) String nueva) {}
}
