package uk.co.ogauthority.pwa.features.application.tasks.projectinfo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.features.application.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.features.application.files.PadFileService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.enums.mailmerge.MailMergeFieldMnem;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.entitycopier.EntityCopyingService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.fileupload.FileUpdateMode;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.util.DateUtils;

@RunWith(MockitoJUnitRunner.class)
public class PadProjectInformationServiceTest {

  @Mock
  private PadProjectInformationRepository padProjectInformationRepository;

  @Mock
  private ProjectInformationEntityMappingService projectInformationEntityMappingService;

  @Mock
  private ProjectInformationValidator validator;

  @Mock
  private PadFileService padFileService;

  @Mock
  private PadLicenceTransactionService padLicenceTransactionService;

  @Mock
  private EntityCopyingService entityCopyingService;

  @Mock
  private MasterPwaService masterPwaService;

  private PadProjectInformationService service;
  private PadProjectInformation padProjectInformation;
  private ProjectInformationForm form;
  private PwaApplicationDetail pwaApplicationDetail;
  private LocalDate date;
  private WebUserAccount user = new WebUserAccount(1);

  @Before
  public void setUp() {

    service = new PadProjectInformationService(
        padProjectInformationRepository,
        projectInformationEntityMappingService,
        validator,
        padFileService,
        padLicenceTransactionService,
        entityCopyingService,
        masterPwaService);

    date = LocalDate.now();

    var pwaApplication = new PwaApplication(null, PwaApplicationType.INITIAL, null);
    pwaApplicationDetail = new PwaApplicationDetail(pwaApplication, null, null, null);
    padProjectInformation = ProjectInformationTestUtils.buildEntity(date);
    padProjectInformation.setPwaApplicationDetail(pwaApplicationDetail);
    form = ProjectInformationTestUtils.buildForm(date);

  }


