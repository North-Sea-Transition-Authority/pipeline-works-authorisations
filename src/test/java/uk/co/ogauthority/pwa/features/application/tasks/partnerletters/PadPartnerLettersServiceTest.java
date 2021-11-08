package uk.co.ogauthority.pwa.features.application.tasks.partnerletters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.features.application.files.PadFile;
import uk.co.ogauthority.pwa.features.application.files.PadFileService;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadedFileView;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;


@RunWith(MockitoJUnitRunner.class)
public class PadPartnerLettersServiceTest {

  private PadPartnerLettersService padPartnerLettersService;

  @Mock
  private PwaApplicationDetailService applicationDetailService;

  @Mock
  private PadFileService padFileService;

  private PartnerLettersValidator validator;

  private PwaApplicationDetail pwaApplicationDetail;


  @Before
  public void setUp() {
    validator = new PartnerLettersValidator();
    padPartnerLettersService = new PadPartnerLettersService(applicationDetailService,
        validator, padFileService);
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 100);
  }

  private PartnerLettersForm createValidForm() {
    var form = new PartnerLettersForm();
    form.setPartnerLettersRequired(true);
    form.setPartnerLettersConfirmed(true);
    var uploadedFile = new UploadFileWithDescriptionForm();
    uploadedFile.setUploadedFileDescription("description");
    form.setUploadedFileWithDescriptionForms(List.of(uploadedFile));
    return form;
  }

  private PwaApplicationDetail createValidEntity() {
    PwaApplicationDetail appDetail = new PwaApplicationDetail();
    appDetail.setPartnerLettersRequired(true);
    appDetail.setPartnerLettersConfirmed(true);
    return appDetail;
  }




  @Test
  public void mapEntityToForm_partnerLettersRequired() {
    var actualForm = new PartnerLettersForm();
    padPartnerLettersService.mapEntityToForm(createValidEntity(), actualForm);
    var expectedForm = createValidForm();
    assertThat(actualForm.getPartnerLettersRequired()).isEqualTo(expectedForm.getPartnerLettersRequired());
    assertThat(actualForm.getPartnerLettersConfirmed()).isEqualTo(expectedForm.getPartnerLettersConfirmed());
  }

  @Test
  public void mapEntityToForm_partnerLettersNotRequired() {
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
  public void saveEntityUsingForm_partnerLettersRequired() {
    padPartnerLettersService.saveEntityUsingForm(new PwaApplicationDetail(), createValidForm(), new WebUserAccount());
    verify(padFileService, times(1)).updateFiles(any(), any() ,any(), any(), any());
  }

  @Test
  public void saveEntityUsingForm_partnerLettersNotRequired() {
    when(padFileService.getAllByPwaApplicationDetailAndPurpose(pwaApplicationDetail, ApplicationDetailFilePurpose.PARTNER_LETTERS))
        .thenReturn(List.of(new PadFile(), new PadFile(), new PadFile()));
    var form = new PartnerLettersForm();
    form.setPartnerLettersRequired(false);
    padPartnerLettersService.saveEntityUsingForm(pwaApplicationDetail, form, new WebUserAccount());
    verify(padFileService, times(3)).processFileDeletion(any(), any());
  }

  @Test
  public void getPartnerLettersView_lettersRequired() {
    var appDetail = createValidEntity();
    var uploadedFileViews = List.of(
        new UploadedFileView(null,null,1L,null,null,null),
        new UploadedFileView(null,null,1L,null,null,null));
    when(padFileService.getUploadedFileViews(appDetail, ApplicationDetailFilePurpose.PARTNER_LETTERS,
        ApplicationFileLinkStatus.FULL)).thenReturn(uploadedFileViews);

    var partnerLettersView = padPartnerLettersService.getPartnerLettersView(appDetail);

    assertThat(partnerLettersView.getPartnerLettersRequired()).isTrue();
    assertThat(partnerLettersView.getPartnerLettersConfirmed()).isTrue();
    assertThat(partnerLettersView.getUploadedLetterFileViews()).isEqualTo(uploadedFileViews);
  }

  @Test
  public void getPartnerLettersView_noData() {
    var partnerLettersView = padPartnerLettersService.getPartnerLettersView(pwaApplicationDetail);

    assertThat(partnerLettersView.getPartnerLettersRequired()).isNull();
    assertThat(partnerLettersView.getPartnerLettersConfirmed()).isNull();
    assertThat(partnerLettersView.getUploadedLetterFileViews()).isEmpty();
  }

  @Test
  public void getPartnerLettersView_lettersNotRequired() {
    pwaApplicationDetail.setPartnerLettersRequired(false);
    var partnerLettersView = padPartnerLettersService.getPartnerLettersView(pwaApplicationDetail);

    assertThat(partnerLettersView.getPartnerLettersRequired()).isFalse();
    assertThat(partnerLettersView.getPartnerLettersConfirmed()).isNull();
    assertThat(partnerLettersView.getUploadedLetterFileViews()).isEmpty();
  }


  @Test
  public void validate_isComplete_valid() {
    pwaApplicationDetail.setPartnerLettersRequired(false);
    var isComplete = padPartnerLettersService.isComplete(pwaApplicationDetail);
    assertTrue(isComplete);
  }

  @Test
  public void validate_isComplete_invalid() {
    pwaApplicationDetail.setPartnerLettersRequired(true);
    var isComplete = padPartnerLettersService.isComplete(pwaApplicationDetail);
    assertFalse(isComplete);
  }

  @Test
  public void validate_partialValidation() {
    var inCompleteForm = new PartnerLettersForm();
    var bindingResult = new BeanPropertyBindingResult(inCompleteForm, "empty");
    padPartnerLettersService.validate(inCompleteForm, bindingResult, ValidationType.PARTIAL, pwaApplicationDetail);
    assertFalse(bindingResult.hasErrors());
  }

  @Test
  public void validate_fullValidation_valid() {
    var validForm = createValidForm();
    var bindingResult = new BeanPropertyBindingResult(validForm, "empty");
    padPartnerLettersService.validate(validForm, bindingResult, ValidationType.FULL, pwaApplicationDetail);
    assertFalse(bindingResult.hasErrors());
  }

  @Test
  public void validate_fullValidation_invalid() {
    var invalidForm = new PartnerLettersForm();
    var bindingResult = new BeanPropertyBindingResult(invalidForm, "empty");
    padPartnerLettersService.validate(invalidForm, bindingResult, ValidationType.FULL, pwaApplicationDetail);
    assertTrue(bindingResult.hasErrors());
  }

  @Test
  public void canShowInTaskList_allowed() {

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
  public void canShowInTaskList_notAllowed() {

    var detail = new PwaApplicationDetail();
    var app = new PwaApplication();
    app.setApplicationType(PwaApplicationType.OPTIONS_VARIATION);
    detail.setPwaApplication(app);

    assertThat(padPartnerLettersService.canShowInTaskList(detail)).isFalse();

  }



}