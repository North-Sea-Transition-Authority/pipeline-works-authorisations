package uk.co.ogauthority.pwa.service.pwaapplications.shared.campaignworks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.campaignworks.PadCampaignWorkSchedule;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.campaignworks.PadCampaignWorksPipeline;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.campaignworks.CampaignWorkScheduleValidationHint;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.campaignworks.WorkScheduleForm;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.campaignworks.WorkScheduleFormValidator;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PadPipelineOverview;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.campaignworks.PadCampaignWorkScheduleRepository;
import uk.co.ogauthority.pwa.repository.pwaapplications.shared.campaignworks.PadCampaignWorksPipelineRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ValidationType;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.projectinformation.PadProjectInformationService;
import uk.co.ogauthority.pwa.util.PwaApplicationTestUtil;
import uk.co.ogauthority.pwa.util.forminputs.twofielddate.TwoFieldDateInput;

@RunWith(MockitoJUnitRunner.class)
public class CampaignWorksServiceTest {

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

  private CampaignWorksService campaignWorksService;

  private List<PadPipeline> padPipelineList;
  private List<Integer> padPipelineIdList;
  private PadPipeline pipe1;
  private PadPipeline pipe2;

  private WorkScheduleForm workScheduleForm;

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
        padCampaignWorksPipelineRepository
    );

    pipe1 = new PadPipeline(pwaApplicationDetail);
    pipe1.setId(10);

    pipe2 = new PadPipeline(pwaApplicationDetail);
    pipe2.setId(20);
    padPipelineList = List.of(pipe1, pipe2);
    padPipelineIdList = List.of(pipe1.getId(), pipe2.getId());

    workScheduleForm = new WorkScheduleForm();
    workScheduleForm.setWorkStart(new TwoFieldDateInput(2020, 2));
    workScheduleForm.setWorkEnd(new TwoFieldDateInput(2021, 1));
    workScheduleForm.setPadPipelineIds(padPipelineIdList);

    when(padPipelineService.getByIdList(pwaApplicationDetail, padPipelineIdList)).thenReturn(padPipelineList);
    when(workScheduleFormValidator.supports(any())).thenCallRealMethod();

  }

  @Test
  public void canShowInTaskList_whennAppPipelines_andCampaignApproachIsUsed() {
    when(padProjectInformationService.isCampaignApproachBeingUsed(pwaApplicationDetail)).thenReturn(true);
    when(padPipelineService.totalPipelineContainedInApplication(pwaApplicationDetail)).thenReturn(1L);

    assertThat(campaignWorksService.canShowInTaskList(pwaApplicationDetail)).isTrue();
  }

  @Test
  public void canShowInTaskList_whenNoAppPipelines_andCampaignApproachIsUsed() {
    when(padProjectInformationService.isCampaignApproachBeingUsed(pwaApplicationDetail)).thenReturn(true);
    when(padPipelineService.totalPipelineContainedInApplication(pwaApplicationDetail)).thenReturn(0L);

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

    var pipe1Overview = new PadPipelineOverview(pipe1, 1L);
    var pipe2Overview = new PadPipelineOverview(pipe2, 2L);

    when(padPipelineService.getPipelineOverviews(pwaApplicationDetail))
        .thenReturn(List.of(pipe1Overview, pipe2Overview));

    var workScheduleViewList = campaignWorksService.getWorkScheduleViews(pwaApplicationDetail);

    assertThat(workScheduleViewList.size()).isEqualTo(2);

    //schedule 1 view checks
    assertThat(workScheduleViewList).anySatisfy(workScheduleView -> {
      assertThat(workScheduleView.getWorkStartDate()).isEqualTo(schedule1.getWorkFromDate());
      assertThat(workScheduleView.getWorkEndDate()).isEqualTo(schedule1.getWorkToDate());
      assertThat(workScheduleView.getSchedulePipelines()).anySatisfy(po -> po.getPadPipelineId().equals(pipe1.getId()));
    });

    //schedule 2 view checks
    assertThat(workScheduleViewList).anySatisfy(workScheduleView -> {
      assertThat(workScheduleView.getWorkStartDate()).isEqualTo(schedule2.getWorkFromDate());
      assertThat(workScheduleView.getWorkEndDate()).isEqualTo(schedule2.getWorkToDate());
      assertThat(workScheduleView.getSchedulePipelines()).anySatisfy(po -> po.getPadPipelineId().equals(pipe1.getId()));
      assertThat(workScheduleView.getSchedulePipelines()).anySatisfy(po -> po.getPadPipelineId().equals(pipe2.getId()));
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

  public void assetValidationHintsWhenNoProjectInfoDate(List<Object> validationHints, long expectedLatestDateMonths){
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

    var clock = Clock.fixed(Clock.systemUTC().instant(), ZoneId.systemDefault() );

    when(padProjectInformationService.getProposedStartDate(pwaApplicationDetail)).thenReturn(Optional.of(clock.instant()));

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
      assertThat(hint.getEarliestWorkStartDateHint().getDateLabel()).contains("Project information proposed start date");
      assertThat(hint.getEarliestWorkStartDateHint().getDate()).isEqualTo(LocalDate.ofInstant(clock.instant(), ZoneId.systemDefault()));
      // check embedded latest date hint
      var expectedLatestDate = LocalDate.now().plusMonths(12L);
      assertThat(hint.getLatestWorkEndDateHint().getDateLabel())
          .contains(expectedLatestDate.format(CampaignWorkScheduleValidationHint.DATETIME_FORMATTER));
      assertThat(hint.getLatestWorkEndDateHint().getDate()).isEqualTo(expectedLatestDate);
    });

  }


}