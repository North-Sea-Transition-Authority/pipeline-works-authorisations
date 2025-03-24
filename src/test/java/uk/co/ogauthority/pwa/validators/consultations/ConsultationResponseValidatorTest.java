package uk.co.ogauthority.pwa.validators.consultations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.ogauthority.pwa.features.filemanagement.FileManagementValidatorTestUtils;
import uk.co.ogauthority.pwa.features.filemanagement.FileValidationUtils;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationResponseDataForm;
import uk.co.ogauthority.pwa.model.form.consultation.ConsultationResponseForm;
import uk.co.ogauthority.pwa.model.form.enums.ConsultationResponseOption;
import uk.co.ogauthority.pwa.model.form.enums.ConsultationResponseOptionGroup;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;


@ExtendWith(MockitoExtension.class)
class ConsultationResponseValidatorTest {

  private ConsultationResponseValidator validator;

  @Mock
  private ConsultationResponseDataValidator consultationResponseDataValidator;

  private final ConsultationResponseForm form = new ConsultationResponseForm();
  private final ConsultationResponseDataForm dataForm1 = new ConsultationResponseDataForm();
  private final ConsultationResponseDataForm dataForm2 = new ConsultationResponseDataForm();
  private final UploadFileWithDescriptionForm uploadedFileForm = new UploadFileWithDescriptionForm();

  @BeforeEach
  void setUp() {
    validator = new ConsultationResponseValidator(consultationResponseDataValidator);

    uploadedFileForm.setUploadedFileId("file id 1");

    when(consultationResponseDataValidator.supports(dataForm1.getClass())).thenReturn(true);
  }

  @Test
  void validate_validateFileUploads_eia_agree_noDocumentsUploaded_fail() {

    dataForm1.setConsultationResponseOption(ConsultationResponseOption.EIA_AGREE);
    form.setResponseDataForms(Map.of(ConsultationResponseOptionGroup.EIA_REGS, dataForm1));

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ConsultationResponseOptionGroup.EIA_REGS);
    assertThat(errorsMap)
        .extractingFromEntries(Map.Entry::getKey, Map.Entry::getValue)
        .containsExactly(
            tuple(
                "uploadedFiles",
                Set.of(FileValidationUtils.BELOW_THRESHOLD_ERROR_CODE))
        );
  }

  @Test
  void validate_validateFileUploads_eia_agree_documentUploaded_pass() {

    dataForm1.setConsultationResponseOption(ConsultationResponseOption.EIA_AGREE);
    form.setResponseDataForms(Map.of(ConsultationResponseOptionGroup.EIA_REGS, dataForm1));
    form.setUploadedFiles(List.of(FileManagementValidatorTestUtils.createUploadedFileForm()));

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ConsultationResponseOptionGroup.EIA_REGS);
    assertThat(errorsMap).isEmpty();
  }

  @Test
  void validate_validateFileUploads_eiaAndHabitats_agree_DocumentUploaded_pass() {

    dataForm1.setConsultationResponseOption(ConsultationResponseOption.EIA_AGREE);
    dataForm2.setConsultationResponseOption(ConsultationResponseOption.HABITATS_AGREE);
    form.setResponseDataForms(Map.of(
        ConsultationResponseOptionGroup.EIA_REGS, dataForm1,
        ConsultationResponseOptionGroup.HABITATS_REGS, dataForm2));
    form.setUploadedFiles(List.of(FileManagementValidatorTestUtils.createUploadedFileForm()));

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ConsultationResponseOptionGroup.EIA_REGS);
    assertThat(errorsMap).isEmpty();
  }

  @Test
  void validate_validateFileUploads_eiaAndHabitats_agree_noDocumentsUploaded_fail() {

    dataForm1.setConsultationResponseOption(ConsultationResponseOption.EIA_AGREE);
    dataForm2.setConsultationResponseOption(ConsultationResponseOption.HABITATS_AGREE);
    form.setResponseDataForms(Map.of(
        ConsultationResponseOptionGroup.EIA_REGS, dataForm1,
        ConsultationResponseOptionGroup.HABITATS_REGS, dataForm2));

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ConsultationResponseOptionGroup.EIA_REGS);
    assertThat(errorsMap)
        .extractingFromEntries(Map.Entry::getKey, Map.Entry::getValue)
        .containsExactly(
            tuple(
                "uploadedFiles",
                Set.of(FileValidationUtils.BELOW_THRESHOLD_ERROR_CODE))
        );
  }

  @Test
  void validate_validateFileUploads_eia_disagree_documentNotUploaded_pass() {

    dataForm1.setConsultationResponseOption(ConsultationResponseOption.EIA_DISAGREE);
    form.setResponseDataForms(Map.of(ConsultationResponseOptionGroup.EIA_REGS, dataForm1));
    form.setUploadedFiles(List.of(FileManagementValidatorTestUtils.createUploadedFileForm()));

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ConsultationResponseOptionGroup.EIA_REGS);
    assertThat(errorsMap).isEmpty();
  }

  @Test
  void validate_validateFileUploads_habitats_disagree_documentNotUploaded_pass() {

    dataForm1.setConsultationResponseOption(ConsultationResponseOption.HABITATS_DISAGREE);
    form.setResponseDataForms(Map.of(ConsultationResponseOptionGroup.HABITATS_REGS, dataForm1));
    form.setUploadedFiles(List.of(FileManagementValidatorTestUtils.createUploadedFileForm()));

    var errorsMap = ValidatorTestUtils.getFormValidationErrors(validator, form, ConsultationResponseOptionGroup.HABITATS_REGS);
    assertThat(errorsMap).isEmpty();
  }

}