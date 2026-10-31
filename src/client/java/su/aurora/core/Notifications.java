package su.aurora.core;

import su.aurora.Aurora;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Notifications {
    public static class Entry {
        public final String title;
        public final String text;
        public final int accent;
        public final long created;
        public final long duration;

        public Entry(String title, String text, int accent, long duration) {
            this.title = title;
            this.text = text;
            this.accent = accent;
            this.created = System.currentTimeMillis();
            this.duration = duration;
        }

        public float progress() {
            return 1.0f - Math.min(1.0f, (System.currentTimeMillis() - created) / (float) duration);
        }

        public boolean expired() {
            return System.currentTimeMillis() - created > duration;
        }
    }

    private final List<Entry> entries = new ArrayList<>();

    public void push(String title, String text, int accent) {
        push(title, text, accent, 2600L);
    }

    public void push(String title, String text, int accent, long duration) {
        entries.add(new Entry(title, text, accent, duration));
        while (entries.size() > 6) {
            entries.remove(0);
        }
    }

    public void tick() {
        Iterator<Entry> iterator = entries.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().expired()) {
                iterator.remove();
            }
        }
    }

    public Entry latest() {
        return entries.isEmpty() ? null : entries.get(entries.size() - 1);
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public void clear() {
        entries.clear();
    }
}
