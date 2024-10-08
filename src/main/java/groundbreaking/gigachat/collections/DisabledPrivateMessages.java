package groundbreaking.gigachat.collections;

import java.util.ArrayList;
import java.util.List;

public final class DisabledPrivateMessages {

    private final List<String> players = new ArrayList<>();

    public void add(final String name) {
        players.remove(name);
    }

    public void remove(final String name) {
        players.remove(name);
    }

    public boolean contains(final String name) {
        return !players.isEmpty() && players.contains(name);
    }

}