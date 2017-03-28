package pl.edu.agh.dsrg.sr.chat.UI;

import pl.edu.agh.dsrg.sr.chat.Channel;
import pl.edu.agh.dsrg.sr.chat.Chat;
import pl.edu.agh.dsrg.sr.chat.UI.listener.ChatActionListener;
import pl.edu.agh.dsrg.sr.chat.UI.listener.ChatListSelectionListener;
import pl.edu.agh.dsrg.sr.chat.channelClient.ChannelThread;
import pl.edu.agh.dsrg.sr.chat.channelClient.SyncThread;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import static pl.edu.agh.dsrg.sr.chat.Chat.nickname;

public class ChatFrame extends JFrame {
    private final Chat chat;

    private Channel selectedChannel = null;
    private ChannelThread selectedChannelThread = null;

    private JLabel labelNickname;

    private JTextArea textAreaChat;
    private JTextArea textAreaLog;
    private JTextArea textAreaMessage;

    public ChatFrame(Chat chat) {
        super("Chat");
        this.chat = chat;
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                chat.exit();
            }
        });
        initLayout();
        setVisible(true);
        ChatDialog enterNicknameDialog = new ChatDialog(this, "Enter your nickname", "Nickname");
        if (enterNicknameDialog.isOkClicked()) {
            nickname = enterNicknameDialog.getField();
            labelNickname.setText("Your nickname: " + nickname);
            chat.setSyncThread(new SyncThread(chat, this));
            chat.getSyncThread().start();
        }
        else
            chat.exit();
    }

    private void initLayout() {
        setSize(800, 700);
        setResizable(false);
        setLayout(null);

        chat.setDefaultComboBoxModelChannels(new DefaultComboBoxModel<>());
        JComboBox<ChannelThread> comboBoxChannelThread = new JComboBox<>(chat.getDefaultComboBoxModelChannels());
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

        chat.setDefaultListModelChannels(new DefaultListModel<>());
        JList<Channel> listChannels = new JList<>(chat.getDefaultListModelChannels());
        listChannels.setFont(listChannels.getFont().deriveFont(Font.PLAIN));
        ListSelectionModel listSelectionModel = listChannels.getSelectionModel();
        listSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listSelectionModel.addListSelectionListener(new ChatListSelectionListener(chat, this));
        JScrollPane scrollPaneTopRight = new JScrollPane(listChannels);
        scrollPaneTopRight.setBounds(500, 45, 285, 155);
        add(scrollPaneTopRight);

        JLabel labelUsers = new JLabel("Users");
        labelUsers.setBounds(500, 205, 285, 20);
        add(labelUsers);

        chat.setDefaultListModelUsers(new DefaultListModel<>());
        JList<String> listUsers = new JList<>(chat.getDefaultListModelUsers());
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

        ChatActionListener chatActionListener = new ChatActionListener(chat);
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

    private void updateTextArea() {
        textAreaChat.setText("\t\tChannel \"" + selectedChannelThread.getChannelName() + "\"\n\n");
        for (String message : chat.getHistory().get(selectedChannelThread.getChannelName()))
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
        chat.getHistory().get(channelName).add(text);
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

    public void setSelectedChannel(Channel selectedChannel) {
        this.selectedChannel = selectedChannel;
    }

    public Channel getSelectedChannel() {
        return selectedChannel;
    }
}
