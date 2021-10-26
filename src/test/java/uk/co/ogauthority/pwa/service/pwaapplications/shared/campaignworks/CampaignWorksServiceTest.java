package uk.co.ogauthority.pwa.service.pwaapplications.shared.campaignworks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineType;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.campaignworks.PadCampaignWorkSchedule;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.campaignworks.PadCampaignWorksPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.campaignworks.CampaignWorkScheduleValidationHint;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.campaignworks.WorkScheduleForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.campaignworks.WorkScheduleFormValidator;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PadPipelineOverview;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.campaignworks.PadCampaignWorkScheduleRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.campaignworks.PadCampaignWorksPipelineRepository;
import uk.co.ogauthority.pwa.service.entitycopier.EntityCopyingService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.appdetailreconciliation.PadPipelineReconcilerService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.projectinformation.PadProjectInformationService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.TwoFieldDateInput;

@RunWith(MockitoJUnitRunner.class)
public class CampaignWorksServiceTest {

  private static final int SCHEDULE_ID = 1;
  private static final int INVALID_SCHEDULE_ID = 2;

  @Mock
  private PadProjectInformationService padProjectInformationService;

  @Mock
  private PadPipelineService padPipelineService;

  @Mock
  private WorkScheduleFormValidator workScheduleFormValidator;

  @Mock
  private PadCampaignWorkScheduleRepository padCampaignWorkScheduleRepository;

  @Mock
  private PadCampaignWorksPipelineRepository padCampaignWorksPipelineRepository;

  @Mock
  private EntityCopyingService entityCopyingService;

  @Mock
  private PadPipelineReconcilerService padPipelineReconcilerService;

  private CampaignWorksService campaignWorksService;

  private List<PadPipeline> padPipelineList;
  private List<Integer> padPipelineIdList;
  private PadPipeline pipe1;
  private PadPipeline pipe2;

  private WorkScheduleForm workScheduleForm;

  private PadCampaignWorkSchedule workSchedule;

  private PwaApplicationDetail pwaApplicationDetail;

  @Before
  public void setup() {
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(
        PwaApplicationType.INITIAL);

    campaignWorksService = new CampaignWorksService(
        padProjectInformationService,
        padPipelineService,
        workScheduleFormValidator,
        padCampaignWorkScheduleRepository,
        padCampaignWorksPipelineRepository,
        entityCopyingService,
        padPipelineReconcilerService);

    pipe1 = createPadPipeline(10, "PIPE1", pwaApplicationDetail);
    pipe2 = createPadPipeline(20, "PIPE2", pwaApplicationDetail);
    padPipelineList = List.of(pipe1, pipe2);
    padPipelineIdList = List.of(pipe1.getId(), pipe2.getId());

    workScheduleForm = new WorkScheduleForm();

    workScheduleForm.setWorkStart(new TwoFieldDateInput(2020, 2));
    workScheduleForm.setWorkEnd(new TwoFieldDateInput(2021, 1));
    workScheduleForm.setPadPipelineIds(padPipelineIdList);

    when(padPipelineService.getByIdList(pwaApplicationDetail, padPipelineIdList)).thenReturn(padPipelineList);
    when(workScheduleFormValidator.supports(any())).thenCallRealMethod();

    workSchedule = new PadCampaignWorkSchedule();
    workSchedule.setId(SCHEDULE_ID);
    workSchedule.setPwaApplicationDetail(pwaApplicationDetail);
    workSchedule.setWorkFromDate(LocalDate.MIN);
    workSchedule.setWorkToDate(LocalDate.MAX);

    when(padCampaignWorkScheduleRepository.findByPwaApplicationDetailAndId(pwaApplicationDetail, SCHEDULE_ID))
        .thenReturn(Optional.of(workSchedule));


  }

  private PadPipeline createPadPipeline(int id, String pipelineNumber, PwaApplicationDetail pwaApplicationDetail){
    var pipe = new PadPipeline(pwaApplicationDetail);
    pipe.setId(id);
    pipe.setPipelineRef(pipelineNumber);
    pipe.setLength(BigDecimal.ONE);
    pipe.setPipelineType(PipelineType.PRODUCTION_FLOWLINE);
    return pipe;
  }

