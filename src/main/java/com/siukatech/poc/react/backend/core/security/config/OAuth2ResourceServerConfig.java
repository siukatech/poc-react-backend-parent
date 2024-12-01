package com.siukatech.poc.react.backend.core.security.config;

import com.siukatech.poc.react.backend.core.security.resourceserver.MyOpaqueTokenIntrospector;
import com.siukatech.poc.react.backend.core.security.resourceserver.OAuth2ResourceServerExtProp;
import com.siukatech.poc.react.backend.core.util.ResourceServerUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;

import java.text.ParseException;

@Slf4j
@Configuration
public class OAuth2ResourceServerConfig {
    private final OAuth2ClientProperties oAuth2ClientProperties;
    private final OAuth2ResourceServerExtProp oAuth2ResourceServerExtProp;

    public OAuth2ResourceServerConfig(
            OAuth2ClientProperties oAuth2ClientProperties
            , OAuth2ResourceServerExtProp oAuth2ResourceServerExtProp) {
        this.oAuth2ClientProperties = oAuth2ClientProperties;
        this.oAuth2ResourceServerExtProp = oAuth2ResourceServerExtProp;
    }

//    @Bean
//    public OpaqueTokenIntrospector opaqueTokenIntrospector() {
//        MyOpaqueTokenIntrospector myOpaqueTokenIntrospector
//                = new MyOpaqueTokenIntrospector(oAuth2ClientProperties, oAuth2ResourceServerExtProp);
//        return myOpaqueTokenIntrospector;
//    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return (token -> {
            try {
                String issuerUri = ResourceServerUtil.getIssuerUri(token);
                String clientName = ResourceServerUtil.getClientName(oAuth2ClientProperties, issuerUri);
                OAuth2ClientProperties.Registration registration = oAuth2ClientProperties.getRegistration().get(clientName);
                OAuth2ResourceServerProperties.Jwt jwt = ResourceServerUtil.getResourceServerPropJwt(oAuth2ResourceServerExtProp, clientName);
//                NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder
//                        .withIssuerLocation(oAuth2ResourceServerProperties.getJwt().getIssuerUri())
//                        .jwsAlgorithm(SignatureAlgorithm.RS512)
//                        .build();
                log.debug("jwtDecode - issuerUri: [{}], clientName: [{}], jwt: [{}]", issuerUri, clientName, jwt);
                NimbusJwtDecoder jwtDecoder = JwtDecoders.fromOidcIssuerLocation(jwt.getIssuerUri());
                OAuth2TokenValidator<Jwt> withIssuerJwtTokenValidator = JwtValidators.createDefaultWithIssuer(jwt.getIssuerUri());
                OAuth2TokenValidator<Jwt> jwtDelegatingOAuth2TokenValidator = new DelegatingOAuth2TokenValidator<>(withIssuerJwtTokenValidator);
                jwtDecoder.setJwtValidator(jwtDelegatingOAuth2TokenValidator);
                return jwtDecoder.decode(token);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        });
    }

}