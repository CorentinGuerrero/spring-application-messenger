package io.github.corentinguerrero.messenger.example.application.query;

import io.github.corentinguerrero.messenger.Query;
import io.github.corentinguerrero.messenger.example.domain.model.UserId;
import io.github.corentinguerrero.messenger.example.domain.model.UserView;
import jakarta.validation.constraints.NotNull;

public record GetUser(@NotNull UserId userId) implements Query<UserView> {
}
