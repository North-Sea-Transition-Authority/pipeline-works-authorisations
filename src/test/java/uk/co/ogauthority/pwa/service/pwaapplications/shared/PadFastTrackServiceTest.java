package uk.co.ogauthority.pwa.service.pwaapplications.shared;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import javax.validation.Validation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import uk.co.ogauthority.pwa.model.entity.enums.MedianLineStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadFastTrack;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadMedianLineAgreement;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadProjectInformation;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.FastTrackForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadFastTrackRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.projectinformation.PadProjectInformationService;
import uk.co.ogauthority.pwa.util.ValidatorTestUtils;
import uk.co.ogauthority.pwa.validators.FastTrackValidator;

@RunWith(MockitoJUnitRunner.class)
public class PadFastTrackServiceTest {

  @Mock
  private PadFastTrackRepository padFastTrackRepository;

  @Mock
  private PadProjectInformationService padProjectInformationService;

  @Mock
  private PadMedianLineAgreementService padMedianLineAgreementService;

  @Mock
  private FastTrackValidator validator;

  private SpringValidatorAdapter groupValidator;

  private PadFastTrackService padFastTrackService;
  private PwaApplicationDetail pwaApplicationDetail;
  private PadProjectInformation projectInformation;

  @Before
  public void setUp() {

    groupValidator = new SpringValidatorAdapter(Validation.buildDefaultValidatorFactory().getValidator());

    padFastTrackService = new PadFastTrackService(padFastTrackRepository, padProjectInformationService,
        padMedianLineAgreementService, validator, groupValidator);
    pwaApplicationDetail = new PwaApplicationDetail();

    projectInformation = new PadProjectInformation();
    when(padProjectInformationService.getPadProjectInformationData(pwaApplicationDetail))
        .thenReturn(projectInformation);
  }

  @Test
  public void save() {
    var fastTrack = new PadFastTrack();
    padFastTrackService.save(fastTrack);
    verify(padFastTrackRepository, times(1)).save(fastTrack);
  }

