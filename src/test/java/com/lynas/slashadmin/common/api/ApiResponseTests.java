package com.lynas.slashadmin.common.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ApiResponseTests {

  @Test
  void createsFrontendCompatibleSuccessResponse() {
    ApiResponse<String> response = ApiResponse.success("ok");

    assertThat(response.status()).isZero();
    assertThat(response.message()).isEmpty();
    assertThat(response.data()).isEqualTo("ok");
  }
}
