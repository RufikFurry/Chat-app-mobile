package com.example.myapp;

import android.content.Context;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LogDebug {

    private static final String TAG = "LogDebug";

    public static void logError(Context context, Exception e) {
        // Получаем путь к папке для логов
        File logDir = new File(context.getExternalFilesDir(null), "Storage/logs");
        if (!logDir.exists()) {
            if (!logDir.mkdirs()) {
                Log.e(TAG, "Не удалось создать папку для логов");
                return;
            }
        }

        // Создаем имя файла с текущей датой и временем
        String fileName = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(new Date()) + ".log";
        File logFile = new File(logDir, fileName);

        // Записываем подробную информацию об ошибке в файл
        try (FileOutputStream fos = new FileOutputStream(logFile, true)) {
            StringBuilder logBuilder = new StringBuilder();
            logBuilder.append("Дата и время: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date())).append("\n");
            logBuilder.append("Ошибка: ").append(e.getMessage()).append("\n");
            logBuilder.append("Стек вызовов: ").append(Log.getStackTraceString(e)).append("\n");
            fos.write(logBuilder.toString().getBytes());
            Log.i(TAG, "Лог записан в файл: " + logFile.getAbsolutePath());
        } catch (IOException ioException) {
            Log.e(TAG, "Ошибка при записи лога", ioException);
        }
    }
}
