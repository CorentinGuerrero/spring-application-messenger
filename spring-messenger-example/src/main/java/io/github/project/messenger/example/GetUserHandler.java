package io.github.project.messenger.example;

import io.github.project.messenger.spring.QueryHandler;

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
