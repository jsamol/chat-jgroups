package pl.edu.agh.dsrg.sr.chat.UI.listener;

import pl.edu.agh.dsrg.sr.chat.UI.ChatFrame;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class ChatKeyListener implements KeyListener {
    private final ChatFrame chatFrame;

    public ChatKeyListener(ChatFrame chatFrame) {
        this.chatFrame = chatFrame;
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER)
            chatFrame.getTextFromArea();
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
