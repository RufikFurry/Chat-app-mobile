package com.example.myapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity {

    private Socket mSocket;
    private EditText messageEditText;
    private Button sendButton;
    private RecyclerView chatRecyclerView;
    private TextView serverStatusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        serverStatusTextView = findViewById(R.id.serverStatusTextView);

        // Setup RecyclerView
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Set up your RecyclerView Adapter here

        try {
            mSocket = IO.socket("http://localhost:3000");
            mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    runOnUiThread(() -> {
                        serverStatusTextView.setVisibility(View.GONE);
                        sendButton.setEnabled(true);
                    });
                }
            }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    runOnUiThread(() -> {
                        serverStatusTextView.setVisibility(View.VISIBLE);
                        sendButton.setEnabled(false);
                    });
                }
            }).on("message", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    // Handle incoming messages
                }
            });
            mSocket.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        sendButton.setOnClickListener(v -> {
            String message = messageEditText.getText().toString();
            if (!message.isEmpty()) {
                mSocket.emit("message", message);
                messageEditText.setText("");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSocket != null) {
            mSocket.disconnect();
            mSocket.off();
        }
    }
}
