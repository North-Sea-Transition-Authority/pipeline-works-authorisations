package uk.co.ogauthority.pwa.config;

import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.savedrequest.RequestCacheAwareFilter;
import uk.co.ogauthority.pwa.auth.FoxLoginCallbackFilter;
import uk.co.ogauthority.pwa.auth.FoxSessionFilter;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.webapp.SystemAreaAccessService;
import uk.co.ogauthority.pwa.service.FoxUrlService;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebSecurityConfig.class);

  private final FoxSessionFilter foxSessionFilter;
  private final FoxUrlService foxUrlService;
  private final FoxLoginCallbackFilter foxLoginCallbackFilter;
  private final SystemAreaAccessService systemAreaAccessService;

  @Autowired
  public WebSecurityConfig(FoxSessionFilter foxSessionFilter, FoxUrlService foxUrlService,
                           FoxLoginCallbackFilter foxLoginCallbackFilter,
                           SystemAreaAccessService systemAreaAccessService) {
    this.foxSessionFilter = foxSessionFilter;
    this.foxUrlService = foxUrlService;
    this.foxLoginCallbackFilter = foxLoginCallbackFilter;
    this.systemAreaAccessService = systemAreaAccessService;
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {

    http
      .authorizeRequests()

        .mvcMatchers("/work-area/**")
          .hasAnyAuthority(systemAreaAccessService.getValidWorkAreaGrantedAuthorities())

        .mvcMatchers("/application-search")
          .hasAnyAuthority(systemAreaAccessService.getValidApplicationSearchGrantedAuthorities())

        .mvcMatchers("/consents/search")
        .hasAnyAuthority(systemAreaAccessService.getValidConsentSearchGrantedAuthorities())

        .antMatchers("/portal-team-management", "/portal-team-management/**")
          .hasAnyAuthority(systemAreaAccessService.getValidTeamManagementGrantedAuthorities())

        .antMatchers("/create-organisation-team/**")
          .hasAnyAuthority(systemAreaAccessService.getValidCreateOrganisationTeamGrantedAuthorities())

        .mvcMatchers("/document-templates/**")
          .hasAnyAuthority(systemAreaAccessService.getValidDocumentTemplateGrantedAuthorities())

        .antMatchers(
            "/start-application/**",
            "/pwa-application/**/pick-pwa-for-application",
            "/pwa-application/create-initial-pwa/**",
            "/pwa-application/**/new/**")
          .hasAnyAuthority(systemAreaAccessService.getStartApplicationGrantedAuthorities())

        //all application types
        .mvcMatchers(
            Arrays.stream(PwaApplicationType.values())
                .map(pwaApplicationType -> String.format("/pwa-application/%s/new", pwaApplicationType.getUrlPathString()))
                .toArray(String[]::new))
        .hasAnyAuthority(systemAreaAccessService.getStartApplicationGrantedAuthorities())

        .mvcMatchers(
            Arrays.stream(PwaApplicationType.values())
                .map(pwaApplicationType -> String.format("/pwa-application/%s/new", pwaApplicationType))
                .toArray(String[]::new))
        .hasAnyAuthority(systemAreaAccessService.getStartApplicationGrantedAuthorities())

        .antMatchers("/session-info", "/maps-test", "/notify/callback", "/test-controller/type-mismatch-test")
          .permitAll()

        .antMatchers("/actuator/*")
          .permitAll()

        .mvcMatchers("/assets/**", "/error")
          .permitAll()

        .anyRequest()
          .authenticated();

    http.csrf().ignoringAntMatchers("/notify/callback", "/analytics/collect");

    try {
      // Redirect to FOX for login if the request is unauthenticated.
      http.exceptionHandling()
          .authenticationEntryPoint((request, response, authException) -> {
            LOGGER.warn("Unauthenticated user attempted to access authenticated resource: '{}' Redirecting to login screen...",
                request.getRequestURI());
            response.sendRedirect(foxUrlService.getFoxLoginUrl());
          });

      http.addFilterBefore(foxSessionFilter, RequestCacheAwareFilter.class);

      // The FoxLoginCallbackFilter must be hit before the FoxSessionFilter, otherwise the saved request is wiped
      // when the session is cleared
      http.addFilterBefore(foxLoginCallbackFilter, FoxSessionFilter.class);

    } catch (Exception e) {
      throw new RuntimeException("Failed to configure HttpSecurity", e);
    }

  }

}
