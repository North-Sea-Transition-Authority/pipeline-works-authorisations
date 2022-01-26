package uk.co.ogauthority.pwa.features.application.tasks.projectinfo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class ProjectInformationEntityMappingServiceTest {

  private LocalDate baseDate;

  private ProjectInformationEntityMappingService projectInformationEntityMappingService;

  private ProjectInformationForm form;
  private ProjectInformationForm expectedForm;
  private PadProjectInformation entity;

  @Before
  public void setUp() {
    projectInformationEntityMappingService = new ProjectInformationEntityMappingService();
    baseDate = LocalDate.of(2020, 12, 1);

    form = new ProjectInformationForm();
    expectedForm = ProjectInformationTestUtils.buildForm(baseDate);
    entity = ProjectInformationTestUtils.buildEntity(baseDate);


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

    assertThat(form.getLicenceTransferDay()).isEqualTo(expectedForm.getLicenceTransferDay());
    assertThat(form.getLicenceTransferMonth()).isEqualTo(expectedForm.getLicenceTransferMonth());
    assertThat(form.getLicenceTransferYear()).isEqualTo(expectedForm.getLicenceTransferYear());

    assertThat(form.getCommercialAgreementDay()).isEqualTo(expectedForm.getCommercialAgreementDay());
    assertThat(form.getCommercialAgreementMonth()).isEqualTo(expectedForm.getCommercialAgreementMonth());
    assertThat(form.getCommercialAgreementYear()).isEqualTo(expectedForm.getCommercialAgreementYear());

    assertThat(form.getPermanentDepositsMadeType()).isEqualTo(PermanentDepositMade.LATER_APP);
    assertThat(form.getFutureSubmissionDate()).isEqualTo(expectedForm.getFutureSubmissionDate());
    assertThat(form.getTemporaryDepositsMade()).isEqualTo(expectedForm.getTemporaryDepositsMade());
    assertThat(form.getTemporaryDepDescription()).isEqualTo(expectedForm.getTemporaryDepDescription());
  }

  @Test
  public void setEntityValuesUsingForm_whenEntityHasNoMissingData() {

    var dateAsInstant = baseDate.atStartOfDay(ZoneId.systemDefault()).toInstant();

    projectInformationEntityMappingService.setEntityValuesUsingForm(entity, expectedForm);

    assertThat(entity.getProjectName()).isEqualTo(expectedForm.getProjectName());
    assertThat(entity.getProjectOverview()).isEqualTo(expectedForm.getProjectOverview());
    assertThat(entity.getMethodOfPipelineDeployment()).isEqualTo(expectedForm.getMethodOfPipelineDeployment());
    assertThat(entity.getProposedStartTimestamp()).isEqualTo(
        dateAsInstant.plus(ProjectInformationTestUtils.PROPOSED_START_DAY_MODIFIER, ChronoUnit.DAYS));

    assertThat(entity.getMobilisationTimestamp()).isEqualTo(
        dateAsInstant.plus(ProjectInformationTestUtils.MOBILISATION_DAY_MODIFIER, ChronoUnit.DAYS));

    assertThat(entity.getEarliestCompletionTimestamp()).isEqualTo(
        dateAsInstant.plus(ProjectInformationTestUtils.EARLIEST_COMPLETION_DAY_MODIFIER, ChronoUnit.DAYS));

    assertThat(entity.getLatestCompletionTimestamp()).isEqualTo(
        dateAsInstant.plus(ProjectInformationTestUtils.LATEST_COMPLETION_DAY_MODIFIER, ChronoUnit.DAYS));

    assertThat(entity.getLicenceTransferPlanned()).isEqualTo(true);

    assertThat(entity.getLicenceTransferTimestamp()).isEqualTo(
        dateAsInstant.plus(ProjectInformationTestUtils.LICENCE_TRANSFER_DAY_MODIFIER, ChronoUnit.DAYS));

    assertThat(entity.getCommercialAgreementTimestamp()).isEqualTo(
        dateAsInstant.plus(ProjectInformationTestUtils.COMMERCIAL_AGREEMENT_DAY_MODIFIER, ChronoUnit.DAYS));

    assertThat(entity.getPermanentDepositsMade()).isEqualTo(PermanentDepositMade.LATER_APP);

    assertThat(entity.getFutureAppSubmissionMonth()).isEqualTo(
            Integer.parseInt(expectedForm.getFutureSubmissionDate().getMonth()));

    assertThat(entity.getFutureAppSubmissionYear()).isEqualTo(
            Integer.parseInt(expectedForm.getFutureSubmissionDate().getYear()));

    assertThat(entity.getTemporaryDepositsMade()).isEqualTo(
            expectedForm.getTemporaryDepositsMade());

    assertThat(entity.getTemporaryDepDescription()).isEqualTo(
        expectedForm.getTemporaryDepDescription());
  }


  @Test
  public void setEntityValuesUsingForm_whenLicenceTransferIsNotPlanned() {

    form.setLicenceTransferPlanned(false);
    // below values dont matter but proves entity values are not set
    form.setLicenceTransferDay(1);
    form.setLicenceTransferMonth(1);
    form.setLicenceTransferYear(2020);

    form.setCommercialAgreementDay(1);
    form.setCommercialAgreementMonth(1);
    form.setCommercialAgreementYear(2020);

    projectInformationEntityMappingService.setEntityValuesUsingForm(entity, form);

    assertThat(entity.getLicenceTransferPlanned()).isFalse();
    assertThat(entity.getLicenceTransferTimestamp()).isNull();
    assertThat(entity.getCommercialAgreementTimestamp()).isNull();
  }

  @Test
  public void setEntityValuesUsingForm_permanentDepositsTypeIsThisApp(){
    form.setPermanentDepositsMadeType(PermanentDepositMade.THIS_APP);
    projectInformationEntityMappingService.setEntityValuesUsingForm(entity, form);
    assertThat(entity.getPermanentDepositsMade()).isEqualTo(PermanentDepositMade.THIS_APP);
    assertThat(entity.getFutureAppSubmissionMonth()).isNull();
    assertThat(entity.getFutureAppSubmissionYear()).isNull();
  }

  @Test
  public void setEntityValuesUsingForm_noPermanentDeposits(){
    form.setPermanentDepositsMadeType(PermanentDepositMade.NONE);
    projectInformationEntityMappingService.setEntityValuesUsingForm(entity, form);
    assertThat(entity.getPermanentDepositsMade()).isEqualTo(PermanentDepositMade.NONE);
    assertThat(entity.getFutureAppSubmissionMonth()).isNull();
    assertThat(entity.getFutureAppSubmissionYear()).isNull();
  }

  @Test
  public void setEntityValuesUsingForm_TemporaryDeposits(){
    form.setTemporaryDepositsMade(false);
    projectInformationEntityMappingService.setEntityValuesUsingForm(entity, form);
    assertThat(entity.getTemporaryDepDescription()).isNull();
  }

  @Test
  public void mapProjectInformationDataToForm_noPermanentDeposits() {
    entity.setPermanentDepositsMade(PermanentDepositMade.NONE);
    projectInformationEntityMappingService.mapProjectInformationDataToForm(entity, form);
    assertThat(form.getPermanentDepositsMadeType()).isEqualTo(PermanentDepositMade.NONE);
  }

  @Test
  public void mapProjectInformationDataToForm_permanentDepositsTypeIsThisApp() {
    entity.setPermanentDepositsMade(PermanentDepositMade.THIS_APP);
    entity.setFutureAppSubmissionMonth(null);
    entity.setFutureAppSubmissionYear(null);
    projectInformationEntityMappingService.mapProjectInformationDataToForm(entity, form);
    assertThat(form.getPermanentDepositsMadeType()).isEqualTo(PermanentDepositMade.THIS_APP);
  }


  @Test
  public void mapProjectInformationDataToForm_TemporaryDeposits() {
    entity.setTemporaryDepositsMade(true);
    entity.setTemporaryDepDescription("foo");
    projectInformationEntityMappingService.mapProjectInformationDataToForm(entity, form);
    assertThat(form.getTemporaryDepositsMade()).isTrue();
    assertThat(form.getTemporaryDepDescription()).isEqualTo(entity.getTemporaryDepDescription());
  }


  @Test
  public void setEntityValuesUsingForm_fdpQuestionRequired_fdpOptionSelected() {
    form.setFdpOptionSelected(true);
    form.setFdpConfirmationFlag(true);
    projectInformationEntityMappingService.setEntityValuesUsingForm(entity, form);
    assertTrue(entity.getFdpOptionSelected());
    assertTrue(entity.getFdpConfirmationFlag());
  }

  @Test
  public void setEntityValuesUsingForm_fdpQuestionRequired_fdpOptionSelectedIsNo() {
    form.setFdpOptionSelected(false);
    form.setFdpNotSelectedReason("my reason");
    projectInformationEntityMappingService.setEntityValuesUsingForm(entity, form);
    assertFalse(entity.getFdpOptionSelected());
    assertThat(entity.getFdpNotSelectedReason()).isEqualTo("my reason");
  }

  @Test
  public void mapProjectInformationDataToForm_fdpQuestionRequired_fdpOptionSelected() {
    entity.setFdpOptionSelected(true);
    entity.setFdpConfirmationFlag(true);
    projectInformationEntityMappingService.mapProjectInformationDataToForm(entity, form);
    assertTrue(form.getFdpOptionSelected());
    assertTrue(form.getFdpConfirmationFlag());
  }

  @Test
  public void mapProjectInformationDataToForm_fdpQuestionRequired_fdpOptionSelectedIsNo() {
    entity.setFdpOptionSelected(false);
    entity.setFdpNotSelectedReason("my reason");
    projectInformationEntityMappingService.mapProjectInformationDataToForm(entity, form);
    assertFalse(form.getFdpOptionSelected());
    assertThat(form.getFdpNotSelectedReason()).isEqualTo("my reason");
  }




}