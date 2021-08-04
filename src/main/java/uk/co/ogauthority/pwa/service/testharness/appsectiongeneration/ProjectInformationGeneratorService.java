package uk.co.ogauthority.pwa.service.testharness.appsectiongeneration;

import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.enums.ProjectInformationQuestion;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.ProjectInformationForm;
import uk.co.ogauthority.pwa.service.enums.projectinformation.PermanentDepositMade;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.projectinformation.PadProjectInformationService;
import uk.co.ogauthority.pwa.service.testharness.TestHarnessAppFormService;
import uk.co.ogauthority.pwa.service.testharness.TestHarnessAppFormServiceParams;
import uk.co.ogauthority.pwa.service.testharness.filehelper.TestHarnessPadFileService;

@Service
@Profile("test-harness")
class ProjectInformationGeneratorService implements TestHarnessAppFormService {

  private final PadProjectInformationService padProjectInformationService;
  private final TestHarnessPadFileService testHarnessPadFileService;

  private static final ApplicationTask linkedAppFormTask = ApplicationTask.PROJECT_INFORMATION;

  @Autowired
  public ProjectInformationGeneratorService(
      PadProjectInformationService padProjectInformationService,
      TestHarnessPadFileService testHarnessPadFileService) {
    this.padProjectInformationService = padProjectInformationService;
    this.testHarnessPadFileService = testHarnessPadFileService;
  }


  @Override
  public ApplicationTask getLinkedAppFormTask() {
    return linkedAppFormTask;
  }


  @Override
  public void generateAppFormData(TestHarnessAppFormServiceParams appFormServiceParams) {

    var form = createForm(appFormServiceParams.getUser(), appFormServiceParams.getApplicationDetail());
    var entity = padProjectInformationService.getPadProjectInformationData(appFormServiceParams.getApplicationDetail());
    padProjectInformationService.saveEntityUsingForm(entity, form, appFormServiceParams.getUser());
  }


  private ProjectInformationForm createForm(WebUserAccount user, PwaApplicationDetail pwaApplicationDetail) {

    var form = new ProjectInformationForm();

    var requiredQuestions = padProjectInformationService.getRequiredQuestions(
        pwaApplicationDetail.getPwaApplicationType());

    if (requiredQuestions.contains(ProjectInformationQuestion.PROJECT_NAME)) {
      form.setProjectName("My test project name");
    }

    var proposedStartDate = LocalDate.now();
    if (requiredQuestions.contains(ProjectInformationQuestion.PROPOSED_START_DATE)) {
      form.setProposedStartDay(proposedStartDate.getDayOfMonth());
      form.setProposedStartMonth(proposedStartDate.getMonthValue());
      form.setProposedStartYear(proposedStartDate.getYear());
    }

    if (requiredQuestions.contains(ProjectInformationQuestion.PROJECT_OVERVIEW)) {
      form.setProjectOverview("My test project overview");
    }

    if (requiredQuestions.contains(ProjectInformationQuestion.METHOD_OF_PIPELINE_DEPLOYMENT)) {
      form.setMethodOfPipelineDeployment("My method of pipeline deployment");
    }

    if (requiredQuestions.contains(ProjectInformationQuestion.MOBILISATION_DATE)) {
      form.setMobilisationDay(LocalDate.now().getDayOfMonth());
      form.setMobilisationMonth(LocalDate.now().getMonthValue());
      form.setMobilisationYear(LocalDate.now().getYear());
    }

    var earliestCompletionDate = proposedStartDate.plusDays(1);
    if (requiredQuestions.contains(ProjectInformationQuestion.EARLIEST_COMPLETION_DATE)) {
      form.setEarliestCompletionDay(earliestCompletionDate.getDayOfMonth());
      form.setEarliestCompletionMonth(earliestCompletionDate.getMonthValue());
      form.setEarliestCompletionYear(earliestCompletionDate.getYear());
    }

    if (requiredQuestions.contains(ProjectInformationQuestion.LATEST_COMPLETION_DATE)) {
      var latestCompletionDate = earliestCompletionDate.plusDays(1);
      form.setLatestCompletionDay(latestCompletionDate.getDayOfMonth());
      form.setLatestCompletionMonth(latestCompletionDate.getMonthValue());
      form.setLatestCompletionYear(latestCompletionDate.getYear());
    }

    if (requiredQuestions.contains(ProjectInformationQuestion.LICENCE_TRANSFER_PLANNED)) {
      form.setLicenceTransferPlanned(false);
    }

    if (requiredQuestions.contains(ProjectInformationQuestion.USING_CAMPAIGN_APPROACH)) {
      form.setUsingCampaignApproach(false);
    }

    if (requiredQuestions.contains(ProjectInformationQuestion.PERMANENT_DEPOSITS_BEING_MADE)) {
      form.setPermanentDepositsMadeType(PermanentDepositMade.NONE);
    }

    if (requiredQuestions.contains(ProjectInformationQuestion.TEMPORARY_DEPOSITS_BEING_MADE)) {
      form.setTemporaryDepositsMade(false);
    }

    if (requiredQuestions.contains(ProjectInformationQuestion.FIELD_DEVELOPMENT_PLAN)) {
      form.setFdpOptionSelected(true);
      form.setFdpConfirmationFlag(true);
    }

    if (requiredQuestions.contains(ProjectInformationQuestion.PROJECT_LAYOUT_DIAGRAM)) {
      var generatedFileId = testHarnessPadFileService.generateImageUpload(
          user, pwaApplicationDetail, ApplicationDetailFilePurpose.PROJECT_INFORMATION);
      testHarnessPadFileService.setFileIdOnForm(generatedFileId, form.getUploadedFileWithDescriptionForms());
    }

    return form;
  }





}
