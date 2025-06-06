package uk.co.ogauthority.pwa.features.application.tasks.enviromentanddecom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.entitycopier.EntityCopyingService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.util.DateUtils;

@ExtendWith(MockitoExtension.class)
class PadEnvironmentalDecommissioningServiceTest {

  @Mock
  private PadEnvironmentalDecommissioningRepository padEnvironmentalDecommissioningRepository;

  @Mock
  private EnvironmentalDecommissioningValidator validator;

  @Mock
  private EntityCopyingService entityCopyingService;

  private PadEnvironmentalDecommissioningService padEnvironmentalDecommissioningService;
  private PwaApplicationDetail pwaApplicationDetail;
  private Instant instant;

  @BeforeEach
  void setUp() {
    padEnvironmentalDecommissioningService = new PadEnvironmentalDecommissioningService(
        padEnvironmentalDecommissioningRepository, validator, entityCopyingService);
    instant = Instant.now();
  }

  @Test
  void getEnvDecomDataNoneSaved() {
    when(padEnvironmentalDecommissioningRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(
        Optional.empty());
    PadEnvironmentalDecommissioning padEnvironmentalDecommissioning = padEnvironmentalDecommissioningService.getEnvDecomData(
        pwaApplicationDetail);
    assertThat(padEnvironmentalDecommissioning.getPwaApplicationDetail()).isEqualTo(pwaApplicationDetail);
    assertThat(padEnvironmentalDecommissioning.getId()).isNull();
  }

  @Test
  void getEnvDecomDataPreExisting() {
    var existingData = new PadEnvironmentalDecommissioning();
    when(padEnvironmentalDecommissioningRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(
        Optional.of(existingData));
    PadEnvironmentalDecommissioning padEnvironmentalDecommissioning = padEnvironmentalDecommissioningService.getEnvDecomData(
        pwaApplicationDetail);
    assertThat(padEnvironmentalDecommissioning).isEqualTo(existingData);
  }

  @Test
  void mapEntityToForm() {
    var form = new EnvironmentalDecommissioningForm();
    var entity = buildEntity();
    padEnvironmentalDecommissioningService.mapEntityToForm(entity, form);
    assertThat(entity.getTransboundaryEffect()).isEqualTo(form.getTransboundaryEffect());
    assertThat(entity.getEmtHasSubmittedPermits()).isEqualTo(form.getEmtHasSubmittedPermits());
    assertThat(entity.getPermitsSubmitted()).isEqualTo(form.getPermitsSubmitted());
    assertThat(entity.getEmtHasOutstandingPermits()).isEqualTo(form.getEmtHasOutstandingPermits());
    assertThat(entity.getPermitsPendingSubmission()).isEqualTo(form.getPermitsPendingSubmission());
    assertThat(LocalDate.ofInstant(entity.getEmtSubmissionTimestamp(), ZoneId.systemDefault()))
        .isEqualTo(LocalDate.of(form.getEmtSubmissionYear(), form.getEmtSubmissionMonth(), form.getEmtSubmissionDay()));
    assertThat(entity.getEnvironmentalConditions()).isEqualTo(form.getEnvironmentalConditions());
    assertThat(entity.getDecommissioningConditions()).isEqualTo(form.getDecommissioningConditions());
  }

  @Test
  void saveEntityUsingForm_AllExpanded() {
    var form = buildForm();
    var entity = new PadEnvironmentalDecommissioning();
    padEnvironmentalDecommissioningService.saveEntityUsingForm(entity, form);
    assertThat(entity.getTransboundaryEffect()).isEqualTo(form.getTransboundaryEffect());
    assertThat(entity.getEmtHasSubmittedPermits()).isEqualTo(form.getEmtHasSubmittedPermits());
    assertThat(entity.getPermitsSubmitted()).isEqualTo(form.getPermitsSubmitted());
    assertThat(entity.getEmtHasOutstandingPermits()).isEqualTo(form.getEmtHasOutstandingPermits());
    assertThat(entity.getPermitsPendingSubmission()).isEqualTo(form.getPermitsPendingSubmission());
    assertThat(LocalDate.ofInstant(entity.getEmtSubmissionTimestamp(), ZoneId.systemDefault()))
        .isEqualTo(LocalDate.of(2020, 3, 18));
    assertThat(entity.getEnvironmentalConditions()).isEqualTo(form.getEnvironmentalConditions());
    assertThat(entity.getDecommissioningConditions()).isEqualTo(form.getDecommissioningConditions());
    verify(padEnvironmentalDecommissioningRepository, times(1)).save(entity);
  }

  @Test
  void saveEntityUsingForm_NoneExpanded() {
    var form = buildForm();
    form.setEmtHasOutstandingPermits(false);
    form.setEmtHasSubmittedPermits(false);
    var entity = new PadEnvironmentalDecommissioning();
    padEnvironmentalDecommissioningService.saveEntityUsingForm(entity, form);
    assertThat(entity.getTransboundaryEffect()).isEqualTo(form.getTransboundaryEffect());
    assertThat(entity.getEmtHasSubmittedPermits()).isEqualTo(form.getEmtHasSubmittedPermits());
    assertThat(entity.getPermitsSubmitted()).isNull();
    assertThat(entity.getEmtHasOutstandingPermits()).isEqualTo(form.getEmtHasOutstandingPermits());
    assertThat(entity.getPermitsPendingSubmission()).isNull();
    assertThat(entity.getEmtSubmissionTimestamp()).isNull();
    assertThat(entity.getEnvironmentalConditions()).isEqualTo(form.getEnvironmentalConditions());
    assertThat(entity.getDecommissioningConditions()).isEqualTo(form.getDecommissioningConditions());
  }

  @Test
  void saveEntityUsingForm_NullValues() {
    var form = new EnvironmentalDecommissioningForm();
    var entity = new PadEnvironmentalDecommissioning();
    padEnvironmentalDecommissioningService.saveEntityUsingForm(entity, form);
    assertThat(entity.getTransboundaryEffect()).isNull();
    assertThat(entity.getEmtHasSubmittedPermits()).isNull();
    assertThat(entity.getPermitsSubmitted()).isNull();
    assertThat(entity.getEmtHasOutstandingPermits()).isNull();
    assertThat(entity.getPermitsPendingSubmission()).isNull();
    assertThat(entity.getEmtSubmissionTimestamp()).isNull();
    assertThat(entity.getEnvironmentalConditions()).isNull();
    assertThat(entity.getDecommissioningConditions()).isNull();
  }

  @Test
  void getEnvironmentalDecommissioningView() {
    var existingData = buildEntity();
    when(padEnvironmentalDecommissioningRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(
        Optional.of(existingData));

    var environmentalDecommView = padEnvironmentalDecommissioningService.getEnvironmentalDecommissioningView(pwaApplicationDetail);

    assertThat(environmentalDecommView.getTransboundaryEffect()).isEqualTo(existingData.getTransboundaryEffect());
    assertThat(environmentalDecommView.getEmtHasSubmittedPermits()).isEqualTo(existingData.getEmtHasSubmittedPermits());
    assertThat(environmentalDecommView.getPermitsSubmitted()).isEqualTo(existingData.getPermitsSubmitted());
    assertThat(environmentalDecommView.getEmtHasOutstandingPermits()).isEqualTo(existingData.getEmtHasOutstandingPermits());
    assertThat(environmentalDecommView.getPermitsPendingSubmission()).isEqualTo(existingData.getPermitsPendingSubmission());
    assertThat(environmentalDecommView.getEmtSubmissionDate())
        .isEqualTo(DateUtils.formatDate(existingData.getEmtSubmissionTimestamp()));
    assertThat(environmentalDecommView.getEnvironmentalConditions()).isEqualTo(existingData.getEnvironmentalConditions());
    assertThat(environmentalDecommView.getDecommissioningConditions()).isEqualTo(existingData.getDecommissioningConditions());
  }

  @Test
  void validate_serviceInteractions() {

    var form = new EnvironmentalDecommissioningForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    var detail = new PwaApplicationDetail();

    padEnvironmentalDecommissioningService.validate(form, bindingResult, ValidationType.FULL, detail);

    verify(validator, times(1)).validate(form, bindingResult, detail, ValidationType.FULL);

  }

  private PadEnvironmentalDecommissioning buildEntity() {
    var entity = new PadEnvironmentalDecommissioning();
    entity.setTransboundaryEffect(true);
    entity.setEmtHasSubmittedPermits(true);
    entity.setPermitsSubmitted("Submitted permits");
    entity.setEmtHasOutstandingPermits(true);
    entity.setPermitsPendingSubmission("Pending permits");
    entity.setEmtSubmissionTimestamp(instant);
    entity.setEnvironmentalConditions(EnumSet.allOf(EnvironmentalCondition.class));
    entity.setDecommissioningConditions(EnumSet.allOf(DecommissioningCondition.class));
    return entity;
  }

  private EnvironmentalDecommissioningForm buildForm() {
    var form = new EnvironmentalDecommissioningForm();
    form.setTransboundaryEffect(true);
    form.setEmtHasSubmittedPermits(true);
    form.setPermitsSubmitted("Submitted text");
    form.setEmtHasOutstandingPermits(true);
    form.setPermitsPendingSubmission("Pending text");
    form.setEmtSubmissionYear(2020);
    form.setEmtSubmissionMonth(3);
    form.setEmtSubmissionDay(18);
    form.setEnvironmentalConditions(EnumSet.allOf(EnvironmentalCondition.class));
    form.setDecommissioningConditions(EnumSet.allOf(DecommissioningCondition.class));
    return form;
  }

  @Test
  void cleanupData_hiddenData() {

    var envDecom = new PadEnvironmentalDecommissioning();

    envDecom.setEmtHasSubmittedPermits(false);
    envDecom.setPermitsSubmitted("sub");

    envDecom.setEmtHasOutstandingPermits(false);
    envDecom.setPermitsPendingSubmission("test");
    envDecom.setEmtSubmissionTimestamp(Instant.now());

    when(padEnvironmentalDecommissioningRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(Optional.of(envDecom));

    padEnvironmentalDecommissioningService.cleanupData(pwaApplicationDetail);

    assertThat(envDecom.getPermitsSubmitted()).isNull();

    assertThat(envDecom.getPermitsPendingSubmission()).isNull();
    assertThat(envDecom.getEmtSubmissionTimestamp()).isNull();

    verify(padEnvironmentalDecommissioningRepository, times(1)).save(envDecom);

  }

  @Test
  void cleanupData_noHiddenData() {

    var envDecom = new PadEnvironmentalDecommissioning();

    envDecom.setEmtHasSubmittedPermits(true);
    envDecom.setPermitsSubmitted("sub");

    envDecom.setEmtHasOutstandingPermits(true);
    envDecom.setPermitsPendingSubmission("test");
    envDecom.setEmtSubmissionTimestamp(Instant.now());

    when(padEnvironmentalDecommissioningRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(Optional.of(envDecom));

    padEnvironmentalDecommissioningService.cleanupData(pwaApplicationDetail);

    assertThat(envDecom.getPermitsSubmitted()).isNotNull();

    assertThat(envDecom.getPermitsPendingSubmission()).isNotNull();
    assertThat(envDecom.getEmtSubmissionTimestamp()).isNotNull();

    verify(padEnvironmentalDecommissioningRepository, times(1)).save(envDecom);

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

      assertThat(padEnvironmentalDecommissioningService.canShowInTaskList(detail)).isTrue();

    });

  }

  @Test
  void canShowInTaskList_notAllowed() {

    var detail = new PwaApplicationDetail();
    var app = new PwaApplication();
    app.setApplicationType(PwaApplicationType.OPTIONS_VARIATION);
    detail.setPwaApplication(app);

    assertThat(padEnvironmentalDecommissioningService.canShowInTaskList(detail)).isFalse();

  }

  @Test
  void getAvailableQuestions_notCat2OrDepconOrDecom() {

    var detail = new PwaApplicationDetail();
    var app = new PwaApplication();
    detail.setPwaApplication(app);

    PwaApplicationType.stream()
        .filter(applicationType -> applicationType != PwaApplicationType.CAT_2_VARIATION
            && applicationType != PwaApplicationType.DEPOSIT_CONSENT
            && applicationType != PwaApplicationType.DECOMMISSIONING)
        .forEach(applicationType -> {

          app.setApplicationType(applicationType);
          assertThat(padEnvironmentalDecommissioningService.getAvailableQuestions(detail))
              .containsExactlyElementsOf(EnumSet.allOf(EnvDecomQuestion.class));

        });

  }

  @Test
  void getAvailableQuestions_cat2() {

    var detail = new PwaApplicationDetail();
    var app = new PwaApplication();
    app.setApplicationType(PwaApplicationType.CAT_2_VARIATION);
    detail.setPwaApplication(app);

    assertThat(padEnvironmentalDecommissioningService.getAvailableQuestions(detail))
        .containsExactly(EnvDecomQuestion.BEIS_EMT_PERMITS);

  }

  @Test
  void getAvailableQuestions_depcon() {

    var detail = new PwaApplicationDetail();
    var app = new PwaApplication();
    app.setApplicationType(PwaApplicationType.DEPOSIT_CONSENT);
    detail.setPwaApplication(app);

    assertThat(padEnvironmentalDecommissioningService.getAvailableQuestions(detail))
        .containsExactly(EnvDecomQuestion.BEIS_EMT_PERMITS);

  }

  @Test
  void getAvailableQuestions_decom() {

    var detail = new PwaApplicationDetail();
    var app = new PwaApplication();
    app.setApplicationType(PwaApplicationType.DECOMMISSIONING);
    detail.setPwaApplication(app);

    assertThat(padEnvironmentalDecommissioningService.getAvailableQuestions(detail))
        .containsExactlyElementsOf(EnumSet.complementOf(EnumSet.of(EnvDecomQuestion.DECOMMISSIONING)));

  }

}