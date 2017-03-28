package pl.edu.agh.dsrg.sr.chat;

import pl.edu.agh.dsrg.sr.chat.UI.ChatDialog;
import pl.edu.agh.dsrg.sr.chat.UI.ChatFrame;
import pl.edu.agh.dsrg.sr.chat.channelClient.ChannelThread;
import pl.edu.agh.dsrg.sr.chat.channelClient.SyncThread;
import pl.edu.agh.dsrg.sr.chat.protos.ChatOperationProtos;

import javax.swing.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Chat {
    public static final String managementChannel = "ChatManagement321321";
    public static String nickname;

    private ChatFrame chatFrame;
    private SyncThread syncThread;

    private Map<String, List<String>> history;

    private DefaultComboBoxModel<ChannelThread> defaultComboBoxModelChannels;
    private DefaultListModel<Channel> defaultListModelChannels;
    private DefaultListModel<String> defaultListModelUsers;

    private Chat() {
        syncThread = null;
        history = new LinkedHashMap<>();
    }

    private void start() {
        chatFrame = new ChatFrame(this);
    }

    public static void main(String[] args) {
        Chat chat = new Chat();
        chat.start();
    }

    public void createNewChannel() {
        ChatDialog createNewDialog = new ChatDialog(chatFrame, "Create new channel", "Channel name");
        if (createNewDialog.isOkClicked()) {
            String channelName;
            channelName = createNewDialog.getField();
            ChannelThread channelThread = new ChannelThread(chatFrame, syncThread, channelName);
            history.put(channelName, new ArrayList<>());
            defaultComboBoxModelChannels.addElement(channelThread);
            channelThread.start();
            chatFrame.insertText("New channel \"" + channelName + "\" created.\n");
            syncThread.sendAction(ChatOperationProtos.ChatAction.ActionType.JOIN, nickname, channelName);
        }
    }

    public void connect() {
        if (chatFrame.getSelectedChannel() != null) {
            for (int i = 0; i < defaultComboBoxModelChannels.getSize(); i++) {
                if (chatFrame.getSelectedChannel().getName().equals(defaultComboBoxModelChannels.getElementAt(i).getChannelName())) {
                    chatFrame.insertText(
                            "Error. You are already connected to the channel \""
                                    + chatFrame.getSelectedChannel() + "\".\n"
                    );
                    return;
                }
            }
            ChannelThread channelThread =
                    new ChannelThread(chatFrame, syncThread, chatFrame.getSelectedChannel().getName());
            history.put(chatFrame.getSelectedChannel().getName(), new ArrayList<>());
            defaultComboBoxModelChannels.addElement(channelThread);
            channelThread.start();
            syncThread.sendAction(
                    ChatOperationProtos.ChatAction.ActionType.JOIN,
                    nickname,
                    chatFrame.getSelectedChannel().getName()
            );
            chatFrame.insertText("Connected to the channel \"" + chatFrame.getSelectedChannel().getName() + "\".\n");
        }
        else
            chatFrame.insertText("Error. Select a channel to connect first.\n");
    }

    public void disconnect() {
        if (chatFrame.getSelectedChannel() != null) {
            ChannelThread channelThreadToDisconnect = null;
            for (int i = 0; i < defaultComboBoxModelChannels.getSize(); i++) {
                if (chatFrame.getSelectedChannel().getName()
                        .equals(defaultComboBoxModelChannels.getElementAt(i).getChannelName()))
                    channelThreadToDisconnect = defaultComboBoxModelChannels.getElementAt(i);
            }
            if (channelThreadToDisconnect != null) {
                channelThreadToDisconnect.disconnect();
                syncThread.sendAction(
                        ChatOperationProtos.ChatAction.ActionType.LEAVE,
                        nickname,
                        chatFrame.getSelectedChannel().getName()
                );
                chatFrame.insertText("Disconnected from the channel \"" + chatFrame.getSelectedChannel() + "\".\n");
                defaultComboBoxModelChannels.removeElement(channelThreadToDisconnect);
                history.remove(chatFrame.getSelectedChannel().getName());
            }
            else
                chatFrame.insertText(
                        "Error. You are not connected to the channel \""
                        + chatFrame.getSelectedChannel() + "\".\n"
                );
        }
        else
            chatFrame.insertText("Error. Select a channel to disconnect first.\n");
    }

    public void exit() {
        for (int i = 0; i < defaultComboBoxModelChannels.getSize(); i++) {
            if (!defaultComboBoxModelChannels.getElementAt(i).isInterrupted())
                defaultComboBoxModelChannels.getElementAt(i).disconnect();
        }
        if (syncThread != null && !syncThread.isInterrupted())
            syncThread.disconnect();
        chatFrame.dispose();
    }

    public void addChannel(String channelName, String nickname) {
        synchronized (this) {
            for (int i = 0; i < defaultListModelChannels.size(); i++) {
                if (defaultListModelChannels.elementAt(i).getName().equals(channelName)) {
                    defaultListModelChannels.elementAt(i).addNickname(nickname);
                    if (defaultListModelChannels.elementAt(i) == chatFrame.getSelectedChannel())
                        defaultListModelUsers.addElement(nickname);
                    return;
                }
            }
            Channel channel = new Channel(channelName);
            channel.addNickname(nickname);
            defaultListModelChannels.addElement(channel);
        }
    }

    public void removeNicknameFromChannelList(String channelName, String nickname) {
        synchronized (this) {
            for (int i = 0; i < defaultListModelChannels.size(); i++) {
                if (defaultListModelChannels.elementAt(i).getName().equals(channelName)) {
                    defaultListModelChannels.elementAt(i).removeNickname(nickname);
                    if (defaultListModelChannels.elementAt(i).getNicknames().size() == 0)
                        defaultListModelChannels.remove(i);
                    else if (defaultListModelChannels.elementAt(i) == chatFrame.getSelectedChannel())
                        defaultListModelUsers.removeElement(nickname);
                    return;
                }
            }
        }
    }

    public Map<String, List<String>> getHistory() {
        return history;
    }

    public SyncThread getSyncThread() {
        return syncThread;
    }

    public void setSyncThread(SyncThread syncThread) {
        this.syncThread = syncThread;
    }

    public DefaultComboBoxModel<ChannelThread> getDefaultComboBoxModelChannels() {
        return defaultComboBoxModelChannels;
    }

    public DefaultListModel<Channel> getDefaultListModelChannels() {
        return defaultListModelChannels;
    }

    public DefaultListModel<String> getDefaultListModelUsers() {
        return defaultListModelUsers;
    }

    public void setDefaultComboBoxModelChannels(DefaultComboBoxModel<ChannelThread> defaultComboBoxModelChannels) {
        this.defaultComboBoxModelChannels = defaultComboBoxModelChannels;
    }

    public void setDefaultListModelChannels(DefaultListModel<Channel> defaultListModelChannels) {
        this.defaultListModelChannels = defaultListModelChannels;
    }

    public void setDefaultListModelUsers(DefaultListModel<String> defaultListModelUsers) {
        this.defaultListModelUsers = defaultListModelUsers;
    }
}
