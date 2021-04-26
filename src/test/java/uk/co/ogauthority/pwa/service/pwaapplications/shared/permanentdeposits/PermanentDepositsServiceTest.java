package uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.springframework.validation.BindingResult;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import uk.co.ogauthority.pwa.controller.pwaapplications.shared.permanentdeposits.PermanentDepositController;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.enums.permanentdeposits.MaterialType;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineStatus;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineType;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PadDepositPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PadPermanentDeposit;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.permanentdeposits.PadPermanentDepositTestUtil;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.form.fds.ErrorItem;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.PermanentDepositsForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PadPipelineOverview;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineHeaderView;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview;
import uk.co.ogauthority.pwa.model.location.CoordinatePairTestUtil;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadDepositPipelineRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.PadPermanentDepositRepository;
import uk.co.ogauthority.pwa.service.entitycopier.EntityCopyingService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.fileupload.PadFileService;
import uk.co.ogauthority.pwa.service.pwaapplications.options.PadOptionConfirmedService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.viewfactories.PipelineAndIdentViewFactory;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.projectinformation.PadProjectInformationService;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.testutils.ValidatorTestUtils;
import uk.co.ogauthority.pwa.validators.PermanentDepositsValidator;

@RunWith(MockitoJUnitRunner.class)
public class PermanentDepositsServiceTest {

  @Mock
  private PadPermanentDepositRepository permanentDepositInformationRepository;

  @Mock
  private PadDepositPipelineRepository padDepositPipelineRepository;

  @Mock
  private DepositDrawingsService depositDrawingsService;

  @Mock
  private PermanentDepositEntityMappingService permanentDepositEntityMappingService;

  @Mock
  private PadProjectInformationService padProjectInformationService;

  @Mock
  private PermanentDepositsValidator validator;

  @Mock
  private EntityCopyingService entityCopyingService;

  @Mock
  private PipelineAndIdentViewFactory pipelineAndIdentViewFactory;

  @Mock
  private PadFileService padFileService;

  @Mock
  private PipelineDetailService pipelineDetailService;

  @Mock
  private PadOptionConfirmedService padOptionConfirmedService;


  private SpringValidatorAdapter groupValidator;


  private PermanentDepositService permanentDepositService;
  private PadPermanentDeposit padPermanentDeposit = new PadPermanentDeposit();
  private PermanentDepositsForm form = new PermanentDepositsForm();
  private PwaApplicationDetail pwaApplicationDetail;
  private LocalDate date;
  private WebUserAccount user = new WebUserAccount(1);

  private static int DEPOSIT_ID_1 = 1;

  @Before
  public void setUp() {

    groupValidator = new SpringValidatorAdapter(Validation.buildDefaultValidatorFactory().getValidator());

    permanentDepositService = new PermanentDepositService(
        permanentDepositInformationRepository,
        depositDrawingsService,
        permanentDepositEntityMappingService,
        validator,
        groupValidator,
        padDepositPipelineRepository,
        padProjectInformationService,
        entityCopyingService,
        pipelineAndIdentViewFactory,
        padFileService,
        pipelineDetailService,
        padOptionConfirmedService);

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

    permanentDepositService.saveEntityUsingForm(pwaApplicationDetail, form, user);
    verify(permanentDepositEntityMappingService, times(1)).setEntityValuesUsingForm(padPermanentDeposit, form);
    verify(permanentDepositInformationRepository, times(1)).save(padPermanentDeposit);

  }

  @Test
  public void validate_partial_pass() {

    var ok = StringUtils.repeat("a", 4000);
    var form = new PermanentDepositsForm();
    form.setBioGroutBagsNotUsedDescription(ok);
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    permanentDepositService.validate(form, bindingResult, ValidationType.PARTIAL, pwaApplicationDetail);

    var errors = ValidatorTestUtils.extractErrors(bindingResult);

    assertThat(errors).isEmpty();

    verifyNoInteractions(validator);

  }

