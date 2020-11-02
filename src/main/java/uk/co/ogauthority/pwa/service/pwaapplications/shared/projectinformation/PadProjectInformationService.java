package uk.co.ogauthority.pwa.service.pwaapplications.shared.projectinformation;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import javax.transaction.Transactional;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.enums.ProjectInformationQuestion;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadProjectInformation;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.ProjectInformationForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.ProjectInformationView;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadProjectInformationRepository;
import uk.co.ogauthority.pwa.service.entitycopier.EntityCopyingService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.fileupload.FileUpdateMode;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.util.DateUtils;
import uk.co.ogauthority.pwa.validators.ProjectInformationFormValidationHints;
import uk.co.ogauthority.pwa.validators.ProjectInformationValidator;

/* Service providing simplified API for project information app form */
@Service
public class PadProjectInformationService implements ApplicationFormSectionService {

  private static final Logger LOGGER = LoggerFactory.getLogger(PadProjectInformationService.class);

  private final PadProjectInformationRepository padProjectInformationRepository;
  private final ProjectInformationEntityMappingService projectInformationEntityMappingService;
  private final ProjectInformationValidator projectInformationValidator;
  private final PadFileService padFileService;
  private final EntityCopyingService entityCopyingService;

  private static final ApplicationDetailFilePurpose FILE_PURPOSE = ApplicationDetailFilePurpose.PROJECT_INFORMATION;

