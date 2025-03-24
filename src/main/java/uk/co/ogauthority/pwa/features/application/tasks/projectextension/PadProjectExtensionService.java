package uk.co.ogauthority.pwa.features.application.tasks.projectextension;


import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContext;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.PadProjectInformationService;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;

@Service
public class PadProjectExtensionService implements ApplicationFormSectionService {

  private final PadProjectInformationService padProjectInformationService;

  private final ProjectExtensionValidator projectExtensionValidator;
  private final PadFileManagementService padFileManagementService;

  @Autowired
  public PadProjectExtensionService(PadProjectInformationService padProjectInformationService,
                                    ProjectExtensionValidator projectExtensionValidator,
                                    PadFileManagementService padFileManagementService
  ) {
    this.padProjectInformationService = padProjectInformationService;
    this.projectExtensionValidator = projectExtensionValidator;
    this.padFileManagementService = padFileManagementService;
  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {
    var projectExtensionForm = new ProjectExtensionForm();
    padFileManagementService.mapFilesToForm(projectExtensionForm, detail, FileDocumentType.PROJECT_EXTENSION);
    var bindingResult = new BeanPropertyBindingResult(projectExtensionForm, "form");
    validate(projectExtensionForm, bindingResult, ValidationType.FULL, detail);

    return !bindingResult.hasErrors();
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
    padFileManagementService.copyUploadedFiles(fromDetail, toDetail, FileDocumentType.PROJECT_EXTENSION);
  }

  public void removeExtensionsForProject(PwaApplicationContext applicationContext) {
    var files = padFileManagementService.getUploadedFiles(applicationContext.getApplicationDetail(), FileDocumentType.PROJECT_EXTENSION);

    files.forEach(padFileManagementService::deleteUploadedFile);
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
