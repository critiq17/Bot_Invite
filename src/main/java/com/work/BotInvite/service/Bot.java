package com.work.BotInvite.service;

import com.work.BotInvite.config.BotConfig;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.ExportChatInviteLink;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
public class Bot extends TelegramLongPollingBot {

    private final BotConfig config;
    private final List<String> usersList = new ArrayList<>();

    public void addUser(String username) {
        if(!usersList.contains(username)) {
            usersList.add(username);
        }
    }

    private void removeUser(Long chatId, String username) {
        if(usersList.contains(username)) {
            usersList.remove(username);
            sendMessage(chatId, "User @" + username + "deleted from list");
        } else {
            sendMessage(chatId, "User @" + username + " not found in the list");
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Chat chat = update.getMessage().getChat();
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            long groupId = chat.getId();

            if (messageText.startsWith("/adduser")) {
                String[] parts = messageText.split(" ");
                if (parts.length == 2) {
                    String username = parts[1].replace("@", "");
                    addUser(username);
                    sendMessage(chatId, "User @" + username + " added to list");
                } else {
                    sendMessage(chatId, "correct use of command: " + "/adduser @username" + "\n");

                }
            } else if (messageText.startsWith("/removeuser")) {
                String[] parts = messageText.split(" ");
                if (parts.length == 2) {
                    String username = parts[1].replace("@", "");
                    removeUser(chatId, username);
                } else {
                    sendMessage(chatId, "correct use of command: " + "/removeuser @username");
                }
            }
            else if ("/invite_all".equals(messageText)) {
                inviteUsersToGroup(chatId);
            }
            else if (messageText.equals("/listusers")) {
                sendUserList(chatId);
                System.out.println("Супергрупа ID: " + chatId);
            } else {
                sendMessage(chatId, "Command not recognized");
            } 
        }
    }


    private void inviteUsersToGroup(Long chatId) {
        try {
            ExportChatInviteLink exportInviteLink = new ExportChatInviteLink();
            exportInviteLink.setChatId(config.getGroupId());

            String inviteLink = execute(exportInviteLink);

            for(String username : usersList) {
                // String messageText = "Invite for @" + username + ": " + inviteLink;
                sendMessage(chatId, "Привіт, @" + username + ", приєднуся до группи за цим посиланням: " + inviteLink);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error invite");
        }
    }

    @Override
    public String getBotToken() {
        return config.getBotToken();
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    private void sendUserList(Long chatId) {
        if(usersList.isEmpty()) {
            sendMessage(chatId, "The list is empty");
        } else {
            StringBuilder userListManager = new StringBuilder("List all users:\n");
            for(String user : usersList) {
                userListManager.append("@").append(user).append("\n");
            }
            sendMessage(chatId, userListManager.toString());
        }
    }


    private void sendMessage(Long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(textToSend);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
