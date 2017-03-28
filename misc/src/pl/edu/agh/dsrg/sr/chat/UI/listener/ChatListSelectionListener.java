package pl.edu.agh.dsrg.sr.chat.UI.listener;

import pl.edu.agh.dsrg.sr.chat.UI.ChatFrame;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class ChatListSelectionListener implements ListSelectionListener {
    private ChatFrame chatFrame;

    public ChatListSelectionListener(ChatFrame chatFrame) {
        this.chatFrame = chatFrame;
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        chatFrame.getDefaultListModelUsers().removeAllElements();
        chatFrame.setSelectedChannel(null);
        ListSelectionModel listSelectionModel = (ListSelectionModel) e.getSource();
        if (!e.getValueIsAdjusting() && !listSelectionModel.isSelectionEmpty()) {
            int minIndex = listSelectionModel.getMinSelectionIndex();
            int maxIndex = listSelectionModel.getMaxSelectionIndex();
            for (int i = minIndex; i <= maxIndex; i++) {
                if (listSelectionModel.isSelectedIndex(i)) {
                    chatFrame.setSelectedChannel(chatFrame.getDefaultListModelChannels().elementAt(i));
                    for (String nickname : chatFrame.getDefaultListModelChannels().elementAt(i).getNicknames())
                        chatFrame.getDefaultListModelUsers().addElement(nickname);
                }
            }
        }
    }
}
