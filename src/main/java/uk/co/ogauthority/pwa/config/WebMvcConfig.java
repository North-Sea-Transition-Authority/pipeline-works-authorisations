package uk.co.ogauthority.pwa.config;

import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.CacheControl;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.ResourceUrlEncodingFilter;
import org.springframework.web.servlet.resource.VersionResourceResolver;
import uk.co.ogauthority.pwa.mvc.ResponseBufferSizeHandlerInterceptor;
import uk.co.ogauthority.pwa.mvc.argresolvers.AuthenticatedUserAccountArgumentResolver;
import uk.co.ogauthority.pwa.mvc.argresolvers.PwaAppProcessingContextArgumentResolver;
import uk.co.ogauthority.pwa.mvc.argresolvers.PwaApplicationContextArgumentResolver;
import uk.co.ogauthority.pwa.mvc.argresolvers.PwaContextArgumentResolver;
import uk.co.ogauthority.pwa.mvc.argresolvers.ValidationTypeArgumentResolver;
import uk.co.ogauthority.pwa.util.converters.PwaApplicationTypePathVariableConverterEnumToString;
import uk.co.ogauthority.pwa.util.converters.PwaApplicationTypePathVariableConverterStringToEnum;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

  private final PwaApplicationContextArgumentResolver pwaApplicationContextArgumentResolver;
  private final PwaAppProcessingContextArgumentResolver pwaAppProcessingContextArgumentResolver;
  private final PwaContextArgumentResolver pwaContextArgumentResolver;

  @Autowired
  public WebMvcConfig(PwaApplicationContextArgumentResolver pwaApplicationContextArgumentResolver,
                      PwaAppProcessingContextArgumentResolver pwaAppProcessingContextArgumentResolver,
                      PwaContextArgumentResolver pwaContextArgumentResolver) {
    this.pwaApplicationContextArgumentResolver = pwaApplicationContextArgumentResolver;
    this.pwaAppProcessingContextArgumentResolver = pwaAppProcessingContextArgumentResolver;
    this.pwaContextArgumentResolver = pwaContextArgumentResolver;
  }

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
    resolvers.add(pwaApplicationContextArgumentResolver);
    resolvers.add(pwaAppProcessingContextArgumentResolver);
    resolvers.add(pwaContextArgumentResolver);
    resolvers.add(new AuthenticatedUserAccountArgumentResolver());
    resolvers.add(new ValidationTypeArgumentResolver());
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new ResponseBufferSizeHandlerInterceptor())
        .excludePathPatterns("/assets/**");
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/assets/**")
        .addResourceLocations("classpath:/public/assets/")
        .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS))
        .resourceChain(false)
          .addResolver(new VersionResourceResolver().addContentVersionStrategy("/**"));
  }

  @Bean
  public ResourceUrlEncodingFilter resourceUrlEncodingFilter() {
    return new ResourceUrlEncodingFilter();
  }

  @Override
  public void addFormatters(FormatterRegistry registry) {
    registry.addConverter(new PwaApplicationTypePathVariableConverterStringToEnum());
    registry.addConverter(new PwaApplicationTypePathVariableConverterEnumToString());
  }

}
