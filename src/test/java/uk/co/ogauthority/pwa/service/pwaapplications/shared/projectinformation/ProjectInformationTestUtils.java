package uk.co.ogauthority.pwa.service.pwaapplications.shared.projectinformation;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadProjectInformation;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.ProjectInformationForm;
import uk.co.ogauthority.pwa.service.enums.projectinformation.PermanentDepositMade;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.TwoFieldDateInput;

public class ProjectInformationTestUtils {

  public static final int PROPOSED_START_DAY_MODIFIER = 1;
  public static final int MOBILISATION_DAY_MODIFIER = 2;
  public static final int EARLIEST_COMPLETION_DAY_MODIFIER = 3;
  public static final int LATEST_COMPLETION_DAY_MODIFIER = 4;
  public static final int LICENCE_TRANSFER_DAY_MODIFIER = 5;
  public static final int COMMERCIAL_AGREEMENT_DAY_MODIFIER = 6;

  public static ProjectInformationForm buildForm(LocalDate baseDate) {
    var form = new ProjectInformationForm();
    form.setProjectName("Name");
    form.setProjectOverview("Overview");
    form.setMethodOfPipelineDeployment("Method");
    form.setUsingCampaignApproach(true);

    var proposedStart = baseDate.plusDays(PROPOSED_START_DAY_MODIFIER);
    form.setProposedStartDay(proposedStart.getDayOfMonth());
    form.setProposedStartMonth(proposedStart.getMonthValue());
    form.setProposedStartYear(proposedStart.getYear());

    var mobilisationDate = baseDate.plusDays(MOBILISATION_DAY_MODIFIER);
    form.setMobilisationDay(mobilisationDate.getDayOfMonth());
    form.setMobilisationMonth(mobilisationDate.getMonthValue());
    form.setMobilisationYear(mobilisationDate.getYear());

    var earliestCompletionDate = baseDate.plusDays(EARLIEST_COMPLETION_DAY_MODIFIER);
    form.setEarliestCompletionDay(earliestCompletionDate.getDayOfMonth());
    form.setEarliestCompletionMonth(earliestCompletionDate.getMonthValue());
    form.setEarliestCompletionYear(earliestCompletionDate.getYear());

    var latestCompletionDate = baseDate.plusDays(LATEST_COMPLETION_DAY_MODIFIER);
    form.setLatestCompletionDay(latestCompletionDate.getDayOfMonth());
    form.setLatestCompletionMonth(latestCompletionDate.getMonthValue());
    form.setLatestCompletionYear(latestCompletionDate.getYear());

    var licenceTransferDate = baseDate.plusDays(LICENCE_TRANSFER_DAY_MODIFIER);
    form.setLicenceTransferPlanned(true);
    form.setLicenceTransferDay(licenceTransferDate.getDayOfMonth());
    form.setLicenceTransferMonth(licenceTransferDate.getMonthValue());
    form.setLicenceTransferYear(licenceTransferDate.getYear());

    var commercialAgreementDate = baseDate.plusDays(COMMERCIAL_AGREEMENT_DAY_MODIFIER);
    form.setCommercialAgreementDay(commercialAgreementDate.getDayOfMonth());
    form.setCommercialAgreementMonth(commercialAgreementDate.getMonthValue());
    form.setCommercialAgreementYear(commercialAgreementDate.getYear());

    form.setUploadedFileWithDescriptionForms(new ArrayList<>());

    form.setPermanentDepositsMadeType(PermanentDepositMade.LATER_APP);
    form.setFutureSubmissionDate(new TwoFieldDateInput(2020, 7));
    form.setTemporaryDepositsMade(true);
    form.setTemporaryDepDescription("some description..");

    return form;
  }

  public static PadProjectInformation buildEntity(LocalDate date) {
    var entity = new PadProjectInformation();

    entity.setProjectName("Name");
    entity.setProjectOverview("Overview");
    entity.setMethodOfPipelineDeployment("Method");
    entity.setUsingCampaignApproach(true);

    var instant = date.atStartOfDay(ZoneId.systemDefault()).toInstant();

    entity.setProposedStartTimestamp(instant.plus(PROPOSED_START_DAY_MODIFIER, ChronoUnit.DAYS));
    entity.setMobilisationTimestamp(instant.plus(MOBILISATION_DAY_MODIFIER, ChronoUnit.DAYS));
    entity.setEarliestCompletionTimestamp(instant.plus(EARLIEST_COMPLETION_DAY_MODIFIER, ChronoUnit.DAYS));
    entity.setLatestCompletionTimestamp(instant.plus(LATEST_COMPLETION_DAY_MODIFIER, ChronoUnit.DAYS));

    entity.setLicenceTransferPlanned(true);
    entity.setLicenceTransferTimestamp(instant.plus(LICENCE_TRANSFER_DAY_MODIFIER, ChronoUnit.DAYS));
    entity.setCommercialAgreementTimestamp(instant.plus(COMMERCIAL_AGREEMENT_DAY_MODIFIER, ChronoUnit.DAYS));

    entity.setPermanentDepositsMade(PermanentDepositMade.LATER_APP);
    entity.setFutureAppSubmissionMonth(7);
    entity.setFutureAppSubmissionYear(2020);
    entity.setTemporaryDepositsMade(true);
    entity.setTemporaryDepDescription("some description..");

    return entity;
  }

}
