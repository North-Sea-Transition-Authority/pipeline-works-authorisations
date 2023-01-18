package uk.co.ogauthority.pwa.features.application.tasks.projectextension;

import static uk.co.ogauthority.pwa.features.application.files.ApplicationDetailFilePurpose.PROJECT_EXTENSION;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.features.application.files.PadFile;
import uk.co.ogauthority.pwa.features.application.files.PadFileService;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.PadProjectInformationService;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;

@Service
public class PadProjectExtensionService implements ApplicationFormSectionService {

  private final PadFileService padFileService;
  private final PadProjectInformationService padProjectInformationService;

  private final ProjectExtensionValidator projectExtensionValidator;

  @Autowired
  public PadProjectExtensionService(PadFileService padFileService,
                                    PadProjectInformationService padProjectInformationService,
                                    ProjectExtensionValidator projectExtensionValidator) {
    this.padFileService = padFileService;
    this.padProjectInformationService = padProjectInformationService;
    this.projectExtensionValidator = projectExtensionValidator;
  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    var permissionFile = padFileService.getAllByPwaApplicationDetailAndPurpose(detail, PROJECT_EXTENSION)
        .stream()
        .filter(file -> file.getFileLinkStatus().equals(ApplicationFileLinkStatus.FULL))
        .findFirst()
        .orElse(new PadFile());

    return (permissionFile.getDescription() != null && !permissionFile.getDescription().isEmpty());
  }

  @Override
  public BindingResult validate(Object form, BindingResult bindingResult, ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {
    projectExtensionValidator.validate(form, bindingResult, validationType);
    return bindingResult;
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

  @Override
  public void copySectionInformation(PwaApplicationDetail fromDetail, PwaApplicationDetail toDetail) {
    padFileService.copyPadFilesToPwaApplicationDetail(fromDetail,
        toDetail,
        PROJECT_EXTENSION,
        ApplicationFileLinkStatus.FULL);
  }

  public void removeExtensionsForProject(PwaApplicationContext applicationContext) {
    var extensionFiles = padFileService.getAllByPwaApplicationDetailAndPurpose(
        applicationContext.getApplicationDetail(),
        PROJECT_EXTENSION);
    extensionFiles.forEach(file -> padFileService.processFileDeletion(file, applicationContext.getUser()));
  }

  public String getProjectTimelineGuidance(PwaApplicationDetail pwaApplicationDetail) {
    var projectType = MaxCompletionPeriod.valueOf(pwaApplicationDetail.getPwaApplicationType().name());
    var guidance = "For example, 31 3 2023 \n";
    guidance += String.format("This must be within %s months of the proposed start of works date. ",
        projectType.getMaxMonthsCompletion());

    if (projectType.isExtendable()) {
      guidance += "\n Unless prior approval has been received from the Consents and Authorisations Manager.";
    }
    return guidance;
  }
}
