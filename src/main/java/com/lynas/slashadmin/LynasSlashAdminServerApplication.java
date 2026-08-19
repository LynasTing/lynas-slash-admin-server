package com.lynas.slashadmin;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.context.WebServerApplicationContext;

@SpringBootApplication
@OpenAPIDefinition(
  info = @Info(title = "Lynas Slash Admin API", version = "v1", description = "Lynas Slash Admin 后端接口文档"))
public class LynasSlashAdminServerApplication {
  public static void main(
    String[] args) {
    WebServerApplicationContext applicationContext = (WebServerApplicationContext) SpringApplication
      .run(LynasSlashAdminServerApplication.class, args);
    int port = applicationContext.getWebServer().getPort();
    System.out.println("Swagger UI: http:" + "/" + "/localhost:" + port + "/swagger-ui/index.html");
  }
}
