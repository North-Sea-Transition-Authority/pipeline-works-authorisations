package uk.co.ogauthority.pwa.service.pwaapplications.shared.projectinformation;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.validation.Validation;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadProjectInformation;
import uk.co.ogauthority.pwa.model.form.files.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.ProjectInformationForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadProjectInformationRepository;
import uk.co.ogauthority.pwa.service.entitycopier.EntityCopyingService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.fileupload.FileUpdateMode;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;
import uk.co.ogauthority.pwa.validators.ProjectInformationFormValidationHints;
import uk.co.ogauthority.pwa.validators.ProjectInformationValidator;

@RunWith(MockitoJUnitRunner.class)
public class PadProjectInformationServiceTest {

  private final static String FILE_ID = "1234567u8oplkjmnhbgvfc";

  @Mock
  private PadProjectInformationRepository padProjectInformationRepository;


  @Mock
  private ProjectInformationEntityMappingService projectInformationEntityMappingService;

  @Mock
  private ProjectInformationValidator validator;

  @Mock
  private PadFileService padFileService;

  @Mock
  private EntityCopyingService entityCopyingService;

  private SpringValidatorAdapter groupValidator;

  private PadProjectInformationService service;
  private PadProjectInformation padProjectInformation;
  private ProjectInformationForm form;
  private PwaApplicationDetail pwaApplicationDetail;
  private LocalDate date;
  private WebUserAccount user = new WebUserAccount(1);

  @Before
  public void setUp() {

    groupValidator = new SpringValidatorAdapter(Validation.buildDefaultValidatorFactory().getValidator());

    service = new PadProjectInformationService(
        padProjectInformationRepository,
        projectInformationEntityMappingService,
        validator,
        groupValidator,
        padFileService,
        entityCopyingService);

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
  public void saveEntityUsingForm_verifyServiceInteractions() {

    service.saveEntityUsingForm(padProjectInformation, form, user);

    verify(projectInformationEntityMappingService, times(1)).setEntityValuesUsingForm(padProjectInformation, form);
    verify(padFileService, times(1)).updateFiles(
        form,
        this.padProjectInformation.getPwaApplicationDetail(),
        ApplicationFilePurpose.PROJECT_INFORMATION,
        FileUpdateMode.DELETE_UNLINKED_FILES,
        user
    );
    verify(padProjectInformationRepository, times(1)).save(padProjectInformation);

  }

  @Test
  public void mapEntityToForm_verifyServiceInteractions() {

    service.mapEntityToForm(padProjectInformation, form);

    verify(projectInformationEntityMappingService, times(1))
        .mapProjectInformationDataToForm(padProjectInformation, form);

    verify(padFileService, times(1)).mapFilesToForm(
        form,
        pwaApplicationDetail,
        ApplicationFilePurpose.PROJECT_INFORMATION
    );

  }

  @Test
  public void validate_partial_whenHuooVariationType_andInvalidData() {
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.HUOO_VARIATION);

    var tooBig = StringUtils.repeat("a", 4001);
    var form = new ProjectInformationForm();
    form.setProjectOverview(tooBig);
    form.setProjectName(tooBig);
    form.setMethodOfPipelineDeployment(tooBig);
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    service.validate(form, bindingResult, ValidationType.PARTIAL, pwaApplicationDetail);

    var errors = ValidatorTestUtils.extractErrors(bindingResult);

    assertThat(errors).containsOnly(
        entry("projectOverview", Set.of("Length")),
        entry("projectName", Set.of("Length")),
        entry("methodOfPipelineDeployment", Set.of("Length"))
    );

    verifyNoInteractions(validator);

  }

  @Test
  public void validate_partial_whenHuooVariation_andValidData() {
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.HUOO_VARIATION);

    var ok = StringUtils.repeat("a", 4000);
    var form = new ProjectInformationForm();
    form.setProjectOverview(ok);
    form.setProjectName(ok);
    form.setMethodOfPipelineDeployment(ok);
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    service.validate(form, bindingResult, ValidationType.PARTIAL, pwaApplicationDetail);

    var errors = ValidatorTestUtils.extractErrors(bindingResult);

    assertThat(errors).isEmpty();

