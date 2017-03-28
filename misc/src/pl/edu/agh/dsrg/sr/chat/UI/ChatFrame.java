package pl.edu.agh.dsrg.sr.chat.UI;

import pl.edu.agh.dsrg.sr.chat.Channel;
import pl.edu.agh.dsrg.sr.chat.UI.listener.ChatButtonActionListener;
import pl.edu.agh.dsrg.sr.chat.UI.listener.ChatListSelectionListener;
import pl.edu.agh.dsrg.sr.chat.channelClient.ChannelThread;
import pl.edu.agh.dsrg.sr.chat.channelClient.SyncThread;
import pl.edu.agh.dsrg.sr.chat.protos.ChatOperationProtos;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static pl.edu.agh.dsrg.sr.chat.Chat.nickname;

public class ChatFrame extends JFrame {
    private Map<String, List<String>> history;
    private DefaultComboBoxModel<ChannelThread> defaultComboBoxModelChannels;

    private Channel selectedChannel = null;
    private ChannelThread selectedChannelThread = null;

    private SyncThread syncThread = null;

    private DefaultListModel<Channel> defaultListModelChannels;

    private DefaultListModel<String> defaultListModelUsers;

    private JLabel labelNickname;

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
        ChatDialog enterNicknameDialog = new ChatDialog(this, "Enter your nickname", "Nickname");
        if (enterNicknameDialog.isOkClicked()) {
            nickname = enterNicknameDialog.getField();
            labelNickname.setText("Your nickname: " + nickname);
            syncThread = new SyncThread(this);
            syncThread.start();
            history = new LinkedHashMap<>();
        }
        else
            exit();
    }

    private void initLayout() {
        setSize(800, 700);
        setResizable(false);
        setLayout(null);

        defaultComboBoxModelChannels = new DefaultComboBoxModel<>();
        JComboBox<ChannelThread> comboBoxChannelThread = new JComboBox<>(defaultComboBoxModelChannels);
        comboBoxChannelThread.setBounds(10, 20, 480, 20);
        comboBoxChannelThread.addActionListener(e -> {
            JComboBox comboBox = (JComboBox) e.getSource();
            if (comboBox.getSelectedItem() != null && comboBox.getSelectedItem() != selectedChannel) {
                selectedChannelThread = (ChannelThread) comboBox.getSelectedItem();
                updateTextArea();
            }
            else
                textAreaChat.setText(null);
        });
        add(comboBoxChannelThread);

        textAreaChat = new JTextArea();
        textAreaChat.setEditable(false);
        textAreaChat.setLineWrap(true);
        textAreaChat.setWrapStyleWord(true);
        JScrollPane scrollPaneLeft = new JScrollPane(textAreaChat);
        scrollPaneLeft.setBounds(10, 45, 480,435);
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
        labelLog.setBounds(500, 390, 775, 20);
        add(labelLog);
        JScrollPane scrollPaneBottom = new JScrollPane(textAreaLog);
        scrollPaneBottom.setBounds(500, 415, 285, 185);
        add(scrollPaneBottom);

        labelNickname = new JLabel();
        labelNickname.setBounds(10, 485, 230, 20);
        add(labelNickname);

        textAreaMessage = new JTextArea();
        textAreaMessage.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    getTextFromArea();
            }
        });
        textAreaMessage.setLineWrap(true);
        textAreaMessage.setWrapStyleWord(true);
        textAreaMessage.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "none");
        JScrollPane scrollPaneMessage = new JScrollPane(textAreaMessage);
        scrollPaneMessage.setBounds(10, 510, 480, 90);
        add(scrollPaneMessage);

        JPanel panelBottom = new JPanel();
        panelBottom.setLayout(new GridLayout(1, 4));
        panelBottom.setBounds(10, 615, 775, 35);
        add(panelBottom);

        ChatButtonActionListener chatActionListener = new ChatButtonActionListener(this);
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
            history.put(channelName, new ArrayList<>());
            defaultComboBoxModelChannels.addElement(channelThread);
            channelThread.start();
            insertText("New channel \"" + channelName + "\" created.\n");
            syncThread.sendAction(ChatOperationProtos.ChatAction.ActionType.JOIN, nickname, channelName);
        }
    }

    public void connect() {
        if (selectedChannel != null) {
            for (int i = 0; i < defaultComboBoxModelChannels.getSize(); i++) {
                if (selectedChannel.getName().equals(defaultComboBoxModelChannels.getElementAt(i).getChannelName())) {
                    insertText("Error. You are already connected to the channel \"" + selectedChannel + "\".\n");
                    return;
                }
            }
            ChannelThread channelThread = new ChannelThread(this, syncThread, selectedChannel.getName());
            history.put(selectedChannel.getName(), new ArrayList<>());
            defaultComboBoxModelChannels.addElement(channelThread);
            channelThread.start();
            syncThread.sendAction(ChatOperationProtos.ChatAction.ActionType.JOIN, nickname, selectedChannel.getName());
            insertText("Connected to the channel \"" + selectedChannel.getName() + "\".\n");
        }
        else
            insertText("Error. Select a channel to connect first.\n");
    }

    public void disconnect() {
        if (selectedChannel != null) {
            ChannelThread channelThreadToDisconnect = null;
            for (int i = 0; i < defaultComboBoxModelChannels.getSize(); i++) {
                if (selectedChannel.getName().equals(defaultComboBoxModelChannels.getElementAt(i).getChannelName()))
                    channelThreadToDisconnect = defaultComboBoxModelChannels.getElementAt(i);
            }
            if (channelThreadToDisconnect != null) {
                channelThreadToDisconnect.disconnect();
                syncThread.sendAction(
                        ChatOperationProtos.ChatAction.ActionType.LEAVE,
                        nickname,
                        selectedChannel.getName()
                );
                insertText("Disconnected from the channel \"" + selectedChannel + "\".\n");
                defaultComboBoxModelChannels.removeElement(channelThreadToDisconnect);
                history.remove(selectedChannel.getName());
            }
            else
                insertText("Error. You are not connected to the channel \"" + selectedChannel + "\".\n");
        }
        else
            insertText("Error. Select a channel to disconnect first.\n");
    }

    public void exit() {
        for (int i = 0; i < defaultComboBoxModelChannels.getSize(); i++) {
            if (!defaultComboBoxModelChannels.getElementAt(i).isInterrupted())
                defaultComboBoxModelChannels.getElementAt(i).disconnect();
        }
        if (syncThread != null && !syncThread.isInterrupted())
            syncThread.disconnect();
        dispose();
    }

    private void updateTextArea() {
        textAreaChat.setText("\t\tChannel \"" + selectedChannelThread.getChannelName() + "\"\n\n");
        for (String message : history.get(selectedChannelThread.getChannelName()))
            textAreaChat.append(message);
    }

    private void getTextFromArea() {
        String message = textAreaMessage.getText();
        if (message.equals(""))
            return;
        textAreaMessage.setText("");
        if (selectedChannelThread != null)
            selectedChannelThread.sendMessage(message);
    }

    public void insertText(String text, String channelName) {
        history.get(channelName).add(text);
        if (channelName.equals(selectedChannelThread.getChannelName()))
            textAreaChat.append(text);
        else
            insertText("Received message on the channel \"" + channelName + "\".\n");
    }

    public void insertText(String text) {
        if (SwingUtilities.isEventDispatchThread())
            textAreaLog.append(text);
        else {
            SwingUtilities.invokeLater(() -> textAreaLog.append(text));
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
