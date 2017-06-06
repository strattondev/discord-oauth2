package ca.wstratto.tutorials.springboot.configuration;

import ca.wstratto.tutorials.springboot.configuration.properties.DiscordClientProperties;
import ca.wstratto.tutorials.springboot.security.DiscordTokenServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.provider.token.RemoteTokenServices;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;

@Configuration
@Order(1)
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {
    private final String loginFormUrl;
    private final OAuth2RestOperations oAuth2RestOperations;
    private final OAuth2ClientContextFilter oAuth2ClientContextFilter;
    private final DiscordClientProperties discordClientProperties;

    public WebSecurityConfiguration(@Value("${login.form.url}") String loginFormUrl,
                                       @Autowired OAuth2RestOperations oAuth2RestOperations,
                                       @Autowired OAuth2ClientContextFilter oAuth2ClientContextFilter,
                                       @Autowired DiscordClientProperties discordClientProperties) {
        this.loginFormUrl = loginFormUrl;
        this.oAuth2RestOperations = oAuth2RestOperations;
        this.oAuth2ClientContextFilter = oAuth2ClientContextFilter;
        this.discordClientProperties = discordClientProperties;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .addFilterAfter(oAuth2ClientContextFilter, ExceptionTranslationFilter.class)
            .addFilterBefore(oAuth2ClientAuthenticationProcessingFilter(), FilterSecurityInterceptor.class);

        http
            .anonymous()
            .disable()
            .formLogin()
            .loginPage(loginFormUrl)
            .and()
            .authorizeRequests()
            .anyRequest()
            .fullyAuthenticated()
            .and().csrf().disable();
    }

    @Bean(name = "discordTokenServices")
    @Primary
    public RemoteTokenServices remoteTokenServices() {
        return new DiscordTokenServices(discordClientProperties);
    }

    @Bean(name = "oAuth2AuthenticationProcessingFilter")
    public OAuth2ClientAuthenticationProcessingFilter oAuth2ClientAuthenticationProcessingFilter() {
        OAuth2ClientAuthenticationProcessingFilter oAuth2ClientAuthenticationProcessingFilter = new OAuth2ClientAuthenticationProcessingFilter(loginFormUrl);

        oAuth2ClientAuthenticationProcessingFilter.setTokenServices(remoteTokenServices());
        oAuth2ClientAuthenticationProcessingFilter.setRestTemplate(oAuth2RestOperations);

        return oAuth2ClientAuthenticationProcessingFilter;
    }
}
