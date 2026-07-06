package io.github.corentinguerrero.messenger.example.application.handler;

import io.github.corentinguerrero.messenger.example.application.query.GetUser;
import io.github.corentinguerrero.messenger.example.domain.model.UserView;
import io.github.corentinguerrero.messenger.example.infrastructure.persistence.UserRepository;
import io.github.corentinguerrero.messenger.spring.QueryHandler;

@QueryHandler
public class GetUserHandler {
    private final UserRepository repository;

    public GetUserHandler(UserRepository repository) {
        this.repository = repository;
    }

    public UserView handle(GetUser query) {
        return repository.findById(query.userId())
            .map(UserView::from)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + query.userId().value()));
    }
}
