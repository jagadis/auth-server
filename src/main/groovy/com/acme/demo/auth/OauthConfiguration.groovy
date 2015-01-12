package com.acme.demo.auth

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory
import org.springframework.web.bind.annotation.SessionAttributes

import java.security.KeyPair


/**
 * Configure the application to have a single service to authenticate users and client applications.
 * Since all of these applications really make up one application there is no need to ask the user for
 * grant permissions.  We will be using the auth code grant type.
 *
 * The Authentication flow for the client application will go like this:
 * <ol>
 * <li>The user will be redirected to the auth server to provide credentials or register/activate
 * <li>The user will provide username and password via a login form (auth server)
 * <li>The credentials will be submitted kicking off the user authentication flow
 *<li>The authorization server will authenticate the user, and initiate the oauth auth code grant flow, which is detailed
 * very well by David Syer in his many blog posts on Oauth2
 *<li> The user will be redirected back to the API-GATEWAY service but will now have a valid token in their session
 * Note that on a production system SSL/TLS should be used for transport layer security.  This is most easily
 * done on a SPA application by simply making the entire application SSL, and redirecting the user to HTTPS
 * if they request an HTTP endpoint.
 *
 * Inspired by David Syers excellent spring-security-angular example and presentation at S2GX
 *
 * @author William Gorder
 * @since 12/30/14
 */
@Configuration
@EnableAuthorizationServer
@SessionAttributes("authorizationRequest")
class OauthConfiguration extends AuthorizationServerConfigurerAdapter {

    @Autowired
    AuthenticationManager authenticationManager;

    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        KeyPair keyPair = new KeyStoreKeyFactory(
                new ClassPathResource("keystore.jks"), "foobar".toCharArray())
                .getKeyPair("test");
        converter.setKeyPair(keyPair);
        return converter;
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory()
                .withClient("acme")
                .secret("acmesecret")
                .autoApprove(true)
                .authorizedGrantTypes("authorization_code", "refresh_token", "password").scopes("openid");
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints)
            throws Exception {
        endpoints.authenticationManager(authenticationManager).accessTokenConverter(
                jwtAccessTokenConverter());
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer oauthServer)
            throws Exception {
        oauthServer.tokenKeyAccess("permitAll()").checkTokenAccess(
                "isAuthenticated()");
    }
}
