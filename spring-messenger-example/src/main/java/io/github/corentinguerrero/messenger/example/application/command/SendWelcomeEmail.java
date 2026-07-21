package io.github.corentinguerrero.messenger.example.application.command;

import io.github.corentinguerrero.messenger.Command;
import io.github.corentinguerrero.messenger.example.domain.model.UserId;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SendWelcomeEmail(
    UserId userId,
    @Email @NotBlank String email
) implements Command<Void> {
}
