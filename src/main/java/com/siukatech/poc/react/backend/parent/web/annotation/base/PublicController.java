package com.siukatech.poc.react.backend.parent.web.annotation.base;

import java.lang.annotation.*;

@Target({ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PublicController {
    String REQUEST_MAPPING_URI_PREFIX = "/public";
}
