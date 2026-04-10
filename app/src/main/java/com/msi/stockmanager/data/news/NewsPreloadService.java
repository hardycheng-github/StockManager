package com.msi.stockmanager.data.news;

import java.util.ArrayList;
import java.util.List;

/**
 * 啟動階段新聞預載服務：
 * - 避免重複啟動多次網路請求
 * - 可共用同一批預載結果
 */
public class NewsPreloadService {
    private static final Object lock = new Object();
    private static boolean loading = false;
    private static boolean loaded = false;
    private static Exception lastError = null;
    private static final List<INewsApi.TaskCallback> pendingCallbacks = new ArrayList<>();

    public static void preload(INewsApi newsApi, boolean force, INewsApi.TaskCallback callback) {
        if (newsApi == null) {
            if (callback != null) {
                callback.onException(new IllegalStateException("newsApi is null"));
            }
            return;
        }

        synchronized (lock) {
            if (loaded && !force) {
                if (callback != null) {
                    callback.onSuccess();
                }
                return;
            }

            if (callback != null) {
                pendingCallbacks.add(callback);
            }

            if (loading) {
                return;
            }
            loading = true;
            if (force) {
                loaded = false;
                lastError = null;
            }
        }

        newsApi.preload(force, new INewsApi.TaskCallback() {
            @Override
            public void onSuccess() {
                List<INewsApi.TaskCallback> callbacks;
                synchronized (lock) {
                    loading = false;
                    loaded = true;
                    lastError = null;
                    callbacks = new ArrayList<>(pendingCallbacks);
                    pendingCallbacks.clear();
                }
                for (INewsApi.TaskCallback item : callbacks) {
                    item.onSuccess();
                }
            }

            @Override
            public void onException(Exception e) {
                List<INewsApi.TaskCallback> callbacks;
                synchronized (lock) {
                    loading = false;
                    loaded = false;
                    lastError = e;
                    callbacks = new ArrayList<>(pendingCallbacks);
                    pendingCallbacks.clear();
                }
                for (INewsApi.TaskCallback item : callbacks) {
                    item.onException(e);
                }
            }
        });
    }

    public static boolean isLoaded() {
        synchronized (lock) {
            return loaded;
        }
    }

    public static Exception getLastError() {
        synchronized (lock) {
            return lastError;
        }
    }
}
