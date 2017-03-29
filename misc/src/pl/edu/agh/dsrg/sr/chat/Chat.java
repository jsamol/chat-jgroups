package pl.edu.agh.dsrg.sr.chat;

import pl.edu.agh.dsrg.sr.chat.UI.ChatDialog;
import pl.edu.agh.dsrg.sr.chat.UI.ChatFrame;
import pl.edu.agh.dsrg.sr.chat.client.MessageChannelThread;
import pl.edu.agh.dsrg.sr.chat.client.ActionChannelThread;
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
    private ActionChannelThread actionChannelThread;

    private Map<String, List<String>> history;

    private DefaultComboBoxModel<MessageChannelThread> connectedChannels;
    private DefaultListModel<Channel> allChannels;
    private DefaultListModel<String> users;

    private Chat() {
        actionChannelThread = null;
        history = new LinkedHashMap<>();
    }

    private void start() {
        chatFrame = new ChatFrame(this);
    }

    public static void main(String[] args) {
        System.setProperty("java.net.preferIPv4Stack", "true");
        Chat chat = new Chat();
        chat.start();
    }

    public void createNewChannel() {
        ChatDialog createNewDialog = new ChatDialog(chatFrame, "Create new channel", "Channel name");
        if (createNewDialog.isOkClicked()) {
            String channelName;
            channelName = createNewDialog.getField();
            MessageChannelThread messageChannelThread =
                    new MessageChannelThread(chatFrame, actionChannelThread, channelName);
            history.put(channelName, new ArrayList<>());
            connectedChannels.addElement(messageChannelThread);
            messageChannelThread.start();
            chatFrame.insertText("New channel \"" + channelName + "\" created.\n");
            chatFrame.insertText("Connected to the channel \"" + channelName + "\".\n");
            actionChannelThread.sendAction(ChatOperationProtos.ChatAction.ActionType.JOIN, nickname, channelName);
        }
    }

    public void connect() {
        if (chatFrame.getSelectedChannel() != null) {
            for (int i = 0; i < connectedChannels.getSize(); i++) {
                if (chatFrame.getSelectedChannel().getName()
                        .equals(connectedChannels.getElementAt(i).getChannelName())) {
                    chatFrame.insertText("Error. You are already connected to the channel \""
                            + chatFrame.getSelectedChannel() + "\".\n");
                    return;
                }
            }
            MessageChannelThread messageChannelThread =
                    new MessageChannelThread(chatFrame, actionChannelThread, chatFrame.getSelectedChannel().getName());
            history.put(chatFrame.getSelectedChannel().getName(), new ArrayList<>());
            connectedChannels.addElement(messageChannelThread);
            messageChannelThread.start();
            actionChannelThread.sendAction(
                    ChatOperationProtos.ChatAction.ActionType.JOIN,
                    nickname,
                    chatFrame.getSelectedChannel().getName()
            );
            chatFrame.insertText("Connected to the channel \""
                    + chatFrame.getSelectedChannel().getName() + "\".\n");
        }
        else
            chatFrame.insertText("Error. Select a channel to connect first.\n");
    }

    public void disconnect() {
        if (chatFrame.getSelectedChannel() != null) {
            MessageChannelThread messageChannelThreadToDisconnect = null;
            for (int i = 0; i < connectedChannels.getSize(); i++) {
                if (chatFrame.getSelectedChannel().getName()
                        .equals(connectedChannels.getElementAt(i).getChannelName()))
                    messageChannelThreadToDisconnect = connectedChannels.getElementAt(i);
            }
            if (messageChannelThreadToDisconnect != null) {
                messageChannelThreadToDisconnect.disconnect();
                actionChannelThread.sendAction(
                        ChatOperationProtos.ChatAction.ActionType.LEAVE,
                        nickname,
                        chatFrame.getSelectedChannel().getName()
                );
                chatFrame.insertText("Disconnected from the channel \"" + chatFrame.getSelectedChannel() + "\".\n");
                connectedChannels.removeElement(messageChannelThreadToDisconnect);
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
        for (int i = 0; i < connectedChannels.getSize(); i++) {
            if (!connectedChannels.getElementAt(i).isInterrupted())
                connectedChannels.getElementAt(i).disconnect();
        }
        if (actionChannelThread != null && !actionChannelThread.isInterrupted())
            actionChannelThread.disconnect();
        chatFrame.dispose();
    }

    public void addChannel(String channelName, String nickname) {
        synchronized (this) {
            for (int i = 0; i < allChannels.size(); i++) {
                if (allChannels.elementAt(i).getName().equals(channelName)) {
                    allChannels.elementAt(i).addNickname(nickname);
                    if (allChannels.elementAt(i) == chatFrame.getSelectedChannel())
                        users.addElement(nickname);
                    return;
                }
            }
            Channel channel = new Channel(channelName);
            channel.addNickname(nickname);
            allChannels.addElement(channel);
        }
    }

    public void removeNicknameFromChannelList(String channelName, String nickname) {
        synchronized (this) {
            for (int i = 0; i < allChannels.size(); i++) {
                if (allChannels.elementAt(i).getName().equals(channelName)) {
                    allChannels.elementAt(i).removeNickname(nickname);
                    if (allChannels.elementAt(i).getNicknames().size() == 0)
                        allChannels.remove(i);
                    else if (allChannels.elementAt(i) == chatFrame.getSelectedChannel())
                        users.removeElement(nickname);
                    return;
                }
            }
        }
    }

    public Map<String, List<String>> getHistory() {
        return history;
    }

    public ActionChannelThread getActionChannelThread() {
        return actionChannelThread;
    }

    public void setActionChannelThread(ActionChannelThread actionChannelThread) {
        this.actionChannelThread = actionChannelThread;
    }

    public DefaultComboBoxModel<MessageChannelThread> getConnectedChannels() {
        return connectedChannels;
    }

    public DefaultListModel<Channel> getAllChannels() {
        return allChannels;
    }

    public DefaultListModel<String> getUsers() {
        return users;
    }

    public void setConnectedChannels(DefaultComboBoxModel<MessageChannelThread> connectedChannels) {
        this.connectedChannels = connectedChannels;
    }

    public void setAllChannels(DefaultListModel<Channel> allChannels) {
        this.allChannels = allChannels;
    }

    public void setUsers(DefaultListModel<String> users) {
        this.users = users;
    }
}
