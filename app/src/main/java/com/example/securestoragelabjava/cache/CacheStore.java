package com.example.securestoragelabjava.cache;

import android.content.Context;

import java.io.File;
import java.nio.charset.StandardCharsets;

public final class CacheStore {

    private CacheStore() {}

    public static void write(Context context, String fileName, String content) throws Exception {
        File file = new File(context.getCacheDir(), fileName);
        java.nio.file.Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
    }

    public static String read(Context context, String fileName) throws Exception {
        File file = new File(context.getCacheDir(), fileName);
        if (!file.exists()) return null;
        return java.nio.file.Files.readString(file.toPath(), StandardCharsets.UTF_8);
    }

    public static int purge(Context context) {
        File[] files = context.getCacheDir().listFiles();
        if (files == null) return 0;
        int deleted = 0;
        for (File f : files) {
            if (f.delete()) deleted++;
        }
        return deleted;
    }
}