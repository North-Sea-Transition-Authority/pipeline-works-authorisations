package uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.math.BigDecimal;
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
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
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
import uk.co.ogauthority.pwa.service.entitycopier.EntityCopyingService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.service.location.CoordinateFormValidator;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.viewfactories.PipelineAndIdentViewFactory;
import uk.co.ogauthority.pwa.service.pwaconsents.PipelineDetailService;
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

  @Mock
  private EntityCopyingService entityCopyingService;

  @Mock
  private PipelineAndIdentViewFactory pipelineAndIdentViewFactory;

  @Mock
  private PadFileService padFileService;

  @Mock
  private PipelineDetailService pipelineDetailService;


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
        padProjectInformationRepository,
        entityCopyingService,
        pipelineAndIdentViewFactory,
        padFileService, pipelineDetailService);

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
    var pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.DEPOSIT_CONSENT);
    when(padProjectInformationRepository.findByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(Optional.of(projectInformation));
    assertThat(service.permanentDepositsAreToBeMadeOnApp(pwaApplicationDetail)).isEqualTo(true);
  }

  @Test
  public void getPermanentDepositViews_serviceInteraction() {
    var permDeposit = getPadPermanentDeposit();
    when(permanentDepositInformationRepository.findByPwaApplicationDetailOrderByReferenceAsc(pwaApplicationDetail))
        .thenReturn(List.of(permDeposit));
    assertThat(service.getPermanentDepositViews(pwaApplicationDetail)).isNotNull();
    verify(permanentDepositEntityMappingService, times(1)).createPermanentDepositOverview(permDeposit, new HashMap<>());
  }

  @Test
  public void createViewFromEntity_serviceInteractions(){
    var deposit = getPadPermanentDeposit();
    service.createViewFromEntity(getPadPermanentDeposit());
    verify(permanentDepositEntityMappingService, times(1)).createPermanentDepositOverview(deposit, new HashMap<>());
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

    var depositWithPipelines = new PadPermanentDeposit();
    var depositPipelineLinks = List.of(new PadDepositPipeline());
    when(padDepositPipelineRepository.findAllByPadPermanentDeposit(depositWithPipelines)).thenReturn(depositPipelineLinks);

    when(permanentDepositInformationRepository.findByPwaApplicationDetailOrderByReferenceAsc(pwaApplicationDetail)).thenReturn(List.of(mattress, rock, grout, other, depositWithPipelines));


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

    verify(permanentDepositInformationRepository, times(1)).saveAll(eq(List.of(mattress, rock, grout, other, depositWithPipelines)));
    verify(padDepositPipelineRepository, times(1)).deleteAll(depositPipelineLinks);

  }


  @Test
  public void removePadPipelineFromDeposits_removeOnlyLinks() {

    var pipeline = new Pipeline();
    var padPipeline = new PadPipeline(pwaApplicationDetail);
    padPipeline.setPipeline(pipeline);

    var deposit1 = new PadPermanentDeposit();
    var depositPipelineLink1 = new PadDepositPipeline();
    depositPipelineLink1.setPadPermanentDeposit(deposit1);

    var deposit2 = new PadPermanentDeposit();
    var depositPipelineLink2 = new PadDepositPipeline();
    depositPipelineLink2.setPadPermanentDeposit(deposit2);

    when(padDepositPipelineRepository.getAllByPadPermanentDeposit_PwaApplicationDetailAndPipeline(
        pwaApplicationDetail, padPipeline.getPipeline())).thenReturn(List.of(depositPipelineLink1, depositPipelineLink2));

    when(padDepositPipelineRepository.countAllByPadPermanentDeposit(deposit1)).thenReturn(3L);
    when(padDepositPipelineRepository.countAllByPadPermanentDeposit(deposit2)).thenReturn(3L);

    when(pipelineDetailService.isPipelineConsented(padPipeline.getPipeline())).thenReturn(false);

    service.removePadPipelineFromDeposits(padPipeline);

    verify(padDepositPipelineRepository, times(1)).deleteAll(
        List.of(depositPipelineLink1, depositPipelineLink2));

    verifyNoInteractions(permanentDepositInformationRepository);
    verifyNoInteractions(depositDrawingsService);
  }

  @Test
  public void removePadPipelineFromDeposits_removeLinksAndDeposits() {

    var pipeline = new Pipeline();
    var padPipeline = new PadPipeline(pwaApplicationDetail);
    padPipeline.setPipeline(pipeline);

    var deposit1 = new PadPermanentDeposit();
    deposit1.setDepositIsForPipelinesOnOtherApp(false);
    var depositPipelineLink1 = new PadDepositPipeline();
    depositPipelineLink1.setPadPermanentDeposit(deposit1);

    var deposit2 = new PadPermanentDeposit();
    deposit2.setDepositIsForPipelinesOnOtherApp(true);
    var depositPipelineLink2 = new PadDepositPipeline();
    depositPipelineLink2.setPadPermanentDeposit(deposit2);

    when(padDepositPipelineRepository.getAllByPadPermanentDeposit_PwaApplicationDetailAndPipeline(
        pwaApplicationDetail, padPipeline.getPipeline())).thenReturn(List.of(depositPipelineLink1, depositPipelineLink2));

    when(padDepositPipelineRepository.countAllByPadPermanentDeposit(deposit1)).thenReturn(1L);
    when(padDepositPipelineRepository.countAllByPadPermanentDeposit(deposit2)).thenReturn(1L);

    when(pipelineDetailService.isPipelineConsented(padPipeline.getPipeline())).thenReturn(false);

    service.removePadPipelineFromDeposits(padPipeline);

    verify(padDepositPipelineRepository, times(1)).deleteAll(
        List.of(depositPipelineLink1, depositPipelineLink2));
    verify(permanentDepositInformationRepository, times(1)).deleteAll(
        List.of(deposit1));
    verify(depositDrawingsService, times(1)).removeDepositsFromDrawings(
        List.of(deposit1));
  }

  @Test
  public void removePadPipelineFromDeposits_pipelineConsented() {

    var padPipeline = new PadPipeline(pwaApplicationDetail);

    verifyNoInteractions(padDepositPipelineRepository);
    verifyNoInteractions(permanentDepositInformationRepository);
    verifyNoInteractions(depositDrawingsService);
  }


  private void setAllMaterialData(PadPermanentDeposit deposit) {
    deposit.setMaterialSize("1");
    deposit.setOtherMaterialType("otherType");
    deposit.setGroutBagsBioDegradable(true);
    deposit.setBagsNotUsedDescription("bag");
    deposit.setContingencyAmount("contingency");
    deposit.setQuantity(1);
    deposit.setConcreteMattressWidth(BigDecimal.valueOf(1));
    deposit.setConcreteMattressLength(BigDecimal.valueOf(2));
    deposit.setConcreteMattressDepth(BigDecimal.valueOf(3));
  }

}