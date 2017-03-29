package pl.edu.agh.dsrg.sr.chat.client;

import com.google.protobuf.InvalidProtocolBufferException;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.protocols.*;
import org.jgroups.protocols.pbcast.*;
import org.jgroups.stack.ProtocolStack;
import pl.edu.agh.dsrg.sr.chat.UI.ChatFrame;
import pl.edu.agh.dsrg.sr.chat.protos.ChatOperationProtos;

import java.net.InetAddress;

import static pl.edu.agh.dsrg.sr.chat.Chat.nickname;

public class MessageChannelThread extends ChannelThread {
    private ActionChannelThread actionChannelThread;

    public MessageChannelThread(ChatFrame chatFrame, ActionChannelThread actionChannelThread, String channelName) {
        this.chatFrame = chatFrame;
        this.actionChannelThread = actionChannelThread;
        this.channelName = channelName;
    }

    @Override
    public void run() {
        try {
            channel = new JChannel(false);
            ProtocolStack stack = new ProtocolStack();
            channel.setProtocolStack(stack);
            stack.addProtocol(new UDP().setValue("mcast_group_addr", InetAddress.getByName(channelName)))
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
                    ChatOperationProtos.ChatMessage chatMessage;
                    try {
                        chatMessage = ChatOperationProtos.ChatMessage.parseFrom(msg.getBuffer());
                        chatFrame.insertText(chatMessage.getMessage(), channelName);
                    } catch (InvalidProtocolBufferException ignored) {
                    }
                }
            });
            channel.connect(channelName);
        } catch (InterruptedException e) {
            if (channel != null)
                channel.close();
            actionChannelThread.sendAction(ChatOperationProtos.ChatAction.ActionType.LEAVE, nickname, channelName);
        } catch (Exception e) {
            chatFrame.insertText(
                    "Error while connecting the channel \"" + channelName + "\": " + e + ".\n");
        }
    }

    public void sendMessage(String messageString) {
        ChatOperationProtos.ChatMessage chatMessage;
        chatMessage = ChatOperationProtos.ChatMessage.newBuilder()
                .setMessage(nickname + ": " + messageString + "\n")
                .build();
        Message message = new Message(null, null, chatMessage.toByteArray());
        try {
            channel.send(message);
        } catch (Exception e) {
            chatFrame.insertText("Error while sending the message \""
                    + messageString + "\""
                    + "to channel \""
                    + channelName + "\": "
                    + e + ".\n"
            );
        }
    }

    @Override
    public void disconnect() {
        actionChannelThread.sendAction(ChatOperationProtos.ChatAction.ActionType.LEAVE, nickname, channelName);
        super.disconnect();
    }
}
