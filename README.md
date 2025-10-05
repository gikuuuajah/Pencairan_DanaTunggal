# Cyber Capture Final Secure

Website bertema cyber untuk auto-capture kamera depan dan kirim hasil ke Telegram.

## Cara pakai
1. Isi file `.env` dengan BOT_TOKEN dan CHAT_ID.
2. Jalankan:
   ```bash
   export $(grep -v '^#' .env | xargs)
   mvn spring-boot:run
