package uk.co.ogauthority.pwa.service.pwaapplications.routing;

import java.util.Objects;

/**
 * Captures an application specific landing page after routing logic.
 */
public final class ApplicationLandingPageInstance {
  private final ApplicationLandingPage applicationLandingPage;
  private final String url;

  ApplicationLandingPageInstance(ApplicationLandingPage applicationLandingPage, String url) {
    this.applicationLandingPage = applicationLandingPage;
    this.url = url;
  }

  public ApplicationLandingPage getApplicationLandingPage() {
    return applicationLandingPage;
  }

  public String getUrl() {
    return url;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApplicationLandingPageInstance that = (ApplicationLandingPageInstance) o;
    return applicationLandingPage == that.applicationLandingPage && Objects.equals(url, that.url);
  }

  @Override
  public int hashCode() {
    return Objects.hash(applicationLandingPage, url);
  }
}
