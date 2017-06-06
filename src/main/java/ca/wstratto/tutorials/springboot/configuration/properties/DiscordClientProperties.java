package ca.wstratto.tutorials.springboot.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "discord.client")
@PropertySource(value = "classpath:secret.properties", ignoreResourceNotFound = true)
public class DiscordClientProperties {
    private String id;
    private String secret;
    private String scope[];
    private String accessTokenUri;
    private String userAuthorizationUri;
    private String preestablishedRedirectUrl;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String[] getScope() {
        return scope;
    }

    public void setScope(String[] scope) {
        this.scope = scope;
    }

    public String getAccessTokenUri() {
        return accessTokenUri;
    }

    public void setAccessTokenUri(String accessTokenUri) {
        this.accessTokenUri = accessTokenUri;
    }

    public String getUserAuthorizationUri() {
        return userAuthorizationUri;
    }

    public void setUserAuthorizationUri(String userAuthorizationUri) {
        this.userAuthorizationUri = userAuthorizationUri;
    }

    public String getPreestablishedRedirectUrl() {
        return preestablishedRedirectUrl;
    }

    public void setPreestablishedRedirectUrl(String preestablishedRedirectUrl) {
        this.preestablishedRedirectUrl = preestablishedRedirectUrl;
    }
}
