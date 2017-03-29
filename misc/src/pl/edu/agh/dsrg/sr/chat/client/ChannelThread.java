package pl.edu.agh.dsrg.sr.chat.client;

import org.jgroups.JChannel;
import pl.edu.agh.dsrg.sr.chat.UI.ChatFrame;

public class ChannelThread extends Thread {
    JChannel channel = null;

    ChatFrame chatFrame;
    String channelName;

    public void disconnect() {
        if (channel != null)
            channel.close();
        if (isAlive())
            interrupt();
    }

    public String getChannelName() {
        return channelName;
    }

    @Override
    public String toString() {
        return channelName;
    }
}
