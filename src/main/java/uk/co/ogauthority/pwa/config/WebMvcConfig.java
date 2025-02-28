package uk.co.ogauthority.pwa.config;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.CacheControl;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.ResourceUrlEncodingFilter;
import org.springframework.web.servlet.resource.VersionResourceResolver;
import uk.co.ogauthority.pwa.component.RequestLogger;
import uk.co.ogauthority.pwa.features.analytics.AnalyticsService;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContextArgumentResolver;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContextArgumentResolver;
import uk.co.ogauthority.pwa.mvc.PwaApplicationRouteInterceptor;
import uk.co.ogauthority.pwa.mvc.ResponseBufferSizeHandlerInterceptor;
import uk.co.ogauthority.pwa.mvc.argresolvers.AuthenticatedUserAccountArgumentResolver;
import uk.co.ogauthority.pwa.mvc.argresolvers.PwaContextArgumentResolver;
import uk.co.ogauthority.pwa.mvc.argresolvers.ValidationTypeArgumentResolver;
import uk.co.ogauthority.pwa.teams.management.access.TeamManagementHandlerInterceptor;
import uk.co.ogauthority.pwa.util.converters.PwaApplicationTypePathVariableConverterEnumToString;
import uk.co.ogauthority.pwa.util.converters.PwaApplicationTypePathVariableConverterStringToEnum;
import uk.co.ogauthority.pwa.util.converters.PwaResourceTypePathVariableConverterEnumToString;
import uk.co.ogauthority.pwa.util.converters.PwaResourceTypePathVariableConverterStringToEnum;
import uk.co.ogauthority.pwa.util.converters.PwaStringToCollectionConverter;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

  private final PwaApplicationContextArgumentResolver pwaApplicationContextArgumentResolver;
  private final PwaAppProcessingContextArgumentResolver pwaAppProcessingContextArgumentResolver;
  private final PwaContextArgumentResolver pwaContextArgumentResolver;
  private final Optional<RequestLogger> requestLoggerOpt;
  private final AnalyticsService analyticsService;
  private final ConfigurableEnvironment configurableEnvironment;
  private final TeamManagementHandlerInterceptor teamManagementHandlerInterceptor;

  @Autowired
  public WebMvcConfig(PwaApplicationContextArgumentResolver pwaApplicationContextArgumentResolver,
                      PwaAppProcessingContextArgumentResolver pwaAppProcessingContextArgumentResolver,
                      PwaContextArgumentResolver pwaContextArgumentResolver,
                      Optional<RequestLogger> requestLoggerOpt,
                      AnalyticsService analyticsService,
                      ConfigurableEnvironment configurableEnvironment,
                      TeamManagementHandlerInterceptor teamManagementHandlerInterceptor) {
    this.pwaApplicationContextArgumentResolver = pwaApplicationContextArgumentResolver;
    this.pwaAppProcessingContextArgumentResolver = pwaAppProcessingContextArgumentResolver;
    this.pwaContextArgumentResolver = pwaContextArgumentResolver;
    this.requestLoggerOpt = requestLoggerOpt;
    this.analyticsService = analyticsService;
    this.configurableEnvironment = configurableEnvironment;
    this.teamManagementHandlerInterceptor = teamManagementHandlerInterceptor;
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

    registry.addInterceptor(new PwaApplicationRouteInterceptor(analyticsService))
        .addPathPatterns("/pwa-application/**");

    registry.addInterceptor(teamManagementHandlerInterceptor)
        .addPathPatterns("/team-management/**");

    requestLoggerOpt.ifPresent(requestLogger -> registry.addInterceptor(requestLogger)
          .excludePathPatterns("/assets/**"));

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
    registry.addConverter(new PwaResourceTypePathVariableConverterStringToEnum());
    registry.addConverter(new PwaResourceTypePathVariableConverterEnumToString());

    // Replace the default StringToCollectionConverter to stop Spring splitting strings containing commas
    // into multiple values.
    registry.removeConvertible(String.class, Collection.class);
    registry.addConverter(new PwaStringToCollectionConverter(configurableEnvironment.getConversionService()));
  }

}
