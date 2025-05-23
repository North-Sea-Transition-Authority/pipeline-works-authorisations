package uk.co.ogauthority.pwa.features.application.tasks.permdeposit;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import jakarta.persistence.EntityManager;
import jakarta.validation.Validation;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineOverview;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineStatus;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineType;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.features.application.files.PadFileService;
import uk.co.ogauthority.pwa.features.application.tasks.optionconfirmation.PadOptionConfirmedService;
import uk.co.ogauthority.pwa.features.application.tasks.permdeposit.controller.PermanentDepositController;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipeline;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineOverview;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.PadProjectInformation;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.PadProjectInformationService;
import uk.co.ogauthority.pwa.features.application.tasks.projectinfo.PermanentDepositMade;
import uk.co.ogauthority.pwa.features.datatypes.coordinate.CoordinatePairTestUtil;
import uk.co.ogauthority.pwa.features.filemanagement.PadFileManagementService;
import uk.co.ogauthority.pwa.features.generalcase.pipelineview.PipelineAndIdentViewFactory;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.fds.ErrorItem;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineHeaderView;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.entitycopier.EntityCopyingService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PermanentDepositServiceTest {

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

  @Mock
  private PadFileManagementService padFileManagementService;

  @Mock
  private EntityManager entityManager;


  private SpringValidatorAdapter groupValidator;


  private PermanentDepositService permanentDepositService;
  private PadPermanentDeposit padPermanentDeposit = new PadPermanentDeposit();
  private PermanentDepositsForm form = new PermanentDepositsForm();
  private PwaApplicationDetail pwaApplicationDetail;
  private LocalDate date;
  private WebUserAccount user = new WebUserAccount(1);

  private static int DEPOSIT_ID_1 = 1;

  @BeforeEach
  void setUp() {

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
        padOptionConfirmedService,
        padFileManagementService,
        entityManager);

    date = LocalDate.now();

    var pwaApplication = new PwaApplication(null, PwaApplicationType.INITIAL, null);
    pwaApplicationDetail = new PwaApplicationDetail(pwaApplication, null, null, null);
  }


  @Test
  void saveEntityUsingForm_verifyServiceInteractions() {
    form.setSelectedPipelines(Set.of("1", "2"));
    pwaApplicationDetail.setId(1);
    padPermanentDeposit.setPwaApplicationDetail(pwaApplicationDetail);
    when(permanentDepositInformationRepository.save(padPermanentDeposit)).thenReturn(padPermanentDeposit);

    permanentDepositService.saveEntityUsingForm(pwaApplicationDetail, form, user);
    verify(permanentDepositEntityMappingService, times(1)).setEntityValuesUsingForm(padPermanentDeposit, form);
    verify(permanentDepositInformationRepository, times(1)).save(padPermanentDeposit);

  }

  @Test
  void validate_verifyServiceInteraction() {
    var form = new PermanentDepositsForm();
    var bindingResult = new BeanPropertyBindingResult(form, "form");
    when(padProjectInformationService.getPadProjectInformationData(pwaApplicationDetail)).thenReturn(new PadProjectInformation());
    when(permanentDepositInformationRepository.getAllByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(List.of());
    permanentDepositService.validate(form, bindingResult, ValidationType.FULL, pwaApplicationDetail);
    verify(validator, times(1)).validate(
        form, bindingResult, PadPermanentDepositTestUtil.createValidationHints(pwaApplicationDetail));
  }

  @Test
  void getDepositSummaryScreenValidationResult_noDeposits_incomplete() {
    when( permanentDepositInformationRepository.findByPwaApplicationDetailOrderByReferenceAsc(pwaApplicationDetail)).thenReturn(List.of());
    var summaryResult = permanentDepositService.getDepositSummaryScreenValidationResult(pwaApplicationDetail);
    assertThat(summaryResult.isSectionComplete()).isFalse();
    assertThat(summaryResult.getSectionIncompleteError()).isEqualTo("Ensure that at least one deposit has been added and that they are all valid.");
  }

  @Test
  void getDepositSummaryScreenValidationResult_depositsInvalid_depositsHaveErrors() {
    var entityMapper = new PermanentDepositEntityMappingServiceTest();
    PadPermanentDeposit entity = entityMapper.buildBaseEntity();
    PadPermanentDeposit entity2 = entityMapper.buildBaseEntity();
    entity2.setId(entity2.getId() + 1);

    when(permanentDepositInformationRepository.findByPwaApplicationDetailOrderByReferenceAsc(pwaApplicationDetail))
        .thenReturn(List.of(entity, entity2));
    when(padProjectInformationService.getPadProjectInformationData(pwaApplicationDetail)).thenReturn(new PadProjectInformation());
    when(permanentDepositInformationRepository.getAllByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(List.of());

    doAnswer(invocation -> {
      BindingResult result = invocation.getArgument(1);
      result.rejectValue("materialType", "fake.code", "fake message");
      return result;
    }).when(validator).validate(any(), any(), any(Object[].class));

    var summaryResult = permanentDepositService.getDepositSummaryScreenValidationResult(pwaApplicationDetail);

    assertThat(summaryResult.isSectionComplete()).isFalse();
    assertThat(summaryResult.getInvalidObjectIds()).containsExactly(String.valueOf(entity.getId()), String.valueOf(entity2.getId()));
    assertThat(summaryResult.getIdPrefix()).isEqualTo("deposit-");
    assertThat(summaryResult.getErrorItems()).extracting(ErrorItem::getDisplayOrder, ErrorItem::getFieldName, ErrorItem::getErrorMessage)
        .containsExactly(
            tuple(1, "deposit-" + entity.getId(), entity.getReference() + " must have all sections completed without errors"),
            tuple(2, "deposit-" + entity2.getId(), entity2.getReference() + " must have all sections completed without errors")
        );
  }

  @Test
  void getDepositSummaryScreenValidationResult_depositsValid_noErrors() {
    var entityMapper = new PermanentDepositEntityMappingServiceTest();
    PadPermanentDeposit entity = entityMapper.buildBaseEntity();

    when(permanentDepositInformationRepository.findByPwaApplicationDetailOrderByReferenceAsc(pwaApplicationDetail)).thenReturn(List.of(entity));
    when(padProjectInformationService.getPadProjectInformationData(pwaApplicationDetail)).thenReturn(new PadProjectInformation());
    when(permanentDepositInformationRepository.getAllByPwaApplicationDetail(pwaApplicationDetail)).thenReturn(List.of());

    var summaryResult = permanentDepositService.getDepositSummaryScreenValidationResult(pwaApplicationDetail);
    assertThat(summaryResult.isSectionComplete()).isTrue();
    assertThat(summaryResult.getSectionIncompleteError()).isNull();
    assertThat(summaryResult.getInvalidObjectIds()).isEmpty();
  }


  @Test
  void isPermanentDepositMade_depositMadeTrue() {
    when(padProjectInformationService.getPermanentDepositsMadeAnswer(pwaApplicationDetail)).thenReturn(Optional.of(
        PermanentDepositMade.THIS_APP));

    assertThat(permanentDepositService.permanentDepositsAreToBeMadeOnApp(pwaApplicationDetail)).isEqualTo(true);
  }

  @Test
  void isPermanentDepositMade_depositMadeFalse() {
    when(padProjectInformationService.getPermanentDepositsMadeAnswer(pwaApplicationDetail)).thenReturn(Optional.of(PermanentDepositMade.LATER_APP));

    assertThat(permanentDepositService.permanentDepositsAreToBeMadeOnApp(pwaApplicationDetail)).isEqualTo(false);
  }

  @Test
  void isPermanentDepositMade_depcon() {
    var pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.DEPOSIT_CONSENT);

    assertThat(permanentDepositService.permanentDepositsAreToBeMadeOnApp(pwaApplicationDetail)).isEqualTo(true);
  }

  @Test
  void getPermanentDepositViews_serviceInteraction() {
    var permDeposit = getPadPermanentDeposit();
    when(permanentDepositInformationRepository.findByPwaApplicationDetailOrderByReferenceAsc(pwaApplicationDetail))
        .thenReturn(List.of(permDeposit));
    assertThat(permanentDepositService.getPermanentDepositViews(pwaApplicationDetail)).isNotNull();
    verify(permanentDepositEntityMappingService, times(1)).createPermanentDepositOverview(permDeposit, new HashMap<>());
  }

  @Test
  void createViewFromEntity_serviceInteractions(){
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

  @Test
  void mapEntityToFormById_noEntityExists() {
    var form = new PermanentDepositsForm();
    assertThrows(PwaEntityNotFoundException.class, () ->
      permanentDepositService.mapEntityToFormById(1, form));
  }

  @Test
  void getPipelinesMapForDeposits_1pipelineInService_1pipelineRTS() {

    var pipelineOnSeabed = new Pipeline();
    pipelineOnSeabed.setId(1);
    var padPipelineOnSeabed = new PadPipeline();
    padPipelineOnSeabed.setPipeline(pipelineOnSeabed);
    padPipelineOnSeabed.setPipelineStatus(PipelineStatus.IN_SERVICE);
    padPipelineOnSeabed.setPipelineRef("my ref");
    padPipelineOnSeabed.setPipelineType(PipelineType.HYDRAULIC_JUMPER_MULTI_CORE);
    var padPipelineOverviewOnSeabed = new PadPipelineOverview(padPipelineOnSeabed, 1L);

    var pipelineNotOnSeabed = new Pipeline();
    pipelineNotOnSeabed.setId(2);
    var padPipelineNotOnSeabed = new PadPipeline();
    padPipelineNotOnSeabed.setPipeline(pipelineNotOnSeabed);
    padPipelineNotOnSeabed.setPipelineStatus(PipelineStatus.RETURNED_TO_SHORE);
    var padPipelineOverviewNotOnSeabed = new PadPipelineOverview(padPipelineNotOnSeabed, 1L);

    when(pipelineAndIdentViewFactory.getAllPipelineOverviewsFromAppAndMasterPwa(
        pwaApplicationDetail,
        PipelineAndIdentViewFactory.ConsentedPipelineFilter.ALL_CURRENT_STATUS_PIPELINES
    ))
        .thenReturn(Map.of(
        PipelineId.from(padPipelineOverviewOnSeabed), padPipelineOverviewOnSeabed,
        PipelineId.from(padPipelineOverviewNotOnSeabed), padPipelineOverviewNotOnSeabed));

    var pipelinesIdAndNameMap = permanentDepositService.getPipelinesMapForDeposits(pwaApplicationDetail);
    assertThat(pipelinesIdAndNameMap).containsOnly(
        entry(String.valueOf(padPipelineOverviewOnSeabed.getPipelineId()),
            padPipelineOnSeabed.getPipelineRef() + " - " + padPipelineOnSeabed.getPipelineType().getDisplayName()));
  }


  @Test
  void createViewFromDepositId_onePipelineForDeposit() {

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
  void getEditUrlsForDeposits() {
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
  void getRemoveUrlsForDeposits() {
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
  void removeDeposit_noEntityFound() {
    assertThrows(PwaEntityNotFoundException.class, () ->
      permanentDepositService.removeDeposit(5));
  }


  @Test
  void getPermanentDepositForPipelinesMap() {

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
  void cleanupData_hiddenData() {

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
  void removePadPipelineFromDeposits_removeOnlyLinks() {

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
  void removePadPipelineFromDeposits_removeLinksAndDeposits() {

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
  void removePadPipelineFromDeposits_pipelineConsented() {

    var padPipeline = new PadPipeline(pwaApplicationDetail);

    verifyNoInteractions(padDepositPipelineRepository);
    verifyNoInteractions(permanentDepositInformationRepository);
    verifyNoInteractions(depositDrawingsService);
  }

  @Test
  void canShowInTaskList_notOptionsVariation_andPermDepositsQuestionIsTrue() {
    var notOptions = EnumSet.allOf(PwaApplicationType.class);
    notOptions.remove(PwaApplicationType.OPTIONS_VARIATION);

    when(padProjectInformationService.getPermanentDepositsMadeAnswer(pwaApplicationDetail)).thenReturn(Optional.of(PermanentDepositMade.THIS_APP));

    for (PwaApplicationType type : notOptions) {
      pwaApplicationDetail.getPwaApplication().setApplicationType(type);
      assertThat(permanentDepositService.canShowInTaskList(pwaApplicationDetail)).isTrue();
    }

  }

  @Test
  void canShowInTaskList_OptionsVariation_optionsNotComplete() {
    when(padOptionConfirmedService.approvedOptionConfirmed(pwaApplicationDetail)).thenReturn(false);

    pwaApplicationDetail.getPwaApplication().setApplicationType(PwaApplicationType.OPTIONS_VARIATION);

    assertThat(permanentDepositService.canShowInTaskList(pwaApplicationDetail)).isFalse();

  }

  @Test
  void canShowInTaskList_OptionsVariation_optionsComplete_andPermDepositsQuestionIsTrue() {
    when(padOptionConfirmedService.approvedOptionConfirmed(pwaApplicationDetail)).thenReturn(true);
    when(padProjectInformationService.getPermanentDepositsMadeAnswer(pwaApplicationDetail)).thenReturn(Optional.of(PermanentDepositMade.YES));

    pwaApplicationDetail.getPwaApplication().setApplicationType(PwaApplicationType.OPTIONS_VARIATION);

    assertThat(permanentDepositService.canShowInTaskList(pwaApplicationDetail)).isTrue();

  }

  @Test
  void canShowInTaskList_OptionsVariation_optionsComplete_andPermDepositsQuestionIsFalse() {
    when(padOptionConfirmedService.approvedOptionConfirmed(pwaApplicationDetail)).thenReturn(true);
    when(padProjectInformationService.getPermanentDepositsMadeAnswer(pwaApplicationDetail)).thenReturn(Optional.of(PermanentDepositMade.NONE));

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