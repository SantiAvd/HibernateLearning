package org.example.config;

public class BotConfig {
    private final String botToken;
    private final String botName;

    public BotConfig(String botToken, String botName) {
        this.botToken = botToken;
        this.botName = botName;
    }

    public String getBotToken() {
        return botToken;
    }

    public String getBotName() {
        return botName;
    }
}
