package io.github.applicationmessenger.messenger.example.application.handler;

import io.github.applicationmessenger.messenger.example.domain.event.UserRegistered;
import io.github.applicationmessenger.messenger.spring.EventHandler;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@EventHandler
public class SendWelcomeEmailHandler {
    private final List<UserRegistered> sentEmails = new CopyOnWriteArrayList<>();

    public void handle(UserRegistered event) {
        sentEmails.add(event);
        System.out.println("Sending welcome email to " + event.email());
    }

    public List<UserRegistered> sentEmails() {
        return List.copyOf(sentEmails);
    }
}
