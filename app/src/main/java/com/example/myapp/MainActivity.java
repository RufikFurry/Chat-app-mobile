package com.example.myapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import androidx.appcompat.app.AppCompatActivity;
import java.net.URISyntaxException;
import java.util.ArrayList;
import io.socket.client.IO;
import io.socket.client.Socket;

import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity {

    private Socket socket;
    private EditText messageInput;
    private Button sendButton;
    private ListView messageList;
    private ArrayAdapter<String> adapter;
    private RelativeLayout errorLayout;
    private Button reconnectButton;
    private Button exitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);
        messageList = findViewById(R.id.message_list);
        errorLayout = findViewById(R.id.error_layout);
        reconnectButton = findViewById(R.id.reconnect_button);
        exitButton = findViewById(R.id.exit_button);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        messageList.setAdapter(adapter);

        try {
            socket = IO.socket("http://localhost:3000");
            socket.on(Socket.EVENT_CONNECT, args -> runOnUiThread(() -> {
                errorLayout.setVisibility(View.GONE);
                sendButton.setEnabled(true);
            })).on(Socket.EVENT_DISCONNECT, args -> runOnUiThread(() -> {
                errorLayout.setVisibility(View.VISIBLE);
                sendButton.setEnabled(false);
            })).on("message", args -> runOnUiThread(() -> {
                String message = (String) args[0];
                adapter.add(message);
            }));

            socket.connect();
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

        reconnectButton.setOnClickListener(v -> {
            socket.connect();
        });

        exitButton.setOnClickListener(v -> {
            finish();
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
