package uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PermanentDepositInformation;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.PermanentDepositsForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.DepositsForPipelinesRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadPipelineRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PermanentDepositInformationRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.util.ValidatorTestUtils;
import uk.co.ogauthority.pwa.validators.PermanentDepositsValidator;

import javax.validation.Validation;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PermanentDepositsServiceTest {

  private final static String FILE_ID = "1234567u8oplkjmnhbgvfc";

  @Mock
  private PermanentDepositInformationRepository permanentDepositInformationRepository;

  @Mock
  private PadPipelineRepository padPipelineRepository;

  @Mock
  private DepositsForPipelinesRepository depositsForPipelinesRepository;

  @Mock
  private PermanentDepositsEntityMappingService permanentDepositsEntityMappingService;

  @Mock
  private PermanentDepositsValidator validator;

  private SpringValidatorAdapter groupValidator;


  private PermanentDepositsService service;
  private PermanentDepositInformation permanentDepositInformation = new PermanentDepositInformation();
  private PermanentDepositsForm form = new PermanentDepositsForm();
  private PwaApplicationDetail pwaApplicationDetail;
  private LocalDate date;
  private WebUserAccount user = new WebUserAccount(1);

  @Before
  public void setUp() {

    groupValidator = new SpringValidatorAdapter(Validation.buildDefaultValidatorFactory().getValidator());

    service = new PermanentDepositsService(
        permanentDepositInformationRepository,
        permanentDepositsEntityMappingService,
        validator,
        groupValidator,
        padPipelineRepository,
        depositsForPipelinesRepository
    );

    date = LocalDate.now();

    var pwaApplication = new PwaApplication(null, PwaApplicationType.INITIAL, null);
    pwaApplicationDetail = new PwaApplicationDetail(pwaApplication, null, null, null);
//    padProjectInformation = ProjectInformationTestUtils.buildEntity(date);
//    padProjectInformation.setPwaApplicationDetail(pwaApplicationDetail);
//    form = ProjectInformationTestUtils.buildForm(date);

  }


  @Test
  public void getPermanentDepositInformationData_WithExisting() {
    when(permanentDepositInformationRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(
        Optional.of(permanentDepositInformation));
    var result = service.getPermanentDepositData(pwaApplicationDetail);
    assertThat(result).isEqualTo(permanentDepositInformation);
  }

  @Test
  public void getPermanentDepositInformationData_NoExisting() {
    when(permanentDepositInformationRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(Optional.empty());
    var result = service.getPermanentDepositData(pwaApplicationDetail);
    assertThat(result).isNotEqualTo(permanentDepositInformation);
    assertThat(result.getPwaApplicationDetail()).isEqualTo(pwaApplicationDetail);
  }


  @Test
  public void saveEntityUsingForm_verifyServiceInteractions() {

    form.setSelectedPipelines("1,2");
    permanentDepositInformation.setId(1);
    when(permanentDepositInformationRepository.save(permanentDepositInformation)).thenReturn(permanentDepositInformation);
    service.saveEntityUsingForm(permanentDepositInformation, form, user);

    verify(permanentDepositsEntityMappingService, times(1)).setEntityValuesUsingForm(permanentDepositInformation, form);
//    verify(permanentDepositsFileService, times(1)).updateOrDeleteLinkedFilesUsingForm(
//        this.padProjectInformation.getPwaApplicationDetail(),
//        form,
//        user
//    );
    verify(permanentDepositInformationRepository, times(1)).save(permanentDepositInformation);

  }

//  @Test
//  public void mapEntityToForm_verifyServiceInteractions_andUploadedFilesGetUpdated() {
//    var returnedList = List.of(new UploadFileWithDescriptionForm(FILE_ID, "desc", Instant.now()));
//    when(permanentDepositsFileService.getUploadedFileListAsFormList(pwaApplicationDetail,
//        ApplicationFileLinkStatus.FULL))
//        .thenReturn(returnedList);
//
//    service.mapEntityToForm(padProjectInformation, form, ApplicationFileLinkStatus.FULL);
//
//    verify(permanentDepositsEntityMappingService, times(1)).mapProjectInformationDataToForm(padProjectInformation,
//        form);
//    verify(permanentDepositsFileService, times(1)).getUploadedFileListAsFormList(
//        pwaApplicationDetail,
//        ApplicationFileLinkStatus.FULL
//    );
//
//    assertThat(form.getUploadedFileWithDescriptionForms()).isEqualTo(returnedList);
//
//  }
//
//  @Test
//  public void getUpdatedPermanentDepositFileViewsWhenFileOnForm_verifyServiceInteraction() {
//    service.getUpdatedProjectInformationFileViewsWhenFileOnForm(pwaApplicationDetail, form);
//    verify(permanentDepositsFileService, times(1)).getUpdatedProjectInformationFileViewsWhenFileOnForm(
//        pwaApplicationDetail, form
//    );
//
//  }
//
//  @Test
//  public void getPermanentDepositFile_verifyServiceInteraction() {
//    service.getProjectInformationFile(FILE_ID, pwaApplicationDetail);
//    verify(permanentDepositsFileService, times(1)).getProjectInformationFile(
//        FILE_ID, pwaApplicationDetail
//    );
//  }
//
//
//  @Test
//  public void deleteUploadedFileLink_verifyServiceInteraction() {
//    var testFile = new PermanentDepositInformationFile();
//    when(permanentDepositsFileService.getProjectInformationFile(FILE_ID, pwaApplicationDetail)).thenReturn(
//        testFile
//    );
//    service.deleteUploadedFileLink(FILE_ID, pwaApplicationDetail);
//
//    verify(permanentDepositsFileService, times(1)).getProjectInformationFile(
//        FILE_ID,
//        pwaApplicationDetail
//    );
//
//    verify(permanentDepositsFileService, times(1)).deleteProjectInformationFileLink(
//        testFile
//    );
//
//  }
//
//  @Test
//  public void createUploadedFileLink_verifyServiceInteraction() {
//    service.createUploadedFileLink(FILE_ID, pwaApplicationDetail);
//    verify(permanentDepositsFileService, times(1)).createAndSaveProjectInformationFile(
//        pwaApplicationDetail, FILE_ID
//    );
//  }

  @Test
  public void validate_partial_fail() {

    var tooBig = StringUtils.repeat("a", 4001);
    var form = new PermanentDepositsForm();
    form.setBioGroutBagsNotUsedDescription(tooBig);
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    service.validate(form, bindingResult, ValidationType.PARTIAL, pwaApplicationDetail);

    var errors = ValidatorTestUtils.extractErrors(bindingResult);

//    assertThat(errors).containsOnly(
//        entry("bioGroutBagsNotUsedDescription", Set.of("Length"))
//    );
//
//    verifyNoInteractions(validator);

  }

  @Test
  public void validate_partial_pass() {

    var ok = StringUtils.repeat("a", 4000);
    var form = new PermanentDepositsForm();
    form.setBioGroutBagsNotUsedDescription(ok);
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    service.validate(form, bindingResult, ValidationType.PARTIAL, pwaApplicationDetail);

    var errors = ValidatorTestUtils.extractErrors(bindingResult);

    assertThat(errors).isEmpty();

    verifyNoInteractions(validator);

  }

  @Test
  public void validate_full_fail() {

    var tooBig = StringUtils.repeat("a", 4001);
    var form = new PermanentDepositsForm();
    form.setBioGroutBagsNotUsedDescription(tooBig);
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    service.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);

    verify(validator, times(1)).validate(form, bindingResult);

    var errors = ValidatorTestUtils.extractErrors(bindingResult);

//    assertThat(errors).containsOnly(
//        entry("bioGroutBagsNotUsedDescription", Set.of("Length"))
//    );

  }

  @Test
  public void validate_full_pass() {

    var ok = StringUtils.repeat("a", 4000);
    var form = new PermanentDepositsForm();
    form.setBioGroutBagsNotUsedDescription(ok);
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    service.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);

    verify(validator, times(1)).validate(form, bindingResult);

    var errors = ValidatorTestUtils.extractErrors(bindingResult);

    assertThat(errors).isEmpty();
  }


  @Test
  public void getPipelines() {
    var pipelinesMocked = new ArrayList<PadPipeline>();
    var PadPipeline = new PadPipeline();
    PadPipeline.setId(1);
    PadPipeline.setFromLocation("l1");
    pipelinesMocked.add(PadPipeline);
    PadPipeline = new PadPipeline();
    PadPipeline.setId(2);
    PadPipeline.setFromLocation("l2");
    pipelinesMocked.add(PadPipeline);

    var pipeLinesExpected = new HashMap<String, String >();
    pipeLinesExpected.put("1", "l1");
    pipeLinesExpected.put("2", "l2");

    when(padPipelineRepository.findAllByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(pipelinesMocked);

    assertThat(service.getPipelines(pwaApplicationDetail)).isEqualTo(pipeLinesExpected);
  }



}