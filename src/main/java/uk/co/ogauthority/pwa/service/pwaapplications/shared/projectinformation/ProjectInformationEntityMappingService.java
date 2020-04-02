package uk.co.ogauthority.pwa.service.pwaapplications.shared.projectinformation;

import java.time.LocalDate;
import java.time.ZoneId;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadProjectInformation;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.ProjectInformationForm;
import uk.co.ogauthority.pwa.util.DateUtils;

/**
 * Mapping of form data to entity and entity to form data for project information application form.
 */
@Service
public class ProjectInformationEntityMappingService {

  /**
   * Map project information stored data to form.
   */
  void mapProjectInformationDataToForm(PadProjectInformation padProjectInformation,
                                              ProjectInformationForm form) {
    form.setProjectOverview(padProjectInformation.getProjectOverview());
    form.setProjectName(padProjectInformation.getProjectName());
    form.setMethodOfPipelineDeployment(padProjectInformation.getMethodOfPipelineDeployment());
    form.setUsingCampaignApproach(padProjectInformation.getUsingCampaignApproach());

    if (padProjectInformation.getProposedStartTimestamp() != null) {
      var date = LocalDate.ofInstant(padProjectInformation.getProposedStartTimestamp(), ZoneId.systemDefault());
      form.setProposedStartDay(date.getDayOfMonth());
      form.setProposedStartMonth(date.getMonthValue());
      form.setProposedStartYear(date.getYear());
    }
    if (padProjectInformation.getMobilisationTimestamp() != null) {
      var date = LocalDate.ofInstant(padProjectInformation.getMobilisationTimestamp(), ZoneId.systemDefault());
      form.setMobilisationDay(date.getDayOfMonth());
      form.setMobilisationMonth(date.getMonthValue());
      form.setMobilisationYear(date.getYear());
    }
    if (padProjectInformation.getEarliestCompletionTimestamp() != null) {
      var date = LocalDate.ofInstant(padProjectInformation.getEarliestCompletionTimestamp(), ZoneId.systemDefault());
      form.setEarliestCompletionDay(date.getDayOfMonth());
      form.setEarliestCompletionMonth(date.getMonthValue());
      form.setEarliestCompletionYear(date.getYear());
    }
    if (padProjectInformation.getLatestCompletionTimestamp() != null) {
      var date = LocalDate.ofInstant(padProjectInformation.getLatestCompletionTimestamp(), ZoneId.systemDefault());
      form.setLatestCompletionDay(date.getDayOfMonth());
      form.setLatestCompletionMonth(date.getMonthValue());
      form.setLatestCompletionYear(date.getYear());
    }
  }


  /**
   * Map Project Information form data to entity.
   */
  void setEntityValuesUsingForm(PadProjectInformation padProjectInformation, ProjectInformationForm form) {
    padProjectInformation.setProjectOverview(form.getProjectOverview());
    padProjectInformation.setProjectName(form.getProjectName());
    padProjectInformation.setMethodOfPipelineDeployment(form.getMethodOfPipelineDeployment());
    padProjectInformation.setUsingCampaignApproach(form.getUsingCampaignApproach());

    // TODO: PWA-379
    DateUtils.consumeInstantFromIntegersElseNull(
        form.getProposedStartYear(),
        form.getProposedStartMonth(),
        form.getProposedStartDay(),
        padProjectInformation::setProposedStartTimestamp
    );

    DateUtils.consumeInstantFromIntegersElseNull(
        form.getMobilisationYear(),
        form.getMobilisationMonth(),
        form.getMobilisationDay(),
        padProjectInformation::setMobilisationTimestamp
    );

    DateUtils.consumeInstantFromIntegersElseNull(
        form.getEarliestCompletionYear(),
        form.getEarliestCompletionMonth(),
        form.getEarliestCompletionDay(),
        padProjectInformation::setEarliestCompletionTimestamp
    );

    DateUtils.consumeInstantFromIntegersElseNull(
        form.getLatestCompletionYear(),
        form.getLatestCompletionMonth(),
        form.getLatestCompletionDay(),
        padProjectInformation::setLatestCompletionTimestamp
    );

  }

}
