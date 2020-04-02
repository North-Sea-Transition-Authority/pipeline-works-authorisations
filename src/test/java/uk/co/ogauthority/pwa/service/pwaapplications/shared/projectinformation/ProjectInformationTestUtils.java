package uk.co.ogauthority.pwa.service.pwaapplications.shared.projectinformation;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadProjectInformation;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.ProjectInformationForm;

public class ProjectInformationTestUtils {

  public static ProjectInformationForm buildForm(LocalDate date) {
    var form = new ProjectInformationForm();
    form.setProjectName("Name");
    form.setProjectOverview("Overview");
    form.setMethodOfPipelineDeployment("Method");
    form.setUsingCampaignApproach(true);

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

    form.setUploadedFileWithDescriptionForms(new ArrayList<>());

    return form;
  }

  public static PadProjectInformation buildEntity(LocalDate date) {
    var entity = new PadProjectInformation();

    entity.setProjectName("Name");
    entity.setProjectOverview("Overview");
    entity.setMethodOfPipelineDeployment("Method");
    entity.setUsingCampaignApproach(true);

    var instant = date.atStartOfDay(ZoneId.systemDefault()).toInstant();

    entity.setProposedStartTimestamp(instant);
    entity.setMobilisationTimestamp(instant);
    entity.setEarliestCompletionTimestamp(instant);
    entity.setLatestCompletionTimestamp(instant);

    return entity;
  }

}