  @Test
  public void getPadProjectInformationData_WithExisting() {
    when(padProjectInformationRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(
        Optional.of(padProjectInformation));
    var result = service.getPadProjectInformationData(pwaApplicationDetail);
    assertThat(result).isEqualTo(padProjectInformation);
  }

  @Test
  public void getPadProjectInformationData_NoExisting() {
    when(padProjectInformationRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(Optional.empty());
    var result = service.getPadProjectInformationData(pwaApplicationDetail);
    assertThat(result).isNotEqualTo(padProjectInformation);
    assertThat(result.getPwaApplicationDetail()).isEqualTo(pwaApplicationDetail);
  }


  @Test
  public void saveEntityUsingForm_verifyServiceInteractions_applicationTypeContainsLicence() {

    service.saveEntityUsingForm(padProjectInformation, form, user);

    verify(projectInformationEntityMappingService, times(1)).setEntityValuesUsingForm(padProjectInformation, form);
    verify(padFileService, times(1)).updateFiles(
        form,
        this.padProjectInformation.getPwaApplicationDetail(),
        ApplicationDetailFilePurpose.PROJECT_INFORMATION,
        FileUpdateMode.DELETE_UNLINKED_FILES,
        user
    );
    verify(padProjectInformationRepository, times(1)).save(padProjectInformation);
    verify(padLicenceTransactionService).saveApplicationsToPad(padProjectInformation, form);

  }

  @Test
  public void saveEntityUsingForm_verifyServiceInteractions_applicationTypeDoesNotContainLicence() {

    var pwaApplication = new PwaApplication();
    pwaApplication.setApplicationType(PwaApplicationType.DEPOSIT_CONSENT);
    var pwaApplicationDetail = new PwaApplicationDetail();
    pwaApplicationDetail.setPwaApplication(pwaApplication);

    padProjectInformation.setPwaApplicationDetail(pwaApplicationDetail);

    service.saveEntityUsingForm(padProjectInformation, form, user);

    verify(projectInformationEntityMappingService, times(1)).setEntityValuesUsingForm(padProjectInformation, form);
    verify(padFileService, times(1)).updateFiles(
        form,
        this.padProjectInformation.getPwaApplicationDetail(),
        ApplicationDetailFilePurpose.PROJECT_INFORMATION,
        FileUpdateMode.DELETE_UNLINKED_FILES,
        user
    );
    verify(padProjectInformationRepository, times(1)).save(padProjectInformation);
    verify(padLicenceTransactionService, never()).saveApplicationsToPad(padProjectInformation, form);

  }

  @Test
  public void mapEntityToForm_verifyServiceInteractions() {

    service.mapEntityToForm(padProjectInformation, form);

    verify(projectInformationEntityMappingService, times(1))
        .mapProjectInformationDataToForm(padProjectInformation, form);

    verify(padFileService, times(1)).mapFilesToForm(
        form,
        pwaApplicationDetail,
        ApplicationDetailFilePurpose.PROJECT_INFORMATION
    );

    verify(padLicenceTransactionService).mapApplicationsToForm(
        form,
        padProjectInformation
    );
  }

  @Test
  public void validate() {
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    service.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);
    verify(validator, times(1)).validate(form, bindingResult,
        new ProjectInformationFormValidationHints(
            pwaApplicationDetail.getPwaApplicationType(),
            pwaApplicationDetail.getResourceType(),
            ValidationType.FULL,
            EnumSet.complementOf(EnumSet.of(ProjectInformationQuestion.CARBON_STORAGE_PERMIT)),
            false
        ));
  }

  @Test
  public void getFormattedProposedStartDate() {
    LocalDateTime dateTime = LocalDateTime.of(2017, 5, 15, 0, 0);
    Instant instant = dateTime.atZone(ZoneId.systemDefault()).toInstant();
    var projectInformation = new PadProjectInformation();
    projectInformation.setProposedStartTimestamp(instant);

    when(padProjectInformationRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(Optional.of(projectInformation));

    assertThat(service.getFormattedProposedStartDate(pwaApplicationDetail)).isEqualTo("15 May 2017");
  }

  @Test
  public void getAvailableQuestions_depositConsentAppType() {
    var requiredQuestions = service.getRequiredQuestions(PwaApplicationType.DEPOSIT_CONSENT, PwaResourceType.PETROLEUM);
    assertThat(requiredQuestions).containsOnlyElementsOf(EnumSet.complementOf(EnumSet.of(
        ProjectInformationQuestion.LICENCE_TRANSFER_PLANNED,
        ProjectInformationQuestion.LICENCE_TRANSFER_DATE,
        ProjectInformationQuestion.LICENCE_TRANSFER_REFERENCE,
        ProjectInformationQuestion.COMMERCIAL_AGREEMENT_DATE,
        ProjectInformationQuestion.METHOD_OF_PIPELINE_DEPLOYMENT,
        ProjectInformationQuestion.USING_CAMPAIGN_APPROACH,
        ProjectInformationQuestion.FIELD_DEVELOPMENT_PLAN,
        ProjectInformationQuestion.PROJECT_LAYOUT_DIAGRAM,
        ProjectInformationQuestion.PERMANENT_DEPOSITS_BEING_MADE,
        ProjectInformationQuestion.CARBON_STORAGE_PERMIT
    )));
  }

  @Test
  public void getAvailableQuestions_huooVariationAppType() {
    var requiredQuestions = service.getRequiredQuestions(PwaApplicationType.HUOO_VARIATION, PwaResourceType.PETROLEUM);
    assertThat(requiredQuestions).containsOnlyElementsOf(EnumSet.complementOf(EnumSet.of(
        ProjectInformationQuestion.PROJECT_OVERVIEW,
        ProjectInformationQuestion.METHOD_OF_PIPELINE_DEPLOYMENT,
        ProjectInformationQuestion.MOBILISATION_DATE,
        ProjectInformationQuestion.EARLIEST_COMPLETION_DATE,
        ProjectInformationQuestion.LATEST_COMPLETION_DATE,
        ProjectInformationQuestion.USING_CAMPAIGN_APPROACH,
        ProjectInformationQuestion.FIELD_DEVELOPMENT_PLAN,
        ProjectInformationQuestion.PROJECT_LAYOUT_DIAGRAM,
        ProjectInformationQuestion.PERMANENT_DEPOSITS_BEING_MADE,
        ProjectInformationQuestion.TEMPORARY_DEPOSITS_BEING_MADE,
        ProjectInformationQuestion.CARBON_STORAGE_PERMIT
    )));
  }

  @Test
  public void getAvailableQuestions_decomAppType() {
    var requiredQuestions = service.getRequiredQuestions(PwaApplicationType.DECOMMISSIONING, PwaResourceType.PETROLEUM);
    assertThat(requiredQuestions).containsOnlyElementsOf(EnumSet.complementOf(
        EnumSet.of(ProjectInformationQuestion.METHOD_OF_PIPELINE_DEPLOYMENT,
            ProjectInformationQuestion.CARBON_STORAGE_PERMIT)));
  }

  @Test
  public void getAvailableQuestions_optionsVariationAppType() {
    var requiredQuestions = service.getRequiredQuestions(PwaApplicationType.OPTIONS_VARIATION, PwaResourceType.PETROLEUM);
    assertThat(requiredQuestions).containsOnlyElementsOf(EnumSet.complementOf(EnumSet.of(
        ProjectInformationQuestion.LICENCE_TRANSFER_PLANNED,
        ProjectInformationQuestion.USING_CAMPAIGN_APPROACH,
        ProjectInformationQuestion.CARBON_STORAGE_PERMIT
    )));
  }

  @Test
  public void getAvailableQuestions_allAppTypesExceptDepConAndHuooAndDecom() {
    PwaApplicationType.stream()
        .filter(appType -> appType != PwaApplicationType.HUOO_VARIATION
            && appType != PwaApplicationType.DEPOSIT_CONSENT
            && appType != PwaApplicationType.DECOMMISSIONING
            && appType != PwaApplicationType.OPTIONS_VARIATION
        )
        .forEach(appType -> {
          var requiredQuestions = service.getRequiredQuestions(appType, PwaResourceType.PETROLEUM);
          var expectedQuestions = EnumSet.allOf(ProjectInformationQuestion.class);
          expectedQuestions.remove(ProjectInformationQuestion.CARBON_STORAGE_PERMIT);
          assertThat(requiredQuestions).isEqualTo(expectedQuestions);
        });
  }

  @Test
  public void cleanupData_whenInitialVariation_andAllConditionalFieldsHidden() {

    padProjectInformation.setLicenceTransferPlanned(false);
    padProjectInformation.setLicenceTransferTimestamp(Instant.now());
    padProjectInformation.setCommercialAgreementTimestamp(Instant.now());

    padProjectInformation.setPermanentDepositsMade(PermanentDepositMade.NONE);
    padProjectInformation.setFutureAppSubmissionMonth(LocalDateTime.now().getMonthValue());
    padProjectInformation.setFutureAppSubmissionYear(LocalDateTime.now().getYear());

    padProjectInformation.setTemporaryDepositsMade(false);
    padProjectInformation.setTemporaryDepDescription("desc");

    when(padProjectInformationRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(Optional.of(padProjectInformation));

    service.cleanupData(pwaApplicationDetail);

    assertThat(padProjectInformation.getLicenceTransferTimestamp()).isNull();
    assertThat(padProjectInformation.getCommercialAgreementTimestamp()).isNull();

    assertThat(padProjectInformation.getFutureAppSubmissionMonth()).isNull();
    assertThat(padProjectInformation.getFutureAppSubmissionYear()).isNull();

    assertThat(padProjectInformation.getTemporaryDepDescription()).isNull();

    verify(padProjectInformationRepository, times(1)).save(padProjectInformation);

  }

  @Test
  public void cleanupData_whenHuooVariation_conditionalQuestionsNeverShown() {
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.HUOO_VARIATION);

    padProjectInformation = new PadProjectInformation();
    // cleanup data shown on all apps and safely assumed to have value
    padProjectInformation.setLicenceTransferPlanned(false);
    // set values on never shown value to prove nothing changed
    padProjectInformation.setFutureAppSubmissionMonth(1);
    padProjectInformation.setFutureAppSubmissionYear(2);

    when(padProjectInformationRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(Optional.of(padProjectInformation));

    service.cleanupData(pwaApplicationDetail);

    var padProjectInfoCaptor = ArgumentCaptor.forClass(PadProjectInformation.class);
    verify(padProjectInformationRepository, times(1)).save(padProjectInfoCaptor.capture());

    assertThat(padProjectInfoCaptor.getValue().getFutureAppSubmissionMonth()).isEqualTo(1);
    assertThat(padProjectInfoCaptor.getValue().getFutureAppSubmissionYear()).isEqualTo(2);

  }

  @Test
  public void cleanupData_whenDepositConsentVariation_conditionalQuestionsNeverShown() {
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.DEPOSIT_CONSENT);

    padProjectInformation = new PadProjectInformation();
    padProjectInformation.setLicenceTransferPlanned(null);
    // cleanup data shown on all apps and safely assumed to have value
    // set answers on visible questions
    padProjectInformation.setTemporaryDepositsMade(false);
    padProjectInformation.setTemporaryDepDescription("Some content");

    when(padProjectInformationRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(Optional.of(padProjectInformation));

    service.cleanupData(pwaApplicationDetail);

    var padProjectInfoCaptor = ArgumentCaptor.forClass(PadProjectInformation.class);
    verify(padProjectInformationRepository, times(1)).save(padProjectInfoCaptor.capture());

    assertThat(padProjectInfoCaptor.getValue().getTemporaryDepDescription()).isNull();

  }

  @Test
  public void cleanupData_whenInitialApplication_andAllConditionalQuestionsShownAndPopulated() {

    padProjectInformation.setLicenceTransferPlanned(true);
    padProjectInformation.setLicenceTransferTimestamp(Instant.now());
    padProjectInformation.setCommercialAgreementTimestamp(Instant.now());

    padProjectInformation.setPermanentDepositsMade(PermanentDepositMade.LATER_APP);
    padProjectInformation.setFutureAppSubmissionMonth(LocalDateTime.now().getMonthValue());
    padProjectInformation.setFutureAppSubmissionYear(LocalDateTime.now().getYear());

    padProjectInformation.setTemporaryDepositsMade(true);
    padProjectInformation.setTemporaryDepDescription("desc");

    when(padProjectInformationRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(Optional.of(padProjectInformation));

    service.cleanupData(pwaApplicationDetail);

    assertThat(padProjectInformation.getLicenceTransferTimestamp()).isNotNull();
    assertThat(padProjectInformation.getCommercialAgreementTimestamp()).isNotNull();

    assertThat(padProjectInformation.getFutureAppSubmissionMonth()).isNotNull();
    assertThat(padProjectInformation.getFutureAppSubmissionYear()).isNotNull();

    assertThat(padProjectInformation.getTemporaryDepDescription()).isNotNull();

    verify(padProjectInformationRepository, times(1)).save(padProjectInformation);

  }

  @Test
  public void removeFdpQuestionData() {
    padProjectInformation.setFdpOptionSelected(true);
    padProjectInformation.setFdpConfirmationFlag(true);
    padProjectInformation.setFdpNotSelectedReason("reason");
    when(padProjectInformationRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(Optional.of(padProjectInformation));

    service.removeFdpQuestionData(pwaApplicationDetail);

    padProjectInformation.setFdpOptionSelected(null);
    padProjectInformation.setFdpConfirmationFlag(null);
    padProjectInformation.setFdpNotSelectedReason(null);
    verify(padProjectInformationRepository, times(1)).save(padProjectInformation);
  }

  @Test
  public void copySectionInformation_serviceIteractions(){
    var copyToDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 1000, 1001);
    var oldProjectInformation = new PadProjectInformation();
    oldProjectInformation.setId(2222);
    var newProjectInformation = new PadProjectInformation();
    newProjectInformation.setId(321);

    when(padProjectInformationRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(
        Optional.of(oldProjectInformation));
    when(padProjectInformationRepository.findByPwaApplicationDetail(copyToDetail)).thenReturn(
        Optional.of(newProjectInformation));

    service.copySectionInformation(pwaApplicationDetail, copyToDetail);

    verify(entityCopyingService, times(1))
        .duplicateEntityAndSetParent(any(), eq(copyToDetail), eq(PadProjectInformation.class));

    verify(padFileService, times(1))
        .copyPadFilesToPwaApplicationDetail(
            pwaApplicationDetail,
            copyToDetail,
            ApplicationDetailFilePurpose.PROJECT_INFORMATION,
            ApplicationFileLinkStatus.FULL);

    verify(padLicenceTransactionService)
        .copyApplicationsToPad(oldProjectInformation, newProjectInformation);

  }

  @Test
  public void getPermanentDepositsMadeAnswer_depositMadeNull() {

    PadProjectInformation projectInformation = new PadProjectInformation();
    projectInformation.setPermanentDepositsMade(null);

    when(padProjectInformationRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(Optional.of(projectInformation));

    assertThat(service.getPermanentDepositsMadeAnswer(pwaApplicationDetail)).isEmpty();

  }

  @Test
  public void getPermanentDepositsMadeAnswer_depositMadeFalse() {

    PadProjectInformation projectInformation = new PadProjectInformation();
    projectInformation.setPermanentDepositsMade(PermanentDepositMade.NONE);

    when(padProjectInformationRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(Optional.of(projectInformation));

    assertThat(service.getPermanentDepositsMadeAnswer(pwaApplicationDetail)).contains(projectInformation.getPermanentDepositsMade());

  }

  @Test
  public void getAvailableMailMergeFields() {

    PwaApplicationType.stream().forEach(appType -> {

      var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(appType);

      var mergeFields = service.getAvailableMailMergeFields(detail.getPwaApplicationType());

      var expectedMergeFields = new ArrayList<>(
          List.of(MailMergeFieldMnem.PROPOSED_START_OF_WORKS_DATE, MailMergeFieldMnem.PROJECT_NAME));

      if (appType != PwaApplicationType.INITIAL) {
        expectedMergeFields.add(MailMergeFieldMnem.PWA_REFERENCE);
      }

      assertThat(mergeFields).containsExactlyInAnyOrderElementsOf(expectedMergeFields);

    });

  }

  @Test
  public void resolveMailMergeFields() {

    var pwaDetail = new MasterPwaDetail();
    pwaDetail.setReference("1/W/1");
    when(masterPwaService.getCurrentDetailOrThrow(any())).thenReturn(pwaDetail);

    var projectInfoData = new PadProjectInformation();
    projectInfoData.setProjectName("project name");
    projectInfoData.setProposedStartTimestamp(Instant.now());

    PwaApplicationType.stream().forEach(appType -> {

      var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(appType);

      when(padProjectInformationRepository.findByPwaApplicationDetail(detail)).thenReturn(Optional.of(projectInfoData));

      var mergeFieldsMap = service.resolveMailMergeFields(detail);

      var expectedMergeFieldsMap = new HashMap<>(Map.of(
          MailMergeFieldMnem.PROPOSED_START_OF_WORKS_DATE, DateUtils.formatDate(projectInfoData.getProposedStartTimestamp()),
          MailMergeFieldMnem.PROJECT_NAME, projectInfoData.getProjectName()));

      if (appType != PwaApplicationType.INITIAL) {
        expectedMergeFieldsMap.put(MailMergeFieldMnem.PWA_REFERENCE, pwaDetail.getReference());
      }

      assertThat(mergeFieldsMap).containsExactlyInAnyOrderEntriesOf(expectedMergeFieldsMap);

    });

  }

  @Test
  public void isIncludingPermanentDepositsIn_trueWithGivenValues() {
    var depositSelections = EnumSet.of(
        PermanentDepositMade.THIS_APP,
        PermanentDepositMade.YES
    );

    // Assert that each deposit type in depositSelections variable result in true
    depositSelections.forEach(deposit -> {

      // Assert each expected deposit
      var projectInfoData = new PadProjectInformation();
      projectInfoData.setPermanentDepositsMade(deposit);
      when(padProjectInformationRepository.findByPwaApplicationDetail(pwaApplicationDetail))
          .thenReturn(Optional.of(projectInfoData));
      var result = service.isIncludingPermanentDepositsIn(pwaApplicationDetail);

      assertTrue(result);

    });

    // Assert that the remaining deposit types result in false
    EnumSet.complementOf(depositSelections)
        .forEach(deposit -> {

          // Assert each expected deposit
          var projectInfoData = new PadProjectInformation();
          projectInfoData.setPermanentDepositsMade(deposit);
          when(padProjectInformationRepository.findByPwaApplicationDetail(pwaApplicationDetail))
              .thenReturn(Optional.of(projectInfoData));
          var result = service.isIncludingPermanentDepositsIn(pwaApplicationDetail);

          assertFalse(result);
        });
  }

}
