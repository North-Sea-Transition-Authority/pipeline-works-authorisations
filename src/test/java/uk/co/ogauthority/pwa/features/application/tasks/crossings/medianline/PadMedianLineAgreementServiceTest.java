package uk.co.ogauthority.pwa.features.application.tasks.crossings.medianline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileViewTestUtil;
import uk.co.ogauthority.pwa.service.entitycopier.EntityCopyingService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.util.validationgroups.FullValidation;

@ExtendWith(MockitoExtension.class)
class PadMedianLineAgreementServiceTest {

  @Mock
  private PadMedianLineAgreementRepository padMedianLineAgreementRepository;

  @Mock
  private MedianLineAgreementValidator medianLineAgreementValidator;

  @Mock
  private MedianLineCrossingFileService medianLineCrossingFileService;

  @Mock
  private PadFileManagementService padFileManagementService;

  @Mock
  private EntityCopyingService entityCopyingService;

  private PadMedianLineAgreementService padMedianLineAgreementService;

  private PwaApplicationDetail pwaApplicationDetail;

  @BeforeEach
  void setUp() {
    padMedianLineAgreementService = new PadMedianLineAgreementService(
        padMedianLineAgreementRepository,
        medianLineAgreementValidator,
        medianLineCrossingFileService,
        entityCopyingService,
        padFileManagementService
    );
    pwaApplicationDetail = new PwaApplicationDetail();
  }

