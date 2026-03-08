package com.example.demo.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.FirebaseDatabase;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.service-account.path}")
    private Resource serviceAccount;

    @Value("${firebase.database.url}")
    private String firebaseDatabaseUrl;

    @PostConstruct
    public void init() throws Exception {
        if (!serviceAccount.exists()) {
            throw new IllegalStateException(
                    "Firebase service account JSON not found at: " + serviceAccount.getFilename());
        }

        if (FirebaseApp.getApps().isEmpty()) {
            try (InputStream serviceAccountStream = serviceAccount.getInputStream()) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
                        .setDatabaseUrl(firebaseDatabaseUrl)
                        .build();
                FirebaseApp.initializeApp(options);
                System.out.println("🔥 Firebase Admin initialized successfully!");
            }
        }

        FirebaseDatabase.getInstance();
    }
}
