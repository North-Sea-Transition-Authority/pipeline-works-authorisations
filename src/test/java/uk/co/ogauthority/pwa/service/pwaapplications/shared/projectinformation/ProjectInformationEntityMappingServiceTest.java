package uk.co.ogauthority.pwa.service.pwaapplications.shared.projectinformation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadProjectInformation;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.ProjectInformationForm;

@RunWith(MockitoJUnitRunner.class)
public class ProjectInformationEntityMappingServiceTest {

  private LocalDate date;

  private ProjectInformationEntityMappingService projectInformationEntityMappingService;

  private ProjectInformationForm form;
  private ProjectInformationForm expectedForm;
  private PadProjectInformation entity;

  @Before
  public void setUp() {
    projectInformationEntityMappingService = new ProjectInformationEntityMappingService();
    date = LocalDate.now();

    form = new ProjectInformationForm();
    expectedForm = ProjectInformationTestUtils.buildForm(date);
    entity = ProjectInformationTestUtils.buildEntity(date);
  }

  @Test
  public void mapProjectInformationDataToForm_basicFieldMapping_whenAllDateFieldsHaveValues() {

    projectInformationEntityMappingService.mapProjectInformationDataToForm(entity, form);

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
  public void setEntityValuesUsingForm_whenEntityHasNoMissingData() {

    var dateAsInstant = date.atStartOfDay(ZoneId.systemDefault()).toInstant();

    projectInformationEntityMappingService.setEntityValuesUsingForm(entity, expectedForm);

    assertThat(entity.getProjectName()).isEqualTo(expectedForm.getProjectName());
    assertThat(entity.getProjectOverview()).isEqualTo(expectedForm.getProjectOverview());
    assertThat(entity.getMethodOfPipelineDeployment()).isEqualTo(expectedForm.getMethodOfPipelineDeployment());
    assertThat(entity.getProposedStartTimestamp()).isEqualTo(dateAsInstant);
    assertThat(entity.getMobilisationTimestamp()).isEqualTo(dateAsInstant);
    assertThat(entity.getEarliestCompletionTimestamp()).isEqualTo(dateAsInstant);
    assertThat(entity.getLatestCompletionTimestamp()).isEqualTo(dateAsInstant);

  }

}