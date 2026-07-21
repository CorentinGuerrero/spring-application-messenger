package io.github.applicationmessenger.messenger.example.application.handler;

import io.github.applicationmessenger.messenger.example.application.query.GetUser;
import io.github.applicationmessenger.messenger.example.domain.model.UserView;
import io.github.applicationmessenger.messenger.example.infrastructure.persistence.UserRepository;
import io.github.applicationmessenger.messenger.spring.QueryHandler;

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
