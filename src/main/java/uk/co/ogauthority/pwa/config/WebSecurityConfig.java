package uk.co.ogauthority.pwa.config;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.saml2.core.Saml2X509Credential;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider;
import org.springframework.security.saml2.provider.service.registration.InMemoryRelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.Saml2MessageBinding;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.auth.saml.SamlResponseParser;
import uk.co.ogauthority.pwa.integrations.energyportal.access.EnergyPortalAccessApiConfiguration;
import uk.co.ogauthority.pwa.mvc.PostAuthenticationRequestMdcFilter;
import uk.co.ogauthority.pwa.mvc.RequestLogFilter;

@Configuration
public class WebSecurityConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebSecurityConfig.class);

  private static final String[] NO_AUTH_ENDPOINTS = {
      "/session-info",
      "/maps-test",
      "/notify/callback",
      "/test-controller/type-mismatch-test",
      "/assets/**",
      "/error",
      "/api/v1/logout/*",
      "/analytics/collect",
      "/devtool/**",
      "/actuator/health"
  };

  private final SamlProperties samlProperties;
  private final SamlResponseParser samlResponseParser;
  private final LogoutSuccessHandler serviceLogoutSuccessHandler;
  private final RequestLogFilter requestLogFilter;
  private final PostAuthenticationRequestMdcFilter postAuthenticationRequestMdcFilter;
  private final EnergyPortalAccessApiConfiguration energyPortalAccessApiConfiguration;

  @Autowired
  public WebSecurityConfig(SamlProperties samlProperties,
                           SamlResponseParser samlResponseParser,
                           LogoutSuccessHandler serviceLogoutSuccessHandler,
                           RequestLogFilter requestLogFilter,
                           PostAuthenticationRequestMdcFilter postAuthenticationRequestMdcFilter,
                           EnergyPortalAccessApiConfiguration energyPortalAccessApiConfiguration
  ) {
    this.serviceLogoutSuccessHandler = serviceLogoutSuccessHandler;
    this.samlProperties = samlProperties;
    this.samlResponseParser = samlResponseParser;
    this.requestLogFilter = requestLogFilter;
    this.postAuthenticationRequestMdcFilter = postAuthenticationRequestMdcFilter;
    this.energyPortalAccessApiConfiguration = energyPortalAccessApiConfiguration;
  }

  @Bean
  @Order(2)
  SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {

    httpSecurity
        .authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests

            .requestMatchers(NO_AUTH_ENDPOINTS).permitAll()

            .anyRequest().hasAnyAuthority(
                energyPortalAccessApiConfiguration.privilegeName(),
                String.valueOf(PwaUserPrivilege.PIPELINE_VIEW) // allows external access
            )

        )
        .csrf(csrf -> csrf
            .ignoringRequestMatchers(NO_AUTH_ENDPOINTS)
        )
        .saml2Login(saml2 -> saml2.authenticationManager(getSamlAuthenticationManager()))
        .logout(logoutConfigurer -> logoutConfigurer.logoutSuccessHandler(serviceLogoutSuccessHandler))
        .addFilterBefore(requestLogFilter, SecurityContextHolderFilter.class)
        .addFilterAfter(postAuthenticationRequestMdcFilter, SecurityContextHolderFilter.class);

    return httpSecurity.build();
  }

  private ProviderManager getSamlAuthenticationManager() {
    var authenticationProvider = new OpenSaml4AuthenticationProvider();
    authenticationProvider.setResponseAuthenticationConverter(r -> samlResponseParser.parseSamlResponse(r.getResponse()));
    return new ProviderManager(authenticationProvider);
  }

  @Bean
  protected RelyingPartyRegistrationRepository relyingPartyRegistrations() throws CertificateException {
    var registration = getRelyingPartyRegistration();
    return new InMemoryRelyingPartyRegistrationRepository(registration);
  }

  @Bean
  public RelyingPartyRegistration getRelyingPartyRegistration() throws CertificateException {

    var certificateStream = new ByteArrayInputStream(samlProperties.getCertificate().getBytes(StandardCharsets.UTF_8));

    var certificate = (X509Certificate) CertificateFactory.getInstance("X.509")
        .generateCertificate(certificateStream);

    var credential = Saml2X509Credential.verification(Objects.requireNonNull(certificate));

    return RelyingPartyRegistration
        .withRegistrationId(samlProperties.getRegistrationId())
        .assertingPartyDetails(party -> party
            .entityId(samlProperties.getEntityId())
            .singleSignOnServiceLocation(samlProperties.getLoginUrl())
            .singleSignOnServiceBinding(Saml2MessageBinding.POST)
            .wantAuthnRequestsSigned(false)
            .verificationX509Credentials(c -> c.add(credential))
        )
        .assertionConsumerServiceLocation(samlProperties.getConsumerServiceLocation())
        .build();
  }
}
