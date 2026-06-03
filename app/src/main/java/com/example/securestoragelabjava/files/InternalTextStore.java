package com.example.securestoragelabjava.files;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

public final class InternalTextStore {

    private InternalTextStore() {}

    public static void writeUtf8(Context context, String fileName, String content) throws Exception {
        try (FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE)) {
            fos.write(content.getBytes(StandardCharsets.UTF_8));
        }
    }

    public static String readUtf8(Context context, String fileName) throws Exception {
        try (FileInputStream fis = context.openFileInput(fileName)) {
            byte[] bytes = fis.readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }

    public static boolean delete(Context context, String fileName) {
        return context.deleteFile(fileName);
    }
}