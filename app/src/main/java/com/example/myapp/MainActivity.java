package com.example.myapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.net.URISyntaxException;
import java.util.ArrayList;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity {

    private Socket socket;
    private EditText urlInput;
    private EditText messageInput;
    private Button connectButton;
    private Button sendButton;
    private TextView statusMessage;
    private ListView messageList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        urlInput = findViewById(R.id.url_input);
        messageInput = findViewById(R.id.message_input);
        connectButton = findViewById(R.id.connect_button);
        sendButton = findViewById(R.id.send_button);
        statusMessage = findViewById(R.id.status_message);
        messageList = findViewById(R.id.message_list);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        messageList.setAdapter(adapter);

        // Обработчик для кнопки "Подключиться"
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = urlInput.getText().toString();
                if (!url.isEmpty()) {
                    initializeSocket(url);
                } else {
                    Toast.makeText(MainActivity.this, "Введите URL", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Обработчик для кнопки "Отправить"
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = messageInput.getText().toString();
                if (!message.isEmpty()) {
                    socket.emit("message", message);
                    messageInput.setText("");
                }
            }
        });
    }

    private void initializeSocket(String url) {
        try {
            socket = IO.socket(url);

            socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    runOnUiThread(() -> {
                        statusMessage.setText("Подключено к серверу");
                        sendButton.setEnabled(true);
                        messageInput.setEnabled(true);
                    });
                }
            });

            socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    runOnUiThread(() -> {
                        statusMessage.setText("Отключено от сервера");
                        sendButton.setEnabled(false);
                        messageInput.setEnabled(false);
                    });
                }
            });

            socket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    runOnUiThread(() -> {
                        statusMessage.setText("Ошибка подключения: " + args[0]);
                    });
                }
            });

            socket.on("message", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    runOnUiThread(() -> {
                        String message = (String) args[0];
                        adapter.add(message);
                    });
                }
            });

            socket.connect();

        } catch (URISyntaxException e) {
            e.printStackTrace();
            statusMessage.setText("Неверный URL");
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
