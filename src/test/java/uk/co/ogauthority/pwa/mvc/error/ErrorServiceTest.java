package uk.co.ogauthority.pwa.mvc.error;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.config.TechnicalSupportContactProperties;

@RunWith(MockitoJUnitRunner.class)
public class ErrorServiceTest {

  @Mock
  private TechnicalSupportContactProperties technicalSupportContactProperties;

  private ErrorService errorService;

  @Before
  public void setup() {
    errorService = new ErrorService(technicalSupportContactProperties);
  }

  @Test
  public void addErrorAttributesToModel_whenThrowableError_assertExpectedModelAttributes() {
    final var resultingModelMap =  errorService.addErrorAttributesToModel(
        new ModelAndView(),
        new NullPointerException()
    ).getModelMap();

    assertThat(resultingModelMap).containsOnlyKeys(
        "errorRef",
        "technicalSupportContact");
    assertThat(resultingModelMap.get("errorRef")).isNotNull();
    assertCommonModelProperties(resultingModelMap);
  }

  @Test
  public void addErrorAttributesToModel_whenNoThrowableError_assertExpectedModelAttributes() {
    final var resultingModelMap =  errorService.addErrorAttributesToModel(
        new ModelAndView(),
        null
    ).getModelMap();

    assertThat(resultingModelMap).containsOnlyKeys(
        "technicalSupportContact"
    );
    assertCommonModelProperties(resultingModelMap);
  }

  private void assertCommonModelProperties(ModelMap modelMap) {
    assertThat(modelMap.get("technicalSupportContact")).isEqualTo(technicalSupportContactProperties);
  }

}