package io.github.applicationmessenger.messenger.example.application.query;

import io.github.applicationmessenger.messenger.Query;
import io.github.applicationmessenger.messenger.example.domain.model.UserId;
import io.github.applicationmessenger.messenger.example.domain.model.UserView;
import jakarta.validation.constraints.NotNull;

public record GetUser(@NotNull UserId userId) implements Query<UserView> {
}
