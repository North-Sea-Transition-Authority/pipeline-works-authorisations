package uk.co.ogauthority.pwa.features.application.tasks.crossings.carbonstoragearea;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@ExtendWith(MockitoExtension.class)
class AddCarbonStorageAreaCrossingFormValidatorTest {

  @Mock
  private EditCarbonStorageAreaCrossingFormValidator editValidator;
  @Mock
  private CarbonStorageAreaCrossingService service;

  private AddCarbonStorageAreaFormValidator validator;

  private PwaApplicationDetail detail;

  @BeforeEach
  void setUp() {
    detail = new PwaApplicationDetail();
    validator = new AddCarbonStorageAreaFormValidator(editValidator, service);
  }

  @Test
  void validate_pickedArea_doesExistOnApp() {

    var form = new AddCarbonStorageAreaCrossingForm();
    form.setStorageAreaRef("ref");

    when(service.doesAreaExistOnApp(detail, form.getStorageAreaRef())).thenReturn(true);

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, detail);
    assertThat(errorsMap).contains(
        entry("storageAreaRef", Set.of("storageAreaRef" + FieldValidationErrorCodes.NOT_UNIQUE.getCode()))
    );
  }

  @Test
  void validate_pickedArea_doesNotExistOnApp() {

    var form = new AddCarbonStorageAreaCrossingForm();
    form.setStorageAreaRef("ref");
    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, detail);
    assertThat(errorsMap).doesNotContain(
        entry("pistorageAreaRefckedBlock", Set.of("storageAreaRef" + FieldValidationErrorCodes.NOT_UNIQUE.getCode()))
    );
  }
}
