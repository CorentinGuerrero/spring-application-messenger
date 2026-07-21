package io.github.applicationmessenger.messenger.example.application.command;

import io.github.applicationmessenger.messenger.Command;
import io.github.applicationmessenger.messenger.example.domain.model.UserId;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterUser(
    @Email @NotBlank String email,
    @NotBlank String displayName
) implements Command<UserId> {
}
