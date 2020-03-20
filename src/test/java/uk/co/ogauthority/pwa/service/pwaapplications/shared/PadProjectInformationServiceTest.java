package uk.co.ogauthority.pwa.service.pwaapplications.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadProjectInformation;
import uk.co.ogauthority.pwa.model.form.pwaapplications.initial.ProjectInformationForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadProjectInformationRepository;

@RunWith(MockitoJUnitRunner.class)
public class PadProjectInformationServiceTest {

  @Mock
  private PadProjectInformationRepository repository;

  private PadProjectInformationService service;
  private PadProjectInformation padProjectInformation;
  private PwaApplicationDetail pwaApplicationDetail;
  private LocalDate date;

  @Before
  public void setUp() {
    service = new PadProjectInformationService(repository);
    padProjectInformation = new PadProjectInformation();
    pwaApplicationDetail = new PwaApplicationDetail();
    date = LocalDate.now();
  }

  @Test
  public void save() {
    when(repository.save(padProjectInformation)).thenReturn(padProjectInformation);
    var result = service.save(padProjectInformation);
    assertThat(result).isEqualTo(padProjectInformation);
    verify(repository, times(1)).save(padProjectInformation);
  }

  @Test
  public void getPadProjectInformationData_WithExisting() {
    when(repository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(Optional.of(padProjectInformation));
    var result = service.getPadProjectInformationData(pwaApplicationDetail);
    assertThat(result).isEqualTo(padProjectInformation);
  }

  @Test
  public void getPadProjectInformationData_NoExisting() {
    when(repository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(Optional.empty());
    var result = service.getPadProjectInformationData(pwaApplicationDetail);
    assertThat(result).isNotEqualTo(padProjectInformation);
    assertThat(result.getPwaApplicationDetail()).isEqualTo(pwaApplicationDetail);
  }

  @Test
  public void mapEntityToForm() {
    var form = new ProjectInformationForm();
    var expectedForm  = buildForm();
    var entity = buildEntity();
    service.mapEntityToForm(entity, form);
    assertThat(form.getProjectName()).isEqualTo(expectedForm.getProjectName());
    assertThat(form.getProjectOverview()).isEqualTo(expectedForm.getProjectOverview());
    assertThat(form.getMethodOfPipelineDeployment()).isEqualTo(expectedForm.getMethodOfPipelineDeployment());

    assertThat(form.getProposedStartDay()).isEqualTo(expectedForm.getProposedStartDay());
    assertThat(form.getProposedStartMonth()).isEqualTo(expectedForm.getProposedStartMonth());
    assertThat(form.getProposedStartYear()).isEqualTo(expectedForm.getProposedStartYear());

    assertThat(form.getMobilisationDay()).isEqualTo(expectedForm.getMobilisationDay());
    assertThat(form.getMobilisationMonth()).isEqualTo(expectedForm.getMobilisationMonth());
    assertThat(form.getMobilisationYear()).isEqualTo(expectedForm.getMobilisationYear());

    assertThat(form.getEarliestCompletionDay()).isEqualTo(expectedForm.getEarliestCompletionDay());
    assertThat(form.getEarliestCompletionMonth()).isEqualTo(expectedForm.getEarliestCompletionMonth());
    assertThat(form.getEarliestCompletionYear()).isEqualTo(expectedForm.getEarliestCompletionYear());

    assertThat(form.getLatestCompletionDay()).isEqualTo(expectedForm.getLatestCompletionDay());
    assertThat(form.getLatestCompletionMonth()).isEqualTo(expectedForm.getLatestCompletionMonth());
    assertThat(form.getLatestCompletionYear()).isEqualTo(expectedForm.getLatestCompletionYear());
  }

  @Test
  public void saveEntityUsingForm() {
    var entity = new PadProjectInformation();
    var expectedEntity = buildEntity();
    var form = buildForm();
    service.saveEntityUsingForm(entity, form);

    assertThat(entity.getProjectName()).isEqualTo(expectedEntity.getProjectName());
    assertThat(entity.getProjectOverview()).isEqualTo(expectedEntity.getProjectOverview());
    assertThat(entity.getMethodOfPipelineDeployment()).isEqualTo(expectedEntity.getMethodOfPipelineDeployment());
    assertThat(entity.getProposedStartTimestamp()).isEqualTo(expectedEntity.getProposedStartTimestamp());
    assertThat(entity.getMobilisationTimestamp()).isEqualTo(expectedEntity.getMobilisationTimestamp());
    assertThat(entity.getEarliestCompletionTimestamp()).isEqualTo(expectedEntity.getEarliestCompletionTimestamp());
    assertThat(entity.getLatestCompletionTimestamp()).isEqualTo(expectedEntity.getLatestCompletionTimestamp());

    verify(repository, times(1)).save(entity);
  }

  private ProjectInformationForm buildForm() {
    var form = new ProjectInformationForm();
    form.setProjectName("Name");
    form.setProjectOverview("Overview");
    form.setMethodOfPipelineDeployment("Method");

    form.setProposedStartDay(date.getDayOfMonth());
    form.setProposedStartMonth(date.getMonthValue());
    form.setProposedStartYear(date.getYear());

    form.setMobilisationDay(date.getDayOfMonth());
    form.setMobilisationMonth(date.getMonthValue());
    form.setMobilisationYear(date.getYear());

    form.setEarliestCompletionDay(date.getDayOfMonth());
    form.setEarliestCompletionMonth(date.getMonthValue());
    form.setEarliestCompletionYear(date.getYear());

    form.setLatestCompletionDay(date.getDayOfMonth());
    form.setLatestCompletionMonth(date.getMonthValue());
    form.setLatestCompletionYear(date.getYear());

    return form;
  }

  private PadProjectInformation buildEntity() {
    var entity = new PadProjectInformation();

    entity.setProjectName("Name");
    entity.setProjectOverview("Overview");
    entity.setMethodOfPipelineDeployment("Method");

    var instant = Instant.ofEpochSecond(date.toEpochSecond(LocalTime.MIDNIGHT, ZoneOffset.UTC));

    entity.setProposedStartTimestamp(instant);
    entity.setMobilisationTimestamp(instant);
    entity.setEarliestCompletionTimestamp(instant);
    entity.setLatestCompletionTimestamp(instant);

    return entity;
  }
}