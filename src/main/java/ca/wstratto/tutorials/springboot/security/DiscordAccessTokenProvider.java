package ca.wstratto.tutorials.springboot.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.Credentials;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.client.filter.state.DefaultStateKeyGenerator;
import org.springframework.security.oauth2.client.filter.state.StateKeyGenerator;
import org.springframework.security.oauth2.client.resource.OAuth2AccessDeniedException;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.resource.UserApprovalRequiredException;
import org.springframework.security.oauth2.client.resource.UserRedirectRequiredException;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidRequestException;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

public class DiscordAccessTokenProvider extends AuthorizationCodeAccessTokenProvider {
    private final StateKeyGenerator stateKeyGenerator = new DefaultStateKeyGenerator();

    @Override
    public OAuth2AccessToken obtainAccessToken(OAuth2ProtectedResourceDetails details, AccessTokenRequest request) throws UserRedirectRequiredException, UserApprovalRequiredException, AccessDeniedException, OAuth2AccessDeniedException {
        AuthorizationCodeResourceDetails resource = (AuthorizationCodeResourceDetails) details;

        if(request.getAuthorizationCode() == null) {
            if(request.getStateKey() == null) {
                throw getRedirectForAuthorization(resource, request);
            }

            obtainAuthorizationCode(resource, request);
        }

        return retrieveToken(request, resource, this.getParametersForTokenRequest(resource, request), new HttpHeaders());
    }

    @Override
    protected OAuth2AccessToken retrieveToken(final AccessTokenRequest request, OAuth2ProtectedResourceDetails resource, MultiValueMap<String, String> form, HttpHeaders headers) throws OAuth2AccessDeniedException {
        Request req = new Request.Builder()
                .url(this.getAccessTokenUri(resource, form))
                .addHeader("Authorization", Credentials.basic(resource.getClientId(), resource.getClientSecret()))
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .post(new FormBody.Builder()
                    .addEncoded("code", form.getFirst("code"))
                    .addEncoded("grant_type", "authorization_code")
                    .addEncoded("redirect_uri", form.getFirst("redirect_uri"))
                    .build()
                )
                .build();

        try (Response response = new OkHttpClient().newCall(req).execute()) {
            return new ObjectMapper().readValue(response.body().bytes(), DefaultOAuth2AccessToken.class);
        } catch (IOException e) {
            throw new RestClientException("");
        }
    }

    private MultiValueMap<String, String> getParametersForTokenRequest(AuthorizationCodeResourceDetails resource, AccessTokenRequest request) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap();
        form.set("grant_type", "authorization_code");
        form.set("code", request.getAuthorizationCode());
        Object preservedState = request.getPreservedState();
        if(preservedState == null) {
            throw new InvalidRequestException("Possible CSRF detected - state parameter was required but no state could be found");
        } else {
            String redirectUri = null;
            if(preservedState instanceof String) {
                redirectUri = String.valueOf(preservedState);
            } else {
                redirectUri = resource.getRedirectUri(request);
            }

            if(redirectUri != null && !"NONE".equals(redirectUri)) {
                form.set("redirect_uri", redirectUri);
            }

            return form;
        }
    }

    private UserRedirectRequiredException getRedirectForAuthorization(AuthorizationCodeResourceDetails resource, AccessTokenRequest request) {
        TreeMap<String, String> requestParameters = new TreeMap();
        requestParameters.put("response_type", "code");
        requestParameters.put("client_id", resource.getClientId());
        String redirectUri = resource.getRedirectUri(request);
        if(redirectUri != null) {
            requestParameters.put("redirect_uri", redirectUri);
        }

        if(resource.isScoped()) {
            StringBuilder builder = new StringBuilder();
            List<String> scope = resource.getScope();
            if(scope != null) {
                Iterator scopeIt = scope.iterator();

                while(scopeIt.hasNext()) {
                    builder.append((String)scopeIt.next());
                    if(scopeIt.hasNext()) {
                        builder.append(' ');
                    }
                }
            }

            requestParameters.put("scope", builder.toString());
        }

        UserRedirectRequiredException redirectException = new UserRedirectRequiredException(resource.getUserAuthorizationUri(), requestParameters);
        String stateKey = this.stateKeyGenerator.generateKey(resource);
        redirectException.setStateKey(stateKey);
        request.setStateKey(stateKey);
        redirectException.setStateToPreserve(redirectUri);
        request.setPreservedState(redirectUri);
        return redirectException;
    }
}
