package uk.co.ogauthority.pwa.service.documents.clauses;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.form.documents.ClauseForm;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class ClauseFormValidatorTest {

  private final ClauseFormValidator validator = new ClauseFormValidator();

  @Test
  public void validate_emptyForm() {

    var form = new ClauseForm();

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form);

    assertThat(errors).containsOnly(entry("name", Set.of("name.required")));

  }

  @Test
  public void validate_noText() {

    var form = new ClauseForm();
    form.setName("name");

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form);

    assertThat(errors).isEmpty();

  }

  @Test
  public void validate_withText() {

    var form = new ClauseForm();
    form.setName("name");
    form.setText("text");

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form);

    assertThat(errors).isEmpty();

  }

}