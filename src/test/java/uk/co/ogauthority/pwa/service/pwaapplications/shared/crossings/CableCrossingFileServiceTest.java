package uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.files.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.crossings.CrossingDocumentsForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadCableCrossingRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class CableCrossingFileServiceTest {

  @Mock
  private PadCableCrossingRepository padCableCrossingRepository;

  @Mock
  private PadFileService padFileService;

  private CableCrossingFileService cableCrossingFileService;

  private PwaApplicationDetail pwaApplicationDetail;

  private CrossingDocumentsForm form = new CrossingDocumentsForm();

  @Before
  public void setUp() {

    cableCrossingFileService = new CableCrossingFileService(padCableCrossingRepository, padFileService);

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
  }

  @Test
  public void validate_full_whenNoDocumentRequired_andDocumentProvidedWithDescription() {

    form.setUploadedFileWithDescriptionForms(List.of(new UploadFileWithDescriptionForm("1", "2", Instant.now())));
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    cableCrossingFileService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isFalse();
  }

  @Test
  public void validate_full_whenNoDocumentRequired_andDocumentProvidedWithoutDescription() {

    form.setUploadedFileWithDescriptionForms(List.of(new UploadFileWithDescriptionForm("1", "", Instant.now())));
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    cableCrossingFileService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isTrue();
  }

  @Test
  public void validate_full_whenDocumentRequired_andZeroDocuments() {
    when(padCableCrossingRepository.countAllByPwaApplicationDetail(eq(pwaApplicationDetail))).thenReturn(1);

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    cableCrossingFileService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isTrue();
  }

  @Test
  public void validate_full_whenDocumentRequired_andDocumentWithDescriptionProvided() {
    when(padCableCrossingRepository.countAllByPwaApplicationDetail(eq(pwaApplicationDetail))).thenReturn(1);
    form.setUploadedFileWithDescriptionForms(List.of(new UploadFileWithDescriptionForm("1", "desc", Instant.now())));
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    cableCrossingFileService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isFalse();
  }

  @Test
  public void validate_partial_whenDocumentWithoutDescriptionProvided() {
    form.setUploadedFileWithDescriptionForms(List.of(new UploadFileWithDescriptionForm("1", "", Instant.now())));
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    cableCrossingFileService.validate(form, bindingResult, ValidationType.PARTIAL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isTrue();
  }

  @Test
  public void validate_partial_whenDocumentWithDescriptionProvided() {
    form.setUploadedFileWithDescriptionForms(List.of(new UploadFileWithDescriptionForm("1", "desc", Instant.now())));
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    cableCrossingFileService.validate(form, bindingResult, ValidationType.PARTIAL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isFalse();
  }

  @Test
  public void validate_full_existingDocumentDeleted_newDocumentAdded_noErrors() {

    var existingDocumentDeleted = new UploadFileWithDescriptionForm(null, null, null);
    var newDocAdded = new UploadFileWithDescriptionForm("1", "new", Instant.now());
    form.setUploadedFileWithDescriptionForms(List.of(existingDocumentDeleted, newDocAdded));

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    cableCrossingFileService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isFalse();

  }

  @Test
  public void validate_full_existingDocumentDeleted_newDocumentAdded_noDescription_error() {

    var existingDocumentDeleted = new UploadFileWithDescriptionForm(null, null, null);
    var newDocAdded = new UploadFileWithDescriptionForm("1", null, Instant.now());
    form.setUploadedFileWithDescriptionForms(List.of(existingDocumentDeleted, newDocAdded));

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    cableCrossingFileService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);

    assertThat(bindingResult.hasErrors()).isTrue();

  }

  @Test
  public void isComplete_serviceInteraction() {
    var result = cableCrossingFileService.isComplete(pwaApplicationDetail);
    verify(padFileService, times(1)).mapFilesToForm(any(), eq(pwaApplicationDetail), eq(ApplicationDetailFilePurpose.CABLE_CROSSINGS));
    assertThat(result).isTrue();
  }

}