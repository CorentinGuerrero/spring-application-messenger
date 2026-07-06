package io.github.corentinguerrero.messenger.example.application.command;

import io.github.corentinguerrero.messenger.Command;
import io.github.corentinguerrero.messenger.example.domain.model.UserId;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterUser(
    @Email @NotBlank String email,
    @NotBlank String displayName
) implements Command<UserId> {
}
