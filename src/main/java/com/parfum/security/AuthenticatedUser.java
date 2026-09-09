package com.parfum.security;

public record AuthenticatedUser(Long id, String email, String nombre, String rol) {}
