package com.lynas.slashadmin.auth.vo;

import com.lynas.slashadmin.auth.model.AuthMenuNode;
import com.lynas.slashadmin.auth.model.AuthRole;
import com.lynas.slashadmin.common.response.ApiResponse;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.media.Schema;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 认证响应 OpenAPI 契约测试。
 *
 * <p>验证 Swagger 将稳定返回的字段标记为 required，并保留允许 null 的资料字段语义。
 */
class AuthResponseSchemaTest {

  @Test
  void authRole_whenResolved_marksAllRoleFieldsRequired() {
    Schema<?> schema = resolveSchema(AuthRole.class);

    assertRequired(schema, "id", "code", "name");
  }

  @Test
  void authenticatedUserVo_whenResolved_marksPropertiesRequiredAndProfileFieldsNullable() {
    Schema<?> schema = resolveSchema(AuthenticatedUserVo.class);

    assertRequired(schema, "userId", "username", "nickname", "email", "phone", "avatar", "roles", "permissions",
      "menu");
    assertNullable(schema, "nickname", "email", "phone", "avatar");
  }

  @Test
  void authMenuNode_whenResolved_marksPropertiesRequiredAndOptionalValuesNullable() {
    Schema<?> schema = resolveSchema(AuthMenuNode.class);

    assertRequired(schema, "id", "parentId", "name", "code", "category", "sort", "status", "path", "component", "icon",
      "i18nKey", "hidden", "description", "externalLink", "children");
    assertNullable(schema, "path", "component", "icon", "i18nKey", "description", "externalLink");
  }

  @Test
  void tokenAndLoginResponses_whenResolved_markStableFieldsRequired() {
    assertRequired(resolveSchema(AccessTokenResponse.class), "accessToken", "expiresAt");
    assertRequired(resolveSchema(CsrfTokenResponse.class), "token", "headerName", "parameterName");
    assertRequired(resolveSchema(LoginResponse.class), "accessToken", "user", "expiresAt");
  }

  @Test
  void apiResponse_whenResolved_marksEnvelopeFieldsRequired() {
    Schema<?> schema = resolveSchema(ApiResponse.class);

    assertRequired(schema, "code", "message", "data");
  }

  private static Schema<?> resolveSchema(
    Class<?> responseType) {
    return ModelConverters.getInstance()
      .resolveAsResolvedSchema(new AnnotatedType(responseType).resolveAsRef(false)).schema;
  }

  private static void assertRequired(
    Schema<?> schema,
    String... propertyNames) {
    assertNotNull(schema.getRequired());
    assertTrue(schema.getRequired().containsAll(List.of(propertyNames)));
  }

  private static void assertNullable(
    Schema<?> schema,
    String... propertyNames) {
    for (String propertyName : propertyNames) {
      Schema<?> property = schema.getProperties().get(propertyName);

      assertNotNull(property);
      assertTrue(Boolean.TRUE.equals(property.getNullable()));
    }
  }
}
