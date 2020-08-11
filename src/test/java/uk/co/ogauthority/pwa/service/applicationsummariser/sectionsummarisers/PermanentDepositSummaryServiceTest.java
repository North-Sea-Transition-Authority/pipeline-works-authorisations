package uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers;


import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.enums.permanentdeposits.MaterialType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PermanentDepositOverview;
import uk.co.ogauthority.pwa.model.location.CoordinatePairTestUtil;
import uk.co.ogauthority.pwa.model.view.StringWithTag;
import uk.co.ogauthority.pwa.model.view.Tag;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.service.diff.DiffService;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.TaskListService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits.PermanentDepositService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PermanentDepositSummaryServiceTest {

  private final String TEMPLATE = "TEMPLATE";

  @Mock
  private TaskListService taskListService;

  @Mock
  private PermanentDepositService permanentDepositService;

  @Mock
  private DiffService diffService;

  private PermanentDepositSummaryService permanentDepositSummaryService;
  private PwaApplicationDetail oldPwaApplicationDetail;
  private PwaApplicationDetail newPwaApplicationDetail;

  @Before
  public void setUp() throws Exception {
    permanentDepositSummaryService = new PermanentDepositSummaryService(
        taskListService,
        permanentDepositService,
        diffService);

    oldPwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 1, 1);
    newPwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 1, 2);


  }

  @Test
  public void canSummarise_serviceInteractions() {
    when(taskListService.anyTaskShownForApplication(any(), any())).thenReturn(true);
    assertThat(permanentDepositSummaryService.canSummarise(newPwaApplicationDetail, oldPwaApplicationDetail)).isTrue();

  }

  @Test
  public void canSummarise_whenOnlyOldVersionHasTask() {
    when(taskListService.anyTaskShownForApplication(any(), eq(oldPwaApplicationDetail))).thenReturn(true);
    assertThat(permanentDepositSummaryService.canSummarise(newPwaApplicationDetail, oldPwaApplicationDetail)).isTrue();

  }

  @Test
  public void canSummarise_whenOnlyNewVersionHasTask() {
    when(taskListService.anyTaskShownForApplication(any(), eq(newPwaApplicationDetail))).thenReturn(true);
    assertThat(permanentDepositSummaryService.canSummarise(newPwaApplicationDetail, oldPwaApplicationDetail)).isTrue();
  }

  @Test
  public void canSummarise_whenNeitherVersionHasTask() {
    assertThat(permanentDepositSummaryService.canSummarise(newPwaApplicationDetail, oldPwaApplicationDetail)).isFalse();
  }

  @Test
  public void summariseDifferences_verifyServiceInteractions() {
    var newDetailOverviewList = List.of(
        getOverview(1, "ONE", "Type 1"),
        getOverview(2, "TWO", "Type 2")
    );
    when(permanentDepositService.getPermanentDepositViews(newPwaApplicationDetail)).thenReturn(newDetailOverviewList);

    List<Map<String, ?>> fakeDiffOutput = List.of();
    when(diffService.diffComplexLists(any(), any(), any(), any())).thenReturn(fakeDiffOutput);

    var appSummary = permanentDepositSummaryService.summariseDifferences(newPwaApplicationDetail, oldPwaApplicationDetail, TEMPLATE);

    verify(diffService, times(1)).diffComplexLists(eq(newDetailOverviewList), any(), any(), any());

    assertThat(appSummary.getTemplatePath()).isEqualTo(TEMPLATE);
    assertThat(appSummary.getTemplateModel()).contains(entry("diffedDepositList", fakeDiffOutput));
    assertThat(appSummary.getTemplateModel()).contains(entry("sectionDisplayText", ApplicationTask.PERMANENT_DEPOSITS.getDisplayName()));
    assertThat(appSummary.getSidebarSectionLinks()).containsExactly(
        SidebarSectionLink.createAnchorLink(ApplicationTask.PERMANENT_DEPOSITS.getDisplayName(), "#permanentDeposits")
    );



  }

  private PermanentDepositOverview getOverview(int id, String reference, String type) {

    return new PermanentDepositOverview(id,
        MaterialType.OTHER,
        reference,
        List.of("PL1", "PL2"),
        "FROM DATE",
        "TO_DATE",
        new StringWithTag(type, Tag.NOT_FROM_PORTAL),
        "1",
        null,
        "2",
        "3",
        "4",
        CoordinatePairTestUtil.getDefaultCoordinate(),
        CoordinatePairTestUtil.getDefaultCoordinate()
    );

  }
}