package uk.co.ogauthority.pwa.validators.consultations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationResponseDataForm;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationResponseForm;
import uk.co.ogauthority.pwa.model.form.enums.ConsultationResponseOption;
import uk.co.ogauthority.pwa.model.form.enums.ConsultationResponseOptionGroup;
import uk.co.ogauthority.pwa.model.form.files.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.service.enums.validation.FieldValidationErrorCodes;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class ConsultationResponseValidatorTest {

  private ConsultationResponseValidator validator;

  @Mock
  private ConsultationResponseDataValidator consultationResponseDataValidator;

  private final ConsultationResponseForm form = new ConsultationResponseForm();
  private final ConsultationResponseDataForm dataForm1 = new ConsultationResponseDataForm();
  private final ConsultationResponseDataForm dataForm2 = new ConsultationResponseDataForm();
  private final UploadFileWithDescriptionForm uploadedFileForm = new UploadFileWithDescriptionForm();

  @Before
  public void setUp() {
    validator = new ConsultationResponseValidator(consultationResponseDataValidator);

    uploadedFileForm.setUploadedFileId("file id 1");

    when(consultationResponseDataValidator.supports(dataForm1.getClass())).thenReturn(true);
  }

  @Test
  public void validate_validateFileUploads_eia_agree_noDocumentsUploaded_fail() {

    dataForm1.setConsultationResponseOption(ConsultationResponseOption.EIA_AGREE);
    form.setResponseDataForms(Map.of(ConsultationResponseOptionGroup.EIA_REGS, dataForm1));

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ConsultationResponseOptionGroup.EIA_REGS);
    assertThat(errorsMap)
        .extractingFromEntries(Map.Entry::getKey, Map.Entry::getValue)
        .containsExactly(
            tuple(
                "uploadedFileWithDescriptionForms",
                Set.of("uploadedFileWithDescriptionForms" + FieldValidationErrorCodes.MIN_FILE_COUNT_NOT_REACHED.getCode()))
        );
  }

  @Test
  public void validate_validateFileUploads_eia_agree_documentUploaded_pass() {

    dataForm1.setConsultationResponseOption(ConsultationResponseOption.EIA_AGREE);
    form.setResponseDataForms(Map.of(ConsultationResponseOptionGroup.EIA_REGS, dataForm1));
    form.setUploadedFileWithDescriptionForms(List.of(uploadedFileForm));

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ConsultationResponseOptionGroup.EIA_REGS);
    assertThat(errorsMap).isEmpty();
  }

  @Test
  public void validate_validateFileUploads_eiaAndHabitats_agree_notEnoughDocumentsUploaded_fail() {

    dataForm1.setConsultationResponseOption(ConsultationResponseOption.EIA_AGREE);
    dataForm2.setConsultationResponseOption(ConsultationResponseOption.HABITATS_DISAGREE);
    form.setResponseDataForms(Map.of(
        ConsultationResponseOptionGroup.EIA_REGS, dataForm1,
        ConsultationResponseOptionGroup.HABITATS_REGS, dataForm2));
    form.setUploadedFileWithDescriptionForms(List.of(uploadedFileForm));

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ConsultationResponseOptionGroup.EIA_REGS);
    assertThat(errorsMap).extractingFromEntries(Map.Entry::getKey, Map.Entry::getValue)
        .containsExactly(
            tuple(
                "uploadedFileWithDescriptionForms",
                Set.of("uploadedFileWithDescriptionForms" + FieldValidationErrorCodes.MIN_FILE_COUNT_NOT_REACHED.getCode()))
        );
  }

}