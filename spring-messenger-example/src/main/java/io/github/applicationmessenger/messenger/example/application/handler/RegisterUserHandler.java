package io.github.applicationmessenger.messenger.example.application.handler;

import io.github.applicationmessenger.messenger.EventBus;
import io.github.applicationmessenger.messenger.example.application.command.RegisterUser;
import io.github.applicationmessenger.messenger.example.domain.event.UserRegistered;
import io.github.applicationmessenger.messenger.example.domain.model.User;
import io.github.applicationmessenger.messenger.example.domain.model.UserId;
import io.github.applicationmessenger.messenger.example.infrastructure.persistence.UserRepository;
import io.github.applicationmessenger.messenger.spring.TransactionalCommandHandler;

@TransactionalCommandHandler
public class RegisterUserHandler {
    private final UserRepository repository;
    private final EventBus eventBus;

    public RegisterUserHandler(UserRepository repository, EventBus eventBus) {
        this.repository = repository;
        this.eventBus = eventBus;
    }

    public UserId handle(RegisterUser command) {
        User user = User.register(command.email(), command.displayName());
        repository.save(user);
        eventBus.publish(new UserRegistered(user.id(), user.email()));
        return user.id();
    }
}