  @Autowired
  public PadProjectInformationService(
      PadProjectInformationRepository padProjectInformationRepository,
      ProjectInformationEntityMappingService projectInformationEntityMappingService,
      ProjectInformationValidator projectInformationValidator,
      PadFileService padFileService,
      EntityCopyingService entityCopyingService) {
    this.padProjectInformationRepository = padProjectInformationRepository;
    this.projectInformationEntityMappingService = projectInformationEntityMappingService;
    this.projectInformationValidator = projectInformationValidator;
    this.padFileService = padFileService;
    this.entityCopyingService = entityCopyingService;
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
   * @param padProjectInformation stored data
   * @param form                  form to map to
   */
  public void mapEntityToForm(PadProjectInformation padProjectInformation,
                              ProjectInformationForm form) {
    projectInformationEntityMappingService.mapProjectInformationDataToForm(padProjectInformation, form);
    padFileService.mapFilesToForm(form, padProjectInformation.getPwaApplicationDetail(), FILE_PURPOSE);
  }


  public ProjectInformationView getProjectInformationView(PwaApplicationDetail pwaApplicationDetail) {

    var layoutDiagramFileViews = padFileService.getUploadedFileViews(pwaApplicationDetail, ApplicationDetailFilePurpose.PROJECT_INFORMATION,
        ApplicationFileLinkStatus.FULL);

    return new ProjectInformationView(
        getPadProjectInformationData(pwaApplicationDetail),
        isFdpQuestionRequired(pwaApplicationDetail),
        !layoutDiagramFileViews.isEmpty() ? layoutDiagramFileViews.get(0) : null);
  }


  /**
   * From the form extract form data and file data which should be persisted.
   * Any linked files which are not part of official "save" action are discarded.
   */
  @Transactional
  public void saveEntityUsingForm(PadProjectInformation padProjectInformation,
                                  ProjectInformationForm form,
                                  WebUserAccount user) {
    projectInformationEntityMappingService.setEntityValuesUsingForm(
        padProjectInformation, form);
    padProjectInformationRepository.save(padProjectInformation);
    padFileService.updateFiles(form, padProjectInformation.getPwaApplicationDetail(), FILE_PURPOSE,
        FileUpdateMode.DELETE_UNLINKED_FILES, user);
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

    var projectInfoValidationHints = new ProjectInformationFormValidationHints(
        pwaApplicationDetail.getPwaApplicationType(), validationType,
        getRequiredQuestions(pwaApplicationDetail.getPwaApplicationType()),
        isFdpQuestionRequired(pwaApplicationDetail));
    projectInformationValidator.validate(form, bindingResult, projectInfoValidationHints);

    return bindingResult;
  }

  public boolean isFdpQuestionRequired(PwaApplicationDetail pwaApplicationDetail) {
    return BooleanUtils.toBooleanDefaultIfNull(pwaApplicationDetail.getLinkedToField(), false);
  }

  public String getFormattedProposedStartDate(PwaApplicationDetail pwaApplicationDetail) {
    var projectInformation = getPadProjectInformationData(pwaApplicationDetail);
    return DateUtils.formatDate(LocalDate.ofInstant(
        projectInformation.getProposedStartTimestamp(), ZoneId.systemDefault()));
  }

  public Optional<Instant> getProposedStartDate(PwaApplicationDetail pwaApplicationDetail) {
    var projectInformation = getPadProjectInformationData(pwaApplicationDetail);
    return Optional.ofNullable(projectInformation.getProposedStartTimestamp());
  }

  public Set<ProjectInformationQuestion> getRequiredQuestions(PwaApplicationType pwaApplicationType) {

    EnumSet<ProjectInformationQuestion> hiddenQuestions;

    if (pwaApplicationType == PwaApplicationType.DEPOSIT_CONSENT) {
      hiddenQuestions =  EnumSet.of(
          ProjectInformationQuestion.LICENCE_TRANSFER_PLANNED,
          ProjectInformationQuestion.LICENCE_TRANSFER_DATE,
          ProjectInformationQuestion.COMMERCIAL_AGREEMENT_DATE,
          ProjectInformationQuestion.METHOD_OF_PIPELINE_DEPLOYMENT,
          ProjectInformationQuestion.USING_CAMPAIGN_APPROACH,
          ProjectInformationQuestion.FIELD_DEVELOPMENT_PLAN,
          ProjectInformationQuestion.PROJECT_LAYOUT_DIAGRAM,
          ProjectInformationQuestion.PERMANENT_DEPOSITS_BEING_MADE
      );

    } else if (pwaApplicationType == PwaApplicationType.HUOO_VARIATION) {
      hiddenQuestions =  EnumSet.of(
          ProjectInformationQuestion.PROJECT_OVERVIEW,
          ProjectInformationQuestion.METHOD_OF_PIPELINE_DEPLOYMENT,
          ProjectInformationQuestion.MOBILISATION_DATE,
          ProjectInformationQuestion.EARLIEST_COMPLETION_DATE,
          ProjectInformationQuestion.LATEST_COMPLETION_DATE,
          ProjectInformationQuestion.USING_CAMPAIGN_APPROACH,
          ProjectInformationQuestion.FIELD_DEVELOPMENT_PLAN,
          ProjectInformationQuestion.PROJECT_LAYOUT_DIAGRAM,
          ProjectInformationQuestion.PERMANENT_DEPOSITS_BEING_MADE,
          ProjectInformationQuestion.TEMPORARY_DEPOSITS_BEING_MADE
      );

    } else if (pwaApplicationType == PwaApplicationType.DECOMMISSIONING) {
      hiddenQuestions = EnumSet.of(ProjectInformationQuestion.METHOD_OF_PIPELINE_DEPLOYMENT);

    } else {
      hiddenQuestions = EnumSet.noneOf(ProjectInformationQuestion.class);
    }

    return EnumSet.complementOf(hiddenQuestions);
  }

  @Override
  public void cleanupData(PwaApplicationDetail detail) {

    var projectInformation = getPadProjectInformationData(detail);

    // null out licence transfer info if no licence transfer
    if (!projectInformation.getLicenceTransferPlanned()) {
      projectInformation.setLicenceTransferTimestamp(null);
      projectInformation.setCommercialAgreementTimestamp(null);
    }

    var requiredQuestions = getRequiredQuestions(detail.getPwaApplicationType());

    if (requiredQuestions.contains(ProjectInformationQuestion.TEMPORARY_DEPOSITS_BEING_MADE)) {
      // null out temporary deposit description if temporary deposits not made
      if (!projectInformation.getTemporaryDepositsMade()) {
        projectInformation.setTemporaryDepDescription(null);
      }
    }

    if (requiredQuestions.contains(ProjectInformationQuestion.PERMANENT_DEPOSITS_BEING_MADE)) {
      // null out permanent deposit month and year if not "part of later application"
      if (!projectInformation.getPermanentDepositsMade()) {
        projectInformation.setFutureAppSubmissionMonth(null);
        projectInformation.setFutureAppSubmissionYear(null);
      }
    }

    padProjectInformationRepository.save(projectInformation);

  }


  public void removeFdpQuestionData(PwaApplicationDetail detail) {
    var padProjectInformation = getPadProjectInformationData(detail);
    padProjectInformation.setFdpOptionSelected(null);
    padProjectInformation.setFdpConfirmationFlag(null);
    padProjectInformation.setFdpNotSelectedReason(null);
    padProjectInformationRepository.save(padProjectInformation);
  }

  @Transactional
  @Override
  public void copySectionInformation(PwaApplicationDetail fromDetail, PwaApplicationDetail toDetail) {

    var newProjInfo = entityCopyingService.duplicateEntityAndSetParent(
        () -> getPadProjectInformationData(fromDetail),
        toDetail,
        PadProjectInformation.class);

    var duplicatedPadFiles = padFileService.copyPadFilesToPwaApplicationDetail(
        fromDetail,
        toDetail,
        FILE_PURPOSE,
        ApplicationFileLinkStatus.FULL
    );

  }
}
