package pl.edu.agh.dsrg.sr.chat.channelClient;

import com.google.protobuf.InvalidProtocolBufferException;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.protocols.*;
import org.jgroups.protocols.pbcast.*;
import org.jgroups.stack.ProtocolStack;
import org.jgroups.util.Util;
import pl.edu.agh.dsrg.sr.chat.Channel;
import pl.edu.agh.dsrg.sr.chat.Chat;
import pl.edu.agh.dsrg.sr.chat.UI.ChatFrame;
import pl.edu.agh.dsrg.sr.chat.protos.ChatOperationProtos;

import javax.swing.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class SyncThread extends Thread {
    private ChatFrame chatFrame;
    private JChannel channel;

    public SyncThread(ChatFrame chatFrame) {
        this.chatFrame = chatFrame;
    }

    @Override
    public void run() {
        System.setProperty("java.net.preferIPv4Stack", "true");
        try {
            channel = new JChannel(false);
            ProtocolStack stack = new ProtocolStack();
            channel.setProtocolStack(stack);
            stack.addProtocol(new UDP())
                    .addProtocol(new PING())
                    .addProtocol(new MERGE3())
                    .addProtocol(new FD_SOCK())
                    .addProtocol(new FD_ALL().setValue("timeout", 12000).setValue("interval", 3000))
                    .addProtocol(new VERIFY_SUSPECT())
                    .addProtocol(new BARRIER())
                    .addProtocol(new NAKACK2())
                    .addProtocol(new UNICAST3())
                    .addProtocol(new STABLE())
                    .addProtocol(new GMS())
                    .addProtocol(new UFC())
                    .addProtocol(new MFC())
                    .addProtocol(new FRAG2())
                    .addProtocol(new STATE_TRANSFER())
                    .addProtocol(new FLUSH());
            stack.init();

            channel.setReceiver(new ReceiverAdapter() {
                @Override
                public void receive(Message msg) {
                    try {
                        ChatOperationProtos.ChatAction chatAction =
                                ChatOperationProtos.ChatAction.parseFrom(msg.getBuffer());
                        ChatOperationProtos.ChatAction.ActionType actionType = chatAction.getAction();
                        String nickname = chatAction.getNickname();
                        String channelName = chatAction.getChannel();
                        if (actionType == ChatOperationProtos.ChatAction.ActionType.JOIN)
                            chatFrame.addChannel(channelName, nickname);
                        else if (actionType == ChatOperationProtos.ChatAction.ActionType.LEAVE)
                            chatFrame.removeNicknameFromChannelList(channelName, nickname);
                    } catch (InvalidProtocolBufferException ignored) {
                    }
                }

                @Override
                public void getState(OutputStream output) throws Exception {
                    synchronized (chatFrame.getDefaultListModelChannels()) {
                        ChatOperationProtos.ChatState chatState;
                        List<ChatOperationProtos.ChatAction> chatActionsList = new ArrayList<>();
                        DefaultListModel<Channel> defaultListModelChannels = chatFrame.getDefaultListModelChannels();
                        for (int i = 0; i < defaultListModelChannels.size(); i++) {
                            String channelName = defaultListModelChannels.elementAt(i).getName();
                            for (String nickname : defaultListModelChannels.elementAt(i).getNicknames()) {
                                ChatOperationProtos.ChatAction chatAction = ChatOperationProtos.ChatAction.newBuilder()
                                        .setAction(ChatOperationProtos.ChatAction.ActionType.JOIN)
                                        .setNickname(nickname)
                                        .setChannel(channelName)
                                        .build();
                                chatActionsList.add(chatAction);
                            }
                        }

                        ChatOperationProtos.ChatState.Builder builder = ChatOperationProtos.ChatState.newBuilder();
                        for (ChatOperationProtos.ChatAction chatAction : chatActionsList) {
                            builder.addState(chatAction);
                        }
                        chatState = builder.build();
                        Util.objectToStream(chatState, new DataOutputStream(output));
                    }
                }

                @Override
                public void setState(InputStream input) throws Exception {
                    ChatOperationProtos.ChatState chatState =
                            (ChatOperationProtos.ChatState) Util.objectFromStream(new DataInputStream(input));

                    List<ChatOperationProtos.ChatAction> chatActionList = chatState.getStateList();
                    for (ChatOperationProtos.ChatAction chatAction : chatActionList)
                        chatFrame.addChannel(chatAction.getChannel(), chatAction.getNickname());
                }
            });
            channel.connect(Chat.managementChannel);
            channel.getState(null, 1000);
            join();
            channel.close();
        } catch (InterruptedException e) {
            if (channel != null && channel.isOpen())
                channel.close();
        } catch (Exception e) {
            chatFrame.insertText("Error while connecting the management channel: " + e + "\n", "LOG");
        }
    }

    public void sendAction(ChatOperationProtos.ChatAction.ActionType actionType, String nickname, String channelName) {
        ChatOperationProtos.ChatAction chatAction;
        chatAction = ChatOperationProtos.ChatAction.newBuilder()
                .setAction(actionType)
                .setNickname(nickname)
                .setChannel(channelName)
                .build();
        Message message = new Message(null, null, chatAction.toByteArray());
        try {
            channel.send(message);
        } catch (Exception e) {
            chatFrame.insertText("Error while sending a message to the management channel: " + e + "\n", "LOG");
        }
    }

    public void disconnect() {
        if (channel != null)
            channel.close();
        interrupt();
    }
}
