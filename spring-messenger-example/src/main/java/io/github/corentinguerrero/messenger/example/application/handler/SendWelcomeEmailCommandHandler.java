package io.github.corentinguerrero.messenger.example.application.handler;

import io.github.corentinguerrero.messenger.example.application.command.SendWelcomeEmail;
import io.github.corentinguerrero.messenger.spring.CommandHandler;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@CommandHandler
public class SendWelcomeEmailCommandHandler {
    private final List<SendWelcomeEmail> sentCommands = new CopyOnWriteArrayList<>();

    public Void handle(SendWelcomeEmail command) {
        sentCommands.add(command);
        System.out.println("Sending async welcome email command to " + command.email());
        return null;
    }

    public List<SendWelcomeEmail> sentCommands() {
        return List.copyOf(sentCommands);
    }
}
