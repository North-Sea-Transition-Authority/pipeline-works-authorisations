package uk.co.ogauthority.pwa.features.application.tasks.fasttrack;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import jakarta.validation.Validation;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.medianline.MedianLineStatus;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.medianline.PadMedianLineAgreement;
import uk.co.ogauthority.pwa.features.application.tasks.crossings.medianline.PadMedianLineAgreementService;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.PadProjectInformation;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.PadProjectInformationService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.entitycopier.EntityCopyingService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PadFastTrackServiceTest {

  @Mock
  private PadFastTrackRepository padFastTrackRepository;

  @Mock
  private PadProjectInformationService padProjectInformationService;

  @Mock
  private PadMedianLineAgreementService padMedianLineAgreementService;

  @Mock
  private FastTrackValidator validator;

  @Mock
  private EntityCopyingService entityCopyingService;

  private SpringValidatorAdapter groupValidator;

  private PadFastTrackService padFastTrackService;
  private PwaApplicationDetail pwaApplicationDetail;
  private PadProjectInformation projectInformation;

  @BeforeEach
  void setUp() {

    groupValidator = new SpringValidatorAdapter(Validation.buildDefaultValidatorFactory().getValidator());

    padFastTrackService = new PadFastTrackService(
        padFastTrackRepository,
        padProjectInformationService,
        padMedianLineAgreementService,
        validator,
        groupValidator,
        entityCopyingService
    );

    pwaApplicationDetail = new PwaApplicationDetail();

    projectInformation = new PadProjectInformation();
    when(padProjectInformationService.getPadProjectInformationData(pwaApplicationDetail))
        .thenReturn(projectInformation);
  }

  @Test
  void save() {
    var fastTrack = new PadFastTrack();
    padFastTrackService.save(fastTrack);
    verify(padFastTrackRepository, times(1)).save(fastTrack);
  }

  @Test
  void getFastTrackForDraft_Existing() {
    var fastTrack = new PadFastTrack();
    when(padFastTrackRepository.findByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(Optional.of(fastTrack));
    var result = padFastTrackService.getFastTrackForDraft(pwaApplicationDetail);
    assertThat(fastTrack).isEqualTo(result);
  }

  @Test
  void getFastTrackForDraft_NotExisting() {
    when(padFastTrackRepository.findByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(Optional.empty());
    var result = padFastTrackService.getFastTrackForDraft(pwaApplicationDetail);
    assertThat(result).extracting(PadFastTrack::getId).isNull();
  }

  @Test
  void isFastTrackRequired_NoProposedStart() {
    var result = padFastTrackService.isFastTrackRequired(pwaApplicationDetail);
    assertThat(result).isFalse();
  }

  @Test
  void isFastTrackRequired_BeforeMinPeriod_NoMedianLine() {
    EnumSet.allOf(PwaApplicationType.class).forEach(type -> {
      var start = LocalDate.now().plus(type.getMinProcessingPeriod()).minusDays(1);
      assertFastTrackRequired(type, start, true);
    });
  }

  @Test
  void isFastTrackRequired_AtMinPeriod_NoMedianLine() {
    EnumSet.allOf(PwaApplicationType.class).forEach(type -> {
      var start = LocalDate.now().plus(type.getMinProcessingPeriod());
      assertFastTrackRequired(type, start, false);
    });
  }

  @Test
  void isFastTrackRequired_PastMinPeriod_NoMedianLine() {

    EnumSet.allOf(PwaApplicationType.class).forEach(type -> {
      var start = LocalDate.now().plus(type.getMinProcessingPeriod()).plusDays(1);
      assertFastTrackRequired(type, start, false);
    });
  }

  @Test
  void isFastTrackRequired_BeforeMaxPeriod_WithMedianLine() {
    var medianLine = new PadMedianLineAgreement();
    medianLine.setAgreementStatus(MedianLineStatus.NEGOTIATIONS_COMPLETED);
    when(padMedianLineAgreementService.getMedianLineAgreement(pwaApplicationDetail)).thenReturn(medianLine);

    EnumSet.allOf(PwaApplicationType.class).forEach(type -> {
      var start = LocalDate.now().plus(type.getMaxProcessingPeriod()).minusDays(1);
      assertFastTrackRequired(type, start, true);
    });
  }

  @Test
  void isFastTrackRequired_AtMaxPeriod_WithMedianLine() {
    var medianLine = new PadMedianLineAgreement();
    medianLine.setAgreementStatus(MedianLineStatus.NEGOTIATIONS_COMPLETED);
    when(padMedianLineAgreementService.getMedianLineAgreement(pwaApplicationDetail)).thenReturn(medianLine);

    EnumSet.allOf(PwaApplicationType.class).forEach(type -> {
      var start = LocalDate.now().plus(type.getMaxProcessingPeriod());
      assertFastTrackRequired(type, start, false);
    });
  }

  @Test
  void isFastTrackRequired_PastMaxPeriod_WithMedianLine() {
    var medianLine = new PadMedianLineAgreement();
    medianLine.setAgreementStatus(MedianLineStatus.NEGOTIATIONS_COMPLETED);
    when(padMedianLineAgreementService.getMedianLineAgreement(pwaApplicationDetail)).thenReturn(medianLine);

    EnumSet.allOf(PwaApplicationType.class).forEach(type -> {
      var start = LocalDate.now().plus(type.getMaxProcessingPeriod()).plusDays(1);
      assertFastTrackRequired(type, start, false);
    });
  }

  private void assertFastTrackRequired(PwaApplicationType type, LocalDate startDate, Boolean expecting) {
    projectInformation.setProposedStartTimestamp(
        Instant.ofEpochSecond(startDate.atStartOfDay().toEpochSecond(ZoneOffset.UTC))
    );

    var application = new PwaApplication();
    application.setApplicationType(type);
    pwaApplicationDetail.setPwaApplication(application);

    var result = padFastTrackService.isFastTrackRequired(pwaApplicationDetail);
    assertThat(result).isEqualTo(expecting);
  }

  @Test
  void isFastTrackRequired_BeforeAndAfterMedianLine() {
    var medianLine = new PadMedianLineAgreement();
    medianLine.setAgreementStatus(MedianLineStatus.NOT_CROSSED);
    when(padMedianLineAgreementService.getMedianLineAgreement(pwaApplicationDetail)).thenReturn(medianLine);

    var start = LocalDate.now().plus(PwaApplicationType.CAT_2_VARIATION.getMinProcessingPeriod()).plusWeeks(1);
    projectInformation.setProposedStartTimestamp(
        Instant.ofEpochSecond(start.atStartOfDay().toEpochSecond(ZoneOffset.UTC))
    );

    var application = new PwaApplication();
    application.setApplicationType(PwaApplicationType.CAT_2_VARIATION);
    pwaApplicationDetail.setPwaApplication(application);

    // Without median line
    var result = padFastTrackService.isFastTrackRequired(pwaApplicationDetail);
    assertThat(result).isFalse();

    // With median line
    medianLine.setAgreementStatus(MedianLineStatus.NEGOTIATIONS_COMPLETED);
    result = padFastTrackService.isFastTrackRequired(pwaApplicationDetail);
    assertThat(result).isTrue();
  }

  @Test
  void mapEntityToForm() {
    var entity = buildEntity();
    var form = new FastTrackForm();
    var expectedForm = buildForm();
    padFastTrackService.mapEntityToForm(entity, form);
    assertThat(form.getAvoidEnvironmentalDisaster()).isEqualTo(expectedForm.getAvoidEnvironmentalDisaster());
    assertThat(form.getEnvironmentalDisasterReason()).isEqualTo(expectedForm.getEnvironmentalDisasterReason());
    assertThat(form.getSavingBarrels()).isEqualTo(expectedForm.getSavingBarrels());
    assertThat(form.getSavingBarrelsReason()).isEqualTo(expectedForm.getSavingBarrelsReason());
    assertThat(form.getProjectPlanning()).isEqualTo(expectedForm.getProjectPlanning());
    assertThat(form.getProjectPlanningReason()).isEqualTo(expectedForm.getProjectPlanningReason());
    assertThat(form.getHasOtherReason()).isEqualTo(expectedForm.getHasOtherReason());
    assertThat(form.getOtherReason()).isEqualTo(expectedForm.getOtherReason());
  }

  @Test
  void saveEntityUsingForm() {
    var entity = new PadFastTrack();
    var expectedEntity = buildEntity();
    var form = buildForm();
    padFastTrackService.saveEntityUsingForm(entity, form);
    assertThat(entity.getAvoidEnvironmentalDisaster()).isEqualTo(expectedEntity.getAvoidEnvironmentalDisaster());
    assertThat(entity.getEnvironmentalDisasterReason()).isEqualTo(expectedEntity.getEnvironmentalDisasterReason());
    assertThat(entity.getSavingBarrels()).isEqualTo(expectedEntity.getSavingBarrels());
    assertThat(entity.getSavingBarrelsReason()).isEqualTo(expectedEntity.getSavingBarrelsReason());
    assertThat(entity.getProjectPlanning()).isEqualTo(expectedEntity.getProjectPlanning());
    assertThat(entity.getProjectPlanningReason()).isEqualTo(expectedEntity.getProjectPlanningReason());
    assertThat(entity.getHasOtherReason()).isEqualTo(expectedEntity.getHasOtherReason());
    assertThat(entity.getOtherReason()).isEqualTo(expectedEntity.getOtherReason());
    verify(padFastTrackRepository, times(1)).save(entity);
  }

  @Test
  void saveEntityUsingForm_EnvironmentalUnchecked() {
    var entity = new PadFastTrack();
    var expectedEntity = buildEntity();
    var form = buildForm();
    form.setAvoidEnvironmentalDisaster(null);
    padFastTrackService.saveEntityUsingForm(entity, form);
    assertThat(entity.getEnvironmentalDisasterReason()).isNull();
    assertThat(entity.getSavingBarrelsReason()).isEqualTo(expectedEntity.getSavingBarrelsReason());
    assertThat(entity.getProjectPlanningReason()).isEqualTo(expectedEntity.getProjectPlanningReason());
    assertThat(entity.getOtherReason()).isEqualTo(expectedEntity.getOtherReason());
  }

  @Test
  void saveEntityUsingForm_SavingBarrelsUnchecked() {
    var entity = new PadFastTrack();
    var expectedEntity = buildEntity();
    var form = buildForm();
    form.setSavingBarrels(null);
    padFastTrackService.saveEntityUsingForm(entity, form);
    assertThat(entity.getEnvironmentalDisasterReason()).isEqualTo(expectedEntity.getEnvironmentalDisasterReason());
    assertThat(entity.getSavingBarrelsReason()).isNull();
    assertThat(entity.getProjectPlanningReason()).isEqualTo(expectedEntity.getProjectPlanningReason());
    assertThat(entity.getOtherReason()).isEqualTo(expectedEntity.getOtherReason());
  }

  @Test
  void saveEntityUsingForm_ProjectPlanningUnchecked() {
    var entity = new PadFastTrack();
    var expectedEntity = buildEntity();
    var form = buildForm();
    form.setProjectPlanning(null);
    padFastTrackService.saveEntityUsingForm(entity, form);
    assertThat(entity.getEnvironmentalDisasterReason()).isEqualTo(expectedEntity.getEnvironmentalDisasterReason());
    assertThat(entity.getSavingBarrelsReason()).isEqualTo(expectedEntity.getSavingBarrelsReason());
    assertThat(entity.getProjectPlanningReason()).isNull();
    assertThat(entity.getOtherReason()).isEqualTo(expectedEntity.getOtherReason());
  }

  @Test
  void saveEntityUsingForm_OtherReasonUnchecked() {
    var entity = new PadFastTrack();
    var expectedEntity = buildEntity();
    var form = buildForm();
    form.setHasOtherReason(null);
    padFastTrackService.saveEntityUsingForm(entity, form);
    assertThat(entity.getEnvironmentalDisasterReason()).isEqualTo(expectedEntity.getEnvironmentalDisasterReason());
    assertThat(entity.getSavingBarrelsReason()).isEqualTo(expectedEntity.getSavingBarrelsReason());
    assertThat(entity.getProjectPlanningReason()).isEqualTo(expectedEntity.getProjectPlanningReason());
    assertThat(entity.getOtherReason()).isNull();
  }

  @Test
  void getFastTrackView_dataExists() {
    var fastTrack = buildEntity();
    when(padFastTrackRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(Optional.of(fastTrack));

    var fastTrackView = padFastTrackService.getFastTrackView(pwaApplicationDetail);
    assertThat(fastTrackView.getAvoidEnvironmentalDisaster()).isTrue();
    assertThat(fastTrackView.getEnvironmentalDisasterReason()).isEqualTo(fastTrack.getEnvironmentalDisasterReason());

    assertThat(fastTrackView.getSavingBarrels()).isTrue();
    assertThat(fastTrackView.getSavingBarrelsReason()).isEqualTo(fastTrack.getSavingBarrelsReason());

    assertThat(fastTrackView.getProjectPlanning()).isTrue();
    assertThat(fastTrackView.getProjectPlanningReason()).isEqualTo(fastTrack.getProjectPlanningReason());

    assertThat(fastTrackView.getHasOtherReason()).isTrue();
    assertThat(fastTrackView.getOtherReason()).isEqualTo(fastTrack.getOtherReason());

    assertThat(fastTrackView.isFastTrackDataExists()).isTrue();
  }

  @Test
  void getFastTrackView_noFastTrackData() {
    var fastTrack = new PadFastTrack();
    when(padFastTrackRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(Optional.of(fastTrack));

    var fastTrackView = padFastTrackService.getFastTrackView(pwaApplicationDetail);
    assertThat(fastTrackView.isFastTrackDataExists()).isFalse();
  }

  @Test
  void validate_verifyValidatorInteraction() {

    var form = new FastTrackForm();
    form.setEnvironmentalDisasterReason(ValidatorTestUtils.overMaxDefaultCharLength());
    form.setOtherReason(ValidatorTestUtils.overMaxDefaultCharLength());
    form.setProjectPlanningReason(ValidatorTestUtils.overMaxDefaultCharLength());
    form.setSavingBarrelsReason(ValidatorTestUtils.overMaxDefaultCharLength());

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    padFastTrackService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);
    verify(validator, times(1)).validate(form, bindingResult, ValidationType.FULL);
  }

  @Test
  void copySectionInformation_dataPresent_dataCopied() {

    var newDetail = new PwaApplicationDetail();
    var fastTrackData = new PadFastTrack();
    when(padFastTrackRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(Optional.of(fastTrackData));

    padFastTrackService.copySectionInformation(pwaApplicationDetail, newDetail);

    verify(entityCopyingService, times(1)).duplicateEntityAndSetParent(any(), eq(newDetail), eq(PadFastTrack.class));

  }

  @Test
  void copySectionInformation_dataNotPresent_noError() {

    var newDetail = new PwaApplicationDetail();
    when(padFastTrackRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(Optional.empty());

    padFastTrackService.copySectionInformation(pwaApplicationDetail, newDetail);

    verifyNoInteractions(entityCopyingService);

  }

  private PadFastTrack buildEntity() {
    var fastTrack = new PadFastTrack();
    fastTrack.setAvoidEnvironmentalDisaster(true);
    fastTrack.setEnvironmentalDisasterReason("Env Reason");
    fastTrack.setSavingBarrels(true);
    fastTrack.setSavingBarrelsReason("Barrels Reason");
    fastTrack.setProjectPlanning(true);
    fastTrack.setProjectPlanningReason("Planning reason");
    fastTrack.setHasOtherReason(true);
    fastTrack.setOtherReason("Other reason");
    return fastTrack;
  }

  private FastTrackForm buildForm() {
    var form = new FastTrackForm();
    form.setAvoidEnvironmentalDisaster(true);
    form.setEnvironmentalDisasterReason("Env Reason");
    form.setSavingBarrels(true);
    form.setSavingBarrelsReason("Barrels Reason");
    form.setProjectPlanning(true);
    form.setProjectPlanningReason("Planning reason");
    form.setHasOtherReason(true);
    form.setOtherReason("Other reason");
    return form;
  }

}