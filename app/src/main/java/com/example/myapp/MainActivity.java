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

public class MainActivity extends AppCompatActivity {

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
    }

    private void initializeSocket() {
        try {
            socket = IO.socket("http://localhost:3000");

            socket.on(Socket.EVENT_CONNECT, args -> runOnUiThread(() -> {
                errorLayout.setVisibility(View.GONE);
                sendButton.setEnabled(true);
            }));

            socket.on(Socket.EVENT_DISCONNECT, args -> runOnUiThread(() -> {
                errorLayout.setVisibility(View.VISIBLE);
                sendButton.setEnabled(false);
                errorMessage.setText("Отсутствует интернет либо сервер выкл код ошибки: Disconnected");
            }));

            socket.on(Socket.EVENT_CONNECT_ERROR, args -> runOnUiThread(() -> {
                String errorMsg = "Connect error: " + args[0];
                errorLayout.setVisibility(View.VISIBLE);
                sendButton.setEnabled(false);
                errorMessage.setText("Отсутствует интернет либо сервер выкл код ошибки: " + errorMsg);
            }));

            socket.on(Socket.EVENT_CONNECT_TIMEOUT, args -> runOnUiThread(() -> {
                errorLayout.setVisibility(View.VISIBLE);
                sendButton.setEnabled(false);
                errorMessage.setText("Отсутствует интернет либо сервер выкл код ошибки: Connection timeout");
            }));

            socket.on("message", args -> runOnUiThread(() -> {
                String message = (String) args[0];
                adapter.add(message);
            }));

            socket.connect();

            reconnectButton.setOnClickListener(v -> {
                initializeSocket();
            });

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        sendButton.setOnClickListener(v -> {
            String message = messageInput.getText().toString();
            if (!message.isEmpty()) {
                socket.emit("message", message);
                messageInput.setText("");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (socket != null) {
            socket.disconnect();
        }
    }
}