  @Test
  void getMedianLineAgreementForDraft_WithExisting() {
    var agreement = new PadMedianLineAgreement();
    when(padMedianLineAgreementRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(
        Optional.of(agreement));
    var result = padMedianLineAgreementService.getMedianLineAgreement(pwaApplicationDetail);
    assertThat(result).isEqualTo(agreement);
  }

  @Test
  void getMedianLineAgreementForDraft_NoneExisting() {
    when(padMedianLineAgreementRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(
        Optional.empty());
    var result = padMedianLineAgreementService.getMedianLineAgreement(pwaApplicationDetail);
    assertThat(result.getPwaApplicationDetail()).isEqualTo(pwaApplicationDetail);
  }

  @Test
  void save() {
    var agreement = new PadMedianLineAgreement();
    padMedianLineAgreementService.save(agreement);
    verify(padMedianLineAgreementRepository, times(1)).save(agreement);
  }

  @Test
  void mapEntityToForm_Nulls() {
    var form = new MedianLineAgreementsForm();
    var entity = new PadMedianLineAgreement();
    padMedianLineAgreementService.mapEntityToForm(entity, form);
    assertThat(form.getNegotiatorNameIfOngoing()).isNull();
    assertThat(form.getNegotiatorEmailIfOngoing()).isNull();
    assertThat(form.getNegotiatorNameIfCompleted()).isNull();
    assertThat(form.getNegotiatorEmailIfCompleted()).isNull();
    assertThat(form.getAgreementStatus()).isNull();
  }

  @Test
  void mapEntityToForm_NotCrossed() {
    var form = new MedianLineAgreementsForm();
    var entity = new PadMedianLineAgreement();
    entity.setAgreementStatus(MedianLineStatus.NOT_CROSSED);
    entity.setNegotiatorName("NOT CROSSED");
    entity.setNegotiatorEmail("not@crossed");
    padMedianLineAgreementService.mapEntityToForm(entity, form);
    assertThat(form.getNegotiatorNameIfOngoing()).isNull();
    assertThat(form.getNegotiatorEmailIfOngoing()).isNull();
    assertThat(form.getNegotiatorNameIfCompleted()).isNull();
    assertThat(form.getNegotiatorEmailIfCompleted()).isNull();
    assertThat(form.getAgreementStatus()).isEqualTo(entity.getAgreementStatus());
  }

  @Test
  void mapEntityToForm_Ongoing() {
    var form = new MedianLineAgreementsForm();
    var entity = new PadMedianLineAgreement();
    entity.setAgreementStatus(MedianLineStatus.NEGOTIATIONS_ONGOING);
    entity.setNegotiatorName("ONGOING");
    entity.setNegotiatorEmail("on@going");
    padMedianLineAgreementService.mapEntityToForm(entity, form);
    assertThat(form.getNegotiatorNameIfOngoing()).isEqualTo(entity.getNegotiatorName());
    assertThat(form.getNegotiatorEmailIfOngoing()).isEqualTo(entity.getNegotiatorEmail());
    assertThat(form.getNegotiatorNameIfCompleted()).isNull();
    assertThat(form.getNegotiatorEmailIfCompleted()).isNull();
    assertThat(form.getAgreementStatus()).isEqualTo(entity.getAgreementStatus());
  }

  @Test
  void mapEntityToForm_Completed() {
    var form = new MedianLineAgreementsForm();
    var entity = new PadMedianLineAgreement();
    entity.setAgreementStatus(MedianLineStatus.NEGOTIATIONS_COMPLETED);
    entity.setNegotiatorName("COMPLETED");
    entity.setNegotiatorEmail("completed@test");
    padMedianLineAgreementService.mapEntityToForm(entity, form);
    assertThat(form.getNegotiatorNameIfOngoing()).isNull();
    assertThat(form.getNegotiatorEmailIfOngoing()).isNull();
    assertThat(form.getNegotiatorNameIfCompleted()).isEqualTo(entity.getNegotiatorName());
    assertThat(form.getNegotiatorEmailIfCompleted()).isEqualTo(entity.getNegotiatorEmail());
    assertThat(form.getAgreementStatus()).isEqualTo(entity.getAgreementStatus());
  }

  @Test
  void saveEntityUsingForm_Nulls() {
    var form = new MedianLineAgreementsForm();
    var entity = new PadMedianLineAgreement();
    padMedianLineAgreementService.saveEntityUsingForm(entity, form);
    assertThat(entity.getAgreementStatus()).isNull();
    assertThat(entity.getNegotiatorName()).isNull();
    assertThat(entity.getNegotiatorEmail()).isNull();
  }

  @Test
  void saveEntityUsingForm_NotCrossed() {
    var form = new MedianLineAgreementsForm();
    form.setAgreementStatus(MedianLineStatus.NOT_CROSSED);
    form.setNegotiatorNameIfOngoing("Ongoing name");
    form.setNegotiatorEmailIfOngoing("Ongoing email");
    form.setNegotiatorNameIfCompleted("Completed name");
    form.setNegotiatorEmailIfCompleted("Completed email");
    var entity = new PadMedianLineAgreement();
    padMedianLineAgreementService.saveEntityUsingForm(entity, form);
    assertThat(entity.getAgreementStatus()).isEqualTo(form.getAgreementStatus());
    assertThat(entity.getNegotiatorName()).isNull();
    assertThat(entity.getNegotiatorEmail()).isNull();
  }

  @Test
  void saveEntityUsingForm_Ongoing() {
    var form = new MedianLineAgreementsForm();
    form.setAgreementStatus(MedianLineStatus.NEGOTIATIONS_ONGOING);
    form.setNegotiatorNameIfOngoing("Ongoing name");
    form.setNegotiatorEmailIfOngoing("Ongoing email");
    form.setNegotiatorNameIfCompleted("Completed name");
    form.setNegotiatorEmailIfCompleted("Completed email");
    var entity = new PadMedianLineAgreement();
    padMedianLineAgreementService.saveEntityUsingForm(entity, form);
    assertThat(entity.getAgreementStatus()).isEqualTo(form.getAgreementStatus());
    assertThat(entity.getNegotiatorName()).isEqualTo(form.getNegotiatorNameIfOngoing());
    assertThat(entity.getNegotiatorEmail()).isEqualTo(form.getNegotiatorEmailIfOngoing());
  }

  @Test
  void saveEntityUsingForm_Completed() {
    var form = new MedianLineAgreementsForm();
    form.setAgreementStatus(MedianLineStatus.NEGOTIATIONS_COMPLETED);
    form.setNegotiatorNameIfOngoing("Ongoing name");
    form.setNegotiatorEmailIfOngoing("Ongoing email");
    form.setNegotiatorNameIfCompleted("Completed name");
    form.setNegotiatorEmailIfCompleted("Completed email");
    var entity = new PadMedianLineAgreement();
    padMedianLineAgreementService.saveEntityUsingForm(entity, form);
    assertThat(entity.getAgreementStatus()).isEqualTo(form.getAgreementStatus());
    assertThat(entity.getNegotiatorName()).isEqualTo(form.getNegotiatorNameIfCompleted());
    assertThat(entity.getNegotiatorEmail()).isEqualTo(form.getNegotiatorEmailIfCompleted());
  }

  @Test
  void isComplete_serviceInteractions_whenAgreementValid() {
    var agreement = new PadMedianLineAgreement();
    when(padMedianLineAgreementRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(
        Optional.of(agreement));

    padMedianLineAgreementService.isComplete(pwaApplicationDetail);

    verify(medianLineAgreementValidator, times(1)).validate(any(), any(), eq(FullValidation.class));
    verify(medianLineCrossingFileService, times(1)).isComplete(pwaApplicationDetail);
  }

  @Test
  void isComplete_serviceInteractions_whenAgreementInvalid() {
    var agreement = new PadMedianLineAgreement();
    when(padMedianLineAgreementRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(
        Optional.of(agreement));

    doAnswer(invocation -> {
      var errors = (Errors) invocation.getArgument(1);
      errors.rejectValue("agreementStatus", "agreementStatus.bad", "agreementStatus bad");
      return invocation;
    }).when(medianLineAgreementValidator).validate(any(), any(), any(Object[].class));

    padMedianLineAgreementService.isComplete(pwaApplicationDetail);

    verify(medianLineAgreementValidator, times(1)).validate(any(), any(), eq(FullValidation.class));
    verify(medianLineCrossingFileService, times(0)).isComplete(pwaApplicationDetail);
  }

  @Test
  void isMedianLineAgreementFormComplete_whenAgreementInvalid() {
    var agreement = new PadMedianLineAgreement();
    when(padMedianLineAgreementRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(
        Optional.of(agreement));

    doAnswer(invocation -> {
      var errors = (Errors) invocation.getArgument(1);
      errors.rejectValue("agreementStatus", "agreementStatus.bad", "agreementStatus bad");
      return invocation;
    }).when(medianLineAgreementValidator).validate(any(), any(), any(Object[].class));

    assertThat(padMedianLineAgreementService.isMedianLineAgreementFormComplete(pwaApplicationDetail)).isFalse();

    verifyNoInteractions(medianLineCrossingFileService);
  }

  @Test
  void isMedianLineAgreementFormComplete_whenAgreementValid() {
    var agreement = new PadMedianLineAgreement();
    when(padMedianLineAgreementRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(
        Optional.of(agreement));

    assertThat(padMedianLineAgreementService.isMedianLineAgreementFormComplete(pwaApplicationDetail)).isTrue();

    verifyNoInteractions(medianLineCrossingFileService);
  }


  @Test
  void validate_serviceInteractions_whenFullValidation() {
    var form = new MedianLineAgreementsForm();
    var errors = new BeanPropertyBindingResult(form, "form");
    padMedianLineAgreementService.validate(form, errors, ValidationType.FULL, pwaApplicationDetail);

    verify(medianLineAgreementValidator, times(1)).validate(any(), any(), eq(FullValidation.class));
  }

  @Test
  void validate_serviceInteractions_whenPartialValidation() {
    var form = new MedianLineAgreementsForm();
    var errors = new BeanPropertyBindingResult(form, "form");
    padMedianLineAgreementService.validate(form, errors, ValidationType.PARTIAL, pwaApplicationDetail);

    verify(medianLineAgreementValidator, times(1)).validate(any(), any());
  }

  @Test
  void getMedianLineCrossingView_whenPadMedianLineAgreementFound_mapsData() {

    var agreement = new PadMedianLineAgreement();
    agreement.setNegotiatorName("NAME");
    agreement.setNegotiatorEmail("EMAIL");
    agreement.setAgreementStatus(MedianLineStatus.NEGOTIATIONS_COMPLETED);

    var fileViews = List.of(UploadedFileViewTestUtil.createDefaultFileView());
    when(padFileManagementService.getUploadedFileViews(pwaApplicationDetail, FileDocumentType.MEDIAN_LINE_CROSSING)).thenReturn(fileViews);

    when(padMedianLineAgreementRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(
        Optional.of(agreement));

    var view = padMedianLineAgreementService.getMedianLineCrossingView(pwaApplicationDetail);

    assertThat(view.getAgreementStatus()).isEqualTo(agreement.getAgreementStatus());
    assertThat(view.getNegotiatorEmail()).isEqualTo(agreement.getNegotiatorEmail());
    assertThat(view.getNegotiatorName()).isEqualTo(agreement.getNegotiatorName());
    assertThat(view.getSortedFileViews()).isEqualTo(fileViews);

  }
}