package com.momento.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;

@Configuration
public class FirebaseConfig {
    @Value("${app.firebase.service-account-path}")
    private String serviceAccountPath;
    @Value("${app.firebase.project-id}")
    private String projectId;

    @PostConstruct
    public void init() throws Exception {
        if (FirebaseApp.getApps().isEmpty()) {
            var options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(new FileInputStream(serviceAccountPath)))
                    .setProjectId(projectId)
                    .build();
            FirebaseApp.initializeApp(options);
        }
    }
}
