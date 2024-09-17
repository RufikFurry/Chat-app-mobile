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

import io.socket.emitter.Emitter;
import org.json.JSONException;
import org.json.JSONObject;
import java.net.URISyntaxException;

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
    private RelativeLayout statusPanel;
    private TextView statusText;
    private Button retryButton;
    private Button exitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);
        messageList = findViewById(R.id.message_list);
        statusPanel = findViewById(R.id.status_panel);
        statusText = findViewById(R.id.status_text);
        retryButton = findViewById(R.id.retry_button);
        exitButton = findViewById(R.id.exit_button);
        
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        messageList.setAdapter(adapter);
       
        initializeSocket();
    }

    private void initializeSocket() {
        try {
	   socket = IO.socket("http://localhost:3000");
            socket.on(Socket.EVENT_CONNECT, args -> runOnUiThread(() -> {
                statusPanel.setVisibility(View.GONE);
                sendButton.setEnabled(true);
            }));

            socket.on(Socket.EVENT_DISCONNECT, args -> runOnUiThread(() -> {
                statusPanel.setVisibility(View.VISIBLE);
                sendButton.setEnabled(false);
                statusText.setText("Отсутствует интернет либо сервер выкл.");
            }));

            socket.on(Socket.EVENT_CONNECT_ERROR, args -> runOnUiThread(() -> {
                String errorMsg = "Ошибка подключения: " + args[0];
                statusPanel.setVisibility(View.VISIBLE);
                sendButton.setEnabled(false);
                statusText.setText(errorMsg);
                LogDebug(this, args[0]);
            }));

            socket.on("message", args -> runOnUiThread(() -> {
                String message = (String) args[0];
                adapter.add(message);
            }));

            socket.connect();

            retryButton.setOnClickListener(v -> socket.connect());

            exitButton.setOnClickListener(v -> finish());

        } catch (URISyntaxException e) {
            e.printStackTrace();
            LogDebug(this, e);
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
