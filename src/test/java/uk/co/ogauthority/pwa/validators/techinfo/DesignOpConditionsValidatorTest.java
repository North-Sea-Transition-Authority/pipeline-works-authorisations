package uk.co.ogauthority.pwa.validators.techinfo;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelinetechinfo.DesignOpConditionsForm;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;
import uk.co.ogauthority.pwa.validators.pipelinetechinfo.PadDesignOpConditionsValidator;

@RunWith(MockitoJUnitRunner.class)
public class DesignOpConditionsValidatorTest {

  private PadDesignOpConditionsValidator validator;

  @Before
  public void setUp() {
    validator = new PadDesignOpConditionsValidator();
  }


  @Test
  public void validate_form_empty() {
    var form = new DesignOpConditionsForm();
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).contains(
        entry("___", Set.of("___.required")),
        entry("___", Set.of("___.required")),
        entry("___", Set.of("___.required")),
        entry("___", Set.of("___.required")),
        entry("___", Set.of("___.required")),
        entry("___", Set.of("___.required")),
        entry("___", Set.of("___.required")),
        entry("___", Set.of("___.required")),
        entry("___", Set.of("___.required")),
        entry("___", Set.of("___.required"))
    );
  }

  @Test
  public void validate_form_valid() {
    var form = new DesignOpConditionsForm();

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form);
    assertThat(errorsMap).isEmpty();
  }






}