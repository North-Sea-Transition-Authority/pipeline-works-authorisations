package uk.co.ogauthority.pwa.service.pwaapplications.shared.projectinformation;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.Optional;
import javax.transaction.Transactional;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadProjectInformation;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.ProjectInformationForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadProjectInformationRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.util.DateUtils;
import uk.co.ogauthority.pwa.util.validationgroups.FullValidation;
import uk.co.ogauthority.pwa.util.validationgroups.MandatoryUploadValidation;
import uk.co.ogauthority.pwa.util.validationgroups.PartialValidation;
import uk.co.ogauthority.pwa.validators.ProjectInformationFormValidationHints;
import uk.co.ogauthority.pwa.validators.ProjectInformationValidator;

/* Service providing simplified API for project information app form */
@Service
public class PadProjectInformationService implements ApplicationFormSectionService {

  private final PadProjectInformationRepository padProjectInformationRepository;
  private final ProjectInformationEntityMappingService projectInformationEntityMappingService;
  private final ProjectInformationValidator projectInformationValidator;
  private final SpringValidatorAdapter groupValidator;
  private final PadFileService padFileService;

  private final ApplicationFilePurpose filePurpose = ApplicationFilePurpose.PROJECT_INFORMATION;

  @Autowired
  public PadProjectInformationService(
      PadProjectInformationRepository padProjectInformationRepository,
      ProjectInformationEntityMappingService projectInformationEntityMappingService,
      ProjectInformationValidator projectInformationValidator,
      SpringValidatorAdapter groupValidator,
      PadFileService padFileService) {
    this.padProjectInformationRepository = padProjectInformationRepository;
    this.projectInformationEntityMappingService = projectInformationEntityMappingService;
    this.projectInformationValidator = projectInformationValidator;
    this.groupValidator = groupValidator;
    this.padFileService = padFileService;
  }

  public PadProjectInformation getPadProjectInformationData(PwaApplicationDetail pwaApplicationDetail) {
    var projectInformation = padProjectInformationRepository.findByPwaApplicationDetail(pwaApplicationDetail)
        .orElse(new PadProjectInformation());
    projectInformation.setPwaApplicationDetail(pwaApplicationDetail);
    return projectInformation;
  }

  /**
   * Map stored data to form.
   *
   * @param padProjectInformation     stored data
   * @param form                      form to map to
   */
  public void mapEntityToForm(PadProjectInformation padProjectInformation,
                              ProjectInformationForm form) {
    projectInformationEntityMappingService.mapProjectInformationDataToForm(padProjectInformation, form);
    padFileService.mapFilesToForm(form, padProjectInformation.getPwaApplicationDetail(), filePurpose);
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
    padProjectInformationRepository.save(padProjectInformation);
    padFileService.updateFiles(form, padProjectInformation.getPwaApplicationDetail(), filePurpose, user);
  }

  public boolean isCampaignApproachBeingUsed(PwaApplicationDetail pwaApplicationDetail) {
    return padProjectInformationRepository.findByPwaApplicationDetail(pwaApplicationDetail)
        .map(o -> BooleanUtils.isTrue(o.getUsingCampaignApproach()))
        .orElse(false);
  }

  @Override
  public boolean isComplete(PwaApplicationDetail detail) {

    PadProjectInformation projectInformation = getPadProjectInformationData(detail);
    var projectInformationForm = new ProjectInformationForm();
    mapEntityToForm(projectInformation, projectInformationForm);
    BindingResult bindingResult = new BeanPropertyBindingResult(projectInformationForm, "form");
    validate(projectInformationForm, bindingResult, ValidationType.FULL, detail);

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
      groupValidator.validate(form, bindingResult, FullValidation.class, MandatoryUploadValidation.class);
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
    return !pwaApplicationDetail.getPwaApplicationType().equals(PwaApplicationType.HUOO_VARIATION);
  }

  public String getFormattedProposedStartDate(PwaApplicationDetail pwaApplicationDetail) {
    var projectInformation = getPadProjectInformationData(pwaApplicationDetail);
    return  DateUtils.formatDate(LocalDate.ofInstant(
        projectInformation.getProposedStartTimestamp(), ZoneId.systemDefault()));
  }

  public Optional<Instant> getProposedStartDate(PwaApplicationDetail pwaApplicationDetail) {
    var projectInformation = getPadProjectInformationData(pwaApplicationDetail);
    return Optional.ofNullable(projectInformation.getProposedStartTimestamp());
  }



}
