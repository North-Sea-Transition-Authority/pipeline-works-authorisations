package uk.co.ogauthority.pwa.features.application.tasks.projectinfo;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.features.application.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.features.application.files.PadFileService;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationFormSectionService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.enums.mailmerge.MailMergeFieldMnem;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.entitycopier.EntityCopyingService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.fileupload.FileUpdateMode;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaService;
import uk.co.ogauthority.pwa.util.DateUtils;

/* Service providing simplified API for project information app form */
@Service
public class PadProjectInformationService implements ApplicationFormSectionService {

  private final PadProjectInformationRepository padProjectInformationRepository;
  private final ProjectInformationEntityMappingService projectInformationEntityMappingService;
  private final ProjectInformationValidator projectInformationValidator;
  private final PadFileService padFileService;

  private final PadLicenceTransactionService padLicenceTransactionService;
  private final EntityCopyingService entityCopyingService;
  private final MasterPwaService masterPwaService;
  private final EntityManager entityManager;

  private static final ApplicationDetailFilePurpose FILE_PURPOSE = ApplicationDetailFilePurpose.PROJECT_INFORMATION;

  @Autowired
  public PadProjectInformationService(
      PadProjectInformationRepository padProjectInformationRepository,
      ProjectInformationEntityMappingService projectInformationEntityMappingService,
      ProjectInformationValidator projectInformationValidator,
      PadFileService padFileService,
      PadLicenceTransactionService padLicenceTransactionService,
      EntityCopyingService entityCopyingService,
      MasterPwaService masterPwaService, EntityManager entityManager) {
    this.padProjectInformationRepository = padProjectInformationRepository;
    this.projectInformationEntityMappingService = projectInformationEntityMappingService;
    this.projectInformationValidator = projectInformationValidator;
    this.padFileService = padFileService;
    this.padLicenceTransactionService = padLicenceTransactionService;
    this.entityCopyingService = entityCopyingService;
    this.masterPwaService = masterPwaService;
    this.entityManager = entityManager;
  }

  public PadProjectInformation getPadProjectInformationData(PwaApplicationDetail pwaApplicationDetail) {
    var projectInformation = padProjectInformationRepository.findByPwaApplicationDetail(pwaApplicationDetail)
        .orElseGet(() -> getNewPadProjectInformation(pwaApplicationDetail));
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
    padLicenceTransactionService.mapApplicationsToForm(form, padProjectInformation);
  }

  public ProjectInformationView getProjectInformationView(PwaApplicationDetail pwaApplicationDetail) {

    var projectInformation = getPadProjectInformationData(pwaApplicationDetail);
    var layoutDiagramFileViews = padFileService.getUploadedFileViews(pwaApplicationDetail,
        ApplicationDetailFilePurpose.PROJECT_INFORMATION,
        ApplicationFileLinkStatus.FULL);

    List<String> licenceApplications = Optional.of(projectInformation)
        .filter(entityManager::contains)
        .map(padLicenceTransactionService::getInformationSummary)
        .orElse(List.of());

    return new ProjectInformationView(
        projectInformation,
        isFdpQuestionRequired(pwaApplicationDetail),
        !layoutDiagramFileViews.isEmpty() ? layoutDiagramFileViews.get(0) : null,
        licenceApplications
    );
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
    if (getRequiredQuestions(
        padProjectInformation.getPwaApplicationDetail().getPwaApplicationType(),
        padProjectInformation.getPwaApplicationDetail().getResourceType())
        .contains(ProjectInformationQuestion.LICENCE_TRANSFER_PLANNED)) {
      padLicenceTransactionService.saveApplicationsToPad(padProjectInformation, form);
    }
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
        pwaApplicationDetail.getPwaApplicationType(),
        pwaApplicationDetail.getResourceType(),
        validationType,
        getRequiredQuestions(pwaApplicationDetail.getPwaApplicationType(), pwaApplicationDetail.getResourceType()),
        isFdpQuestionRequired(pwaApplicationDetail));
    projectInformationValidator.validate(form, bindingResult, projectInfoValidationHints);

