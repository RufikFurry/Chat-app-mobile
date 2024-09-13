package com.example.myapp;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Создание WebView для загрузки чата
        WebView webView = new WebView(this);
        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);

        // Замените "http://localhost:3000" на ваш IP-адрес, если сервер запущен на другом устройстве
        webView.loadUrl("http://localhost:3000");

        setContentView(webView);
    }
}
