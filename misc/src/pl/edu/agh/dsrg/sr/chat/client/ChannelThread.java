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

/**
 * Created by julia on 27.03.2017.
 */
public class ChannelThread extends Thread {
    private JChannel channel = null;

    private ChatFrame chatFrame;
    private String channelName;

    public ChannelThread(ChatFrame chatFrame, String channelName) {
        this.chatFrame = chatFrame;
        this.channelName = channelName;
    }

    @Override
    public void run() {
        System.setProperty("java.net.preferIPv4Stack", "true");
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
                    ChatOperationProtos.ChatMessage chatMessage = null;
                    try {
                        chatMessage = ChatOperationProtos.ChatMessage.parseFrom(msg.getBuffer());
                        chatFrame.insertText(chatMessage.getMessage(), "MESSAGE");
                    } catch (InvalidProtocolBufferException e) {
                    }
                }
            });
            channel.connect(channelName);
            join();
        } catch (InterruptedException e) {
        } catch (Exception e) {
            chatFrame.insertText("Error while connecting the channel \"" + channelName + "\": " + e + "\n", "LOG");
        }
    }

    public void sendMessage(String messageString) {
        ChatOperationProtos.ChatMessage chatMessage;
        chatMessage = ChatOperationProtos.ChatMessage.newBuilder()
                .setMessage("[" + channelName + "] " + chatFrame.getNickname() + ": " + messageString + "\n")
                .build();
        Message message = new Message(null, null, chatMessage.toByteArray());
        try {
            channel.send(message);
        } catch (Exception e) {
            chatFrame.insertText("Error while sending the message \""
                    + messageString + "\""
                    + "to channel \""
                    + channelName + "\": "
                    + e + "\n", "LOG");
        }
    }

    public String getChannelName() {
        return channelName;
    }


    public void disconnect() {
        if (channel != null)
            channel.close();
        interrupt();
    }
}
