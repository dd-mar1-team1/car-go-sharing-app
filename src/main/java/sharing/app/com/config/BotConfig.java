package sharing.app.com.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class BotConfig {
    @Value("${bot.name}")
    private String botName;

    @Value("${bot.token}")
    private String token;

    @Value("${bot.chatId}")
    private String chatId;
}
