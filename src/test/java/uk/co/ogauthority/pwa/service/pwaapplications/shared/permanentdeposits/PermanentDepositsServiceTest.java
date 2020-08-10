package uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
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
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.permanentdeposits.PermanentDepositController;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.enums.permanentdeposits.MaterialType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.PadProjectInformation;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PadDepositPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PadPermanentDeposit;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PadPermanentDepositTestUtil;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.PermanentDepositsForm;
import uk.co.ogauthority.pwa.model.location.CoordinatePairTestUtil;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadDepositPipelineRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadPermanentDepositRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadProjectInformationRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.pipelines.PadPipelineRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.location.CoordinateFormValidator;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdepositdrawings.DepositDrawingsService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;
import uk.co.ogauthority.pwa.validators.PermanentDepositsValidator;

@RunWith(MockitoJUnitRunner.class)
public class PermanentDepositsServiceTest {

  private final static String FILE_ID = "1234567u8oplkjmnhbgvfc";

  @Mock
  private PadPermanentDepositRepository permanentDepositInformationRepository;

  @Mock
  private PadPipelineRepository padPipelineRepository;

  @Mock
  private PadDepositPipelineRepository padDepositPipelineRepository;

  @Mock
  private DepositDrawingsService depositDrawingsService;

  @Mock
  private PermanentDepositEntityMappingService permanentDepositEntityMappingService;

  @Mock
  private PadProjectInformationRepository padProjectInformationRepository;

  @Mock
  private PermanentDepositsValidator validator;

  @Mock
  private CoordinateFormValidator coordinateFormValidator;

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
        depositDrawingsService,
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
    form.setSelectedPipelines(Set.of("1", "2"));
    pwaApplicationDetail.setId(1);
    padPermanentDeposit.setPwaApplicationDetail(pwaApplicationDetail);
    when(permanentDepositInformationRepository.save(padPermanentDeposit)).thenReturn(padPermanentDeposit);

    var padPipeLine = new PadPipeline();
    padPipeLine.setId(1);
    when(padPipelineRepository.findById(1)).thenReturn(Optional.of(padPipeLine));
    padPipeLine = new PadPipeline();
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
  public void validateDepositOverview_valid() {
    var entityMapper = new PermanentDepositEntityMappingServiceTest();
    PadPermanentDeposit entity = entityMapper.buildBaseEntity();
    entityMapper.setEntityConcreteProperties(entity);

    when( permanentDepositInformationRepository.findByPwaApplicationDetailOrderByReferenceAsc(pwaApplicationDetail)).thenReturn(List.of(entity));
    assertThat(service.validateDepositOverview(pwaApplicationDetail)).isEqualTo(true);
  }


  @Test
  public void validateDepositOverview_inValid() {
    when( permanentDepositInformationRepository.findByPwaApplicationDetailOrderByReferenceAsc(pwaApplicationDetail)).thenReturn(new ArrayList<>());
    assertThat(service.validateDepositOverview(pwaApplicationDetail)).isEqualTo(false);
  }


