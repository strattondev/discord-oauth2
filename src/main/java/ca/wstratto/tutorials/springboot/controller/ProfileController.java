package ca.wstratto.tutorials.springboot.controller;

import ca.wstratto.tutorials.springboot.domain.DiscordUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClientException;

import java.io.IOException;

@Controller
@RequestMapping("/profile")
public class ProfileController {
    private static final String DISCORD_USER_URL = "https://discordapp.com/api/users/@me";

    private final OAuth2RestOperations oAuth2RestOperations;

    public ProfileController(@Autowired OAuth2RestOperations oAuth2RestOperations) {
        this.oAuth2RestOperations = oAuth2RestOperations;
    }

    @RequestMapping(value = {"", "/"})
    @ResponseBody
    public DiscordUser profile() {
        Request req = new Request.Builder()
                .url(DISCORD_USER_URL)
                .addHeader("Authorization", "Bearer " + oAuth2RestOperations.getAccessToken())
                .get()
                .build();

        try (Response response = new OkHttpClient().newCall(req).execute()) {
            return new ObjectMapper().readValue(response.body().bytes(), DiscordUser.class);
        } catch (IOException e) {
            throw new RestClientException("");
        }
    }

    @RequestMapping("/auth")
    @ResponseBody
    public Authentication auth() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
