package com.siukatech.poc.react.backend.parent.web.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siukatech.poc.react.backend.parent.security.interceptor.AuthorizationDataInterceptor;
import com.siukatech.poc.react.backend.parent.security.provider.AuthorizationDataProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

@Slf4j
//public class WebMvcConfig extends WebMvcConfigurationSupport {
public class WebMvcConfig implements WebMvcConfigurer {

    private final ObjectMapper objectMapper;
    private final AuthorizationDataInterceptor authorizationDataInterceptor;

    public WebMvcConfig(ObjectMapper objectMapper, AuthorizationDataInterceptor authorizationDataInterceptor) {
        this.objectMapper = objectMapper;
        this.authorizationDataInterceptor = authorizationDataInterceptor;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        log.debug("addCorsMappings - start");
        registry
                .addMapping("/**")
//                .allowedMethods(HttpMethod.HEAD.name()
//                        , HttpMethod.GET.name()
//                        , HttpMethod.POST.name()
//                        , HttpMethod.PUT.name()
//                        , HttpMethod.DELETE.name()
//                        , HttpMethod.PATCH.name()
//                        , HttpMethod.OPTIONS.name()
//                )
                .allowedMethods(Arrays.stream(HttpMethod.values()).map(HttpMethod::name).toArray(String[]::new))
                //.allowedOrigins("http://localhost:3000/")
                .allowedOrigins("*")
        ;
        log.debug("addCorsMappings - end");
    }

//    private static final String dateFormat = "yyyy-MM-dd";
//    private static final String dateTimeFormat = "yyyy-MM-dd HH:mm:ss";
//
//    @Bean
//    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
//        return builder -> {
//            builder.simpleDateFormat(dateTimeFormat);
//            builder.serializers(new LocalDateSerializer(DateTimeFormatter.ofPattern(dateFormat)));
//            builder.serializers(new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(dateTimeFormat)));
//        };
//    }

    /**
     * Reference:
     * https://stackoverflow.com/q/36906877
     * disable feature WRITE_DATES_AS_TIMESTAMPS
     *
     * @param converters the list of configured converters to extend
     */
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.debug("extendMessageConverters - start");
        log.debug("extendMessageConverters - converters.size: [{}]", converters.size());
        converters.stream().forEach(httpMessageConverter -> {
            log.debug("extendMessageConverters - httpMessageConverter.getClass.getName: [{}]", httpMessageConverter.getClass().getName());
            if (httpMessageConverter instanceof MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter) {
////                MappingJackson2HttpMessageConverter jacksonMessageConverter = (MappingJackson2HttpMessageConverter) httpMessageConverter;
//                ObjectMapper objectMapper = mappingJackson2HttpMessageConverter.getObjectMapper();
//                log.debug("extendMessageConverters - MappingJackson2HttpMessageConverter.getObjectMapper: [{}]"
//                        , mappingJackson2HttpMessageConverter.getObjectMapper());
//                objectMapper =
//                        // here is configured for non-encrypted data, general response body
//                        objectMapper
//                                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
//
//                                // ignore unknown json properties to prevent HttpMessageNotReadableException
//                                // https://stackoverflow.com/a/5455563
////                objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
//                                .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
////                                .disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES)
////                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//                                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
////                                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
//                ;
//                objectMapper.getDeserializationConfig().without(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);

                mappingJackson2HttpMessageConverter.setObjectMapper(objectMapper);
            }
        });
        log.debug("extendMessageConverters - end");
    }

    public void addInterceptors(InterceptorRegistry registry) {
        log.debug("addInterceptors - start");
        registry.addInterceptor(authorizationDataInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/auth/**", "/logout")
        ;
        log.debug("addInterceptors - end");
    }

}
