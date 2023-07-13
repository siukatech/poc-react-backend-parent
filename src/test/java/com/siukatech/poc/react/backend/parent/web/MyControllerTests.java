package com.siukatech.poc.react.backend.parent.web;

import com.siukatech.poc.react.backend.parent.AbstractUnitTests;
import com.siukatech.poc.react.backend.parent.business.dto.UserDto;
import com.siukatech.poc.react.backend.parent.business.service.UserService;
import com.siukatech.poc.react.backend.parent.data.repository.UserRepository;
import com.siukatech.poc.react.backend.parent.web.annotation.v1.ProtectedApiV1Controller;
import com.siukatech.poc.react.backend.parent.web.context.EncryptedBodyContext;
import com.siukatech.poc.react.backend.parent.web.controller.MyController;
import com.siukatech.poc.react.backend.parent.web.controller.UserController;
import com.siukatech.poc.react.backend.parent.web.helper.EncryptedBodyAdviceHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {MyController.class})
@AutoConfigureMockMvc(addFilters = false)
public class MyControllerTests extends AbstractUnitTests {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
//    @Autowired

    @Autowired
    private WebApplicationContext webApplicationContext;
    private MockMvc mockMvc;
    @MockBean
    private UserService userService;
    @SpyBean
    private EncryptedBodyContext encryptedBodyContext;
    @MockBean
    private EncryptedBodyAdviceHelper encryptedBodyAdviceHelper;


    private UserDto prepareUserDto_basic() {
        UserDto userDto = new UserDto();
        userDto.setUserId("app-user-01");
        userDto.setName("App-User-01");
        userDto.setPublicKey("public-key");
        userDto.setPrivateKey("private-key");
        return userDto;
    }

    private UsernamePasswordAuthenticationToken prepareAuthenticationToken_basic() {
        List<GrantedAuthority> convertedAuthorities = new ArrayList<GrantedAuthority>();
        UserDetails userDetails = new User("app-user-01", "", convertedAuthorities);
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        return authenticationToken;
    }

//    @BeforeAll
//    public static void init() {
//    }

    @BeforeEach
    public void setup(TestInfo testInfo) {
        Method method = testInfo.getTestMethod().get();
        switch (method.getName()) {
            case "getPublicKey_basic":
            case "getUserInfo_basic":
            default:
        }
        //
        UsernamePasswordAuthenticationToken authenticationToken = prepareAuthenticationToken_basic();
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        //
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        //
        logger.debug("setup - SecurityContextHolder.getContext.getAuthentication: [" + SecurityContextHolder.getContext().getAuthentication() + "]");
    }

    @Test
//    @WithMockUser("app-user-01")
    public void getPublicKey_basic() throws Exception {
        // given
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        logger.debug("getPublicKey_basic - authentication: [" + authentication + "]");

        UserDto userDto = this.prepareUserDto_basic();
        when(userService.findByUserId(anyString())).thenReturn(userDto);

        // when
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post(ProtectedApiV1Controller.REQUEST_MAPPING_URI_PREFIX
                        + "/my/public-key")
                .with(csrf())
                //.with(SecurityMockMvcRequestPostProcessors.user((UserDetails) authentication.getPrincipal()))
                .accept(MediaType.APPLICATION_JSON);

        // then / verify
        MvcResult mvcResult = this.mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("public-key"))
                .andReturn();

        // result
        logger.debug("getPublicKey_basic - end - mvcResult.getResponse.getContentAsString: [" + mvcResult.getResponse().getContentAsString() + "]");

    }

    @Test
    public void getUserInfo_basic() throws Exception {
        // given
        UserDto userDto = this.prepareUserDto_basic();
        when(userService.findByUserId(anyString())).thenReturn(userDto);

        // when
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .post(ProtectedApiV1Controller.REQUEST_MAPPING_URI_PREFIX
                        + "/my/user-info")
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON);

        // then / verify
        MvcResult mvcResult = this.mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().json("{userId: \"app-user-01\"}"))
                .andReturn();

        // result
        logger.debug("getUserInfo_basic - end - mvcResult.getResponse.getContentAsString: [" + mvcResult.getResponse().getContentAsString() + "]");

    }

}
