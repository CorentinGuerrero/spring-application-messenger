package io.github.project.messenger.example;

import io.github.project.messenger.Query;
import jakarta.validation.constraints.NotNull;

public record GetUser(@NotNull UserId userId) implements Query<UserView> {
}
