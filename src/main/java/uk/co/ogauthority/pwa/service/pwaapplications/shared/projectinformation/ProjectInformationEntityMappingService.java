package uk.co.ogauthority.pwa.service.pwaapplications.shared.projectinformation;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadProjectInformation;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.ProjectInformationForm;
import uk.co.ogauthority.pwa.service.enums.projectinformation.PermanentDeposits;
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

    DateUtils.setYearMonthDayFromInstant(
        form::setProposedStartYear,
        form::setProposedStartMonth,
        form::setProposedStartDay,
        padProjectInformation.getProposedStartTimestamp()
    );

    DateUtils.setYearMonthDayFromInstant(
        form::setMobilisationYear,
        form::setMobilisationMonth,
        form::setMobilisationDay,
        padProjectInformation.getMobilisationTimestamp()
    );

    DateUtils.setYearMonthDayFromInstant(
        form::setEarliestCompletionYear,
        form::setEarliestCompletionMonth,
        form::setEarliestCompletionDay,
        padProjectInformation.getEarliestCompletionTimestamp()
    );

    DateUtils.setYearMonthDayFromInstant(
        form::setLatestCompletionYear,
        form::setLatestCompletionMonth,
        form::setLatestCompletionDay,
        padProjectInformation.getLatestCompletionTimestamp()
    );


    form.setLicenceTransferPlanned(padProjectInformation.getLicenceTransferPlanned());
    if (BooleanUtils.isTrue(padProjectInformation.getLicenceTransferPlanned())) {
      DateUtils.setYearMonthDayFromInstant(
          form::setLicenceTransferYear,
          form::setLicenceTransferMonth,
          form::setLicenceTransferDay,
          padProjectInformation.getLicenceTransferTimestamp()
      );

      DateUtils.setYearMonthDayFromInstant(
          form::setCommercialAgreementYear,
          form::setCommercialAgreementMonth,
          form::setCommercialAgreementDay,
          padProjectInformation.getCommercialAgreementTimestamp()
      );
    }

    if (padProjectInformation.getPermanentDepositsMade() != null) {
      if (padProjectInformation.getPermanentDepositsMade() == false) {
        form.setPermanentDepositsMadeType(PermanentDeposits.NONE);
      } else if (padProjectInformation.getFutureAppSubmissionMonth() != null
              && padProjectInformation.getFutureAppSubmissionYear() != null) {
        form.setPermanentDepositsMadeType(PermanentDeposits.LATER_APP);
        form.setFutureAppSubmissionMonth(padProjectInformation.getFutureAppSubmissionMonth());
        form.setFutureAppSubmissionYear(padProjectInformation.getFutureAppSubmissionYear());
      } else {
        form.setPermanentDepositsMadeType(PermanentDeposits.THIS_APP);
      }
    }
    if (padProjectInformation.getTemporaryDepositsMade() != null) {
      form.setIsTemporaryDepositsMade(padProjectInformation.getTemporaryDepositsMade());
      form.setTemporaryDepDescription(padProjectInformation.getTemporaryDepDescription());
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

    padProjectInformation.setLicenceTransferPlanned(form.getLicenceTransferPlanned());
    if (BooleanUtils.isTrue(form.getLicenceTransferPlanned())) {

      DateUtils.consumeInstantFromIntegersElseNull(
          form.getLicenceTransferYear(),
          form.getLicenceTransferMonth(),
          form.getLicenceTransferDay(),
          padProjectInformation::setLicenceTransferTimestamp
      );

      DateUtils.consumeInstantFromIntegersElseNull(
          form.getCommercialAgreementYear(),
          form.getCommercialAgreementMonth(),
          form.getCommercialAgreementDay(),
          padProjectInformation::setCommercialAgreementTimestamp
      );
    } else {
      padProjectInformation.setLicenceTransferTimestamp(null);
      padProjectInformation.setCommercialAgreementTimestamp(null);
    }

    if (form.getPermanentDepositsMadeType() != null) {
      padProjectInformation.setFutureAppSubmissionMonth(null);
      padProjectInformation.setFutureAppSubmissionYear(null);
      padProjectInformation.setPermanentDepositsMade(true);
      if (form.getPermanentDepositsMadeType().equals(PermanentDeposits.LATER_APP)) {
        padProjectInformation.setFutureAppSubmissionMonth(form.getFutureAppSubmissionMonth());
        padProjectInformation.setFutureAppSubmissionYear(form.getFutureAppSubmissionYear());
      } else {
        padProjectInformation.setPermanentDepositsMade(false);
      }
    }

    if (form.getIsTemporaryDepositsMade() != null) {
      padProjectInformation.setTemporaryDepositsMade(form.getIsTemporaryDepositsMade());
      padProjectInformation.setTemporaryDepDescription(
              form.getIsTemporaryDepositsMade() == false ? null : form.getTemporaryDepDescription());
    }


  }

}
