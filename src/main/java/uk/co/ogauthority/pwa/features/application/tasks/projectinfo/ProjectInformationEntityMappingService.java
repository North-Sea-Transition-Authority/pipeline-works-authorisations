package uk.co.ogauthority.pwa.features.application.tasks.projectinfo;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.util.DateUtils;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.TwoFieldDateInput;

/**
 * Mapping of form data to entity and entity to form data for project information application form.
 */
@Service
public class ProjectInformationEntityMappingService {

  /**
   * Map project information stored data to form.
   */
  void mapProjectInformationDataToForm(PadProjectInformation padProjectInformation, ProjectInformationForm form) {
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
      form.setPermanentDepositsMadeType(padProjectInformation.getPermanentDepositsMade());
      if (padProjectInformation.getFutureAppSubmissionMonth() != null
          && padProjectInformation.getFutureAppSubmissionYear() != null) {
        form.setFutureSubmissionDate(new TwoFieldDateInput(padProjectInformation.getFutureAppSubmissionYear(),
            padProjectInformation.getFutureAppSubmissionMonth()));
      }
    }

    if (padProjectInformation.getTemporaryDepositsMade() != null) {
      form.setTemporaryDepositsMade(padProjectInformation.getTemporaryDepositsMade());
      form.setTemporaryDepDescription(padProjectInformation.getTemporaryDepDescription());
    }


    if (padProjectInformation.getFdpOptionSelected() != null) {
      form.setFdpOptionSelected(padProjectInformation.getFdpOptionSelected());
      if (BooleanUtils.isTrue(padProjectInformation.getFdpOptionSelected())) {
        form.setFdpConfirmationFlag(padProjectInformation.getFdpConfirmationFlag());
      } else if (BooleanUtils.isFalse(padProjectInformation.getFdpOptionSelected())) {
        form.setFdpNotSelectedReason(padProjectInformation.getFdpNotSelectedReason());
      }
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
      padProjectInformation.setPermanentDepositsMade(form.getPermanentDepositsMadeType());
      if (form.getPermanentDepositsMadeType() == PermanentDepositMade.LATER_APP) {
        padProjectInformation.setFutureAppSubmissionMonth(Integer.parseInt(form.getFutureSubmissionDate().getMonth()));
        padProjectInformation.setFutureAppSubmissionYear(Integer.parseInt(form.getFutureSubmissionDate().getYear()));
      }
    }

    padProjectInformation.setTemporaryDepositsMade(form.getTemporaryDepositsMade());
    String tempDescription = BooleanUtils.isTrue(form.getTemporaryDepositsMade()) ? form.getTemporaryDepDescription() : null;
    padProjectInformation.setTemporaryDepDescription(tempDescription);

    if (form.getFdpOptionSelected() != null) {
      padProjectInformation.setFdpOptionSelected(form.getFdpOptionSelected());
      if (form.getFdpOptionSelected()) {
        padProjectInformation.setFdpConfirmationFlag(form.getFdpConfirmationFlag());
      } else {
        padProjectInformation.setFdpNotSelectedReason(form.getFdpNotSelectedReason());
      }
    }
  }

}