  @Test
  public void isPermanentDepositMade_depositMadeTrue() {
    PadProjectInformation projectInformation = new PadProjectInformation();
    projectInformation.setPermanentDepositsMade(true);
    when(padProjectInformationRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(Optional.of(projectInformation));
    assertThat(service.permanentDepositsAreToBeMadeOnApp(pwaApplicationDetail)).isEqualTo(true);
  }

  @Test
  public void isPermanentDepositMade_depositMadeFalse() {
    PadProjectInformation projectInformation = new PadProjectInformation();
    projectInformation.setPermanentDepositsMade(false);
    when(padProjectInformationRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(Optional.of(projectInformation));
    assertThat(service.permanentDepositsAreToBeMadeOnApp(pwaApplicationDetail)).isEqualTo(false);
  }

  @Test
  public void isPermanentDepositMade_depositMadeNull() {
    PadProjectInformation projectInformation = new PadProjectInformation();
    projectInformation.setPermanentDepositsMade(null);
    when(padProjectInformationRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(Optional.of(projectInformation));
    assertThat(service.permanentDepositsAreToBeMadeOnApp(pwaApplicationDetail)).isEqualTo(false);
  }

  @Test
  public void isPermanentDepositMade_depcon() {
    PadProjectInformation projectInformation = new PadProjectInformation();
    projectInformation.setPermanentDepositsMade(false);
    var pwaApplicationDetail = PwaApplicationTestUtil.createApplicationDetail(null, PwaApplicationType.DEPOSIT_CONSENT, null, 0, 0);
    when(padProjectInformationRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(Optional.of(projectInformation));
    assertThat(service.permanentDepositsAreToBeMadeOnApp(pwaApplicationDetail)).isEqualTo(true);
  }

  @Test
  public void getPermanentDepositViews_serviceInteraction() {
    var permDeposit = getPadPermanentDeposit();
    when(permanentDepositInformationRepository.findByPwaApplicationDetailOrderByReferenceAsc(pwaApplicationDetail))
        .thenReturn(List.of(permDeposit));
    assertThat(service.getPermanentDepositViews(pwaApplicationDetail)).isNotNull();
    verify(permanentDepositEntityMappingService, times(1)).createPermanentDepositOverview(permDeposit);
  }

  @Test
  public void createViewFromEntity_serviceInteractions(){
    var deposit = getPadPermanentDeposit();
    service.createViewFromEntity(getPadPermanentDeposit());
    verify(permanentDepositEntityMappingService, times(1)).createPermanentDepositOverview(deposit);
  }


  private PadPermanentDeposit getPadPermanentDeposit(){
    return PadPermanentDepositTestUtil.createRockPadDeposit(
        100,
        "some reference",
        pwaApplicationDetail,
        "10",
        20,
        "30",
        LocalDate.now().plusDays(265),
        LocalDate.now().plusDays(365),
        CoordinatePairTestUtil.getDefaultCoordinate(),
        CoordinatePairTestUtil.getDefaultCoordinate()
    );
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

  @Test
  public void cleanupData_hiddenData() {

    var mattress = new PadPermanentDeposit();
    mattress.setMaterialType(MaterialType.CONCRETE_MATTRESSES);
    setAllMaterialData(mattress);

    var rock = new PadPermanentDeposit();
    rock.setMaterialType(MaterialType.ROCK);
    setAllMaterialData(rock);

    var grout = new PadPermanentDeposit();
    grout.setMaterialType(MaterialType.GROUT_BAGS);
    setAllMaterialData(grout);

    var other = new PadPermanentDeposit();
    other.setMaterialType(MaterialType.OTHER);
    setAllMaterialData(other);

    when(permanentDepositInformationRepository.findByPwaApplicationDetailOrderByReferenceAsc(pwaApplicationDetail)).thenReturn(List.of(mattress, rock, grout, other));

    service.cleanupData(pwaApplicationDetail);

    assertThat(mattress.getGroutBagsBioDegradable()).isNull();
    assertThat(mattress.getBagsNotUsedDescription()).isNull();
    assertThat(mattress.getOtherMaterialType()).isNull();
    assertThat(mattress.getMaterialSize()).isNull();
    assertThat(mattress.getContingencyAmount()).isNotNull();
    assertThat(mattress.getQuantity()).isNotNull();
    assertThat(mattress.getConcreteMattressDepth()).isNotNull();
    assertThat(mattress.getConcreteMattressWidth()).isNotNull();
    assertThat(mattress.getConcreteMattressLength()).isNotNull();

    assertThat(rock.getGroutBagsBioDegradable()).isNull();
    assertThat(rock.getBagsNotUsedDescription()).isNull();
    assertThat(rock.getOtherMaterialType()).isNull();
    assertThat(rock.getMaterialSize()).isNotNull();
    assertThat(rock.getContingencyAmount()).isNotNull();
    assertThat(rock.getQuantity()).isNotNull();
    assertThat(rock.getConcreteMattressDepth()).isNull();
    assertThat(rock.getConcreteMattressWidth()).isNull();
    assertThat(rock.getConcreteMattressLength()).isNull();

    assertThat(grout.getGroutBagsBioDegradable()).isNotNull();
    assertThat(grout.getBagsNotUsedDescription()).isNotNull();
    assertThat(grout.getOtherMaterialType()).isNull();
    assertThat(grout.getMaterialSize()).isNotNull();
    assertThat(grout.getContingencyAmount()).isNotNull();
    assertThat(grout.getQuantity()).isNotNull();
    assertThat(grout.getConcreteMattressDepth()).isNull();
    assertThat(grout.getConcreteMattressWidth()).isNull();
    assertThat(grout.getConcreteMattressLength()).isNull();

    assertThat(other.getGroutBagsBioDegradable()).isNull();
    assertThat(other.getBagsNotUsedDescription()).isNull();
    assertThat(other.getOtherMaterialType()).isNotNull();
    assertThat(other.getMaterialSize()).isNotNull();
    assertThat(other.getContingencyAmount()).isNotNull();
    assertThat(other.getQuantity()).isNotNull();
    assertThat(other.getConcreteMattressDepth()).isNull();
    assertThat(other.getConcreteMattressWidth()).isNull();
    assertThat(other.getConcreteMattressLength()).isNull();

    verify(permanentDepositInformationRepository, times(1)).saveAll(eq(List.of(mattress, rock, grout, other)));

  }

  @Test
  public void removePipelineFromDeposits_serviceInteraction() {
    var depositPipeline = new PadDepositPipeline();
    var padPipeline = new PadPipeline();
    when(padDepositPipelineRepository.getAllByPadPipeline(padPipeline))
        .thenReturn(List.of(depositPipeline));
    service.removePadPipelineDepositLinks(padPipeline);
    verify(padDepositPipelineRepository, times(1)).deleteAll(List.of(depositPipeline));
  }

  @Test
  public void cleanupUnlinkedSchedules_serviceInteraction_noLinks() {
    var padPipeline = new PadPipeline(pwaApplicationDetail);
    when(permanentDepositInformationRepository.getAllByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(padPermanentDeposit));
    when(padDepositPipelineRepository.getAllByPadPipeline_PwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of());
    service.removePadPipelineFromDeposits(padPipeline);
    verify(permanentDepositInformationRepository, times(1)).deleteAll(List.of(padPermanentDeposit));
  }

  @Test
  public void cleanupUnlinkedSchedules_serviceInteraction_remainingLinks() {
    var padPipeline = new PadPipeline(pwaApplicationDetail);
    var depositPipeline = new PadDepositPipeline();
    depositPipeline.setPadPermanentDeposit(padPermanentDeposit);
    padPermanentDeposit.setId(1);
    when(permanentDepositInformationRepository.getAllByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(padPermanentDeposit));
    when(padDepositPipelineRepository.getAllByPadPipeline_PwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(depositPipeline));
    service.removePadPipelineFromDeposits(padPipeline);
    verify(permanentDepositInformationRepository, never()).deleteAll(any());
  }

  private void setAllMaterialData(PadPermanentDeposit deposit) {
    deposit.setMaterialSize("1");
    deposit.setOtherMaterialType("otherType");
    deposit.setGroutBagsBioDegradable(true);
    deposit.setBagsNotUsedDescription("bag");
    deposit.setContingencyAmount("contingency");
    deposit.setQuantity(1);
    deposit.setConcreteMattressWidth(1);
    deposit.setConcreteMattressLength(2);
    deposit.setConcreteMattressDepth(3);
  }

}