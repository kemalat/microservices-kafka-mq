package de.oriontec.microservice.order.config;


import static springfox.documentation.builders.PathSelectors.regex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.BasicAuth;
import springfox.documentation.service.Contact;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.service.SecurityScheme;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


@Configuration
@EnableSwagger2
public class SwaggerConfig {

  @Bean
  public Docket productApi() {
    List<SecurityScheme> schemeList = new ArrayList<>();
    schemeList.add(new BasicAuth("basicAuth"));
    return new Docket(DocumentationType.SWAGGER_2).select()
        .apis(RequestHandlerSelectors.basePackage("de.oriontec.microservice")).paths(regex("/.*"))
        .paths(PathSelectors.any()).build()
        .securityContexts(Collections.singletonList(securityContext()))
        .securitySchemes(schemeList).apiInfo(apiInfo());

  }

  private SecurityContext securityContext() {
    return SecurityContext.builder()
        .securityReferences(defaultAuth())
        .build();
  }

  private List<SecurityReference> defaultAuth() {
    AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
    AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
    authorizationScopes[0] = authorizationScope;
    List<SecurityReference> SecurityReferences = new ArrayList<>();
    SecurityReferences.add(new SecurityReference("basicAuth", authorizationScopes));
    return SecurityReferences;
  }
  /**
   *
   * @return ApiInf
   */
  private ApiInfo apiInfo() {
    return new ApiInfoBuilder().title("Authentication API").description("")
        .termsOfServiceUrl("https://www.example.com/api")
        .contact(new Contact("Developers", "https://projects.spring.io/spring-boot/", ""))
        .license("Open Source")
        .licenseUrl("\"https://www.apache.org/licenses/LICENSE-2.0")
        .version("1.0.0")
        .build();

  }

}

