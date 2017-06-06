package ca.wstratto.tutorials.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;

@SpringBootApplication
@EnableOAuth2Sso
public class DiscordOauth2Application {
	public static void main(String[] args) {
		SpringApplication.run(DiscordOauth2Application.class, args);
	}
}
