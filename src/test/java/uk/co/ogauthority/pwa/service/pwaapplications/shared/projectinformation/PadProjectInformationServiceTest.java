package uk.co.ogauthority.pwa.service.pwaapplications.shared.projectinformation;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
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
import uk.co.ogauthority.pwa.model.entity.enums.ApplicationFileLinkStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadProjectInformation;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadProjectInformationFile;
import uk.co.ogauthority.pwa.model.form.files.UploadFileWithDescriptionForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.ProjectInformationForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadProjectInformationRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.util.ValidatorTestUtils;
import uk.co.ogauthority.pwa.validators.ProjectInformationValidator;

@RunWith(MockitoJUnitRunner.class)
public class PadProjectInformationServiceTest {

  private final static String FILE_ID = "1234567u8oplkjmnhbgvfc";

  @Mock
  private PadProjectInformationRepository padProjectInformationRepository;

  @Mock
  private ProjectInformationFileService projectInformationFileService;

  @Mock
  private ProjectInformationEntityMappingService projectInformationEntityMappingService;

  @Mock
  private ProjectInformationValidator validator;

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
        projectInformationFileService,
        projectInformationEntityMappingService,
        validator,
        groupValidator
    );

    date = LocalDate.now();

    pwaApplicationDetail = new PwaApplicationDetail();
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
    verify(projectInformationFileService, times(1)).updateOrDeleteLinkedFilesUsingForm(
        this.padProjectInformation.getPwaApplicationDetail(),
        form,
        user
    );
    verify(padProjectInformationRepository, times(1)).save(padProjectInformation);

  }

  @Test
  public void mapEntityToForm_verifyServiceInteractions_andUploadedFilesGetUpdated() {
    var returnedList = List.of(new UploadFileWithDescriptionForm(FILE_ID, "desc", Instant.now()));
    when(projectInformationFileService.getUploadedFileListAsFormList(pwaApplicationDetail,
        ApplicationFileLinkStatus.FULL))
        .thenReturn(returnedList);

    service.mapEntityToForm(padProjectInformation, form, ApplicationFileLinkStatus.FULL);

    verify(projectInformationEntityMappingService, times(1)).mapProjectInformationDataToForm(padProjectInformation,
        form);
    verify(projectInformationFileService, times(1)).getUploadedFileListAsFormList(
        pwaApplicationDetail,
        ApplicationFileLinkStatus.FULL
    );

    assertThat(form.getUploadedFileWithDescriptionForms()).isEqualTo(returnedList);

  }

  @Test
  public void getUpdatedProjectInformationFileViewsWhenFileOnForm_verifyServiceInteraction() {
    service.getUpdatedProjectInformationFileViewsWhenFileOnForm(pwaApplicationDetail, form);
    verify(projectInformationFileService, times(1)).getUpdatedProjectInformationFileViewsWhenFileOnForm(
        pwaApplicationDetail, form
    );

  }

  @Test
  public void getProjectInformationFile_verifyServiceInteraction() {
    service.getProjectInformationFile(FILE_ID, pwaApplicationDetail);
    verify(projectInformationFileService, times(1)).getProjectInformationFile(
        FILE_ID, pwaApplicationDetail
    );
  }


  @Test
  public void deleteUploadedFileLink_verifyServiceInteraction() {
    var testFile = new PadProjectInformationFile();
    when(projectInformationFileService.getProjectInformationFile(FILE_ID, pwaApplicationDetail)).thenReturn(
        testFile
    );
    service.deleteUploadedFileLink(FILE_ID, pwaApplicationDetail);

    verify(projectInformationFileService, times(1)).getProjectInformationFile(
        FILE_ID,
        pwaApplicationDetail
    );

    verify(projectInformationFileService, times(1)).deleteProjectInformationFileLink(
        testFile
    );

  }

  @Test
  public void createUploadedFileLink_verifyServiceInteraction() {
    service.createUploadedFileLink(FILE_ID, pwaApplicationDetail);
    verify(projectInformationFileService, times(1)).createAndSaveProjectInformationFile(
        pwaApplicationDetail, FILE_ID
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

    verify(validator, times(1)).validate(form, bindingResult);

    var errors = ValidatorTestUtils.extractErrors(bindingResult);

    assertThat(errors).containsOnly(
        entry("projectOverview", Set.of("Length")),
        entry("projectName", Set.of("Length")),
        entry("methodOfPipelineDeployment", Set.of("Length")),
        entry("usingCampaignApproach", Set.of("NotNull")) // only required when full
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
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    service.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);

    verify(validator, times(1)).validate(form, bindingResult);

    var errors = ValidatorTestUtils.extractErrors(bindingResult);

    assertThat(errors).isEmpty();

  }

}