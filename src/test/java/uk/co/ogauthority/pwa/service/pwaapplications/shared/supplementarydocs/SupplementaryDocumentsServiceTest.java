package uk.co.ogauthority.pwa.service.pwaapplications.shared.supplementarydocs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.List;
import javax.validation.Validation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.files.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.supplementarydocs.SupplementaryDocumentsForm;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class SupplementaryDocumentsServiceTest {

  @Mock
  private PadFileService padFileService;

  @Mock
  private PwaApplicationDetailService detailService;

  private SupplementaryDocumentsService supplementaryDocumentsService;

  private PwaApplicationDetail pwaApplicationDetail;
  private SupplementaryDocumentsForm form;

  @Before
  public void setUp() {

    supplementaryDocumentsService = new SupplementaryDocumentsService(padFileService, detailService);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.OPTIONS_VARIATION);

    form = new SupplementaryDocumentsForm();

  }

  @Test
  public void isComplete() {

    supplementaryDocumentsService.isComplete(pwaApplicationDetail);
    verify(padFileService, times(1)).mapFilesToForm(any(), eq(pwaApplicationDetail), eq(ApplicationDetailFilePurpose.SUPPLEMENTARY_DOCUMENTS));

  }

  @Test
  public void validate_full_noDataEntered() {

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    supplementaryDocumentsService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isTrue();

  }

  @Test
  public void validate_full_noDocsToUpload() {

    form.setHasFilesToUpload(false);
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    supplementaryDocumentsService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isFalse();

  }

  @Test
  public void validate_full_docsToUpload_noDocsProvided() {

    form.setHasFilesToUpload(true);
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    supplementaryDocumentsService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isTrue();

  }

  @Test
  public void validate_full_docsToUpload_docsProvided() {

    form.setHasFilesToUpload(true);
    form.setUploadedFileWithDescriptionForms(List.of(new UploadFileWithDescriptionForm("1", "2", Instant.now())));
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    supplementaryDocumentsService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isFalse();

  }

  @Test
  public void validate_partial_noDataEntered() {

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    supplementaryDocumentsService.validate(form, bindingResult, ValidationType.PARTIAL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isFalse();

  }

  @Test
  public void validate_partial_noDocsToUpload() {

    form.setHasFilesToUpload(false);
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    supplementaryDocumentsService.validate(form, bindingResult, ValidationType.PARTIAL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isFalse();

  }

  @Test
  public void validate_partial_docsToUpload_noDocsProvided() {

    form.setHasFilesToUpload(true);
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    supplementaryDocumentsService.validate(form, bindingResult, ValidationType.PARTIAL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isFalse();

  }

  @Test
  public void validate_partial_docsToUpload_docsProvided() {

    form.setHasFilesToUpload(true);
    form.setUploadedFileWithDescriptionForms(List.of(new UploadFileWithDescriptionForm("1", "2", Instant.now())));
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    supplementaryDocumentsService.validate(form, bindingResult, ValidationType.PARTIAL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isFalse();

  }

  @Test
  public void updateDocumentFlag() {
    form.setHasFilesToUpload(false);
    supplementaryDocumentsService.updateDocumentFlag(pwaApplicationDetail, form);
    verify(detailService, times(1)).setSupplementaryDocumentsFlag(pwaApplicationDetail, form.getHasFilesToUpload());
  }

  @Test
  public void mapSavedDataToForm() {

    pwaApplicationDetail.setSupplementaryDocumentsFlag(false);

    supplementaryDocumentsService.mapSavedDataToForm(pwaApplicationDetail, form);

    form.setHasFilesToUpload(pwaApplicationDetail.getSupplementaryDocumentsFlag());

    verify(padFileService, times(1)).mapFilesToForm(form, pwaApplicationDetail, ApplicationDetailFilePurpose.SUPPLEMENTARY_DOCUMENTS);

  }

}