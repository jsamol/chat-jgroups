package pl.edu.agh.dsrg.sr.chat.UI.listener;

import pl.edu.agh.dsrg.sr.chat.UI.ChatFrame;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ChatActionListener implements ActionListener {
    private final ChatFrame chatFrame;

    public ChatActionListener(ChatFrame chatFrame) {
        this.chatFrame = chatFrame;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        switch (command) {
            case "Exit":
                chatFrame.exit();
                break;
            case "Create":
                chatFrame.createNewChannel();
                break;
            case "Connect":
                chatFrame.connect();
                break;
            case "Disconnect":
                chatFrame.disconnect();
                break;
        }
    }
}
