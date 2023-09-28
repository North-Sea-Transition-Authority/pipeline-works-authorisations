package uk.co.ogauthority.pwa.model.form;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.model.form.pwaapplications.PwaHolderForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.PwaResourceTypeForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.PwaResourceTypeFormValidator;

@RunWith(MockitoJUnitRunner.class)
public class PwaResourceTypeFormValidatorTest {

  private final PwaResourceTypeFormValidator validator = new PwaResourceTypeFormValidator();

  @Test
  public void testSupports() {
    assertTrue(validator.supports(PwaResourceTypeForm.class));
    assertFalse(validator.supports(PwaHolderForm.class));
  }

  @Test
  public void validate_emptyInvalid() {
    var pwaResourceTypeForm = new PwaResourceTypeForm();
    var bindingResult = new BeanPropertyBindingResult(pwaResourceTypeForm, "pwaResourceTypeForm");

    validator.validate(pwaResourceTypeForm, bindingResult);
    assertThat(List.of(bindingResult.getFieldError("resourceType").getCodes())).contains("resourceType.required");

  }

  @Test
  public void validate_valid() {
    var pwaResourceTypeForm = new PwaResourceTypeForm();
    var bindingResult = new BeanPropertyBindingResult(pwaResourceTypeForm, "pwaResourceTypeForm");
    pwaResourceTypeForm.setResourceType(PwaResourceType.HYDROGEN);

    validator.validate(pwaResourceTypeForm, bindingResult);
    assertFalse(bindingResult.hasErrors());
  }
}
