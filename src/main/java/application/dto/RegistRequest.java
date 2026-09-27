package application.dto;

public record RegistRequest(
    String userName,
    String email,
    String password
) {}
