package com.example.idempotency.model;

public class IdempotencyRequestResponse {
    private Object request;
    private Object response;

    public IdempotencyRequestResponse(Object request, Object response) {
        this.request = request;
        this.response = response;
    }

    public static IdempotencyRequestResponseBuilder builder() {
        return new IdempotencyRequestResponseBuilder();
    }

    public Object getRequest() {
        return this.request;
    }

    public Object getResponse() {
        return this.response;
    }

    public void setRequest(Object request) {
        this.request = request;
    }

    public void setResponse(Object response) {
        this.response = response;
    }

    public static class IdempotencyRequestResponseBuilder {
        private Object request;
        private Object response;

        IdempotencyRequestResponseBuilder() {
        }

        public IdempotencyRequestResponseBuilder request(Object request) {
            this.request = request;
            return this;
        }

        public IdempotencyRequestResponseBuilder response(Object response) {
            this.response = response;
            return this;
        }

        public IdempotencyRequestResponse build() {
            return new IdempotencyRequestResponse(this.request, this.response);
        }
    }
}
