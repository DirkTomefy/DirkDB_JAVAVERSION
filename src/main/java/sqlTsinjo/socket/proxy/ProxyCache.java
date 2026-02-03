package sqlTsinjo.socket.proxy;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class ProxyCache {

    private static final class Entry {
        final String payload;
        final long expireAtMillis;

        Entry(String payload, long expireAtMillis) {
            this.payload = payload;
            this.expireAtMillis = expireAtMillis;
        }

        boolean isExpired(long now) {
            return now >= expireAtMillis;
        }
    }

    private final long ttlMillis;
    private final int maxEntries;

    private final LinkedHashMap<String, Entry> lru = new LinkedHashMap<>(16, 0.75f, true);

    public ProxyCache(long ttlMillis, int maxEntries) {
        this.ttlMillis = ttlMillis;
        this.maxEntries = maxEntries;
    }

    public synchronized String getIfPresent(String key) {
        long now = System.currentTimeMillis();
        Entry e = lru.get(key);
        if (e == null) {
            return null;
        }
        if (e.isExpired(now)) {
            lru.remove(key);
            return null;
        }
        return e.payload;
    }

    public synchronized void put(String key, String payload) {
        long expireAt = System.currentTimeMillis() + ttlMillis;
        lru.put(key, new Entry(payload, expireAt));
        evictIfNeeded();
    }

    public synchronized void clearAll() {
        lru.clear();
    }

    public synchronized void clearByDbPrefix(String dbName) {
        if (dbName == null) {
            return;
        }
        String prefix = dbName + "|";
        Iterator<Map.Entry<String, Entry>> it = lru.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Entry> e = it.next();
            if (e.getKey().startsWith(prefix)) {
                it.remove();
            }
        }
    }

    private void evictIfNeeded() {
        while (lru.size() > maxEntries) {
            Iterator<Map.Entry<String, Entry>> it = lru.entrySet().iterator();
            if (!it.hasNext()) {
                return;
            }
            it.next();
            it.remove();
        }
    }
}
