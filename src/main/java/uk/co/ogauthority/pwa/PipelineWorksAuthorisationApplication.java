package uk.co.ogauthority.pwa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class PipelineWorksAuthorisationApplication extends SpringBootServletInitializer {

  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
    return application.sources(PipelineWorksAuthorisationApplication.class);
  }

  public static void main(String[] args) {
    SpringApplication.run(PipelineWorksAuthorisationApplication.class, args);
  }

}
