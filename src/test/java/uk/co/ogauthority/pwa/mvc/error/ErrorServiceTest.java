package uk.co.ogauthority.pwa.mvc.error;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.ui.ModelMap;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.config.ServiceProperties;
import uk.co.ogauthority.pwa.config.TechnicalSupportContactProperties;
import uk.co.ogauthority.pwa.exception.ActionNotAllowedException;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.features.analytics.AnalyticsConfigurationProperties;

@ExtendWith(MockitoExtension.class)
class ErrorServiceTest {

  @Mock
  private TechnicalSupportContactProperties technicalSupportContactProperties;

  @Mock
  private ServiceProperties serviceProperties;

  @Mock
  private AnalyticsConfigurationProperties analyticsConfigurationProperties;

  private ErrorService errorService;

  @BeforeEach
  void setup() {
    errorService = new ErrorService(technicalSupportContactProperties, serviceProperties,
        analyticsConfigurationProperties);
  }

  @Test
  void addErrorAttributesToModel_whenThrowableError_assertExpectedModelAttributes() {
    final var resultingModelMap =  errorService.addErrorAttributesToModel(
        new ModelAndView(),
        new NullPointerException()
    ).getModelMap();

    assertThat(resultingModelMap).containsOnlyKeys(
        "errorRef",
        "technicalSupportContact",
        "feedbackUrl",
        "service",
        "cookiePrefsUrl",
        "analyticsMeasurementUrl",
        "analyticsClientIdCookieName",
        "analytics");
    assertThat(resultingModelMap.get("errorRef")).isNotNull();
    assertCommonModelProperties(resultingModelMap);
  }

  @Test
  void addErrorAttributesToModel_whenNoThrowableError_assertExpectedModelAttributes() {
    final var resultingModelMap =  errorService.addErrorAttributesToModel(
        new ModelAndView(),
        null
    ).getModelMap();

    assertThat(resultingModelMap).containsOnlyKeys(
        "technicalSupportContact",
        "feedbackUrl",
        "service",
        "cookiePrefsUrl",
        "analyticsMeasurementUrl",
        "analyticsClientIdCookieName",
        "analytics"
    );
    assertCommonModelProperties(resultingModelMap);
  }

  @Test
  void addErrorAttributesToModel_when4xxResponseStatusAnnotatedException_assertNoErrorReference() {
    final var resultingModelMap = errorService.addErrorAttributesToModel(
        new ModelAndView(),
        new PwaEntityNotFoundException("not found")
    ).getModelMap();

    assertThat(resultingModelMap).doesNotContainKey("errorRef");
    assertCommonModelProperties(resultingModelMap);
  }

  @Test
  void addErrorAttributesToModel_when5xxResponseStatusAnnotatedException_assertErrorReference() {
    final var resultingModelMap = errorService.addErrorAttributesToModel(
        new ModelAndView(),
        new ActionNotAllowedException("not allowed")
    ).getModelMap();

    assertThat(resultingModelMap.get("errorRef")).isNotNull();
    assertCommonModelProperties(resultingModelMap);
  }

  @Test
  void addErrorAttributesToModel_when4xxResponseStatusException_assertNoErrorReference() {
    final var resultingModelMap = errorService.addErrorAttributesToModel(
        new ModelAndView(),
        new ResponseStatusException(HttpStatus.BAD_REQUEST, "bad request")
    ).getModelMap();

    assertThat(resultingModelMap).doesNotContainKey("errorRef");
    assertCommonModelProperties(resultingModelMap);
  }

  @Test
  void addErrorAttributesToModel_when5xxResponseStatusException_assertErrorReference() {
    final var resultingModelMap = errorService.addErrorAttributesToModel(
        new ModelAndView(),
        new ResponseStatusException(HttpStatus.BAD_GATEWAY, "bad gateway")
    ).getModelMap();

    assertThat(resultingModelMap.get("errorRef")).isNotNull();
    assertCommonModelProperties(resultingModelMap);
  }

  private void assertCommonModelProperties(ModelMap modelMap) {
    assertThat(modelMap.get("technicalSupportContact")).isEqualTo(technicalSupportContactProperties);
  }

}