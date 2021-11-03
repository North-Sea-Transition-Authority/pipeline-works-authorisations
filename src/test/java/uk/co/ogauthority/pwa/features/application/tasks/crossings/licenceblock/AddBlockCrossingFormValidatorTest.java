package uk.co.ogauthority.pwa.features.application.tasks.crossings.licenceblock;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicensing.external.PearsBlock;
import uk.co.ogauthority.pwa.integrations.energyportal.pearslicensing.external.PearsBlockService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class AddBlockCrossingFormValidatorTest {

  @Mock
  private EditBlockCrossingFormValidator editBlockCrossingFormValidator;
  @Mock
  private PearsBlockService pearsBlockService;
  @Mock
  private BlockCrossingService blockCrossingService;

  private AddBlockCrossingFormValidator validator;

  private PwaApplicationDetail detail;

  @Before
  public void setUp() {

    detail = new PwaApplicationDetail();
    validator = new AddBlockCrossingFormValidator(editBlockCrossingFormValidator, pearsBlockService, blockCrossingService);
  }

  @Test
  public void validate_pickedBlock_doesExistOnApp() {

    var form = new AddBlockCrossingForm();
    form.setPickedBlock("ref");
    var pearsBlock = new PearsBlock();

    when(pearsBlockService.getExtantOrUnlicensedOffshorePearsBlockByCompositeKey("ref")).thenReturn(Optional.of(pearsBlock));
    when(blockCrossingService.doesBlockExistOnApp(detail, pearsBlock)).thenReturn(true);

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, detail);
    assertThat(errorsMap).contains(
        entry("pickedBlock", Set.of("pickedBlock" + FieldValidationErrorCodes.NOT_UNIQUE.getCode()))
    );

  }

  @Test
  public void validate_pickedBlock_doesNotExistOnApp() {

    var form = new AddBlockCrossingForm();
    form.setPickedBlock("ref");

    when(pearsBlockService.getExtantOrUnlicensedOffshorePearsBlockByCompositeKey("ref")).thenReturn(Optional.empty());

    Map<String, Set<String>> errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, detail);
    assertThat(errorsMap).doesNotContain(
        entry("pickedBlock", Set.of("pickedBlock" + FieldValidationErrorCodes.NOT_UNIQUE.getCode()))
    );

  }



}
