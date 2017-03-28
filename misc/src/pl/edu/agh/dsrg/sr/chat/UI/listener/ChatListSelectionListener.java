package pl.edu.agh.dsrg.sr.chat.UI.listener;

import pl.edu.agh.dsrg.sr.chat.Chat;
import pl.edu.agh.dsrg.sr.chat.UI.ChatFrame;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class ChatListSelectionListener implements ListSelectionListener {
    private final Chat chat;
    private final ChatFrame chatFrame;

    public ChatListSelectionListener(Chat chat, ChatFrame chatFrame) {
        this.chat = chat;
        this.chatFrame = chatFrame;
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        synchronized (chat) {
            chat.getDefaultListModelUsers().removeAllElements();
            chatFrame.setSelectedChannel(null);
            ListSelectionModel listSelectionModel = (ListSelectionModel) e.getSource();
            if (!e.getValueIsAdjusting() && !listSelectionModel.isSelectionEmpty()) {
                int minIndex = listSelectionModel.getMinSelectionIndex();
                int maxIndex = listSelectionModel.getMaxSelectionIndex();
                for (int i = minIndex; i <= maxIndex; i++) {
                    if (listSelectionModel.isSelectedIndex(i)) {
                        chatFrame.setSelectedChannel(chat.getDefaultListModelChannels().elementAt(i));
                        for (String nickname : chat.getDefaultListModelChannels().elementAt(i).getNicknames())
                            chat.getDefaultListModelUsers().addElement(nickname);
                    }
                }
            }
        }
    }
}
