package com.siukatech.poc.react.backend.parent.web.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siukatech.poc.react.backend.parent.AbstractUnitTests;
import com.siukatech.poc.react.backend.parent.business.dto.UserDto;
import com.siukatech.poc.react.backend.parent.global.config.ParentAppConfig;
import com.siukatech.poc.react.backend.parent.util.EncryptionUtil;
import com.siukatech.poc.react.backend.parent.web.model.EncryptedDetail;
import com.siukatech.poc.react.backend.parent.web.model.EncryptedInfo;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class
        , SpringExtension.class
})
@EnableConfigurationProperties
@ContextConfiguration(classes = {OAuth2ClientProperties.class, ParentAppConfig.class})
@TestPropertySource({"classpath:abstract-oauth2-tests.properties"
        , "classpath:global/parent-app-config-tests.properties"
})
public class EncryptedBodyAdviceHelperTests extends AbstractUnitTests {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * This is the OAuth2ClientProperties initiated with @TestPropertySource
     */
    @Autowired
    private OAuth2ClientProperties oAuth2ClientPropertiesForTests;
    @Autowired
    private ParentAppConfig parentAppConfigForTests;

    @InjectMocks
    private EncryptedBodyAdviceHelper encryptedBodyAdviceHelper;

    @Spy
    private ObjectMapper objectMapper;
    //    @Mock
    @Spy
    private RestTemplate oauth2ClientRestTemplate;
    @Spy
    private ParentAppConfig parentAppConfig;


    private UserDto prepareUserDto_basic() throws NoSuchAlgorithmException {
        KeyPair keyPair = EncryptionUtil.generateRsaKeyPair();
        UserDto userDto = new UserDto();
        userDto.setUserId("app-user-01");
        userDto.setName("App User 01");
//        userDto.setPublicKey("public-key");
//        userDto.setPrivateKey("private-key");
        String privateKeyBase64 = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
        String publicKeyBase64 = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        userDto.setPrivateKey(privateKeyBase64);
        userDto.setPublicKey(publicKeyBase64);
        return userDto;
    }

    private EncryptedInfo prepareEncryptedInfo_basic() {
        // Reference:
        // https://stackoverflow.com/a/29354222
        // aes key MUST be 16, 24, 32
        // iv MUST be 16
        // salt MUST be 16
        String key = EncryptionUtil.generateRandomToken(32);
        String iv = EncryptionUtil.generateRandomToken(16);
        String salt = EncryptionUtil.generateRandomToken(16);
        EncryptedInfo encryptedInfo = new EncryptedInfo(key, iv, salt);
        return encryptedInfo;
    }

    @Test
    public void encryptBodyToDataBase64_basic() throws Exception {
        // given
        UserDto body = this.prepareUserDto_basic();
        String bodyStr = this.objectMapper.writeValueAsString(body);
        EncryptedInfo encryptedInfo = this.prepareEncryptedInfo_basic();
        byte[] aesKey = EncryptionUtil.encryptWithRsaPublicKey(encryptedInfo.key(), body.getPublicKey());
        String aesKeyBase64 = Base64.getEncoder().encodeToString(aesKey);
        logger.debug("encryptBodyToDataBase64_basic - aesKeyBase64.length: [{}]"
                        + ", bodyStr: [{}]"
                , aesKeyBase64.length()
                , bodyStr
        );

        // when
        String cipherText = this.encryptedBodyAdviceHelper.encryptBodyToDataBase64(body, encryptedInfo);

        // then
        logger.debug("encryptBodyToDataBase64_basic - cipherText: [{}]", cipherText);
        byte[] data = Base64.getDecoder().decode(cipherText);
        byte[] secret = Base64.getDecoder().decode(encryptedInfo.key());
        byte[] iv = Base64.getDecoder().decode(encryptedInfo.iv());
        byte[] content = EncryptionUtil.decryptWithAesCbcSecret(data, secret, iv);
        String contentStr = new String(content);
        logger.debug("encryptBodyToDataBase64_basic - contentStr: [{}]", contentStr);

        assertThat(bodyStr).isEqualTo(contentStr);

    }

//    @Test
//    public void resolveRsaInfoAesContent_basic() throws Exception {
//        // given
//        UserDto body = this.prepareUserDto_basic();
//        String bodyStr = this.objectMapper.writeValueAsString(body);
//        EncryptedInfo encryptedInfo = this.prepareEncryptedInfo_basic();
//        byte[] aesKey = EncryptionUtil.encryptWithRsaPublicKey(encryptedInfo.key(), body.getPublicKey());
//        String aesKeyBase64 = Base64.getEncoder().encodeToString(aesKey);
//        String contentBase64 = Base64.getEncoder().encodeToString("This is a test".getBytes(StandardCharsets.UTF_8));
//
//        // when
//        this.encryptedBodyAdviceHelper.re
//
//        // then
//    }

