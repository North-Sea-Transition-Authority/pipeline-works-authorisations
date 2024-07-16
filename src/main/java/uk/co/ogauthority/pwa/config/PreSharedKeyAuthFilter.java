package uk.co.ogauthority.pwa.config;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

public class PreSharedKeyAuthFilter extends AbstractPreAuthenticatedProcessingFilter {

  private final String principalRequestHeader;

  public PreSharedKeyAuthFilter(String principalRequestHeader) {
    this.principalRequestHeader = principalRequestHeader;
  }

  @Override
  protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
    String header = request.getHeader(principalRequestHeader);
    return Objects.isNull(header) ? "" : header.replaceAll("Bearer\\s", "");
  }

  @Override
  protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
    // No credentials apply to pre-shared key auth, but this method should not
    // return null in the case of a valid principal, hence return a non-null value.
    return "N/A";
  }
}
