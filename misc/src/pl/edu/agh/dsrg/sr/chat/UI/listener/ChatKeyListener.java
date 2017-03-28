package pl.edu.agh.dsrg.sr.chat.UI.listener;

import pl.edu.agh.dsrg.sr.chat.UI.ChatFrame;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class ChatKeyListener implements KeyListener {
    private ChatFrame chatFrame;

    public ChatKeyListener(ChatFrame chatFrame) {
        this.chatFrame = chatFrame;
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        chatFrame.keyPressed(e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
