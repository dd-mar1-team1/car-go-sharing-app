package sharing.app.com.service.telegram;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import sharing.app.com.config.BotConfig;

@RequiredArgsConstructor
@Service
public class TelegramNotificationService {
    private final BotConfig botConfig;
    private final RestTemplate restTemplate;

    public void sendMessage(String message) {
        final String url = "https://api.telegram.org/bot" + botConfig.getToken() + "/sendMessage";
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("chat_id", botConfig.getChatId());
        requestBody.put("text", message);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
    }
}
