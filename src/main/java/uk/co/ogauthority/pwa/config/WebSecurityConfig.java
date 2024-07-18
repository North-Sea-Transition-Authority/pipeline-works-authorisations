package uk.co.ogauthority.pwa.config;

import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.savedrequest.RequestCacheAwareFilter;
import uk.co.ogauthority.pwa.auth.FoxLoginCallbackFilter;
import uk.co.ogauthority.pwa.auth.FoxSessionFilter;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.webapp.SystemAreaAccessService;
import uk.co.ogauthority.pwa.service.FoxUrlService;
import uk.co.ogauthority.pwa.service.UserSessionService;

@Configuration
public class WebSecurityConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebSecurityConfig.class);

  private final UserSessionService userSessionService;
  private final FoxUrlService foxUrlService;
  private final FoxLoginCallbackFilter foxLoginCallbackFilter;
  private final SystemAreaAccessService systemAreaAccessService;

  @Autowired
  public WebSecurityConfig(UserSessionService userSessionService, FoxUrlService foxUrlService,
                           FoxLoginCallbackFilter foxLoginCallbackFilter,
                           SystemAreaAccessService systemAreaAccessService) {
    this.userSessionService = userSessionService;
    this.foxUrlService = foxUrlService;
    this.foxLoginCallbackFilter = foxLoginCallbackFilter;
    this.systemAreaAccessService = systemAreaAccessService;
  }

  @Bean
  @Order(2)
  SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
    httpSecurity
        .authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
            .requestMatchers("/work-area/**")
            .hasAnyAuthority(systemAreaAccessService.getValidWorkAreaGrantedAuthorities())

            .requestMatchers("/application-search")
            .hasAnyAuthority(systemAreaAccessService.getValidApplicationSearchGrantedAuthorities())

            .requestMatchers("/consents/search")
            .hasAnyAuthority(systemAreaAccessService.getValidConsentSearchGrantedAuthorities())

            .requestMatchers("/portal-team-management", "/portal-team-management/**")
            .hasAnyAuthority(systemAreaAccessService.getValidTeamManagementGrantedAuthorities())

            .requestMatchers("/create-organisation-team/**")
            .hasAnyAuthority(systemAreaAccessService.getValidCreateOrganisationTeamGrantedAuthorities())

            .requestMatchers("/document-templates/**")
            .hasAnyAuthority(systemAreaAccessService.getValidDocumentTemplateGrantedAuthorities())

            .requestMatchers(
                "/start-application/**",
                "/pwa-application/*/*/pick-pwa-for-application",
                "/pwa-application/create-initial-pwa/**",
                "/pwa-application/*/new",
                "/pwa-application/*/*/variation/new"
            )
            .hasAnyAuthority(systemAreaAccessService.getStartApplicationGrantedAuthorities())

            .requestMatchers("/session-info", "/maps-test", "/notify/callback", "/test-controller/type-mismatch-test")
            .permitAll()

            .requestMatchers("/actuator/*")
            .permitAll()

            .requestMatchers("/assets/**", "/error")
            .permitAll()

            .anyRequest()
            .authenticated()
        )
        .csrf(csrf -> csrf
            .ignoringRequestMatchers(
                "/notify/callback",
                "/analytics/collect"
            )
        )
        .exceptionHandling(exceptionHandling -> exceptionHandling
            .authenticationEntryPoint((request, response, authException) -> {
              LOGGER.warn(
                  "Unauthenticated user attempted to access authenticated resource: '{}' Redirecting to login screen...",
                  request.getRequestURI()
              );
              response.sendRedirect(foxUrlService.getFoxLoginUrl());
            })
        )
        .addFilterBefore(
            new FoxSessionFilter(userSessionService, () -> httpSecurity.getSharedObject(SecurityContextRepository.class)),
            RequestCacheAwareFilter.class
        )
        // The FoxLoginCallbackFilter must be hit before the FoxSessionFilter, otherwise the saved request is wiped
        // when the session is cleared
        .addFilterBefore(foxLoginCallbackFilter, FoxSessionFilter.class);

    return httpSecurity.build();
  }
}
