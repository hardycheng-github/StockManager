package com.msi.stockmanager.data.notify;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 關注事件訂閱配置（Settings 核選清單）
 */
public class EventSubscriptionConfig {

    public static final String PREF_KEY = "setting_macd_event_subscription";

    public static final String EVENT_GOLDEN = "GOLDEN";
    public static final String EVENT_DEATH = "DEATH";
    public static final String EVENT_EMA_BREAKTHROUGH = "EMA_BREAKTHROUGH";
    public static final String EVENT_EMA_BREAKDOWN = "EMA_BREAKDOWN";
    public static final String EVENT_BOLL_BREAKTHROUGH = "BOLL_BREAKTHROUGH";
    public static final String EVENT_BOLL_BREAKDOWN = "BOLL_BREAKDOWN";

    private static final List<String> ALL_EVENT_KEYS = Arrays.asList(
            EVENT_GOLDEN,
            EVENT_DEATH,
            EVENT_EMA_BREAKTHROUGH,
            EVENT_EMA_BREAKDOWN,
            EVENT_BOLL_BREAKTHROUGH,
            EVENT_BOLL_BREAKDOWN
    );

    private static final List<String> NEW_EVENT_KEYS = Arrays.asList(
            EVENT_EMA_BREAKTHROUGH,
            EVENT_EMA_BREAKDOWN,
            EVENT_BOLL_BREAKTHROUGH,
            EVENT_BOLL_BREAKDOWN
    );

    public static Set<String> defaultSubscribedEvents() {
        return new HashSet<>(ALL_EVENT_KEYS);
    }

    public static List<String> orderedEventKeys() {
        return ALL_EVENT_KEYS;
    }

    public static boolean isSubscribed(Set<String> subscribed, String eventKey) {
        if (subscribed == null || subscribed.isEmpty()) {
            return false;
        }
        return subscribed.contains(eventKey);
    }

    /**
     * 升級時補齊 EMA／布林等新事件 key（僅在尚未包含任一 new key 時執行一次）
     */
    public static Set<String> mergeWithDefaults(Set<String> saved) {
        if (saved == null) {
            return defaultSubscribedEvents();
        }
        Set<String> merged = new HashSet<>(saved);
        boolean hasAnyNewKey = false;
        for (String key : NEW_EVENT_KEYS) {
            if (saved.contains(key)) {
                hasAnyNewKey = true;
                break;
            }
        }
        if (!hasAnyNewKey) {
            merged.addAll(NEW_EVENT_KEYS);
        }
        return merged;
    }
}
