package uk.co.ogauthority.pwa.config;

import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.co.ogauthority.pwa.mvc.ResponseBufferSizeHandlerInterceptor;
import uk.co.ogauthority.pwa.mvc.UserAccountArgumentResolver;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
    resolvers.add(new UserAccountArgumentResolver());
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new ResponseBufferSizeHandlerInterceptor())
        .excludePathPatterns("/assets/**");
  }

}
