package uk.co.ogauthority.pwa.service.pwaapplications.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.ogauthority.pwa.model.entity.enums.DecommissioningCondition;
import uk.co.ogauthority.pwa.model.entity.enums.EnvironmentalCondition;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadEnvironmentalDecommissioning;
import uk.co.ogauthority.pwa.model.enums.pwaapplications.shared.EnvDecomQuestion;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.EnvironmentalDecommissioningForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.initial.PadEnvironmentalDecommissioningRepository;
import uk.co.ogauthority.pwa.service.entitycopier.EntityCopyingService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.util.DateUtils;
import uk.co.ogauthority.pwa.validators.EnvironmentalDecommissioningValidator;

@RunWith(MockitoJUnitRunner.class)
public class PadEnvironmentalDecommissioningServiceTest {

  @Mock
  private PadEnvironmentalDecommissioningRepository padEnvironmentalDecommissioningRepository;

  @Mock
  private EnvironmentalDecommissioningValidator validator;

  @Mock
  private EntityCopyingService entityCopyingService;

  private PadEnvironmentalDecommissioningService padEnvironmentalDecommissioningService;
  private PwaApplicationDetail pwaApplicationDetail;
  private Instant instant;

  @Before
  public void setUp() {
    padEnvironmentalDecommissioningService = new PadEnvironmentalDecommissioningService(
        padEnvironmentalDecommissioningRepository, validator, entityCopyingService);
    instant = Instant.now();
  }

  @Test
  public void testGetEnvDecomData_NoneSaved() {
    when(padEnvironmentalDecommissioningRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(
        Optional.empty());
    PadEnvironmentalDecommissioning padEnvironmentalDecommissioning = padEnvironmentalDecommissioningService.getEnvDecomData(
        pwaApplicationDetail);
    assertThat(padEnvironmentalDecommissioning.getPwaApplicationDetail()).isEqualTo(pwaApplicationDetail);
    assertThat(padEnvironmentalDecommissioning.getId()).isNull();
  }

  @Test
  public void testGetEnvDecomData_PreExisting() {
    var existingData = new PadEnvironmentalDecommissioning();
    when(padEnvironmentalDecommissioningRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(
        Optional.of(existingData));
    PadEnvironmentalDecommissioning padEnvironmentalDecommissioning = padEnvironmentalDecommissioningService.getEnvDecomData(
        pwaApplicationDetail);
    assertThat(padEnvironmentalDecommissioning).isEqualTo(existingData);
  }

  @Test
  public void mapEntityToForm() {
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
  public void saveEntityUsingForm_AllExpanded() {
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
  public void saveEntityUsingForm_NoneExpanded() {
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
  public void saveEntityUsingForm_NullValues() {
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
  public void getEnvironmentalDecommissioningView() {
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
  public void validate_serviceInteractions() {

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
  public void cleanupData_hiddenData() {

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
  public void cleanupData_noHiddenData() {

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
  public void canShowInTaskList_allowed() {

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
  public void canShowInTaskList_notAllowed() {

    var detail = new PwaApplicationDetail();
    var app = new PwaApplication();
    app.setApplicationType(PwaApplicationType.OPTIONS_VARIATION);
    detail.setPwaApplication(app);

    assertThat(padEnvironmentalDecommissioningService.canShowInTaskList(detail)).isFalse();

  }

  @Test
  public void getAvailableQuestions_notCat2OrDepconOrDecom() {

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
  public void getAvailableQuestions_cat2() {

    var detail = new PwaApplicationDetail();
    var app = new PwaApplication();
    app.setApplicationType(PwaApplicationType.CAT_2_VARIATION);
    detail.setPwaApplication(app);

    assertThat(padEnvironmentalDecommissioningService.getAvailableQuestions(detail))
        .containsExactly(EnvDecomQuestion.BEIS_EMT_PERMITS);

  }

  @Test
  public void getAvailableQuestions_depcon() {

    var detail = new PwaApplicationDetail();
    var app = new PwaApplication();
    app.setApplicationType(PwaApplicationType.DEPOSIT_CONSENT);
    detail.setPwaApplication(app);

    assertThat(padEnvironmentalDecommissioningService.getAvailableQuestions(detail))
        .containsExactly(EnvDecomQuestion.BEIS_EMT_PERMITS);

  }

  @Test
  public void getAvailableQuestions_decom() {

    var detail = new PwaApplicationDetail();
    var app = new PwaApplication();
    app.setApplicationType(PwaApplicationType.DECOMMISSIONING);
    detail.setPwaApplication(app);

    assertThat(padEnvironmentalDecommissioningService.getAvailableQuestions(detail))
        .containsExactlyElementsOf(EnumSet.complementOf(EnumSet.of(EnvDecomQuestion.DECOMMISSIONING)));

  }

}