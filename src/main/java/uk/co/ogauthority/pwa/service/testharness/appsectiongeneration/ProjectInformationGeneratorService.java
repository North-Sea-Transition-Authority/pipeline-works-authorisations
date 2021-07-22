package uk.co.ogauthority.pwa.service.testharness.appsectiongeneration;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.enums.ProjectInformationQuestion;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadProjectInformation;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadProjectInformationRepository;
import uk.co.ogauthority.pwa.service.enums.projectinformation.PermanentDepositMade;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.projectinformation.PadProjectInformationService;
import uk.co.ogauthority.pwa.service.testharness.filehelper.TestHarnessFileService;

@Service
@Profile("development")
public class ProjectInformationGeneratorService {

  private final PadProjectInformationService padProjectInformationService;
  private final PadProjectInformationRepository padProjectInformationRepository;
  private final TestHarnessFileService testHarnessFileService;

  @Autowired
  public ProjectInformationGeneratorService(
      PadProjectInformationService padProjectInformationService,
      PadProjectInformationRepository padProjectInformationRepository,
      TestHarnessFileService testHarnessFileService) {
    this.padProjectInformationService = padProjectInformationService;
    this.padProjectInformationRepository = padProjectInformationRepository;
    this.testHarnessFileService = testHarnessFileService;
  }



  public void generateProjectInformation(WebUserAccount user, PwaApplicationDetail pwaApplicationDetail) {

    var padProjectInfo = new PadProjectInformation();
    setProjectInformationData(user, pwaApplicationDetail, padProjectInfo);
    padProjectInformationRepository.save(padProjectInfo);
  }


  private void setProjectInformationData(WebUserAccount user, PwaApplicationDetail pwaApplicationDetail,
                                         PadProjectInformation padProjectInfo) {

    var requiredQuestions = padProjectInformationService.getRequiredQuestions(
        pwaApplicationDetail.getPwaApplicationType());

    padProjectInfo.setPwaApplicationDetail(pwaApplicationDetail);

    if (requiredQuestions.contains(ProjectInformationQuestion.PROJECT_NAME)) {
      padProjectInfo.setProjectName("My test project nName");
    }

    var proposedStartDate = Instant.now();
    if (requiredQuestions.contains(ProjectInformationQuestion.PROPOSED_START_DATE)) {
      padProjectInfo.setProposedStartTimestamp(proposedStartDate);
    }

    if (requiredQuestions.contains(ProjectInformationQuestion.PROJECT_OVERVIEW)) {
      padProjectInfo.setProjectOverview("My test project overview");
    }

    if (requiredQuestions.contains(ProjectInformationQuestion.METHOD_OF_PIPELINE_DEPLOYMENT)) {
      padProjectInfo.setMethodOfPipelineDeployment("My method of pipeline deployment");
    }

    if (requiredQuestions.contains(ProjectInformationQuestion.MOBILISATION_DATE)) {
      padProjectInfo.setMobilisationTimestamp(Instant.now());
    }

    var earliestCompletionDate = proposedStartDate.plus(1, ChronoUnit.DAYS);
    if (requiredQuestions.contains(ProjectInformationQuestion.EARLIEST_COMPLETION_DATE)) {
      padProjectInfo.setEarliestCompletionTimestamp(earliestCompletionDate);
    }

    if (requiredQuestions.contains(ProjectInformationQuestion.LATEST_COMPLETION_DATE)) {
      padProjectInfo.setLatestCompletionTimestamp(earliestCompletionDate.plus(1, ChronoUnit.DAYS));
    }

    if (requiredQuestions.contains(ProjectInformationQuestion.LICENCE_TRANSFER_PLANNED)) {
      padProjectInfo.setLicenceTransferPlanned(false);
    }

    if (requiredQuestions.contains(ProjectInformationQuestion.USING_CAMPAIGN_APPROACH)) {
      padProjectInfo.setUsingCampaignApproach(false);
    }

    if (requiredQuestions.contains(ProjectInformationQuestion.PERMANENT_DEPOSITS_BEING_MADE)) {
      padProjectInfo.setPermanentDepositsMade(PermanentDepositMade.NONE);
    }

    if (requiredQuestions.contains(ProjectInformationQuestion.TEMPORARY_DEPOSITS_BEING_MADE)) {
      padProjectInfo.setTemporaryDepositsMade(false);
    }

    if (requiredQuestions.contains(ProjectInformationQuestion.FIELD_DEVELOPMENT_PLAN)) {
      padProjectInfo.setFdpOptionSelected(true);
      padProjectInfo.setFdpConfirmationFlag(true);
    }


    if (requiredQuestions.contains(ProjectInformationQuestion.PROJECT_LAYOUT_DIAGRAM)) {
      testHarnessFileService.generateImageUpload(user, pwaApplicationDetail, ApplicationDetailFilePurpose.PROJECT_INFORMATION);
    }

  }





}
