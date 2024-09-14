package com.example.myapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.net.URISyntaxException;
import java.util.ArrayList;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Socket socket;
    private EditText messageInput;
    private Button sendButton;
    private ListView messageList;
    private ArrayAdapter<String> adapter;
    private RelativeLayout errorLayout;
    private TextView errorMessage;
    private Button reconnectButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);
        messageList = findViewById(R.id.message_list);
        errorLayout = findViewById(R.id.error_layout);
        errorMessage = findViewById(R.id.error_message);
        reconnectButton = findViewById(R.id.reconnect_button);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        messageList.setAdapter(adapter);

        initializeSocket();

        // Обработка нажатия кнопки "Переподключить"
        reconnectButton.setOnClickListener(v -> {
            initializeSocket();  // Переподключение
        });

        // Обработка нажатия кнопки "Отправить"
        sendButton.setOnClickListener(v -> {
            String message = messageInput.getText().toString();
            if (!message.isEmpty()) {
                socket.emit("message", message);
                messageInput.setText("");
            }
        });
    }

    private void initializeSocket() {
        try {
            socket = IO.socket("http://your-server-ip-address:3000");  // Замените на реальный IP-адрес сервера

            // Подключение успешно
            socket.on(Socket.EVENT_CONNECT, args -> runOnUiThread(() -> {
                errorLayout.setVisibility(View.GONE);
                sendButton.setEnabled(true);
                Log.i(TAG, "Подключено к серверу");
            }));

            // Отключение от сервера
            socket.on(Socket.EVENT_DISCONNECT, args -> runOnUiThread(() -> {
                errorLayout.setVisibility(View.VISIBLE);
                sendButton.setEnabled(false);
                errorMessage.setText("Сервер отключен.");
                Log.i(TAG, "Отключено от сервера");
            }));

            // Ошибка подключения
            socket.on(Socket.EVENT_CONNECT_ERROR, args -> runOnUiThread(() -> {
                errorLayout.setVisibility(View.VISIBLE);
                sendButton.setEnabled(false);
                errorMessage.setText("Ошибка подключения: " + args[0]);
                Log.e(TAG, "Ошибка подключения: " + args[0]);
            }));

            // Таймаут подключения
            socket.on(Socket.EVENT_CONNECT_TIMEOUT, args -> runOnUiThread(() -> {
                errorLayout.setVisibility(View.VISIBLE);
                sendButton.setEnabled(false);
                errorMessage.setText("Время подключения истекло.");
                Log.e(TAG, "Время подключения истекло");
            }));

            // Получение сообщений с сервера
            socket.on("message", args -> runOnUiThread(() -> {
                String message = (String) args[0];
                adapter.add(message);
            }));

            socket.connect();

        } catch (URISyntaxException e) {
            Log.e(TAG, "Ошибка URI: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (socket != null) {
            socket.disconnect();
        }
    }
}
