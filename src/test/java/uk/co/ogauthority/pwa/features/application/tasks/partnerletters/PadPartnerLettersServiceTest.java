package uk.co.ogauthority.pwa.features.application.tasks.partnerletters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.features.filemanagement.FileManagementValidatorTestUtils;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementService;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadedFileView;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;


@ExtendWith(MockitoExtension.class)
class PadPartnerLettersServiceTest {

  private PadPartnerLettersService padPartnerLettersService;

  @Mock
  private PwaApplicationDetailService applicationDetailService;

  @Mock
  private PadFileManagementService padFileManagementService;

  private PartnerLettersValidator validator;

  private PwaApplicationDetail pwaApplicationDetail;


  @BeforeEach
  void setUp() {
    validator = new PartnerLettersValidator();
    padPartnerLettersService = new PadPartnerLettersService(applicationDetailService,
        validator, padFileManagementService);
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 100);
  }

  private PartnerLettersForm createValidForm() {
    var form = new PartnerLettersForm();
    form.setPartnerLettersRequired(true);
    form.setPartnerLettersConfirmed(true);
    form.setUploadedFiles(List.of(FileManagementValidatorTestUtils.createUploadedFileForm()));
    return form;
  }

  private PwaApplicationDetail createValidEntity() {
    PwaApplicationDetail appDetail = new PwaApplicationDetail();
    appDetail.setPartnerLettersRequired(true);
    appDetail.setPartnerLettersConfirmed(true);
    return appDetail;
  }


  @Test
  void mapEntityToForm_partnerLettersRequired() {
    var actualForm = new PartnerLettersForm();
    padPartnerLettersService.mapEntityToForm(createValidEntity(), actualForm);
    var expectedForm = createValidForm();
    assertThat(actualForm.getPartnerLettersRequired()).isEqualTo(expectedForm.getPartnerLettersRequired());
    assertThat(actualForm.getPartnerLettersConfirmed()).isEqualTo(expectedForm.getPartnerLettersConfirmed());
  }

  @Test
  void mapEntityToForm_partnerLettersNotRequired() {
    var actualForm = new PartnerLettersForm();
    var entity = new PwaApplicationDetail();
    entity.setPartnerLettersRequired(false);
    padPartnerLettersService.mapEntityToForm(entity, actualForm);

    var expectedForm = new PartnerLettersForm();
    expectedForm.setPartnerLettersRequired(false);
    assertThat(actualForm.getPartnerLettersRequired()).isEqualTo(expectedForm.getPartnerLettersRequired());
    assertThat(actualForm.getPartnerLettersConfirmed()).isEqualTo(expectedForm.getPartnerLettersConfirmed());
  }


  @Test
  void saveEntityUsingForm_partnerLettersRequired() {
    padPartnerLettersService.saveEntityUsingForm(new PwaApplicationDetail(), createValidForm(), new WebUserAccount());
    verify(padFileManagementService, times(1)).saveFiles(any(), any() ,any());
  }

  @Test
  void saveEntityUsingForm_partnerLettersNotRequired() {
    when(padFileManagementService.getUploadedFiles(pwaApplicationDetail, FileDocumentType.PARTNER_LETTERS))
        .thenReturn(List.of(new UploadedFile(), new UploadedFile(), new UploadedFile()));
    var form = new PartnerLettersForm();
    form.setPartnerLettersRequired(false);
    padPartnerLettersService.saveEntityUsingForm(pwaApplicationDetail, form, new WebUserAccount());
    verify(padFileManagementService, times(3)).deleteUploadedFile(any());
  }

  @Test
  void getPartnerLettersView_lettersRequired() {
    var appDetail = createValidEntity();
    var uploadedFileViews = List.of(
        new UploadedFileView(null,null,1L,null,null,null),
        new UploadedFileView(null,null,1L,null,null,null));
    when(padFileManagementService.getUploadedFileViews(appDetail, FileDocumentType.PARTNER_LETTERS)).thenReturn(uploadedFileViews);

    var partnerLettersView = padPartnerLettersService.getPartnerLettersView(appDetail);

    assertThat(partnerLettersView.getPartnerLettersRequired()).isTrue();
    assertThat(partnerLettersView.getPartnerLettersConfirmed()).isTrue();
    assertThat(partnerLettersView.getUploadedLetterFileViews()).isEqualTo(uploadedFileViews);
  }

  @Test
  void getPartnerLettersView_noData() {
    var partnerLettersView = padPartnerLettersService.getPartnerLettersView(pwaApplicationDetail);

    assertThat(partnerLettersView.getPartnerLettersRequired()).isNull();
    assertThat(partnerLettersView.getPartnerLettersConfirmed()).isNull();
    assertThat(partnerLettersView.getUploadedLetterFileViews()).isEmpty();
  }

  @Test
  void getPartnerLettersView_lettersNotRequired() {
    pwaApplicationDetail.setPartnerLettersRequired(false);
    var partnerLettersView = padPartnerLettersService.getPartnerLettersView(pwaApplicationDetail);

    assertThat(partnerLettersView.getPartnerLettersRequired()).isFalse();
    assertThat(partnerLettersView.getPartnerLettersConfirmed()).isNull();
    assertThat(partnerLettersView.getUploadedLetterFileViews()).isEmpty();
  }


  @Test
  void validate_isComplete_valid() {
    pwaApplicationDetail.setPartnerLettersRequired(false);
    var isComplete = padPartnerLettersService.isComplete(pwaApplicationDetail);
    assertTrue(isComplete);
  }

  @Test
  void validate_isComplete_invalid() {
    pwaApplicationDetail.setPartnerLettersRequired(true);
    var isComplete = padPartnerLettersService.isComplete(pwaApplicationDetail);
    assertFalse(isComplete);
  }

  @Test
  void validate_partialValidation() {
    var inCompleteForm = new PartnerLettersForm();
    var bindingResult = new BeanPropertyBindingResult(inCompleteForm, "empty");
    padPartnerLettersService.validate(inCompleteForm, bindingResult, ValidationType.PARTIAL, pwaApplicationDetail);
    assertFalse(bindingResult.hasErrors());
  }

  @Test
  void validate_fullValidation_valid() {
    var validForm = createValidForm();
    var bindingResult = new BeanPropertyBindingResult(validForm, "empty");
    padPartnerLettersService.validate(validForm, bindingResult, ValidationType.FULL, pwaApplicationDetail);
    assertFalse(bindingResult.hasErrors());
  }

  @Test
  void validate_fullValidation_invalid() {
    var invalidForm = new PartnerLettersForm();
    var bindingResult = new BeanPropertyBindingResult(invalidForm, "empty");
    padPartnerLettersService.validate(invalidForm, bindingResult, ValidationType.FULL, pwaApplicationDetail);
    assertTrue(bindingResult.hasErrors());
  }

  @Test
  void canShowInTaskList_allowed() {

    var detail = new PwaApplicationDetail();
    var app = new PwaApplication();
    detail.setPwaApplication(app);

    PwaApplicationType.stream()
        .filter(type -> !type.equals(PwaApplicationType.OPTIONS_VARIATION))
        .forEach(applicationType -> {

          app.setApplicationType(applicationType);

          assertThat(padPartnerLettersService.canShowInTaskList(detail)).isTrue();

        });

  }

  @Test
  void canShowInTaskList_notAllowed() {

    var detail = new PwaApplicationDetail();
    var app = new PwaApplication();
    app.setApplicationType(PwaApplicationType.OPTIONS_VARIATION);
    detail.setPwaApplication(app);

    assertThat(padPartnerLettersService.canShowInTaskList(detail)).isFalse();

  }



}