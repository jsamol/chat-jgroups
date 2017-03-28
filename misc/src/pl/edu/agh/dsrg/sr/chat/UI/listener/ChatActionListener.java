package pl.edu.agh.dsrg.sr.chat.UI.listener;

import pl.edu.agh.dsrg.sr.chat.Chat;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ChatActionListener implements ActionListener {
    private final Chat chat;

    public ChatActionListener(Chat chat) {
        this.chat = chat;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        switch (command) {
            case "Exit":
                chat.exit();
                break;
            case "Create":
                chat.createNewChannel();
                break;
            case "Connect":
                chat.connect();
                break;
            case "Disconnect":
                chat.disconnect();
                break;
        }
    }
}
