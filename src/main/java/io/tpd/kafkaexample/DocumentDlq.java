package io.tpd.kafkaexample;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class DocumentDlq {
    @Id
    private String identifier;

    private String exception;

    private String stacktrace;

    public DocumentDlq() {
    }

    public DocumentDlq(String identifier, String exception, String stacktrace) {
        this.identifier = identifier;
        this.exception = exception;
        this.stacktrace = stacktrace;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public String getStacktrace() {
        return stacktrace;
    }

    public void setStacktrace(String stacktrace) {
        this.stacktrace = stacktrace;
    }
}
