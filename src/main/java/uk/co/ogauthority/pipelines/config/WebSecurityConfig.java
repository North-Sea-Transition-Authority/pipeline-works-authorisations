package uk.co.ogauthority.pipelines.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.savedrequest.RequestCacheAwareFilter;
import uk.co.ogauthority.pipelines.auth.FoxSessionFilter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebSecurityConfig.class);

  private final FoxSessionFilter foxSessionFilter;
  private final String foxLoginUrl;

  @Autowired
  public WebSecurityConfig(FoxSessionFilter foxSessionFilter, @Value("${app.fox.login-url}") String foxLoginUrl) {
    this.foxSessionFilter = foxSessionFilter;
    this.foxLoginUrl = foxLoginUrl;
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {

    http
      .authorizeRequests()

        .antMatchers("/test")
          .permitAll()

        .anyRequest()
          .authenticated();


    try {
      // Redirect to FOX for login if the request is unauthenticated.
      // Note auto-redirect is not currently set up. Would require small changes to the MMO login screen.
      http.exceptionHandling()
          .authenticationEntryPoint((request, response, authException) -> {
            LOGGER.warn("Unauthenticated user attempted to access authenticated resource. Redirecting to login screen...", authException);
            response.sendRedirect(foxLoginUrl);
          });

      http.addFilterBefore(foxSessionFilter, RequestCacheAwareFilter.class);
    } catch (Exception e) {
      throw new RuntimeException("Failed to configure HttpSecurity", e);
    }
  }

  @Override
  public void configure(WebSecurity web) {
    web.ignoring().antMatchers("/assets/**", "/error");
  }


}
