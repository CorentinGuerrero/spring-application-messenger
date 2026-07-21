package io.github.applicationmessenger.messenger.example.application.handler;

import io.github.applicationmessenger.messenger.example.application.command.RebuildUserSearchIndex;
import io.github.applicationmessenger.messenger.example.domain.model.UserId;
import io.github.applicationmessenger.messenger.spring.CommandHandler;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@CommandHandler
public class RebuildUserSearchIndexHandler {
    private final List<UserId> rebuiltUsers = new CopyOnWriteArrayList<>();

    public Void handle(RebuildUserSearchIndex command) {
        rebuiltUsers.add(command.userId());
        System.out.println("Rebuilding search index for user " + command.userId().value());
        return null;
    }

    public List<UserId> rebuiltUsers() {
        return List.copyOf(rebuiltUsers);
    }
}
