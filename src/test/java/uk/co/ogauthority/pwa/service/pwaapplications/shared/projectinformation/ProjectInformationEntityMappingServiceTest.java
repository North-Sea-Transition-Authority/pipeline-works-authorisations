package uk.co.ogauthority.pwa.service.pwaapplications.shared.projectinformation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadProjectInformation;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.ProjectInformationForm;
import uk.co.ogauthority.pwa.service.enums.projectinformation.PermanentDeposits;

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
    baseDate = LocalDate.now();

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

    assertThat(form.getPermanentDepositsMadeType()).isEqualTo(PermanentDeposits.LATER_APP);
    assertThat(form.getFutureAppSubmissionMonth()).isEqualTo(expectedForm.getFutureAppSubmissionMonth());
    assertThat(form.getFutureAppSubmissionYear()).isEqualTo(expectedForm.getFutureAppSubmissionYear());
    assertThat(form.getIsTemporaryDepositsMade()).isEqualTo(expectedForm.getIsTemporaryDepositsMade());
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

    assertThat(entity.getPermanentDepositsMade()).isEqualTo(
            true);

    assertThat(entity.getFutureAppSubmissionMonth()).isEqualTo(
            expectedForm.getFutureAppSubmissionMonth());

    assertThat(entity.getFutureAppSubmissionYear()).isEqualTo(
            expectedForm.getFutureAppSubmissionYear());

    assertThat(entity.getTemporaryDepositsMade()).isEqualTo(
            expectedForm.getIsTemporaryDepositsMade());

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
    form.setPermanentDepositsMadeType(PermanentDeposits.THIS_APP);
    projectInformationEntityMappingService.setEntityValuesUsingForm(entity, form);
    assertThat(entity.getPermanentDepositsMade().equals(PermanentDeposits.THIS_APP));
    assertThat(entity.getFutureAppSubmissionMonth()).isNull();
    assertThat(entity.getFutureAppSubmissionYear()).isNull();
  }

  @Test
  public void setEntityValuesUsingForm_noPermanentDeposits(){
    form.setPermanentDepositsMadeType(PermanentDeposits.NONE);
    projectInformationEntityMappingService.setEntityValuesUsingForm(entity, form);
    assertThat(entity.getPermanentDepositsMade().equals(PermanentDeposits.NONE));
    assertThat(entity.getFutureAppSubmissionMonth()).isNull();
    assertThat(entity.getFutureAppSubmissionYear()).isNull();
  }

  @Test
  public void setEntityValuesUsingForm_TemporaryDeposits(){
    form.setIsTemporaryDepositsMade(false);
    projectInformationEntityMappingService.setEntityValuesUsingForm(entity, form);
    assertThat(entity.getTemporaryDepDescription()).isNull();
  }

  @Test
  public void mapProjectInformationDataToForm_noPermanentDeposits() {
    entity.setPermanentDepositsMade(false);
    projectInformationEntityMappingService.mapProjectInformationDataToForm(entity, form);
    assertThat(form.getPermanentDepositsMadeType()).isEqualTo(PermanentDeposits.NONE);
  }

  @Test
  public void mapProjectInformationDataToForm_permanentDepositsTypeIsThisApp() {
    entity.setPermanentDepositsMade(true);
    entity.setFutureAppSubmissionMonth(null);
    entity.setFutureAppSubmissionYear(null);
    projectInformationEntityMappingService.mapProjectInformationDataToForm(entity, form);
    assertThat(form.getPermanentDepositsMadeType()).isEqualTo(PermanentDeposits.THIS_APP);
  }

}