  @Test
  public void validate_full_fail() {
    var form = new PermanentDepositsForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    permanentDepositService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);
    verify(validator, times(1)).validate(form, bindingResult, permanentDepositService, pwaApplicationDetail);
  }

  @Test
  public void validate_full_pass() {
    var ok = StringUtils.repeat("a", 4000);
    var form = new PermanentDepositsForm();
    form.setBioGroutBagsNotUsedDescription(ok);
    var bindingResult = new BeanPropertyBindingResult(form, "form");

    permanentDepositService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);
    verify(validator, times(1)).validate(form, bindingResult, permanentDepositService, pwaApplicationDetail);
  }

  @Test
  public void getDepositSummaryScreenValidationResult_noDeposits_incomplete() {
    when( permanentDepositInformationRepository.findByPwaApplicationDetailOrderByReferenceAsc(pwaApplicationDetail)).thenReturn(List.of());
    var summaryResult = permanentDepositService.getDepositSummaryScreenValidationResult(pwaApplicationDetail);
    assertThat(summaryResult.isSectionComplete()).isEqualTo(false);
    assertThat(summaryResult.getSectionIncompleteError()).isEqualTo("Ensure that at least one deposit has been added and that they are all valid.");
  }

  @Test
  public void getDepositSummaryScreenValidationResult_depositsInvalid_depositsHaveErrors() {
    var entityMapper = new PermanentDepositEntityMappingServiceTest();
    PadPermanentDeposit entity = entityMapper.buildBaseEntity();
    PadPermanentDeposit entity2 = entityMapper.buildBaseEntity();
    entity2.setId(entity2.getId() + 1);

    when(permanentDepositInformationRepository.findByPwaApplicationDetailOrderByReferenceAsc(pwaApplicationDetail))
        .thenReturn(List.of(entity, entity2));

    doAnswer(invocation -> {
      BindingResult result = invocation.getArgument(1);
      result.rejectValue("materialType", "fake.code", "fake message");
      return result;
    }).when(validator).validate(any(), any(), any(), any());

    var summaryResult = permanentDepositService.getDepositSummaryScreenValidationResult(pwaApplicationDetail);

    assertThat(summaryResult.isSectionComplete()).isEqualTo(false);
    assertThat(summaryResult.getInvalidObjectIds()).containsExactly(String.valueOf(entity.getId()), String.valueOf(entity2.getId()));
    assertThat(summaryResult.getIdPrefix()).isEqualTo("deposit-");
    assertThat(summaryResult.getErrorItems()).extracting(ErrorItem::getDisplayOrder, ErrorItem::getFieldName, ErrorItem::getErrorMessage)
        .containsExactly(
            tuple(1, "deposit-" + entity.getId(), entity.getReference() + " must have all sections completed without errors"),
            tuple(2, "deposit-" + entity2.getId(), entity2.getReference() + " must have all sections completed without errors")
        );
  }

  @Test
  public void getDepositSummaryScreenValidationResult_depositsValid_noErrors() {
    var entityMapper = new PermanentDepositEntityMappingServiceTest();
    PadPermanentDeposit entity = entityMapper.buildBaseEntity();
    when(permanentDepositInformationRepository.findByPwaApplicationDetailOrderByReferenceAsc(pwaApplicationDetail)).thenReturn(List.of(entity));

    var summaryResult = permanentDepositService.getDepositSummaryScreenValidationResult(pwaApplicationDetail);
    assertThat(summaryResult.isSectionComplete()).isEqualTo(true);
    assertThat(summaryResult.getSectionIncompleteError()).isNull();
    assertThat(summaryResult.getInvalidObjectIds()).isEmpty();
  }


  @Test
  public void isPermanentDepositMade_depositMadeTrue() {
    when(padProjectInformationService.getPermanentDepositsOnApplication(pwaApplicationDetail)).thenReturn(true);

    assertThat(permanentDepositService.permanentDepositsAreToBeMadeOnApp(pwaApplicationDetail)).isEqualTo(true);
  }

  @Test
  public void isPermanentDepositMade_depositMadeFalse() {
    when(padProjectInformationService.getPermanentDepositsOnApplication(pwaApplicationDetail)).thenReturn(false);

    assertThat(permanentDepositService.permanentDepositsAreToBeMadeOnApp(pwaApplicationDetail)).isEqualTo(false);
  }

  @Test
  public void isPermanentDepositMade_depcon() {
    var pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.DEPOSIT_CONSENT);

    assertThat(permanentDepositService.permanentDepositsAreToBeMadeOnApp(pwaApplicationDetail)).isEqualTo(true);
  }

  @Test
  public void getPermanentDepositViews_serviceInteraction() {
    var permDeposit = getPadPermanentDeposit();
    when(permanentDepositInformationRepository.findByPwaApplicationDetailOrderByReferenceAsc(pwaApplicationDetail))
        .thenReturn(List.of(permDeposit));
    assertThat(permanentDepositService.getPermanentDepositViews(pwaApplicationDetail)).isNotNull();
    verify(permanentDepositEntityMappingService, times(1)).createPermanentDepositOverview(permDeposit, new HashMap<>());
  }

  @Test
  public void createViewFromEntity_serviceInteractions(){
    var deposit = getPadPermanentDeposit();
    permanentDepositService.createViewFromEntity(getPadPermanentDeposit());
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
    permanentDepositService.mapEntityToFormById(1, form);
  }

  @Test
  public void getPipelinesMapForDeposits_1pipelineInService_1pipelineRTS() {

    var pipelineOnSeabed = new Pipeline();
    pipelineOnSeabed.setId(1);
    var padPipelineOnSeabed = new PadPipeline();
    padPipelineOnSeabed.setPipeline(pipelineOnSeabed);
    padPipelineOnSeabed.setPipelineStatus(PipelineStatus.IN_SERVICE);
    padPipelineOnSeabed.setPipelineRef("my ref");
    padPipelineOnSeabed.setPipelineType(PipelineType.HYDRAULIC_JUMPER);
    var padPipelineOverviewOnSeabed = new PadPipelineOverview(padPipelineOnSeabed, 1L);

    var pipelineNotOnSeabed = new Pipeline();
    pipelineNotOnSeabed.setId(2);
    var padPipelineNotOnSeabed = new PadPipeline();
    padPipelineNotOnSeabed.setPipeline(pipelineNotOnSeabed);
    padPipelineNotOnSeabed.setPipelineStatus(PipelineStatus.RETURNED_TO_SHORE);
    var padPipelineOverviewNotOnSeabed = new PadPipelineOverview(padPipelineNotOnSeabed, 1L);

    when(pipelineAndIdentViewFactory.getAllPipelineOverviewsFromAppAndMasterPwa(pwaApplicationDetail))
    .thenReturn(Map.of(
        PipelineId.from(padPipelineOverviewOnSeabed), padPipelineOverviewOnSeabed,
        PipelineId.from(padPipelineOverviewNotOnSeabed), padPipelineOverviewNotOnSeabed));

    var pipelinesIdAndNameMap = permanentDepositService.getPipelinesMapForDeposits(pwaApplicationDetail);
    assertThat(pipelinesIdAndNameMap).containsOnly(
        entry(String.valueOf(padPipelineOverviewOnSeabed.getPipelineId()),
            padPipelineOnSeabed.getPipelineRef() + " - " + padPipelineOnSeabed.getPipelineType().getDisplayName()));
  }


  @Test
  public void createViewFromDepositId_onePipelineForDeposit() {

    var deposit = getPadPermanentDeposit();
    deposit.setId(DEPOSIT_ID_1);
    when(permanentDepositInformationRepository.findById(DEPOSIT_ID_1)).thenReturn(Optional.of(deposit));

    var pipeline = new Pipeline();
    pipeline.setId(1);
    var padDepositPipeline = new PadDepositPipeline();
    padDepositPipeline.setPadPermanentDeposit(deposit);
    padDepositPipeline.setPipeline(pipeline);
    when(padDepositPipelineRepository.findAllByPadPermanentDeposit(deposit)).thenReturn(List.of(padDepositPipeline));

    Map<PipelineId, PipelineOverview> pipelineIdAndOverviewMap = Map.of(pipeline.getPipelineId(), new PipelineHeaderView());
    when(pipelineAndIdentViewFactory.getAllPipelineOverviewsFromAppAndMasterPwaByPipelineIds(
        deposit.getPwaApplicationDetail(), List.of(pipeline.getPipelineId())))
        .thenReturn(pipelineIdAndOverviewMap);

    permanentDepositService.createViewFromDepositId(DEPOSIT_ID_1);
    verify(permanentDepositEntityMappingService, times(1))
        .createPermanentDepositOverview(deposit, pipelineIdAndOverviewMap);
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
    var actualUrlMap = permanentDepositService.getEditUrlsForDeposits(pwaApplicationDetail);
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
    var actualUrlMap = permanentDepositService.getRemoveUrlsForDeposits(pwaApplicationDetail);
    assertThat(actualUrlMap).isEqualTo(expectedUrlMap);
  }

  @Test
  public void isDepositReferenceUniqueWithId_true() {
    var entity = new PadPermanentDeposit();
    entity.setId(1);
    when(permanentDepositInformationRepository.findByPwaApplicationDetailAndReferenceIgnoreCase(pwaApplicationDetail,"myRef")).thenReturn(Optional.of(entity));
    assertThat(permanentDepositService.isDepositReferenceUnique("myRef", 1, pwaApplicationDetail)).isTrue();
  }

  @Test
  public void isDepositReferenceUniqueWithId_false() {
    var entity = new PadPermanentDeposit();
    entity.setId(2);
    when(permanentDepositInformationRepository.findByPwaApplicationDetailAndReferenceIgnoreCase(pwaApplicationDetail,"myRef")).thenReturn(Optional.of(entity));
    assertThat(permanentDepositService.isDepositReferenceUnique("myRef", 1, pwaApplicationDetail)).isFalse();
  }


  @Test
  public void isDepositReferenceUniqueNoId_true() {
    when(permanentDepositInformationRepository.findByPwaApplicationDetailAndReferenceIgnoreCase(pwaApplicationDetail,"myRef")).thenReturn(Optional.empty());
    assertThat(permanentDepositService.isDepositReferenceUnique("myRef", null, pwaApplicationDetail)).isTrue();
  }

  @Test
  public void isDepositReferenceUnique_false() {
    var entity = new PadPermanentDeposit();
    when(permanentDepositInformationRepository.findByPwaApplicationDetailAndReferenceIgnoreCase(pwaApplicationDetail,"myRef")).thenReturn(Optional.of(entity));
    assertThat(permanentDepositService.isDepositReferenceUnique("myRef", null, pwaApplicationDetail)).isFalse();
  }

  @Test(expected = PwaEntityNotFoundException.class)
  public void removeDeposit_noEntityFound() {
    permanentDepositService.removeDeposit(5);
  }


  @Test
  public void getPermanentDepositForPipelinesMap() {

    var deposit1 = new PadPermanentDeposit();
    deposit1.setId(1);
    var deposit2 = new PadPermanentDeposit();
    deposit2.setId(2);

    var pipeline1 = new Pipeline();
    var pipeline2 = new Pipeline();

    var deposit1AndPipeline1 = PadPermanentDepositTestUtil.createDepositPipeline(deposit1, pipeline1);
    var deposit1AndPipeline2 = PadPermanentDepositTestUtil.createDepositPipeline(deposit1, pipeline2);
    var deposit2AndPipeline1 = PadPermanentDepositTestUtil.createDepositPipeline(deposit2, pipeline1);

    when(padDepositPipelineRepository.getAllByPadPermanentDeposit_PwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(deposit1AndPipeline1, deposit1AndPipeline2, deposit2AndPipeline1));

    var depositForDepositPipelinesMap = permanentDepositService.getDepositForDepositPipelinesMap(pwaApplicationDetail);

    assertThat(depositForDepositPipelinesMap.get(deposit1)).isEqualTo(List.of(deposit1AndPipeline1, deposit1AndPipeline2));
    assertThat(depositForDepositPipelinesMap.get(deposit2)).isEqualTo(List.of(deposit2AndPipeline1));
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


    permanentDepositService.cleanupData(pwaApplicationDetail);

    assertThat(mattress.getGroutBagsBioDegradable()).isNull();
    assertThat(mattress.getBagsNotUsedDescription()).isNull();
    assertThat(mattress.getOtherMaterialType()).isNull();
    assertThat(mattress.getMaterialSize()).isNull();
    assertThat(mattress.getContingencyAmount()).isNotNull();
    assertThat(mattress.getQuantity()).isEqualTo(Double.valueOf(1.0));
    assertThat(mattress.getConcreteMattressDepth()).isEqualTo(BigDecimal.valueOf(3));
    assertThat(mattress.getConcreteMattressWidth()).isEqualTo(BigDecimal.valueOf(1));
    assertThat(mattress.getConcreteMattressLength()).isEqualTo(BigDecimal.valueOf(2));

    assertThat(rock.getGroutBagsBioDegradable()).isNull();
    assertThat(rock.getBagsNotUsedDescription()).isNull();
    assertThat(rock.getOtherMaterialType()).isNull();
    assertThat(rock.getMaterialSize()).isNotNull();
    assertThat(rock.getContingencyAmount()).isNotNull();
    assertThat(rock.getQuantity()).isEqualTo(Double.valueOf(1));
    assertThat(rock.getConcreteMattressDepth()).isNull();
    assertThat(rock.getConcreteMattressWidth()).isNull();
    assertThat(rock.getConcreteMattressLength()).isNull();

    assertThat(grout.getGroutBagsBioDegradable()).isNotNull();
    assertThat(grout.getBagsNotUsedDescription()).isNotNull();
    assertThat(grout.getOtherMaterialType()).isNull();
    assertThat(grout.getMaterialSize()).isNotNull();
    assertThat(grout.getContingencyAmount()).isNotNull();
    assertThat(grout.getQuantity()).isEqualTo(Double.valueOf(1));
    assertThat(grout.getConcreteMattressDepth()).isNull();
    assertThat(grout.getConcreteMattressWidth()).isNull();
    assertThat(grout.getConcreteMattressLength()).isNull();

    assertThat(other.getGroutBagsBioDegradable()).isNull();
    assertThat(other.getBagsNotUsedDescription()).isNull();
    assertThat(other.getOtherMaterialType()).isNotNull();
    assertThat(other.getMaterialSize()).isNotNull();
    assertThat(other.getContingencyAmount()).isNotNull();
    assertThat(other.getQuantity()).isEqualTo(Double.valueOf(1));
    assertThat(other.getConcreteMattressDepth()).isNull();
    assertThat(other.getConcreteMattressWidth()).isNull();
    assertThat(other.getConcreteMattressLength()).isNull();

    verify(permanentDepositInformationRepository, times(1)).saveAll(List.of(mattress, rock, grout, other, depositWithPipelines));
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

    permanentDepositService.removePadPipelineFromDeposits(padPipeline);

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

    permanentDepositService.removePadPipelineFromDeposits(padPipeline);

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

  @Test
  public void canShowInTaskList_notOptionsVariation_andPermDepositsQuestionIsTrue() {
    var notOptions = EnumSet.allOf(PwaApplicationType.class);
    notOptions.remove(PwaApplicationType.OPTIONS_VARIATION);

    when(padProjectInformationService.getPermanentDepositsOnApplication(pwaApplicationDetail)).thenReturn(true);

    for (PwaApplicationType type : notOptions) {
      pwaApplicationDetail.getPwaApplication().setApplicationType(type);
      assertThat(permanentDepositService.canShowInTaskList(pwaApplicationDetail)).isTrue();
    }

  }

  @Test
  public void canShowInTaskList_OptionsVariation_optionsNotComplete() {
    when(padOptionConfirmedService.approvedOptionConfirmed(pwaApplicationDetail)).thenReturn(false);

    pwaApplicationDetail.getPwaApplication().setApplicationType(PwaApplicationType.OPTIONS_VARIATION);

    assertThat(permanentDepositService.canShowInTaskList(pwaApplicationDetail)).isFalse();

  }

  @Test
  public void canShowInTaskList_OptionsVariation_optionsComplete_andPermDepositsQuestionIsTrue() {
    when(padOptionConfirmedService.approvedOptionConfirmed(pwaApplicationDetail)).thenReturn(true);
    when(padProjectInformationService.getPermanentDepositsOnApplication(pwaApplicationDetail)).thenReturn(true);

    pwaApplicationDetail.getPwaApplication().setApplicationType(PwaApplicationType.OPTIONS_VARIATION);

    assertThat(permanentDepositService.canShowInTaskList(pwaApplicationDetail)).isTrue();

  }

  @Test
  public void canShowInTaskList_OptionsVariation_optionsComplete_andPermDepositsQuestionIsFalse() {
    when(padOptionConfirmedService.approvedOptionConfirmed(pwaApplicationDetail)).thenReturn(true);
    when(padProjectInformationService.getPermanentDepositsOnApplication(pwaApplicationDetail)).thenReturn(false);

    pwaApplicationDetail.getPwaApplication().setApplicationType(PwaApplicationType.OPTIONS_VARIATION);

    assertThat(permanentDepositService.canShowInTaskList(pwaApplicationDetail)).isFalse();

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