    verifyNoInteractions(validator);

  }

  @Test
  public void validate_full_whenHuooVariationType_andInvalidData() {
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.HUOO_VARIATION);
    var tooBig = StringUtils.repeat("a", 4001);
    var form = new ProjectInformationForm();
    form.setProjectOverview(tooBig);
    form.setProjectName(tooBig);
    form.setMethodOfPipelineDeployment(tooBig);
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    service.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);

    verify(validator, times(1)).validate(form, bindingResult, new ProjectInformationFormValidationHints(false, false,
        false));

    var errors = ValidatorTestUtils.extractErrors(bindingResult);

    assertThat(errors).containsOnly(
        entry("projectOverview", Set.of("Length")),
        entry("projectName", Set.of("Length")),
        entry("methodOfPipelineDeployment", Set.of("Length")),
        entry("usingCampaignApproach", Set.of("NotNull")), // only required when full
        entry("licenceTransferPlanned", Set.of("NotNull")), // only required when full
        entry("uploadedFileWithDescriptionForms", Set.of("NotEmpty"))
    );

  }

  @Test
  public void validate_full_whenHuooVariationType_andValidData() {
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.HUOO_VARIATION);

    var ok = StringUtils.repeat("a", 4000);
    var form = new ProjectInformationForm();
    form.setProjectOverview(ok);
    form.setProjectName(ok);
    form.setMethodOfPipelineDeployment(ok);
    form.setUsingCampaignApproach(false);
    form.setLicenceTransferPlanned(false);
    form.setUploadedFileWithDescriptionForms(List.of(new UploadFileWithDescriptionForm("id", "desc", Instant.now())));
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    service.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);

    verify(validator, times(1)).validate(form, bindingResult, new ProjectInformationFormValidationHints(false, false,
        false));

    var errors = ValidatorTestUtils.extractErrors(bindingResult);

    assertThat(errors).isEmpty();

  }

  public PwaApplicationDetail getAppDetailForDepositTest(PwaApplicationType pwaApplicationType){
    PwaApplication pwaApplication = new PwaApplication(null, pwaApplicationType, null);
    return new PwaApplicationDetail(pwaApplication, null, null, null);
  }

  @Test
  public void getIsPermanentDepositRequired_depCon(){
    PwaApplicationDetail pwaApplicationDetail = getAppDetailForDepositTest(PwaApplicationType.DEPOSIT_CONSENT);
    assertThat(service.getIsPermanentDepositQuestionRequired(pwaApplicationDetail)).isEqualTo(false);
  }

  @Test
  public void getIsPermanentDepositRequired_init(){
    PwaApplicationDetail pwaApplicationDetail = getAppDetailForDepositTest(PwaApplicationType.INITIAL);
    assertThat(service.getIsPermanentDepositQuestionRequired(pwaApplicationDetail)).isEqualTo(true);
  }

  @Test
  public void getIsAnyDepositQuestionRequired_HUOO(){
    PwaApplicationDetail pwaApplicationDetail = getAppDetailForDepositTest(PwaApplicationType.HUOO_VARIATION);
    assertThat(service.getIsAnyDepositQuestionRequired(pwaApplicationDetail)).isEqualTo(false);
  }

  @Test
  public void getIsAnyDepositQuestionRequired_init(){
    PwaApplicationDetail pwaApplicationDetail = getAppDetailForDepositTest(PwaApplicationType.INITIAL);
    assertThat(service.getIsAnyDepositQuestionRequired(pwaApplicationDetail)).isEqualTo(true);
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
  public void cleanupData_whenInitialVariation_andAllConditionalFieldsHidden() {

    padProjectInformation.setLicenceTransferPlanned(false);
    padProjectInformation.setLicenceTransferTimestamp(Instant.now());
    padProjectInformation.setCommercialAgreementTimestamp(Instant.now());

    padProjectInformation.setPermanentDepositsMade(false);
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
    // cleanup data shown on all apps and safely assumed to have value
    padProjectInformation.setLicenceTransferPlanned(false);
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

    padProjectInformation.setPermanentDepositsMade(true);
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

    service.copySectionInformation(pwaApplicationDetail, copyToDetail);

    verify(entityCopyingService, times(1))
        .duplicateEntityAndSetParent(any(), eq(copyToDetail), eq(PadProjectInformation.class));

    verify(padFileService, times(1))
        .copyPadFilesToPwaApplicationDetail(
            eq(pwaApplicationDetail),
            eq(copyToDetail),
            eq(ApplicationFilePurpose.PROJECT_INFORMATION),
            eq(ApplicationFileLinkStatus.FULL));

  }


}