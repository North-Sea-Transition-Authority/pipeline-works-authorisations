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
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadProjectInformation;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PermanentDepositInformation;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.PermanentDepositsForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.DepositsForPipelinesRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadPipelineRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadProjectInformationRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PermanentDepositInformationRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.util.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.util.ValidatorTestUtils;
import uk.co.ogauthority.pwa.validators.PermanentDepositsValidator;

import javax.validation.Validation;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

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
  private PadProjectInformationRepository padProjectInformationRepository;

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
        depositsForPipelinesRepository,
        padProjectInformationRepository
    );

    date = LocalDate.now();

    var pwaApplication = new PwaApplication(null, PwaApplicationType.INITIAL, null);
    pwaApplicationDetail = new PwaApplicationDetail(pwaApplication, null, null, null);
  }


  @Test
  public void getPermanentDepositInformationData_WithExisting() {
    List<PermanentDepositInformation> permanentDeposits = new ArrayList<>();
    permanentDeposits.add(permanentDepositInformation);
    when(permanentDepositInformationRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(
        permanentDeposits);
    var result = service.getPermanentDepositData(pwaApplicationDetail);
    assertThat(result).isEqualTo(permanentDepositInformation);
  }

  @Test
  public void getPermanentDepositInformationData_NoExisting() {
    when(permanentDepositInformationRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(new ArrayList<>());
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
    verify(permanentDepositInformationRepository, times(1)).save(permanentDepositInformation);

  }



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
    var form = new PermanentDepositsForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    var padProjectInformation = new PadProjectInformation();
    padProjectInformation.setProposedStartTimestamp(LocalDate.of(2020, 3, 1).atStartOfDay(ZoneId.systemDefault()).toInstant());
    when(padProjectInformationRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(Optional.of(padProjectInformation));

    service.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);
    verify(validator, times(1)).validate(form, bindingResult, padProjectInformation);
  }

  @Test
  public void validate_full_pass() {
    var ok = StringUtils.repeat("a", 4000);
    var form = new PermanentDepositsForm();
    form.setBioGroutBagsNotUsedDescription(ok);
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    var padProjectInformation = new PadProjectInformation();
    padProjectInformation.setProposedStartTimestamp(LocalDate.of(2020, 3, 1).atStartOfDay(ZoneId.systemDefault()).toInstant());
    when(padProjectInformationRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(Optional.of(padProjectInformation));
    service.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);

    verify(validator, times(1)).validate(form, bindingResult, padProjectInformation);
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


  @Test
  public void isPermanentDepositMade_depositMadeTrue() {
    PadProjectInformation projectInformation = new PadProjectInformation();
    projectInformation.setPermanentDepositsMade(true);
    when(padProjectInformationRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(Optional.of(projectInformation));
    assertThat(service.isPermanentDepositMade(pwaApplicationDetail)).isEqualTo(true);
  }

  @Test
  public void isPermanentDepositMade_depositMadeFalse() {
    PadProjectInformation projectInformation = new PadProjectInformation();
    projectInformation.setPermanentDepositsMade(false);
    when(padProjectInformationRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(Optional.of(projectInformation));
    assertThat(service.isPermanentDepositMade(pwaApplicationDetail)).isEqualTo(false);
  }

  @Test
  public void isPermanentDepositMade_depositMadeNull() {
    PadProjectInformation projectInformation = new PadProjectInformation();
    projectInformation.setPermanentDepositsMade(null);
    when(padProjectInformationRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(Optional.of(projectInformation));
    assertThat(service.isPermanentDepositMade(pwaApplicationDetail)).isEqualTo(false);
  }

  @Test
  public void isPermanentDepositMade_depcon() {
    PadProjectInformation projectInformation = new PadProjectInformation();
    projectInformation.setPermanentDepositsMade(false);
    var pwaApplicationDetail = PwaApplicationTestUtil.createApplicationDetail(null, PwaApplicationType.DEPOSIT_CONSENT, null, 0, 0);
    when(padProjectInformationRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(Optional.of(projectInformation));
    assertThat(service.isPermanentDepositMade(pwaApplicationDetail)).isEqualTo(true);
  }



}