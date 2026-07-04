package io.github.project.messenger.example;

import io.github.project.messenger.Command;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterUser(
    @Email @NotBlank String email,
    @NotBlank String displayName
) implements Command<UserId> {
}
