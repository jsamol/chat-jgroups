package pl.edu.agh.dsrg.sr.chat.UI;

import pl.edu.agh.dsrg.sr.chat.Channel;
import pl.edu.agh.dsrg.sr.chat.client.ChannelThread;
import pl.edu.agh.dsrg.sr.chat.client.SyncThread;
import pl.edu.agh.dsrg.sr.chat.protos.ChatOperationProtos;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by julia on 26.03.2017.
 */
public class ChatFrame extends JFrame implements ActionListener, ListSelectionListener {
    private List<ChannelThread> connectedChannels;

    private Channel selectedChannel = null;

    private SyncThread syncThread = null;

    private DefaultListModel<Channel> defaultListModelChannels;
    private JList<Channel> listChannels;

    private DefaultListModel<String> defaultListModelUsers;
    private JList<String> listUsers;

    private JTextArea textAreaChat;
    private JTextArea textAreaLog;
    private JTextArea textAreaMessage;

    private JButton buttonCreateNew;
    private JButton buttonConnect;
    private JButton buttonDisconnect;
    private JButton buttonExit;

    private String nickname;

    public ChatFrame() {
        super("Chat");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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

    public void initLayout() {
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
        listChannels = new JList<>(defaultListModelChannels);
        listChannels.setFont(listChannels.getFont().deriveFont(Font.PLAIN));
        ListSelectionModel listSelectionModel = listChannels.getSelectionModel();
        listSelectionModel.addListSelectionListener(this);
        JScrollPane scrollPaneTopRight = new JScrollPane(listChannels);
        scrollPaneTopRight.setBounds(500, 45, 285, 155);
        add(scrollPaneTopRight);

        JLabel labelUsers = new JLabel("Users");
        labelUsers.setBounds(500, 205, 285, 20);
        add(labelUsers);

        defaultListModelUsers = new DefaultListModel<>();
        listUsers = new JList<>(defaultListModelUsers);
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
        textAreaMessage.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                ChatFrame.this.keyPressed(e.getKeyCode());
            }
        });
        textAreaMessage.setLineWrap(true);
        textAreaMessage.setWrapStyleWord(true);
        JScrollPane scrollPaneMessage = new JScrollPane(textAreaMessage);
        scrollPaneMessage.setBounds(10, 510, 775, 90);
        add(scrollPaneMessage);

        JPanel panelBottom = new JPanel();
        panelBottom.setLayout(new GridLayout(1, 4));
        panelBottom.setBounds(10, 615, 775, 35);
        add(panelBottom);

        buttonCreateNew = new JButton("Create new channel");
        buttonCreateNew.setActionCommand("Create");
        buttonCreateNew.addActionListener(this);
        panelBottom.add(buttonCreateNew);

        buttonConnect = new JButton("Connect");
        buttonConnect.addActionListener(this);
        panelBottom.add(buttonConnect);

        buttonDisconnect = new JButton("Disconnect");
        buttonDisconnect.addActionListener(this);
        panelBottom.add(buttonDisconnect);

        buttonExit = new JButton("Exit");
        buttonExit.addActionListener(this);
        panelBottom.add(buttonExit);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (command.equals("Exit"))
            exit();
        else if (command.equals("Create"))
            createNewChannel();
        else if (command.equals("Connect"))
            connect();
        else if (command.equals("Disconnect"))
            disconnect();
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        defaultListModelUsers.removeAllElements();
        selectedChannel = null;
        ListSelectionModel listSelectionModel = (ListSelectionModel) e.getSource();
        if (!e.getValueIsAdjusting() && !listSelectionModel.isSelectionEmpty()) {
            int minIndex = listSelectionModel.getMinSelectionIndex();
            int maxIndex = listSelectionModel.getMaxSelectionIndex();
            for (int i = minIndex; i <= maxIndex; i++) {
                if (listSelectionModel.isSelectedIndex(i)) {
                    selectedChannel = defaultListModelChannels.elementAt(i);
                    for (String nickname : defaultListModelChannels.elementAt(i).getNicknames())
                        defaultListModelUsers.addElement(nickname);
                }
            }
        }
    }

    public void createNewChannel() {
        ChatDialog createNewDialog = new ChatDialog(this, "Create new channel", "Channel name");
        if (createNewDialog.isOkClicked()) {
            String channelName = createNewDialog.getField();
            ChannelThread channelThread = new ChannelThread(this, channelName);
            connectedChannels.add(channelThread);
            channelThread.start();
            insertText("New channel \"" + channelName + "\" created.\n", "LOG");
            syncThread.sendAction(ChatOperationProtos.ChatAction.ActionType.JOIN, nickname, channelName);
        }
        else
            return;
    }

    public void connect() {
        if (selectedChannel != null) {
            for (ChannelThread channelThread : connectedChannels) {
                if (selectedChannel.getName().equals(channelThread.getChannelName()))
                    insertText("Error. You are already connected to the channel \""
                            + selectedChannel + "\"\n", "LOG");
            }
            ChannelThread channelThread = new ChannelThread(this, selectedChannel.getName());
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
            channelThread.disconnect();
        if (syncThread != null)
            syncThread.disconnect();
        dispose();
    }

    public void keyPressed(int keyCode) {
        if (keyCode != KeyEvent.VK_ENTER)
            return;
        String message = textAreaMessage.getText();
        if (message.equals(""))
            return;
        textAreaMessage.setText("");
        for (ChannelThread channelThread : connectedChannels)
            channelThread.sendMessage(message);
    }

    public void insertText(String text, String type) {
        JTextArea textArea = null;
        if (type.equals("MESSAGE"))
            textArea = textAreaChat;
        else if (type.equals("LOG"))
            textArea = textAreaLog;
        if (SwingUtilities.isEventDispatchThread())
                textArea.append(text);
        else {
            JTextArea finalTextArea = textArea;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    finalTextArea.append(text);
                }
            });
        }
    }

    public String getNickname() {
        return nickname;
    }

    public synchronized void addChannel(String channelName, String nickname) {
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

    public synchronized void removeNicknameFromChannelList(String channelName, String nickname) {
        for (int i = 0; i < defaultListModelChannels.size(); i++) {
            if (defaultListModelChannels.elementAt(i).getName().equals(channelName)) {
                defaultListModelChannels.elementAt(i).removeNickname(nickname);
                if (defaultListModelChannels.elementAt(i).getNicknames().size() == 0)
                    defaultListModelChannels.remove(i);
                if (defaultListModelChannels.elementAt(i) == selectedChannel)
                    defaultListModelUsers.removeElement(nickname);
                return;
            }
        }
    }

    public DefaultListModel<Channel> getDefaultListModelChannels() {
        return defaultListModelChannels;
    }
}
