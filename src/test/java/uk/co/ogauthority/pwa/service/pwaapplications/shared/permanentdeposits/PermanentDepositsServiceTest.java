package uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits;

import org.apache.commons.lang3.StringUtils;
import javax.validation.Validation;
import java.time.LocalDate;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.PermanentDepositController;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadProjectInformation;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PadDepositPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PadPermanentDeposit;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.PermanentDepositsForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PermanentDepositsOverview;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadDepositPipelineRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines.PadPipelineRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadProjectInformationRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PermanentDepositInformationRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.util.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.util.ValidatorTestUtils;
import uk.co.ogauthority.pwa.validators.PermanentDepositsValidator;

@RunWith(MockitoJUnitRunner.class)
public class PermanentDepositsServiceTest {

  private final static String FILE_ID = "1234567u8oplkjmnhbgvfc";

  @Mock
  private PermanentDepositInformationRepository permanentDepositInformationRepository;

  @Mock
  private PadPipelineRepository padPipelineRepository;

  @Mock
  private PadDepositPipelineRepository padDepositPipelineRepository;

  @Mock
  private PermanentDepositEntityMappingService permanentDepositEntityMappingService;

  @Mock
  private PadProjectInformationRepository padProjectInformationRepository;

  @Mock
  private PermanentDepositsValidator validator;

  private SpringValidatorAdapter groupValidator;


  private PermanentDepositService service;
  private PadPermanentDeposit padPermanentDeposit = new PadPermanentDeposit();
  private PermanentDepositsForm form = new PermanentDepositsForm();
  private PwaApplicationDetail pwaApplicationDetail;
  private LocalDate date;
  private WebUserAccount user = new WebUserAccount(1);

  @Before
  public void setUp() {

    groupValidator = new SpringValidatorAdapter(Validation.buildDefaultValidatorFactory().getValidator());

    service = new PermanentDepositService(
        permanentDepositInformationRepository,
        permanentDepositEntityMappingService,
        validator,
        groupValidator,
        padPipelineRepository,
        padDepositPipelineRepository,
        padProjectInformationRepository
    );

    date = LocalDate.now();

    var pwaApplication = new PwaApplication(null, PwaApplicationType.INITIAL, null);
    pwaApplicationDetail = new PwaApplicationDetail(pwaApplication, null, null, null);
  }



