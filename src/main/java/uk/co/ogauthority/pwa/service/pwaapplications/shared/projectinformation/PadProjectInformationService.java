package uk.co.ogauthority.pwa.service.pwaapplications.shared.projectinformation;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadProjectInformation;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadProjectInformationFile;
import uk.co.ogauthority.pwa.model.form.files.UploadedFileView;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.ProjectInformationForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadProjectInformationRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.util.validationgroups.FullValidation;
import uk.co.ogauthority.pwa.util.validationgroups.PartialValidation;
import uk.co.ogauthority.pwa.validators.ProjectInformationFormValidationHints;
import uk.co.ogauthority.pwa.validators.ProjectInformationValidator;

/* Service providing simplified API for project information app form */
@Service
public class PadProjectInformationService implements ApplicationFormSectionService {

  private final PadProjectInformationRepository padProjectInformationRepository;
  private final ProjectInformationFileService projectInformationFileService;
  private final ProjectInformationEntityMappingService projectInformationEntityMappingService;
  private final ProjectInformationValidator projectInformationValidator;
  private final SpringValidatorAdapter groupValidator;

  @Autowired
  public PadProjectInformationService(
      PadProjectInformationRepository padProjectInformationRepository,
      ProjectInformationFileService projectInformationFileService,
      ProjectInformationEntityMappingService projectInformationEntityMappingService,
      ProjectInformationValidator projectInformationValidator,
      SpringValidatorAdapter groupValidator) {
    this.padProjectInformationRepository = padProjectInformationRepository;
    this.projectInformationFileService = projectInformationFileService;
    this.projectInformationEntityMappingService = projectInformationEntityMappingService;
    this.projectInformationValidator = projectInformationValidator;
    this.groupValidator = groupValidator;
  }

  public PadProjectInformation getPadProjectInformationData(PwaApplicationDetail pwaApplicationDetail) {
    var projectInformation = padProjectInformationRepository.findByPwaApplicationDetail(pwaApplicationDetail)
        .orElse(new PadProjectInformation());
    projectInformation.setPwaApplicationDetail(pwaApplicationDetail);
    return projectInformation;
  }

  /**
   * Map stored data to form including uploaded files depending on requested link status.
   *
   * @param padProjectInformation     stored data
   * @param form                      form to map to
   * @param applicationFileLinkStatus link status of uploaded files to be included in form.
   */
  public void mapEntityToForm(PadProjectInformation padProjectInformation,
                              ProjectInformationForm form,
                              ApplicationFileLinkStatus applicationFileLinkStatus) {

    projectInformationEntityMappingService.mapProjectInformationDataToForm(padProjectInformation, form);

    // only attach files with matching link status to form
    var uploadedFilesWithDescriptionFormList = projectInformationFileService.getUploadedFileListAsFormList(
        padProjectInformation.getPwaApplicationDetail(),
        applicationFileLinkStatus
    );

    form.setUploadedFileWithDescriptionForms(uploadedFilesWithDescriptionFormList);
  }


  /**
   * From the form extract form data and file data which should be persisted.
   * Any linked files which are not part of official "save" action are discarded.
   */
  @Transactional
  public void saveEntityUsingForm(PadProjectInformation padProjectInformation,
                                  ProjectInformationForm form,
                                  WebUserAccount user) {
    projectInformationEntityMappingService.setEntityValuesUsingForm(padProjectInformation, form);
    projectInformationFileService.updateOrDeleteLinkedFilesUsingForm(
        padProjectInformation.getPwaApplicationDetail(),
        form,
        user
    );
    padProjectInformationRepository.save(padProjectInformation);

  }

  /**
   * Simplify api by providing pass through method to access file service.
   */
  public List<UploadedFileView> getUpdatedProjectInformationFileViewsWhenFileOnForm(
      PwaApplicationDetail pwaApplicationDetail,
      ProjectInformationForm form) {
    return projectInformationFileService.getUpdatedProjectInformationFileViewsWhenFileOnForm(pwaApplicationDetail,
        form);

  }

  /**
   * Simplify api by providing pass through method to access file service.
   */
  public PadProjectInformationFile getProjectInformationFile(String fileId, PwaApplicationDetail pwaApplicationDetail) {
    return projectInformationFileService.getProjectInformationFile(fileId,
        pwaApplicationDetail);
  }


  @Transactional
  public void deleteUploadedFileLink(String fileId, PwaApplicationDetail pwaApplicationDetail) {
    PadProjectInformationFile existingFile = projectInformationFileService.getProjectInformationFile(fileId,
        pwaApplicationDetail);
    projectInformationFileService.deleteProjectInformationFileLink(existingFile);
  }

  /**
   * Method which creates "temporary" link to application detail project information.
   * If form left unsaved, we know which files are deletable.
   */
  @Transactional
  public void createUploadedFileLink(String uploadedFileId, PwaApplicationDetail pwaApplicationDetail) {
    projectInformationFileService.createAndSaveProjectInformationFile(
        pwaApplicationDetail,
        uploadedFileId
    );
  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {

    PadProjectInformation projectInformation = getPadProjectInformationData(detail);
    var projectInformationForm = new ProjectInformationForm();
    mapEntityToForm(projectInformation, projectInformationForm, ApplicationFileLinkStatus.FULL);
    BindingResult bindingResult = new BeanPropertyBindingResult(projectInformationForm, "form");
    projectInformationValidator.validate(projectInformationForm, bindingResult);

    return !bindingResult.hasErrors();

  }

  @Override
  public BindingResult validate(Object form,
                                BindingResult bindingResult,
                                ValidationType validationType,
                                PwaApplicationDetail pwaApplicationDetail) {
    if (validationType.equals(ValidationType.PARTIAL)) {
      groupValidator.validate(form, bindingResult, PartialValidation.class);
    } else {
      groupValidator.validate(form, bindingResult, FullValidation.class);
      var projectInfoValidationHints = new ProjectInformationFormValidationHints(getIsAnyDepositQuestionRequired(pwaApplicationDetail),
          getIsPermanentDepositQuestionRequired(pwaApplicationDetail));
      projectInformationValidator.validate(form, bindingResult, projectInfoValidationHints);
    }

    return bindingResult;

  }

  public boolean getIsPermanentDepositQuestionRequired(PwaApplicationDetail pwaApplicationDetail) {
    return !EnumSet.of(PwaApplicationType.DEPOSIT_CONSENT, PwaApplicationType.HUOO_VARIATION)
        .contains(pwaApplicationDetail.getPwaApplicationType());
  }

  public boolean getIsAnyDepositQuestionRequired(PwaApplicationDetail pwaApplicationDetail) {
    return pwaApplicationDetail.getPwaApplicationType().equals(PwaApplicationType.HUOO_VARIATION) ? false : true;
  }

  public String getProposedStartDate(PwaApplicationDetail pwaApplicationDetail) {
    var projectInformation = getPadProjectInformationData(pwaApplicationDetail);
    DateTimeFormatter formatter =
        DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
        .withLocale(Locale.UK)
        .withZone(ZoneId.systemDefault());
    return formatter.format(projectInformation.getProposedStartTimestamp());
  }



}
