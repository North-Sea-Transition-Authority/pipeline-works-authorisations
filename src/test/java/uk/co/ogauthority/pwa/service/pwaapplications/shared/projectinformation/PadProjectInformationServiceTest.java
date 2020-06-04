package uk.co.ogauthority.pwa.service.pwaapplications.shared.projectinformation;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
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
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.files.ApplicationFilePurpose;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadProjectInformation;
import uk.co.ogauthority.pwa.model.form.files.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.ProjectInformationForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadProjectInformationRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.fileupload.FileUpdateMode;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.util.ValidatorTestUtils;
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
        padFileService
    );

    date = LocalDate.now();

    var pwaApplication = new PwaApplication(null, PwaApplicationType.HUOO_VARIATION, null);
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
  public void validate_partial_fail() {

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
  public void validate_partial_pass() {

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
  public void validate_full_fail() {

    var tooBig = StringUtils.repeat("a", 4001);
    var form = new ProjectInformationForm();
    form.setProjectOverview(tooBig);
    form.setProjectName(tooBig);
    form.setMethodOfPipelineDeployment(tooBig);
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    service.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);

    verify(validator, times(1)).validate(form, bindingResult, new ProjectInformationFormValidationHints(false, false));

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
  public void validate_full_pass() {

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

    verify(validator, times(1)).validate(form, bindingResult, new ProjectInformationFormValidationHints(false, false));

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

}