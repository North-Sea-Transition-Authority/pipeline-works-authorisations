package uk.co.ogauthority.pwa.service.pwaapplications.initial;

import static org.assertj.core.api.Assertions.assertThat;
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
import uk.co.ogauthority.pwa.model.entity.enums.DecommissioningCondition;
import uk.co.ogauthority.pwa.model.entity.enums.EnvironmentalCondition;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadEnvironmentalDecommissioning;
import uk.co.ogauthority.pwa.model.form.pwaapplications.initial.EnvDecomForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.initial.PadEnvironmentalDecommissioningRepository;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.PadEnvironmentalDecommissioningService;

@RunWith(MockitoJUnitRunner.class)
public class PadEnvironmentalDecommissioningServiceTest {

  @Mock
  private PadEnvironmentalDecommissioningRepository padEnvironmentalDecommissioningRepository;

  private PadEnvironmentalDecommissioningService padEnvironmentalDecommissioningService;
  private PwaApplicationDetail pwaApplicationDetail;
  private Instant instant;

  @Before
  public void setUp() {
    padEnvironmentalDecommissioningService = new PadEnvironmentalDecommissioningService(
        padEnvironmentalDecommissioningRepository);
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
    var form = new EnvDecomForm();
    var entity = buildEntity();
    padEnvironmentalDecommissioningService.mapEntityToForm(entity, form);
    assertThat(entity.getTransboundaryEffect()).isEqualTo(form.getTransboundaryEffect());
    assertThat(entity.getEmtHasSubmittedPermits()).isEqualTo(form.getEmtHasSubmittedPermits());
    assertThat(entity.getPermitsSubmitted()).isEqualTo(form.getPermitsSubmitted());
    assertThat(entity.getEmtHasOutstandingPermits()).isEqualTo(form.getEmtHasOutstandingPermits());
    assertThat(entity.getPermitsPendingSubmission()).isEqualTo(form.getPermitsPendingSubmission());
    assertThat(entity.getDecommissioningPlans()).isEqualTo(form.getDecommissioningPlans());
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
    assertThat(entity.getDecommissioningPlans()).isEqualTo(form.getDecommissioningPlans());
    assertThat(LocalDate.ofInstant(entity.getEmtSubmissionTimestamp(), ZoneId.systemDefault()))
        .isEqualTo(LocalDate.of(2020, 3, 18));
    assertThat(entity.getEnvironmentalConditions()).isEqualTo(form.getEnvironmentalConditions());
    assertThat(entity.getDecommissioningConditions()).isEqualTo(form.getDecommissioningConditions());
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
    assertThat(entity.getDecommissioningPlans()).isEqualTo(form.getDecommissioningPlans());
    assertThat(entity.getEmtSubmissionTimestamp()).isNull();
    assertThat(entity.getEnvironmentalConditions()).isEqualTo(form.getEnvironmentalConditions());
    assertThat(entity.getDecommissioningConditions()).isEqualTo(form.getDecommissioningConditions());
  }

  private PadEnvironmentalDecommissioning buildEntity() {
    var entity = new PadEnvironmentalDecommissioning();
    entity.setTransboundaryEffect(true);
    entity.setEmtHasSubmittedPermits(true);
    entity.setPermitsSubmitted("Submitted permits");
    entity.setEmtHasOutstandingPermits(true);
    entity.setPermitsPendingSubmission("Pending permits");
    entity.setDecommissioningPlans("Decom plans");
    entity.setEmtSubmissionTimestamp(instant);
    entity.setEnvironmentalConditions(EnumSet.allOf(EnvironmentalCondition.class));
    entity.setDecommissioningConditions(EnumSet.allOf(DecommissioningCondition.class));
    return entity;
  }

  private EnvDecomForm buildForm() {
    var form = new EnvDecomForm();
    form.setTransboundaryEffect(true);
    form.setEmtHasSubmittedPermits(true);
    form.setPermitsSubmitted("Submitted text");
    form.setEmtHasOutstandingPermits(true);
    form.setPermitsPendingSubmission("Pending text");
    form.setDecommissioningPlans("Decom text");
    form.setEmtSubmissionYear(2020);
    form.setEmtSubmissionMonth(3);
    form.setEmtSubmissionDay(18);
    form.setEnvironmentalConditions(EnumSet.allOf(EnvironmentalCondition.class));
    form.setDecommissioningConditions(EnumSet.allOf(DecommissioningCondition.class));
    return form;
  }
}