package pl.edu.agh.dsrg.sr.chat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by julia on 28.03.2017.
 */
public class Channel {
    private String name;
    private List<String> nicknames;

    public Channel(String name) {
        this.name = name;
        nicknames = new ArrayList<>();
    }

    public void addNickname(String nickname) {
        this.nicknames.add(nickname);
    }

    public void removeNickname(String nickname) {
        this.nicknames.remove(nickname);
    }

    public String getName() {
        return name;
    }

    public List<String> getNicknames() {
        return nicknames;
    }

    @Override
    public String toString() {
        return name;
    }
}
