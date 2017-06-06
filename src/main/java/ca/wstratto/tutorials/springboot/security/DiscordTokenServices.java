package ca.wstratto.tutorials.springboot.security;

import ca.wstratto.tutorials.springboot.configuration.properties.DiscordClientProperties;
import ca.wstratto.tutorials.springboot.domain.DiscordUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.Credentials;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.security.oauth2.client.resource.OAuth2AccessDeniedException;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.AccessTokenConverter;
import org.springframework.security.oauth2.provider.token.RemoteTokenServices;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.springframework.security.oauth2.common.util.OAuth2Utils.CLIENT_ID;

public class DiscordTokenServices extends RemoteTokenServices {
    private static final String DISCORD_USER_URL = "https://discordapp.com/api/users/@me";

    private final DiscordClientProperties discordClientProperties;

    public DiscordTokenServices(DiscordClientProperties discordClientProperties) {
        this.discordClientProperties = discordClientProperties;
    }

    @Override
    public OAuth2Authentication loadAuthentication(String accessToken) throws AuthenticationException, InvalidTokenException {
        OAuth2Request oAuth2Request = new OAuth2Request(
                Collections.singletonMap(CLIENT_ID, discordClientProperties.getId()),
                discordClientProperties.getId(),
                null,
                true,
                new HashSet<>(Arrays.asList(discordClientProperties.getScope())),
                Collections.emptySet(),
                null,
                null,
                null);

        DiscordUser currentUser = getCurrentUser(accessToken);
        Authentication user = new UsernamePasswordAuthenticationToken(currentUser.getEmail(), "N/A", Collections.emptyList());

        return new OAuth2Authentication(oAuth2Request, user);
    }

    private DiscordUser getCurrentUser(String accessToken) {
        Request req = new Request.Builder()
                .url(DISCORD_USER_URL)
                .addHeader("Authorization", "Bearer " + accessToken)
                .get()
                .build();

        try (Response response = new OkHttpClient().newCall(req).execute()) {
            return new ObjectMapper().readValue(response.body().bytes(), DiscordUser.class);
        } catch (IOException e) {
            throw new RestClientException("");
        }
    }
}