    @Test
    public void decryptDataBase64ToBodyDetail_basic() throws Exception {
        // given
        UserDto userDto = this.prepareUserDto_basic();
        String userDtoStr = this.objectMapper.writeValueAsString(userDto);
        EncryptedInfo encryptedInfo = this.prepareEncryptedInfo_basic();
        String encryptedInfoStr = this.objectMapper.writeValueAsString(encryptedInfo);
        byte[] secret = Base64.getDecoder().decode(encryptedInfo.key());
        byte[] iv = Base64.getDecoder().decode(encryptedInfo.iv());
        byte[] aesKey = EncryptionUtil.encryptWithRsaPublicKey(encryptedInfo.key(), userDto.getPublicKey());
        String aesKeyBase64 = Base64.getEncoder().encodeToString(aesKey);
        byte[] headerData = EncryptionUtil.encryptWithRsaPublicKey(encryptedInfoStr, userDto.getPublicKey());
        String headerDataBase64 = Base64.getEncoder().encodeToString(headerData);
        byte[] bodyData = EncryptionUtil.encryptWithAesCbcSecret(userDtoStr, secret, iv);
        String bodyDataBase64 = Base64.getEncoder().encodeToString(bodyData);
        String encryptedBody = headerDataBase64 + bodyDataBase64;
        String encryptedDataBase64 = Base64.getEncoder().encodeToString(encryptedBody.getBytes(StandardCharsets.UTF_8));
        logger.debug("decryptDataBase64ToBodyDetail_basic - headerDataBase64.length: [{}]"
                        + ", bodyDataBase64.length: [{}]"
                , headerDataBase64.length()
                , bodyDataBase64.length()
        );

        // when
        EncryptedDetail encryptedDetail = this.encryptedBodyAdviceHelper.decryptDataBase64ToBodyDetail(encryptedDataBase64, userDto);

        // then
        logger.debug("decryptDataBase64ToBodyDetail_basic - encryptedDetail: [{}]"
                        + ""
                , encryptedDetail
        );
        assertThat(encryptedDetail.encryptedInfo()).isEqualTo(encryptedInfo);
        assertThat(new String(encryptedDetail.decryptedData())).isEqualTo(userDtoStr);
    }

    @Test
    public void resolveMyUserInfo_basic() throws NoSuchAlgorithmException {
        logger.debug("resolveMyUserInfo_basic - parentAppConfigForTests.myUserInfoUrl: [{}]"
                        + ", parentAppConfig.myUserInfoUrl: [{}]"
                , this.parentAppConfigForTests.getMyUserInfoUrl()
                , this.parentAppConfig.getMyUserInfoUrl()
        );

        // given
        UserDto userDtoRet = prepareUserDto_basic();
        String userId = userDtoRet.getUserId();
        when(this.parentAppConfig.getMyUserInfoUrl())
                .thenReturn(this.parentAppConfigForTests.getMyUserInfoUrl());
//        when(oauth2ClientRestTemplate.exchange(anyString()
//                , eq(HttpMethod.POST), eq(HttpEntity.EMPTY), eq(UserDto.class)))
//                .thenReturn(ResponseEntity.ok(prepareUserDto_basic()));
        doReturn(ResponseEntity.ok(userDtoRet))
                .when(this.oauth2ClientRestTemplate).exchange(anyString(), eq(HttpMethod.POST)
                        , ArgumentMatchers.any(HttpEntity.class), eq(UserDto.class))
        ;

        // when
        UserDto userDto = this.encryptedBodyAdviceHelper.resolveMyUserInfo(userId);

        // then
        logger.debug("resolveMyUserInfo_basic - userDto: [{}]", userDto);
        assertThat(userDto).hasFieldOrProperty("privateKey")
                .has(new Condition<>(u -> u.getPrivateKey().contains(userDtoRet.getPrivateKey())
                        , "Has %s", "private-key"))
        ;

    }

    @Test
    public void isEncryptedApiController_basic() {
        // given
        Method methodForTrue = Arrays.stream(EncryptedForTestController.class.getMethods()).findFirst().get();
        MethodParameter methodParameterForTrue = new MethodParameter(methodForTrue, 0);
        Method methodForFalse = Arrays.stream(ProtectedForTestController.class.getMethods()).findFirst().get();
        MethodParameter methodParameterForFalse = new MethodParameter(methodForFalse, 0);

        // when
        boolean resultForTrue = this.encryptedBodyAdviceHelper.isEncryptedApiController(methodParameterForTrue);
        boolean resultForFalse = this.encryptedBodyAdviceHelper.isEncryptedApiController(methodParameterForFalse);

        // then
        logger.debug("isEncryptedApiController_basic - resultForTrue: [{}]"
                        + ", resultForFalse: [{}]"
                , resultForTrue
                , resultForFalse
        );
        assertThat(resultForTrue).isEqualTo(true);
        assertThat(resultForFalse).isEqualTo(false);
    }

}
