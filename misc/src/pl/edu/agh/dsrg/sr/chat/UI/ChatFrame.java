package pl.edu.agh.dsrg.sr.chat.UI;

import pl.edu.agh.dsrg.sr.chat.Channel;
import pl.edu.agh.dsrg.sr.chat.UI.listener.ChatActionListener;
import pl.edu.agh.dsrg.sr.chat.UI.listener.ChatKeyListener;
import pl.edu.agh.dsrg.sr.chat.UI.listener.ChatListSelectionListener;
import pl.edu.agh.dsrg.sr.chat.channelClient.ChannelThread;
import pl.edu.agh.dsrg.sr.chat.channelClient.SyncThread;
import pl.edu.agh.dsrg.sr.chat.protos.ChatOperationProtos;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

import static pl.edu.agh.dsrg.sr.chat.Chat.nickname;

public class ChatFrame extends JFrame {
    private List<ChannelThread> connectedChannels;

    private Channel selectedChannel = null;

    private SyncThread syncThread = null;

    private DefaultListModel<Channel> defaultListModelChannels;

    private DefaultListModel<String> defaultListModelUsers;

    private JTextArea textAreaChat;
    private JTextArea textAreaLog;
    private JTextArea textAreaMessage;

    public ChatFrame() {
        super("Chat");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                exit();
            }
        });
        initLayout();
        setVisible(true);
        connectedChannels = new ArrayList<>();
        ChatDialog enterNicknameDialog = new ChatDialog(this, "Enter your nickname", "Nickname");
        if (enterNicknameDialog.isOkClicked()) {
            nickname = enterNicknameDialog.getField();
            syncThread = new SyncThread(this);
            syncThread.start();
        }
        else
            exit();
    }

    private void initLayout() {
        setSize(800, 700);
        setResizable(false);
        setLayout(null);

        textAreaChat = new JTextArea();
        textAreaChat.setEditable(false);
        textAreaChat.setLineWrap(true);
        textAreaChat.setWrapStyleWord(true);
        JScrollPane scrollPaneLeft = new JScrollPane(textAreaChat);
        scrollPaneLeft.setBounds(10, 20, 480,365);
        add(scrollPaneLeft);


        JLabel labelChannels = new JLabel("Channels");
        labelChannels.setBounds(500, 20, 285, 20);
        add(labelChannels);

        defaultListModelChannels = new DefaultListModel<>();
        JList<Channel> listChannels = new JList<>(defaultListModelChannels);
        listChannels.setFont(listChannels.getFont().deriveFont(Font.PLAIN));
        ListSelectionModel listSelectionModel = listChannels.getSelectionModel();
        listSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listSelectionModel.addListSelectionListener(new ChatListSelectionListener(this));
        JScrollPane scrollPaneTopRight = new JScrollPane(listChannels);
        scrollPaneTopRight.setBounds(500, 45, 285, 155);
        add(scrollPaneTopRight);

        JLabel labelUsers = new JLabel("Users");
        labelUsers.setBounds(500, 205, 285, 20);
        add(labelUsers);

        defaultListModelUsers = new DefaultListModel<>();
        JList<String> listUsers = new JList<>(defaultListModelUsers);
        listUsers.setFont(listUsers.getFont().deriveFont(Font.PLAIN));
        JScrollPane scrollPaneBottomRight = new JScrollPane(listUsers);
        scrollPaneBottomRight.setBounds(500, 230, 285, 155);
        add(scrollPaneBottomRight);

        textAreaLog = new JTextArea();
        textAreaLog.setEditable(false);
        textAreaLog.setLineWrap(true);
        textAreaLog.setWrapStyleWord(true);
        JLabel labelLog = new JLabel("Log");
        labelLog.setBounds(10, 390, 775, 20);
        add(labelLog);
        JScrollPane scrollPaneBottom = new JScrollPane(textAreaLog);
        scrollPaneBottom.setBounds(10, 415, 775, 90);
        add(scrollPaneBottom);

        textAreaMessage = new JTextArea();
        textAreaMessage.addKeyListener(new ChatKeyListener(this));
        textAreaMessage.setLineWrap(true);
        textAreaMessage.setWrapStyleWord(true);
        textAreaMessage.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "none");
        JScrollPane scrollPaneMessage = new JScrollPane(textAreaMessage);
        scrollPaneMessage.setBounds(10, 510, 775, 90);
        add(scrollPaneMessage);

        JPanel panelBottom = new JPanel();
        panelBottom.setLayout(new GridLayout(1, 4));
        panelBottom.setBounds(10, 615, 775, 35);
        add(panelBottom);

        ChatActionListener chatActionListener = new ChatActionListener(this);
        JButton buttonCreateNew = new JButton("Create new channel");
        buttonCreateNew.setActionCommand("Create");
        buttonCreateNew.addActionListener(chatActionListener);
        panelBottom.add(buttonCreateNew);

        JButton buttonConnect = new JButton("Connect");
        buttonConnect.addActionListener(chatActionListener);
        panelBottom.add(buttonConnect);

        JButton buttonDisconnect = new JButton("Disconnect");
        buttonDisconnect.addActionListener(chatActionListener);
        panelBottom.add(buttonDisconnect);

        JButton buttonExit = new JButton("Exit");
        buttonExit.addActionListener(chatActionListener);
        panelBottom.add(buttonExit);
    }

    public void createNewChannel() {
        ChatDialog createNewDialog = new ChatDialog(this, "Create new channel", "Channel name");
        if (createNewDialog.isOkClicked()) {
            String channelName;
            channelName = createNewDialog.getField();
            ChannelThread channelThread = new ChannelThread(this, syncThread, channelName);
            connectedChannels.add(channelThread);
            channelThread.start();
            insertText("New channel \"" + channelName + "\" created.\n", "LOG");
            syncThread.sendAction(ChatOperationProtos.ChatAction.ActionType.JOIN, nickname, channelName);
        }
    }

    public void connect() {
        if (selectedChannel != null) {
            for (ChannelThread channelThread : connectedChannels) {
                if (selectedChannel.getName().equals(channelThread.getChannelName()))
                    insertText("Error. You are already connected to the channel \""
                            + selectedChannel + "\"\n", "LOG");
            }
            ChannelThread channelThread = new ChannelThread(this, syncThread, selectedChannel.getName());
            connectedChannels.add(channelThread);
            channelThread.start();
            syncThread.sendAction(ChatOperationProtos.ChatAction.ActionType.JOIN, nickname, selectedChannel.getName());
        }
        else
            insertText("Error. Select a channel to connect first.\n", "LOG");
    }

    public void disconnect() {
        if (selectedChannel != null) {
            ChannelThread channelThreadToDisconnect = null;
            for (ChannelThread channelThread : connectedChannels) {
                if (selectedChannel.getName().equals(channelThread.getChannelName()))
                    channelThreadToDisconnect = channelThread;
            }
            if (channelThreadToDisconnect != null) {
                channelThreadToDisconnect.disconnect();
                connectedChannels.remove(channelThreadToDisconnect);
                syncThread.sendAction(
                        ChatOperationProtos.ChatAction.ActionType.LEAVE,
                        nickname,
                        selectedChannel.getName()
                );
                insertText("Channel \"" + selectedChannel + "\" disconnected\n", "LOG");
            }
            else
                insertText("Error. You are not connected to the channel \"" + selectedChannel + "\"\n", "LOG");
        }
        else
            insertText("Error. Select a channel to disconnect first.\n", "LOG");
    }

    public void exit() {
        for (ChannelThread channelThread : connectedChannels)
            if (!channelThread.isInterrupted())
                channelThread.disconnect();
        if (syncThread != null && !syncThread.isInterrupted())
            syncThread.disconnect();
        dispose();
    }

    public void getTextFromArea() {
        String message = textAreaMessage.getText();
        if (message.equals(""))
            return;
        textAreaMessage.setText("");
        if (selectedChannel != null) {
            boolean found = false;
            for (ChannelThread channelThread : connectedChannels) {
                if (channelThread.getChannelName().equals(selectedChannel.getName())) {
                    found = true;
                    channelThread.sendMessage(message);
                }
            }
            if (!found)
                insertText(
                        "You have to be connected to the \"" + selectedChannel.getName() + "channel first\n",
                        "LOG"
                );
        }
        else
            insertText("You have select a channel first\n", "LOG");
    }

    public void insertText(String text, String type) {
        JTextArea textArea = null;
        switch (type) {
            case "MESSAGE":
                textArea = textAreaChat;
                break;
            case "LOG":
                textArea = textAreaLog;
                break;
        }
        if (SwingUtilities.isEventDispatchThread()) {
            if (textArea != null)
                textArea.append(text);
        }
        else {
                JTextArea finalTextArea = textArea;
                SwingUtilities.invokeLater(() -> {
                    if (finalTextArea != null)
                        finalTextArea.append(text);
                });
        }
    }

    public void addChannel(String channelName, String nickname) {
        synchronized (this) {
            for (int i = 0; i < defaultListModelChannels.size(); i++) {
                if (defaultListModelChannels.elementAt(i).getName().equals(channelName)) {
                    defaultListModelChannels.elementAt(i).addNickname(nickname);
                    if (defaultListModelChannels.elementAt(i) == selectedChannel)
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
                    else if (defaultListModelChannels.elementAt(i) == selectedChannel)
                        defaultListModelUsers.removeElement(nickname);
                    return;
                }
            }
        }
    }

    public DefaultListModel<Channel> getDefaultListModelChannels() {
        return defaultListModelChannels;
    }

    public void setSelectedChannel(Channel selectedChannel) {
        this.selectedChannel = selectedChannel;
    }

    public DefaultListModel<String> getDefaultListModelUsers() {
        return defaultListModelUsers;
    }
}
