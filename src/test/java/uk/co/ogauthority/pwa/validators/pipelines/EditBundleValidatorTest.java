package uk.co.ogauthority.pwa.validators.pipelines;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadBundle;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.BundleForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines.PadBundleRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class EditBundleValidatorTest {

  @Mock
  private PadBundleRepository padBundleRepository;

  @Mock
  private PadPipelineService padPipelineService;

  private EditBundleValidator validator;
  private PwaApplicationDetail pwaApplicationDetail;

  @Before
  public void setUp() {
    validator = new EditBundleValidator(padBundleRepository, padPipelineService);
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
  }

  @Test
  public void validate_passValidation() {
    var form = new BundleForm("name", Set.of(1, 2));
    var bundle = new PadBundle();
    when(padPipelineService.getCountOfPipelinesByIdList(pwaApplicationDetail, List.copyOf(form.getPadPipelineIds())))
        .thenReturn(2L);
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, pwaApplicationDetail, bundle);
    assertThat(errors).isEmpty();
  }

  @Test
  public void validate_noValues() {
    var form = new BundleForm();
    var bundle = new PadBundle();

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, pwaApplicationDetail, bundle);
    assertThat(errors).containsValues(
        Set.of("bundleName" + FieldValidationErrorCodes.REQUIRED.getCode()),
        Set.of("padPipelineIds" + FieldValidationErrorCodes.INVALID.getCode())
    );
  }

  @Test
  public void validate_nameNotUnique() {
    var bundleName = "name";
    var form = new BundleForm(bundleName, Set.of(1, 2));
    var bundle = new PadBundle();
    bundle.setId(1);

    var existingBundle = new PadBundle();
    existingBundle.setBundleName(bundleName);
    existingBundle.setId(2);

    when(padBundleRepository.getAllByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(existingBundle));

    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, pwaApplicationDetail, bundle);
    assertThat(errors).containsValues(
        Set.of("bundleName" + FieldValidationErrorCodes.NOT_UNIQUE.getCode())
    );
  }

  @Test
  public void validate_notEnoughPipelines() {
    var bundle = new PadBundle();
    var form = new BundleForm("name", Set.of(1));
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, pwaApplicationDetail, bundle);
    assertThat(errors).containsValues(
        Set.of("padPipelineIds" + FieldValidationErrorCodes.INVALID.getCode())
    );
  }

  @Test
  public void validate_notAllPipelinesFound() {
    var bundle = new PadBundle();
    var form = new BundleForm("name", Set.of(1, 2));
    when(padPipelineService.getCountOfPipelinesByIdList(pwaApplicationDetail, List.copyOf(form.getPadPipelineIds())))
        .thenReturn(1L);
    var errors = ValidatorTestUtils.getFormValidationErrors(validator, form, pwaApplicationDetail, bundle);
    assertThat(errors).containsValues(
        Set.of("padPipelineIds" + FieldValidationErrorCodes.INVALID.getCode())
    );
  }

}