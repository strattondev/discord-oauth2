package ca.wstratto.tutorials.springboot.configuration;

import ca.wstratto.tutorials.springboot.configuration.properties.DiscordClientProperties;
import ca.wstratto.tutorials.springboot.security.DiscordAccessTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.AccessTokenProvider;
import org.springframework.security.oauth2.client.token.AccessTokenProviderChain;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.client.token.grant.implicit.ImplicitAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordAccessTokenProvider;
import org.springframework.security.oauth2.common.AuthenticationScheme;

import java.util.Arrays;

@Configuration
public class OAuth2SecurityConfiguration {
    private final AccessTokenRequest accessTokenRequest;
    private final DiscordClientProperties discordClientProperties;

    public OAuth2SecurityConfiguration(@Autowired AccessTokenRequest accessTokenRequest,
                                       @Autowired DiscordClientProperties discordClientProperties) {
        this.accessTokenRequest = accessTokenRequest;
        this.discordClientProperties = discordClientProperties;
    }

    @Bean
    @Scope("session")
    public OAuth2ProtectedResourceDetails discordResources() {
        AuthorizationCodeResourceDetails details = new AuthorizationCodeResourceDetails();

        details.setId("discord-oauth-client");
        details.setClientId(discordClientProperties.getId());
        details.setClientSecret(discordClientProperties.getSecret());
        details.setAccessTokenUri(discordClientProperties.getAccessTokenUri());
        details.setUserAuthorizationUri(discordClientProperties.getUserAuthorizationUri());
        details.setScope(Arrays.asList(discordClientProperties.getScope()));
        details.setPreEstablishedRedirectUri(discordClientProperties.getPreestablishedRedirectUrl());
        details.setUseCurrentUri(false);
        details.setAuthenticationScheme(AuthenticationScheme.header);
        details.setClientAuthenticationScheme(AuthenticationScheme.header);

        return details;
    }

    @Bean
    @Scope(value = "session", proxyMode = ScopedProxyMode.INTERFACES)
    public OAuth2RestOperations discordRestTemplate() {
        OAuth2RestTemplate oAuth2RestTemplate = new OAuth2RestTemplate(discordResources(), new DefaultOAuth2ClientContext(accessTokenRequest));

        oAuth2RestTemplate.setAccessTokenProvider(new AccessTokenProviderChain(
                Arrays.<AccessTokenProvider> asList(
                        new DiscordAccessTokenProvider(),
                        new ImplicitAccessTokenProvider(),
                        new ResourceOwnerPasswordAccessTokenProvider(),
                        new ClientCredentialsAccessTokenProvider())
                )
        );

        return oAuth2RestTemplate;
    }
}