  @Test
  public void saveEntityUsingForm_verifyServiceInteractions() {
    form.setSelectedPipelines(Set.of("1","2"));
    pwaApplicationDetail.setId(1);
    padPermanentDeposit.setPwaApplicationDetail(pwaApplicationDetail);
    when(permanentDepositInformationRepository.save(padPermanentDeposit)).thenReturn(padPermanentDeposit);

    var padPipeLine  = new PadPipeline();
    padPipeLine.setId(1);
    when(padPipelineRepository.findById(1)).thenReturn(Optional.of(padPipeLine));
    padPipeLine  = new PadPipeline();
    padPipeLine.setId(2);
    when(padPipelineRepository.findById(2)).thenReturn(Optional.of(padPipeLine));

    service.saveEntityUsingForm(pwaApplicationDetail, form, user);
    verify(permanentDepositEntityMappingService, times(1)).setEntityValuesUsingForm(padPermanentDeposit, form);
    verify(permanentDepositInformationRepository, times(1)).save(padPermanentDeposit);

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
    service.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);
    verify(validator, times(1)).validate(form, bindingResult, service, pwaApplicationDetail);
  }

  @Test
  public void validate_full_pass() {
    var ok = StringUtils.repeat("a", 4000);
    var form = new PermanentDepositsForm();
    form.setBioGroutBagsNotUsedDescription(ok);
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    service.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);
    verify(validator, times(1)).validate(form, bindingResult, service, pwaApplicationDetail);
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


  @Test
  public void getPermanentDepositViews() {
    var expectedViews = new ArrayList<PermanentDepositsOverview>();
    var expectedView = new PermanentDepositsOverview();
    expectedView.setPipelineRefs(Set.of("1","2"));
    expectedViews.add(expectedView);
    expectedView = new PermanentDepositsOverview();
    expectedView.setPipelineRefs(Set.of("3"));
    expectedViews.add(expectedView);

    List<PadPermanentDeposit> permanentDepositInfoMockList = new ArrayList<>();
    var permanentDepositInfoMock = new PadPermanentDeposit();
    permanentDepositInfoMock.setId(10);
    permanentDepositInfoMockList.add(permanentDepositInfoMock);
    permanentDepositInfoMock = new PadPermanentDeposit();
    permanentDepositInfoMock.setId(11);
    permanentDepositInfoMockList.add(permanentDepositInfoMock);
    when(permanentDepositInformationRepository.findByPwaApplicationDetailOrderByReferenceAsc(pwaApplicationDetail)).thenReturn(permanentDepositInfoMockList);

    List<PadDepositPipeline> depositsForPipelinesList = new ArrayList<>();
    PadDepositPipeline depositsForPipelines = new PadDepositPipeline();
    var padPipeLine = new PadPipeline();
    padPipeLine.setPipelineRef("1");
    depositsForPipelines.setPadPipelineId(padPipeLine);
    depositsForPipelinesList.add(depositsForPipelines);
    depositsForPipelines = new PadDepositPipeline();
    padPipeLine = new PadPipeline();
    padPipeLine.setPipelineRef("2");
    depositsForPipelines.setPadPipelineId(padPipeLine);
    depositsForPipelinesList.add(depositsForPipelines);
    when(padDepositPipelineRepository.findAllByPermanentDepositInfoId(10)).thenReturn(depositsForPipelinesList);

    depositsForPipelinesList = new ArrayList<>();
    depositsForPipelines = new PadDepositPipeline();
    padPipeLine = new PadPipeline();
    padPipeLine.setPipelineRef("3");
    depositsForPipelines.setPadPipelineId(padPipeLine);
    depositsForPipelinesList.add(depositsForPipelines);
    when(padDepositPipelineRepository.findAllByPermanentDepositInfoId(11)).thenReturn(depositsForPipelinesList);

    List<PermanentDepositsOverview> actualViews = service.getPermanentDepositViews(pwaApplicationDetail);

    assertThat(actualViews).isEqualTo(expectedViews);
  }

  @Test
  public void populatePermanentDepositViews() {
    var expectedView = new PermanentDepositsOverview();
    expectedView.setPipelineRefs(Set.of("1"));

    var permanentDepositInfoMock = new PadPermanentDeposit();
    permanentDepositInfoMock.setId(1);
    when(permanentDepositInformationRepository.findById(1)).thenReturn(Optional.of(permanentDepositInfoMock));

    List<PadDepositPipeline> depositsForPipelinesList = new ArrayList<>();
    PadDepositPipeline depositsForPipelines = new PadDepositPipeline();
    var padPipeLine = new PadPipeline();
    padPipeLine.setPipelineRef("1");
    depositsForPipelines.setPadPipelineId(padPipeLine);
    depositsForPipelinesList.add(depositsForPipelines);
    when(padDepositPipelineRepository.findAllByPermanentDepositInfoId(1)).thenReturn(depositsForPipelinesList);

    var actualView = new PermanentDepositsOverview();
    service.populatePermanentDepositView(1, actualView);
    assertThat(actualView).isEqualTo(expectedView);
  }

  @Test(expected = PwaEntityNotFoundException.class)
  public void mapEntityToFormById_noEntityExists() {
    var form = new PermanentDepositsForm();
    service.mapEntityToFormById(1, form);
  }

  @Test
  public void getEditUrlsForDeposits() {
    var expectedUrlMap = new HashMap<String, String>();
    expectedUrlMap.put("1", ReverseRouter.route(on(PermanentDepositController.class)
        .renderEditPermanentDeposits(
            pwaApplicationDetail.getPwaApplicationType(), pwaApplicationDetail.getMasterPwaApplicationId(),
            1, null, null)));
    var mockedEntity = new PadPermanentDeposit();
    mockedEntity.setId(1);

    when(permanentDepositInformationRepository.findByPwaApplicationDetailOrderByReferenceAsc(pwaApplicationDetail)).thenReturn(List.of(mockedEntity));
    var actualUrlMap = service.getEditUrlsForDeposits(pwaApplicationDetail);
    assertThat(actualUrlMap).isEqualTo(expectedUrlMap);
  }

  @Test
  public void getRemoveUrlsForDeposits() {
    var expectedUrlMap = new HashMap<String, String>();
    expectedUrlMap.put("1", ReverseRouter.route(on(PermanentDepositController.class)
        .renderRemovePermanentDeposits(
            pwaApplicationDetail.getPwaApplicationType(), pwaApplicationDetail.getMasterPwaApplicationId(),
            1, null, null)));
    var mockedEntity = new PadPermanentDeposit();
    mockedEntity.setId(1);

    when(permanentDepositInformationRepository.findByPwaApplicationDetailOrderByReferenceAsc(pwaApplicationDetail)).thenReturn(List.of(mockedEntity));
    var actualUrlMap = service.getRemoveUrlsForDeposits(pwaApplicationDetail);
    assertThat(actualUrlMap).isEqualTo(expectedUrlMap);
  }

  @Test
  public void isDepositReferenceUniqueWithId_true() {
    var entity = new PadPermanentDeposit();
    entity.setId(1);
    when(permanentDepositInformationRepository.findByPwaApplicationDetailAndReferenceIgnoreCase(pwaApplicationDetail,"myRef")).thenReturn(Optional.of(entity));
    assertThat(service.isDepositReferenceUnique("myRef", 1, pwaApplicationDetail)).isTrue();
  }

  @Test
  public void isDepositReferenceUniqueWithId_false() {
    var entity = new PadPermanentDeposit();
    entity.setId(2);
    when(permanentDepositInformationRepository.findByPwaApplicationDetailAndReferenceIgnoreCase(pwaApplicationDetail,"myRef")).thenReturn(Optional.of(entity));
    assertThat(service.isDepositReferenceUnique("myRef", 1, pwaApplicationDetail)).isFalse();
  }


  @Test
  public void isDepositReferenceUniqueNoId_true() {
    when(permanentDepositInformationRepository.findByPwaApplicationDetailAndReferenceIgnoreCase(pwaApplicationDetail,"myRef")).thenReturn(Optional.empty());
    assertThat(service.isDepositReferenceUnique("myRef", null, pwaApplicationDetail)).isTrue();
  }

  @Test
  public void isDepositReferenceUnique_false() {
    var entity = new PadPermanentDeposit();
    when(permanentDepositInformationRepository.findByPwaApplicationDetailAndReferenceIgnoreCase(pwaApplicationDetail,"myRef")).thenReturn(Optional.of(entity));
    assertThat(service.isDepositReferenceUnique("myRef", null, pwaApplicationDetail)).isFalse();
  }

  @Test(expected = PwaEntityNotFoundException.class)
  public void removeDeposit_noEntityFound() {
    service.removeDeposit(5);

  }


}