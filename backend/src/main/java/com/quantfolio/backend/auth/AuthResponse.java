package com.quantfolio.backend.auth;

public record AuthResponse(String token, String email, String fullName) {}