  @Test
  public void getFastTrackForDraft_Existing() {
    var fastTrack = new PadFastTrack();
    when(padFastTrackRepository.findByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(Optional.of(fastTrack));
    var result = padFastTrackService.getFastTrackForDraft(pwaApplicationDetail);
    assertThat(fastTrack).isEqualTo(result);
  }

  @Test
  public void getFastTrackForDraft_NotExisting() {
    when(padFastTrackRepository.findByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(Optional.empty());
    var result = padFastTrackService.getFastTrackForDraft(pwaApplicationDetail);
    assertThat(result).extracting(PadFastTrack::getId).isNull();
  }

  @Test
  public void isFastTrackRequired_NoProposedStart() {
    var result = padFastTrackService.isFastTrackRequired(pwaApplicationDetail);
    assertThat(result).isFalse();
  }

  @Test
  public void isFastTrackRequired_BeforeMinPeriod_NoMedianLine() {
    EnumSet.allOf(PwaApplicationType.class).forEach(type -> {
      var start = LocalDate.now().plus(type.getMinProcessingPeriod()).minusDays(1);
      assertFastTrackRequired(type, start, true);
    });
  }

  @Test
  public void isFastTrackRequired_AtMinPeriod_NoMedianLine() {
    EnumSet.allOf(PwaApplicationType.class).forEach(type -> {
      var start = LocalDate.now().plus(type.getMinProcessingPeriod());
      assertFastTrackRequired(type, start, false);
    });
  }

  @Test
  public void isFastTrackRequired_PastMinPeriod_NoMedianLine() {

    EnumSet.allOf(PwaApplicationType.class).forEach(type -> {
      var start = LocalDate.now().plus(type.getMinProcessingPeriod()).plusDays(1);
      assertFastTrackRequired(type, start, false);
    });
  }

  @Test
  public void isFastTrackRequired_BeforeMaxPeriod_WithMedianLine() {
    var medianLine = new PadMedianLineAgreement();
    medianLine.setAgreementStatus(MedianLineStatus.NEGOTIATIONS_COMPLETED);
    when(padMedianLineAgreementService.getMedianLineAgreement(pwaApplicationDetail)).thenReturn(medianLine);

    EnumSet.allOf(PwaApplicationType.class).forEach(type -> {
      var start = LocalDate.now().plus(type.getMaxProcessingPeriod()).minusDays(1);
      assertFastTrackRequired(type, start, true);
    });
  }

  @Test
  public void isFastTrackRequired_AtMaxPeriod_WithMedianLine() {
    var medianLine = new PadMedianLineAgreement();
    medianLine.setAgreementStatus(MedianLineStatus.NEGOTIATIONS_COMPLETED);
    when(padMedianLineAgreementService.getMedianLineAgreement(pwaApplicationDetail)).thenReturn(medianLine);

    EnumSet.allOf(PwaApplicationType.class).forEach(type -> {
      var start = LocalDate.now().plus(type.getMaxProcessingPeriod());
      assertFastTrackRequired(type, start, false);
    });
  }

  @Test
  public void isFastTrackRequired_PastMaxPeriod_WithMedianLine() {
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
  public void isFastTrackRequired_BeforeAndAfterMedianLine() {
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
  public void mapEntityToForm() {
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
  public void saveEntityUsingForm() {
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
  public void saveEntityUsingForm_EnvironmentalUnchecked() {
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
  public void saveEntityUsingForm_SavingBarrelsUnchecked() {
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
  public void saveEntityUsingForm_ProjectPlanningUnchecked() {
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
  public void saveEntityUsingForm_OtherReasonUnchecked() {
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
  public void validate_partial_fail() {

    var form = new FastTrackForm();
    form.setEnvironmentalDisasterReason(ValidatorTestUtils.over4000Chars());
    form.setOtherReason(ValidatorTestUtils.over4000Chars());
    form.setProjectPlanningReason(ValidatorTestUtils.over4000Chars());
    form.setSavingBarrelsReason(ValidatorTestUtils.over4000Chars());

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    padFastTrackService.validate(form, bindingResult, ValidationType.PARTIAL);
    var errors = ValidatorTestUtils.extractErrors(bindingResult);

    assertThat(errors).containsOnly(
        entry("environmentalDisasterReason", Set.of("Length")),
        entry("otherReason", Set.of("Length")),
        entry("projectPlanningReason", Set.of("Length")),
        entry("savingBarrelsReason", Set.of("Length"))
    );

    verifyNoInteractions(validator);

  }

  @Test
  public void validate_partial_pass() {

    var form = new FastTrackForm();
    form.setEnvironmentalDisasterReason(ValidatorTestUtils.exactly4000chars());
    form.setOtherReason(ValidatorTestUtils.exactly4000chars());
    form.setProjectPlanningReason(ValidatorTestUtils.exactly4000chars());
    form.setSavingBarrelsReason(ValidatorTestUtils.exactly4000chars());

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    padFastTrackService.validate(form, bindingResult, ValidationType.PARTIAL);
    var errors = ValidatorTestUtils.extractErrors(bindingResult);

    assertThat(errors).isEmpty();

  }

  @Test
  public void validate_full_fail() {

    var form = new FastTrackForm();
    form.setEnvironmentalDisasterReason(ValidatorTestUtils.over4000Chars());
    form.setOtherReason(ValidatorTestUtils.over4000Chars());
    form.setProjectPlanningReason(ValidatorTestUtils.over4000Chars());
    form.setSavingBarrelsReason(ValidatorTestUtils.over4000Chars());

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    padFastTrackService.validate(form, bindingResult, ValidationType.FULL);
    var errors = ValidatorTestUtils.extractErrors(bindingResult);

    assertThat(errors).containsOnly(
        entry("environmentalDisasterReason", Set.of("Length")),
        entry("otherReason", Set.of("Length")),
        entry("projectPlanningReason", Set.of("Length")),
        entry("savingBarrelsReason", Set.of("Length"))
    );

    verify(validator, times(1)).validate(form, bindingResult);

  }

  @Test
  public void validate_full_pass() {

    var form = new FastTrackForm();
    form.setEnvironmentalDisasterReason(ValidatorTestUtils.exactly4000chars());
    form.setOtherReason(ValidatorTestUtils.exactly4000chars());
    form.setProjectPlanningReason(ValidatorTestUtils.exactly4000chars());
    form.setSavingBarrelsReason(ValidatorTestUtils.exactly4000chars());

    var bindingResult = new BeanPropertyBindingResult(form, "form");
    padFastTrackService.validate(form, bindingResult, ValidationType.FULL);
    var errors = ValidatorTestUtils.extractErrors(bindingResult);

    assertThat(errors).isEmpty();

    verify(validator, times(1)).validate(form, bindingResult);

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