    return bindingResult;
  }

  public boolean isFdpQuestionRequired(PwaApplicationDetail pwaApplicationDetail) {
    return BooleanUtils.toBooleanDefaultIfNull(pwaApplicationDetail.getLinkedToArea(), false);
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

  public Optional<Instant> getLatestProjectCompletionDate(PwaApplicationDetail pwaApplicationDetail) {
    var projectInformation = getPadProjectInformationData(pwaApplicationDetail);
    return Optional.ofNullable(projectInformation.getLatestCompletionTimestamp());
  }

  public Set<ProjectInformationQuestion> getRequiredQuestions(
      PwaApplicationType pwaApplicationType,
      PwaResourceType resourceType
  ) {
    var questions = EnumSet.allOf(ProjectInformationQuestion.class);

    if (pwaApplicationType == PwaApplicationType.DEPOSIT_CONSENT) {
      questions.removeAll(List.of(
          ProjectInformationQuestion.LICENCE_TRANSFER_PLANNED,
          ProjectInformationQuestion.LICENCE_TRANSFER_REFERENCE,
          ProjectInformationQuestion.LICENCE_TRANSFER_DATE,
          ProjectInformationQuestion.COMMERCIAL_AGREEMENT_DATE,
          ProjectInformationQuestion.METHOD_OF_PIPELINE_DEPLOYMENT,
          ProjectInformationQuestion.USING_CAMPAIGN_APPROACH,
          ProjectInformationQuestion.FIELD_DEVELOPMENT_PLAN,
          ProjectInformationQuestion.PROJECT_LAYOUT_DIAGRAM,
          ProjectInformationQuestion.PERMANENT_DEPOSITS_BEING_MADE
      ));
    } else if (pwaApplicationType == PwaApplicationType.HUOO_VARIATION) {
      questions.removeAll(List.of(
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
      ));
    } else if (pwaApplicationType == PwaApplicationType.DECOMMISSIONING) {
      questions.remove(ProjectInformationQuestion.METHOD_OF_PIPELINE_DEPLOYMENT);
    } else if (pwaApplicationType == PwaApplicationType.OPTIONS_VARIATION) {
      questions.removeAll(List.of(
          ProjectInformationQuestion.LICENCE_TRANSFER_PLANNED,
          ProjectInformationQuestion.USING_CAMPAIGN_APPROACH
      ));
    }
    if (resourceType != null && resourceType.equals(PwaResourceType.CCUS)) {
      questions.remove(ProjectInformationQuestion.FIELD_DEVELOPMENT_PLAN);
    } else {
      questions.remove(ProjectInformationQuestion.CARBON_STORAGE_PERMIT);
    }
    return questions;
  }

  @Override
  public void cleanupData(PwaApplicationDetail detail) {

    var projectInformation = getPadProjectInformationData(detail);
    var requiredQuestions = getRequiredQuestions(detail.getPwaApplicationType(), detail.getResourceType());

    if (requiredQuestions.contains(ProjectInformationQuestion.LICENCE_TRANSFER_PLANNED)
        && BooleanUtils.isFalse(projectInformation.getLicenceTransferPlanned())) {
      // null out licence transfer info if no licence transfer
      projectInformation.setLicenceTransferTimestamp(null);
      projectInformation.setCommercialAgreementTimestamp(null);
    }

    // null out temporary deposit description if temporary deposits not made
    if (requiredQuestions.contains(ProjectInformationQuestion.TEMPORARY_DEPOSITS_BEING_MADE)
        && BooleanUtils.isFalse(projectInformation.getTemporaryDepositsMade())) {
      projectInformation.setTemporaryDepDescription(null);
    }

    // null out permanent deposit month and year if not "part of later application"
    if (requiredQuestions.contains(ProjectInformationQuestion.PERMANENT_DEPOSITS_BEING_MADE)
        && projectInformation.getPermanentDepositsMade() != PermanentDepositMade.LATER_APP) {
      projectInformation.setFutureAppSubmissionMonth(null);
      projectInformation.setFutureAppSubmissionYear(null);
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

    entityCopyingService.duplicateEntityAndSetParent(
        () -> getPadProjectInformationData(fromDetail),
        toDetail,
        PadProjectInformation.class);

    padFileService.copyPadFilesToPwaApplicationDetail(
        fromDetail,
        toDetail,
        FILE_PURPOSE,
        ApplicationFileLinkStatus.FULL
    );

    padLicenceTransactionService.copyApplicationsToPad(
        getPadProjectInformationData(fromDetail),
        getPadProjectInformationData(toDetail));
  }

  public Optional<PermanentDepositMade> getPermanentDepositsMadeAnswer(PwaApplicationDetail pwaApplicationDetail) {
    return padProjectInformationRepository.findByPwaApplicationDetail(pwaApplicationDetail)
        .map(PadProjectInformation::getPermanentDepositsMade);
  }

  @Override
  public List<MailMergeFieldMnem> getAvailableMailMergeFields(PwaApplicationType pwaApplicationType) {

    var questions = getRequiredQuestions(pwaApplicationType, null);
    var mailMergeFieldList = new ArrayList<MailMergeFieldMnem>();

    if (MailMergeFieldMnem.PROPOSED_START_OF_WORKS_DATE.appTypeIsSupported(pwaApplicationType)
        && questions.contains(ProjectInformationQuestion.PROPOSED_START_DATE)) {
      mailMergeFieldList.add(MailMergeFieldMnem.PROPOSED_START_OF_WORKS_DATE);
    }

    if (MailMergeFieldMnem.PROJECT_NAME.appTypeIsSupported(pwaApplicationType)
        && questions.contains(ProjectInformationQuestion.PROJECT_NAME)) {
      mailMergeFieldList.add(MailMergeFieldMnem.PROJECT_NAME);
    }

    if (MailMergeFieldMnem.PWA_REFERENCE.appTypeIsSupported(pwaApplicationType)) {
      mailMergeFieldList.add(MailMergeFieldMnem.PWA_REFERENCE);
    }

    return mailMergeFieldList;

  }

  @Override
  public Map<MailMergeFieldMnem, String> resolveMailMergeFields(PwaApplicationDetail pwaApplicationDetail) {

    var availableMergeFields = getAvailableMailMergeFields(pwaApplicationDetail.getPwaApplicationType());

    EnumMap<MailMergeFieldMnem, String> map = new EnumMap<>(MailMergeFieldMnem.class);

    if (!availableMergeFields.isEmpty()) {

      var projectInformation = getPadProjectInformationData(pwaApplicationDetail);

      if (availableMergeFields.contains(MailMergeFieldMnem.PROPOSED_START_OF_WORKS_DATE)) {
        map.put(MailMergeFieldMnem.PROPOSED_START_OF_WORKS_DATE,
            DateUtils.formatDate(projectInformation.getProposedStartTimestamp()));
      }

      if (availableMergeFields.contains(MailMergeFieldMnem.PROJECT_NAME)) {
        map.put(MailMergeFieldMnem.PROJECT_NAME, projectInformation.getProjectName());
      }

      if (availableMergeFields.contains(MailMergeFieldMnem.PWA_REFERENCE)) {

        var pwaRef = masterPwaService.getCurrentDetailOrThrow(pwaApplicationDetail.getMasterPwa())
            .getReference();

        map.put(MailMergeFieldMnem.PWA_REFERENCE, pwaRef);

      }

    }

    return map;
  }

  private PadProjectInformation getNewPadProjectInformation(PwaApplicationDetail pwaApplicationDetail) {
    var padProjectInformation = new PadProjectInformation();
    padProjectInformation.setPwaApplicationDetail(pwaApplicationDetail);
    return padProjectInformationRepository.save(padProjectInformation);
  }
}
