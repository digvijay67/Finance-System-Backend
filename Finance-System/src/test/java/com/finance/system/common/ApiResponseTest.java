package com.finance.system.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    @Test
    void successResponse_shouldContainMessageDataAndStatus() {
        ApiResponse<String> response = ApiResponse.success("OK", "payload");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("OK");
        assertThat(response.getData()).isEqualTo("payload");
        assertThat(response.getStatusCode()).isNull();
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    void errorResponse_shouldContainErrorMessageAndStatusCode() {
        ApiResponse<Object> response = ApiResponse.error("Not found", 404);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Not found");
        assertThat(response.getStatusCode()).isEqualTo(404);
        assertThat(response.getData()).isNull();
        assertThat(response.getTimestamp()).isNotNull();
    }
}
