package uk.co.ogauthority.pwa.features.application.tasks.projectextension;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.features.application.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.features.application.files.PadFile;
import uk.co.ogauthority.pwa.features.application.files.PadFileService;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.PadProjectInformationService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;

@Service
public class PadProjectExtensionService implements ApplicationFormSectionService {

  private final PadFileService padFileService;
  private final PadProjectInformationService padProjectInformationService;

  @Autowired
  public PadProjectExtensionService(PadFileService padFileService, PadProjectInformationService padProjectInformationService) {
    this.padFileService = padFileService;
    this.padProjectInformationService = padProjectInformationService;
  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    return !padFileService.getAllByPwaApplicationDetailAndPurpose(
        detail,
        ApplicationDetailFilePurpose.PROJECT_EXTENSION)
        .isEmpty();
  }

  @Override
  public BindingResult validate(Object form, BindingResult bindingResult, ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {
    return null;
  }

  @Override
  public boolean canShowInTaskList(PwaApplicationDetail pwaApplicationDetail) {
    var projectInfo = padProjectInformationService
        .getPadProjectInformationData(pwaApplicationDetail);
    var applicationType = projectInfo.getPwaApplicationDetail()
        .getPwaApplicationType();

    var startTimestamp = Optional.ofNullable(projectInfo.getProposedStartTimestamp());
    var endTimestamp = Optional.ofNullable(projectInfo.getLatestCompletionTimestamp());

    var completionPeriod = MaxCompletionPeriod.valueOf(applicationType.name());
    if (completionPeriod.isExtendable() && startTimestamp.isPresent() && endTimestamp.isPresent()) {
      var maxEndDate = LocalDate.ofInstant(startTimestamp.get(), ZoneId.systemDefault())
          .plus(completionPeriod.getMaxMonthsCompletion(), ChronoUnit.MONTHS);

      return LocalDate.ofInstant(endTimestamp.get(), ZoneId.systemDefault()).isAfter(maxEndDate);
    }
    return false;
  }

  public List<PadFile> getSummaryView(PwaApplicationDetail detail) {
    return padFileService.getAllByPwaApplicationDetailAndPurpose(
        detail,
        ApplicationDetailFilePurpose.PROJECT_EXTENSION);
  }

  @Override
  public void copySectionInformation(PwaApplicationDetail fromDetail, PwaApplicationDetail toDetail) {

  }
}