  @Test
  public void canShowInTaskList_whennAppPipelines_andCampaignApproachIsUsed() {
    when(padProjectInformationService.isCampaignApproachBeingUsed(pwaApplicationDetail)).thenReturn(true);
    when(padPipelineService.getTotalPipelinesContainedInApplication(pwaApplicationDetail)).thenReturn(1L);

    assertThat(campaignWorksService.canShowInTaskList(pwaApplicationDetail)).isTrue();
  }

  @Test
  public void canShowInTaskList_whenNoAppPipelines_andCampaignApproachIsUsed() {
    when(padProjectInformationService.isCampaignApproachBeingUsed(pwaApplicationDetail)).thenReturn(true);
    when(padPipelineService.getTotalPipelinesContainedInApplication(pwaApplicationDetail)).thenReturn(0L);

    assertThat(campaignWorksService.canShowInTaskList(pwaApplicationDetail)).isFalse();
  }

  @Test
  public void canShowInTaskList_whenNoConditionsMet() {
    assertThat(campaignWorksService.canShowInTaskList(pwaApplicationDetail)).isFalse();
  }

  @Test
  public void addCampaignWorkScheduleFromForm_serviceInteractions_andEntityMappingChecks() {

    when(padCampaignWorkScheduleRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    var scheduleArgCapture = ArgumentCaptor.forClass(PadCampaignWorkSchedule.class);

    //noinspection unchecked
    ArgumentCaptor<List<PadCampaignWorksPipeline>> schedulePipelineArgCapture = ArgumentCaptor.forClass(List.class);

    campaignWorksService.addCampaignWorkScheduleFromForm(workScheduleForm, pwaApplicationDetail);
    verify(padCampaignWorkScheduleRepository, times(1)).save(scheduleArgCapture.capture());
    verify(padCampaignWorksPipelineRepository, times(1)).saveAll(schedulePipelineArgCapture.capture());

    var savedSchedule = scheduleArgCapture.getValue();
    assertThat(savedSchedule.getPwaApplicationDetail()).isEqualTo(pwaApplicationDetail);
    assertThat(savedSchedule.getWorkFromDate()).isEqualTo(workScheduleForm.getWorkStart().createDateOrNull());
    assertThat(savedSchedule.getWorkToDate()).isEqualTo(workScheduleForm.getWorkEnd().createDateOrNull());


    assertThat(schedulePipelineArgCapture.getValue()).anySatisfy(schedulePipeline -> {
      assertThat(schedulePipeline.getPadCampaignWorkSchedule()).isEqualTo(savedSchedule);
      assertThat(schedulePipeline.getPadPipeline()).isEqualTo(pipe1);
    });

    assertThat(schedulePipelineArgCapture.getValue()).anySatisfy(schedulePipeline -> {
      assertThat(schedulePipeline.getPadCampaignWorkSchedule()).isEqualTo(savedSchedule);
      assertThat(schedulePipeline.getPadPipeline()).isEqualTo(pipe2);
    });

    assertThat(schedulePipelineArgCapture.getValue().size()).isEqualTo(2);

  }

  @Test
  public void getWorkScheduleViews_mutlipleSchedules_multiplePipelines() {
    var schedule1 = new PadCampaignWorkSchedule(pwaApplicationDetail, 1);
    schedule1.setWorkFromDate(LocalDate.of(2020, 1, 1));
    schedule1.setWorkToDate(LocalDate.of(2020, 2, 1));

    var schedule2 = new PadCampaignWorkSchedule(pwaApplicationDetail, 2);
    schedule2.setWorkFromDate(LocalDate.of(2020, 2, 1));
    schedule2.setWorkToDate(LocalDate.of(2020, 3, 1));

    schedule1.setWorkToDate(workScheduleForm.getWorkEnd().createDateOrNull());
    var schedule1Pipeline1 = new PadCampaignWorksPipeline(schedule1, pipe1);
    var schedule2Pipeline1 = new PadCampaignWorksPipeline(schedule2, pipe1);
    var schedule2Pipeline2 = new PadCampaignWorksPipeline(schedule2, pipe2);

    when(padCampaignWorksPipelineRepository.findAllByPadCampaignWorkSchedule_pwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(schedule1Pipeline1, schedule2Pipeline1, schedule2Pipeline2));

    List<PipelineOverview> pipelineOverviews = List.of(new PadPipelineOverview(pipe1), new PadPipelineOverview(pipe2));
    when(padPipelineService.getApplicationPipelineOverviews(pwaApplicationDetail)).thenReturn(pipelineOverviews);


    var workScheduleViewList = campaignWorksService.getWorkScheduleViews(pwaApplicationDetail);

    assertThat(workScheduleViewList.size()).isEqualTo(2);

    //schedule 1 view checks
    assertThat(workScheduleViewList).anySatisfy(workScheduleView -> {
      assertThat(workScheduleView.getWorkStartDate()).isEqualTo(schedule1.getWorkFromDate());
      assertThat(workScheduleView.getWorkEndDate()).isEqualTo(schedule1.getWorkToDate());
      assertThat(workScheduleView.getSchedulePipelines())
          .anySatisfy(campaignWorkSchedulePipelineView -> {
            assertThat(
              campaignWorkSchedulePipelineView.getPipelineNumber()).isEqualTo(pipe1.getPipelineRef());
            assertThat(campaignWorkSchedulePipelineView.getPipelineName()).isNotEmpty();
          });
    });

    //schedule 2 view checks
    assertThat(workScheduleViewList).anySatisfy(workScheduleView -> {
      assertThat(workScheduleView.getWorkStartDate()).isEqualTo(schedule2.getWorkFromDate());
      assertThat(workScheduleView.getWorkEndDate()).isEqualTo(schedule2.getWorkToDate());
      assertThat(workScheduleView.getSchedulePipelines()).anySatisfy(campaignWorkSchedulePipelineView -> {
        assertThat(
          campaignWorkSchedulePipelineView.getPipelineNumber()).isEqualTo(pipe1.getPipelineRef());
        assertThat(campaignWorkSchedulePipelineView.getPipelineName()).isNotEmpty();
      });
      assertThat(workScheduleView.getSchedulePipelines()).anySatisfy(campaignWorkSchedulePipelineView -> {
        assertThat(
          campaignWorkSchedulePipelineView.getPipelineNumber()).isEqualTo(pipe2.getPipelineRef());
        assertThat(campaignWorkSchedulePipelineView.getPipelineName()).isNotEmpty();
      });
    });

  }

  @Test
  public void getWorkScheduleViews_zeroSchedules() {

    var workScheduleViewList = campaignWorksService.getWorkScheduleViews(pwaApplicationDetail);

    assertThat(workScheduleViewList).isEmpty();

  }

  @Test
  public void validate_assertValidationHints_whenNoProjectInfoProposedStartDate_andInitialAppType() {

    var originalBindingResult = new BeanPropertyBindingResult(workScheduleForm, "form");
    var bindingResult = campaignWorksService.validate(
        workScheduleForm,
        originalBindingResult,
        ValidationType.FULL,
        pwaApplicationDetail);

    ArgumentCaptor<Object> hintCapture = ArgumentCaptor.forClass(Object.class);
    verify(workScheduleFormValidator, times(1))
        .validate(eq(workScheduleForm), eq(originalBindingResult), hintCapture.capture());
    var capturedHints = hintCapture.getAllValues();
    assetValidationHintsWhenNoProjectInfoDate(capturedHints, 12L);

  }

  @Test
  public void validate_assertValidationHints_whenNoProjectInfoProposedStartDate_andOptionsAppType() {

    pwaApplicationDetail.getPwaApplication().setApplicationType(PwaApplicationType.OPTIONS_VARIATION);

    var originalBindingResult = new BeanPropertyBindingResult(workScheduleForm, "form");
    var bindingResult = campaignWorksService.validate(
        workScheduleForm,
        originalBindingResult,
        ValidationType.FULL,
        pwaApplicationDetail);

    ArgumentCaptor<Object> hintCapture = ArgumentCaptor.forClass(Object.class);
    verify(workScheduleFormValidator, times(1))
        .validate(eq(workScheduleForm), eq(originalBindingResult), hintCapture.capture());

    var capturedHints = hintCapture.getAllValues();
    assetValidationHintsWhenNoProjectInfoDate(capturedHints, 6L);

  }

  public void assetValidationHintsWhenNoProjectInfoDate(List<Object> validationHints, long expectedLatestDateMonths) {
    assertThat(validationHints.get(0)).isEqualTo(pwaApplicationDetail);
    assertThat(validationHints.get(1)).satisfies(o -> {
      var hint = (CampaignWorkScheduleValidationHint) o;
      // check earliest date
      assertThat(hint.getEarliestDate()).isEqualTo(LocalDate.now());
      // check embedded earliest date hint
      assertThat(hint.getEarliestWorkStartDateHint().getDateLabel()).isEqualTo("today's date");
      assertThat(hint.getEarliestWorkStartDateHint().getDate()).isEqualTo(LocalDate.now());
      // check embedded latest date hint

      var expectedLatestDate = LocalDate.now().plusMonths(expectedLatestDateMonths);
      assertThat(hint.getLatestWorkEndDateHint().getDateLabel())
          .isEqualTo(expectedLatestDate.format(CampaignWorkScheduleValidationHint.DATETIME_FORMATTER));
      assertThat(hint.getLatestWorkEndDateHint().getDate()).isEqualTo(expectedLatestDate);
    });
  }


  @Test
  public void validate_assertValidationHints_whenProjectInfoProposedStartDate_andIntialAppType() {

    var clock = Clock.fixed(Clock.systemUTC().instant(), ZoneId.systemDefault());

    when(padProjectInformationService.getProposedStartDate(pwaApplicationDetail)).thenReturn(
        Optional.of(clock.instant()));

    var originalBindingResult = new BeanPropertyBindingResult(workScheduleForm, "form");
    var bindingResult = campaignWorksService.validate(
        workScheduleForm,
        originalBindingResult,
        ValidationType.FULL,
        pwaApplicationDetail);

    ArgumentCaptor<Object> hintCapture = ArgumentCaptor.forClass(Object.class);
    verify(workScheduleFormValidator, times(1))
        .validate(eq(workScheduleForm), eq(originalBindingResult), hintCapture.capture());

    var capturedHints = hintCapture.getAllValues();

    assertThat(capturedHints.get(0)).isEqualTo(pwaApplicationDetail);
    assertThat(capturedHints.get(1)).satisfies(o -> {
      var hint = (CampaignWorkScheduleValidationHint) o;
      // check earliest date
      assertThat(hint.getEarliestDate()).isEqualTo(LocalDate.now());
      // check embedded earliest date hint
      assertThat(hint.getEarliestWorkStartDateHint().getDateLabel()).contains(
          "Project information proposed start of works date");
      assertThat(hint.getEarliestWorkStartDateHint().getDate()).isEqualTo(
          LocalDate.ofInstant(clock.instant(), ZoneId.systemDefault()));
      // check embedded latest date hint
      var expectedLatestDate = LocalDate.now().plusMonths(12L);
      assertThat(hint.getLatestWorkEndDateHint().getDateLabel())
          .contains(expectedLatestDate.format(CampaignWorkScheduleValidationHint.DATETIME_FORMATTER));
      assertThat(hint.getLatestWorkEndDateHint().getDate()).isEqualTo(expectedLatestDate);
    });

  }

  @Test
  public void updateCampaignWorksScheduleFromForm_serviceInteraction() {
    var spySchedule = spy(PadCampaignWorkSchedule.class);

    var listOfOldPipelineLinks = List.of(new PadCampaignWorksPipeline());

    when(padCampaignWorksPipelineRepository.findAllByPadCampaignWorkSchedule(spySchedule))
        .thenReturn(listOfOldPipelineLinks);

    campaignWorksService.updateCampaignWorksScheduleFromForm(workScheduleForm, spySchedule);

    InOrder orderVerifier = Mockito.inOrder(spySchedule, padCampaignWorkScheduleRepository,
        padCampaignWorksPipelineRepository);

    orderVerifier.verify(padCampaignWorksPipelineRepository).findAllByPadCampaignWorkSchedule(spySchedule);
    orderVerifier.verify(padCampaignWorksPipelineRepository).deleteAll(listOfOldPipelineLinks);
    orderVerifier.verify(spySchedule).setWorkFromDate(eq(workScheduleForm.getWorkStart().createDateOrNull()));
    orderVerifier.verify(spySchedule).setWorkToDate(eq(workScheduleForm.getWorkEnd().createDateOrNull()));
    orderVerifier.verify(padCampaignWorkScheduleRepository).save(spySchedule);

  }

  @Test
  public void mapWorkScheduleToForm_mappingAsExpected() {
    var linkedPipeline = new PadCampaignWorksPipeline();
    linkedPipeline.setPadPipeline(pipe1);
    var linkedPipelines = List.of(linkedPipeline);

    var fromDate = LocalDate.of(2020, 1, 1);
    var toDate = LocalDate.of(2020, 12, 1);
    var schedule = new PadCampaignWorkSchedule();
    schedule.setWorkFromDate(fromDate);
    schedule.setWorkToDate(toDate);

    when(padCampaignWorksPipelineRepository.findAllByPadCampaignWorkSchedule(schedule)).thenReturn(linkedPipelines);

    var emptyForm = new WorkScheduleForm();

    campaignWorksService.mapWorkScheduleToForm(emptyForm, schedule);

    assertThat(emptyForm.getPadPipelineIds()).isEqualTo(List.of(pipe1.getId()));
    assertThat(emptyForm.getWorkStart()).isEqualTo(new TwoFieldDateInput(fromDate));
    assertThat(emptyForm.getWorkEnd()).isEqualTo(new TwoFieldDateInput(toDate));

  }

  @Test(expected= PwaEntityNotFoundException.class)
  public void getWorkScheduleOrError_unknownId(){

    campaignWorksService.getWorkScheduleOrError(pwaApplicationDetail, 123);

  }

  @Test
  public void getWorkScheduleOrError_validId(){

    assertThat(campaignWorksService.getWorkScheduleOrError(pwaApplicationDetail, SCHEDULE_ID))
        .isEqualTo(workSchedule);

  }

  @Test
  public void removeCampaignWorksSchedule_serviceInteractions(){

    var schedulePipeline = new PadCampaignWorksPipeline(workSchedule, pipe1);

    when(padCampaignWorksPipelineRepository.findAllByPadCampaignWorkSchedule(
        workSchedule)).thenReturn(List.of(schedulePipeline));
    campaignWorksService.removeCampaignWorksSchedule(workSchedule);

    var orderVerifier = Mockito.inOrder(padCampaignWorkScheduleRepository, padCampaignWorksPipelineRepository);
    orderVerifier.verify(padCampaignWorksPipelineRepository).findAllByPadCampaignWorkSchedule(workSchedule);
    orderVerifier.verify(padCampaignWorksPipelineRepository).deleteAll(eq(List.of(schedulePipeline)));
    orderVerifier.verify(padCampaignWorkScheduleRepository).delete(workSchedule);
    orderVerifier.verifyNoMoreInteractions();

  }

  @Test
  public void createWorkScheduleView_singlePipeline(){
    var schedulePipeline = new PadCampaignWorksPipeline(workSchedule, pipe1);
    when(padCampaignWorksPipelineRepository.findAllByPadCampaignWorkSchedule(
        workSchedule)).thenReturn(List.of(schedulePipeline));
    when(padPipelineService.getPipelineOverview(pipe1)).thenReturn(new PadPipelineOverview(pipe1));

    var workScheduleView = campaignWorksService.createWorkScheduleView(workSchedule);

    assertThat(workScheduleView.getSchedulePipelines())
        .isNotEmpty()
        .allSatisfy(
          campaignWorkSchedulePipelineView -> {
            assertThat(campaignWorkSchedulePipelineView.getPipelineNumber()).isEqualTo(pipe1.getPipelineRef());
            assertThat(campaignWorkSchedulePipelineView.getFromLocation()).isEqualTo(pipe1.getFromLocation());
            assertThat(campaignWorkSchedulePipelineView.getToLocation()).isEqualTo(pipe1.getToLocation());
            assertThat(campaignWorkSchedulePipelineView.getPipelineTypeDisplayName())
                .isEqualTo(pipe1.getPipelineType().getDisplayName());
        });

  }

  @Test
  public void createWorkScheduleView_zeroPipelines(){
    var workScheduleView = campaignWorksService.createWorkScheduleView(workSchedule);
    assertThat(workScheduleView.getSchedulePipelines()).isEmpty();
  }

  /* duplicate logic required for generation of validation result, and that used by the isComplete method. */
  private void setupValidationResultMocks_whenAllApplicationPipelinesWithinAWorkSchedule_andNoFormValidationErrors(){
    when(padPipelineService.getTotalPipelinesContainedInApplication(pwaApplicationDetail))
        .thenReturn(2L);

    var schedulePipeline1 = new PadCampaignWorksPipeline(workSchedule, pipe1);
    var schedulePipeline2 = new PadCampaignWorksPipeline(workSchedule, pipe2);

    // support overall application schedule pipeline check
    when(padCampaignWorksPipelineRepository.findAllByPadCampaignWorkSchedule_pwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(schedulePipeline1, schedulePipeline2));
    // support form object construction
    when(padCampaignWorksPipelineRepository.findAllByPadCampaignWorkSchedule(workSchedule))
        .thenReturn(List.of(schedulePipeline1, schedulePipeline2));
    when(padCampaignWorkScheduleRepository.findByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(workSchedule));

  }

  @Test
  public void getCampaignWorksValidationResult_whenAllApplicationPipelinesWithinAWorkSchedule_andNoFormValidationErrors(){
    setupValidationResultMocks_whenAllApplicationPipelinesWithinAWorkSchedule_andNoFormValidationErrors();
    var result = campaignWorksService.getCampaignWorksValidationResult(pwaApplicationDetail);
    assertThat(result.isSectionComplete()).isTrue();
  }

  @Test
  public void isComplete_whenAllApplicationPipelinesWithinAWorkSchedule_andNoFormValidationErrors(){
    setupValidationResultMocks_whenAllApplicationPipelinesWithinAWorkSchedule_andNoFormValidationErrors();
    assertThat(campaignWorksService.isComplete(pwaApplicationDetail)).isTrue();
  }

  /* duplicate logic required for generation of validation result, and that used by the isComplete method. */
  private void setupValidationResultMocks_whenSubsetOfApplicationPipelineScheduled_andNoFormValidationErrors() {

    when(padPipelineService.getTotalPipelinesContainedInApplication(pwaApplicationDetail))
        .thenReturn(2L);

    var schedulePipeline1 = new PadCampaignWorksPipeline(workSchedule, pipe1);

    // support overall application schedule pipeline check
    when(padCampaignWorksPipelineRepository.findAllByPadCampaignWorkSchedule_pwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(schedulePipeline1));
    // support form object construction
    when(padCampaignWorksPipelineRepository.findAllByPadCampaignWorkSchedule(workSchedule))
        .thenReturn(List.of(schedulePipeline1));
    when(padCampaignWorkScheduleRepository.findByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(workSchedule));
  }

  @Test
  public void getCampaignWorksValidationResult_whenSubsetOfApplicationPipelineScheduled_andNoFormValidationErrors(){
    setupValidationResultMocks_whenSubsetOfApplicationPipelineScheduled_andNoFormValidationErrors();
    var result = campaignWorksService.getCampaignWorksValidationResult(pwaApplicationDetail);
    assertThat(result.isSectionComplete()).isFalse();
  }

  @Test
  public void isComplete_whenSubsetOfApplicationPipelineScheduled_andNoFormValidationErrors(){
    setupValidationResultMocks_whenSubsetOfApplicationPipelineScheduled_andNoFormValidationErrors();
    assertThat(campaignWorksService.isComplete(pwaApplicationDetail)).isFalse();
  }

  @Test
  public void removePipelineFromAllSchedules_serviceInteraction() {
    var campaignWorksPipeline = new PadCampaignWorksPipeline();
    when(padCampaignWorksPipelineRepository.findAllByPadPipeline(pipe1)
    ).thenReturn(List.of(campaignWorksPipeline));
    campaignWorksService.removePipelineFromAllSchedules(pipe1);
    verify(padCampaignWorksPipelineRepository, times(1)).deleteAll(List.of(campaignWorksPipeline));
  }

  @Test
  public void removePadPipelineFromCampaignWorks_serviceInteraction_noCampaignLinks() {
    when(padCampaignWorkScheduleRepository.findByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(workSchedule));
    when(padCampaignWorksPipelineRepository.findAllByPadCampaignWorkSchedule_pwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of());
    campaignWorksService.removePadPipelineFromCampaignWorks(pipe1);
    verify(padCampaignWorkScheduleRepository, times(1)).deleteAll(List.of(workSchedule));
  }

  @Test
  public void removePadPipelineFromCampaignWorks_serviceInteraction_remainingCampaignLinks() {
    var padPipeline = new PadPipeline();
    var worksPipeline = new PadCampaignWorksPipeline(workSchedule, padPipeline);
    when(padCampaignWorkScheduleRepository.findByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(workSchedule));
    when(padCampaignWorksPipelineRepository.findAllByPadCampaignWorkSchedule_pwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(worksPipeline));
    campaignWorksService.removePadPipelineFromCampaignWorks(pipe1);
    verify(padCampaignWorkScheduleRepository, times(1)).findByPwaApplicationDetail(pwaApplicationDetail);
    verifyNoMoreInteractions(padCampaignWorkScheduleRepository);
  }

  /* duplicate logic required for generation of validation result, and that used by the isComplete method. */
  private void setupValidationResultMocks_whenAllApplicationPipelinesWithinAWorkSchedule_andFormValidationHasErrors() {

    var schedulePipeline1 = new PadCampaignWorksPipeline(workSchedule, pipe1);
    var schedulePipeline2 = new PadCampaignWorksPipeline(workSchedule, pipe2);

    // support form object construction
    when(padCampaignWorksPipelineRepository.findAllByPadCampaignWorkSchedule(workSchedule))
        .thenReturn(List.of(schedulePipeline1, schedulePipeline2));

    var invalidWorkSchedule = new PadCampaignWorkSchedule();
    invalidWorkSchedule.setId(INVALID_SCHEDULE_ID);
    invalidWorkSchedule.setWorkFromDate(LocalDate.MIN);
    invalidWorkSchedule.setWorkToDate(LocalDate.MAX);

    when(padCampaignWorkScheduleRepository.findByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(workSchedule, invalidWorkSchedule));

    // add fake error when form has no pipelines
    doAnswer(invocationOnMock -> {
      WorkScheduleForm form = invocationOnMock.getArgument(0);
      Errors errors = invocationOnMock.getArgument(1);
      if (form.getPadPipelineIds().isEmpty()) {
        errors.rejectValue("padPipelineIds", "fake_error");
      }
      return errors;
    }).when(workScheduleFormValidator).validate(any(), any(), any());
  }

  private void setupValidationResultMocks_whenNotAllApplicationPipelinesWithinAWorkSchedule_andNoFormValidationHasErrors() {
    when(padPipelineService.getTotalPipelinesContainedInApplication(pwaApplicationDetail))
        .thenReturn(2L);

    var schedulePipeline1 = new PadCampaignWorksPipeline(workSchedule, pipe1);
    var schedulePipeline2 = new PadCampaignWorksPipeline(workSchedule, pipe1);

    // support overall application schedule pipeline check
    when(padCampaignWorksPipelineRepository.findAllByPadCampaignWorkSchedule_pwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(schedulePipeline1, schedulePipeline2));

    // support form object construction
    when(padCampaignWorksPipelineRepository.findAllByPadCampaignWorkSchedule(workSchedule))
        .thenReturn(List.of(schedulePipeline1, schedulePipeline2));

    when(padCampaignWorkScheduleRepository.findByPwaApplicationDetail(pwaApplicationDetail))
        .thenReturn(List.of(workSchedule, workSchedule));
  }

  @Test
  public void getCampaignWorksValidationResult_whenAllApplicationPipelinesWithinAWorkSchedule_andFormValidationErrors() {
    setupValidationResultMocks_whenAllApplicationPipelinesWithinAWorkSchedule_andFormValidationHasErrors();
    var result = campaignWorksService.getCampaignWorksValidationResult(pwaApplicationDetail);
    assertThat(result.isSectionComplete()).isFalse();
    assertThat(result.getInvalidObjectIds()).doesNotContain(String.valueOf(SCHEDULE_ID));
    assertThat(result.getInvalidObjectIds()).contains(String.valueOf(INVALID_SCHEDULE_ID));
  }

  @Test
  public void getCampaignWorksValidationResult_whenNotAllApplicationPipelinesWithinAWorkSchedule_andNoFormValidationErrors() {
    setupValidationResultMocks_whenNotAllApplicationPipelinesWithinAWorkSchedule_andNoFormValidationHasErrors();
    var result = campaignWorksService.getCampaignWorksValidationResult(pwaApplicationDetail);
    assertThat(result.isSectionComplete()).isFalse();
    assertThat(result.getSectionIncompleteError()).isEqualTo(
        "All application pipelines must be covered by a work schedule and all work schedules must be valid");
  }

  @Test
  public void isComplete_whenAllApplicationPipelinesWithinAWorkSchedule_andFormValidationErrors() {
    setupValidationResultMocks_whenAllApplicationPipelinesWithinAWorkSchedule_andFormValidationHasErrors();
    assertThat(campaignWorksService.isComplete(pwaApplicationDetail)).isFalse();
  }



}