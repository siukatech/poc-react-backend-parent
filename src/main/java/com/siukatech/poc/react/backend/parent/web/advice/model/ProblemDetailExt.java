package com.siukatech.poc.react.backend.parent.web.advice.model;

import lombok.Data;
import org.springframework.http.ProblemDetail;

@Data
public class ProblemDetailExt extends ProblemDetail {
    protected String correlationId;
}
