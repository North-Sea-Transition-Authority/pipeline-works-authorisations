package uk.co.ogauthority.pwa.config;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.savedrequest.RequestCacheAwareFilter;
import uk.co.ogauthority.pwa.auth.FoxLoginCallbackFilter;
import uk.co.ogauthority.pwa.auth.FoxSessionFilter;
import uk.co.ogauthority.pwa.energyportal.logout.ServiceLogoutSuccessHandler;
import uk.co.ogauthority.pwa.features.webapp.SystemAreaAccessService;
import uk.co.ogauthority.pwa.service.EnergyPortalUrlService;
import uk.co.ogauthority.pwa.service.UserSessionService;

@Configuration
public class WebSecurityConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebSecurityConfig.class);

  private final UserSessionService userSessionService;
  private final EnergyPortalUrlService energyPortalUrlService;
  private final FoxLoginCallbackFilter foxLoginCallbackFilter;
  private final SystemAreaAccessService systemAreaAccessService;
  private final ServiceLogoutSuccessHandler serviceLogoutSuccessHandler;
  private final boolean useEpas;

  @Autowired
  public WebSecurityConfig(UserSessionService userSessionService, EnergyPortalUrlService energyPortalUrlService,
                           FoxLoginCallbackFilter foxLoginCallbackFilter,
                           SystemAreaAccessService systemAreaAccessService,
                           ServiceLogoutSuccessHandler serviceLogoutSuccessHandler,
                           Environment environment) {
    this.userSessionService = userSessionService;
    this.energyPortalUrlService = energyPortalUrlService;
    this.foxLoginCallbackFilter = foxLoginCallbackFilter;
    this.systemAreaAccessService = systemAreaAccessService;
    this.serviceLogoutSuccessHandler = serviceLogoutSuccessHandler;
    this.useEpas = environment.matchesProfiles("use-epas");
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
        .logout(logout -> logout.logoutSuccessHandler(serviceLogoutSuccessHandler))
        .exceptionHandling(exceptionHandling -> exceptionHandling
            .authenticationEntryPoint((request, response, authException) -> {
              LOGGER.warn(
                  "Unauthenticated user attempted to access authenticated resource: '{}' Redirecting to login screen...",
                  request.getRequestURI()
              );

              if (useEpas) {
                var loginUrlWithRelayState = energyPortalUrlService.getLoginUrl() + "?RelayState=" + URLEncoder.encode(
                    String.valueOf(request.getRequestURL()), StandardCharsets.UTF_8);

                response.sendRedirect(loginUrlWithRelayState);
              }  else {
                response.sendRedirect(energyPortalUrlService.getLoginUrl());
              }
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
