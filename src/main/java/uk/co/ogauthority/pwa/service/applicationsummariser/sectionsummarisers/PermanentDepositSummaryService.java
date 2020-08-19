package uk.co.ogauthority.pwa.service.applicationsummariser.sectionsummarisers;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.entity.enums.permanentdeposits.MaterialType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PermanentDepositOverview;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;
import uk.co.ogauthority.pwa.model.location.LatitudeCoordinate;
import uk.co.ogauthority.pwa.model.location.LongitudeCoordinate;
import uk.co.ogauthority.pwa.model.view.StringWithTag;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.service.applicationsummariser.ApplicationSectionSummariser;
import uk.co.ogauthority.pwa.service.applicationsummariser.ApplicationSectionSummary;
import uk.co.ogauthority.pwa.service.diff.DiffService;
import uk.co.ogauthority.pwa.service.enums.location.LatitudeDirection;
import uk.co.ogauthority.pwa.service.enums.location.LongitudeDirection;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.generic.ApplicationTask;
import uk.co.ogauthority.pwa.service.pwaapplications.generic.TaskListService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.permanentdeposits.PermanentDepositService;

@Service
public class PermanentDepositSummaryService implements ApplicationSectionSummariser {

  private final TaskListService taskListService;
  private final PermanentDepositService permanentDepositService;
  private final DiffService diffService;

  @Autowired
  public PermanentDepositSummaryService(
      TaskListService taskListService,
      PermanentDepositService permanentDepositService, DiffService diffService) {
    this.taskListService = taskListService;
    this.permanentDepositService = permanentDepositService;
    this.diffService = diffService;
  }

  @Override
  public boolean canSummarise(PwaApplicationDetail pwaApplicationDetail) {

    var taskFilter = Set.of(
        ApplicationTask.PERMANENT_DEPOSITS,
        ApplicationTask.PERMANENT_DEPOSIT_DRAWINGS
    );

    return taskListService.anyTaskShownForApplication(taskFilter, pwaApplicationDetail);

  }

  @Override
  public ApplicationSectionSummary summariseDifferences(PwaApplicationDetail pwaApplicationDetail,
                                                        String templateName) {

    var newDetailList = permanentDepositService.getPermanentDepositViews(pwaApplicationDetail)
        .stream()
        .sorted(Comparator.comparing(PermanentDepositOverview::getDepositReference))
        .collect(Collectors.toList());

    // TODO PWA-96 this isnt required for this summary section. only diffs to consented model are planned. This is just an example.
    var exampleOldDetailViewList = List.of(
        getRemovedDeposit("Removed Rock", 9999, List.of("PL1", "PL2")),
        getUpdatedDeposit("Updated Grout Bag", 9998, List.of("PL2"))
    );

    // ? unbounded wildcard used here. A refactor effort on the diff service might be possible so Object is used,
    // but they are essentially equivalent so probably not worth the effort.
    List<Map<String, ?>> diffedPermanentDepositOverviewList = diffService.diffComplexLists(
        newDetailList,
        exampleOldDetailViewList,
        PermanentDepositOverview::getDepositReference,
        PermanentDepositOverview::getDepositReference
    );
    var sectionDisplayText = ApplicationTask.PERMANENT_DEPOSITS.getDisplayName();
    Map<String, Object> summaryModel = new HashMap<>();
    summaryModel.put("sectionDisplayText", sectionDisplayText);
    summaryModel.put("diffedDepositList", diffedPermanentDepositOverviewList);
    return new ApplicationSectionSummary(
        templateName,
        List.of(SidebarSectionLink.createAnchorLink(
            sectionDisplayText,
            "#permanentDeposits"
        )),
        summaryModel
    );
  }

  /**
   * Only exists to demo diffing functionality.
   */
  private PermanentDepositOverview getRemovedDeposit(String reference, int entityId, List<String> pipelineRefs) {
    return new PermanentDepositOverview(
        entityId,
        MaterialType.ROCK,
        reference,
        pipelineRefs,
        "January 2020",
        "December 2020",
        new StringWithTag(MaterialType.ROCK.getDisplayText()),
        "3 grade",
        null,
        null,
        "A Quantity",
        "Some contingency",
        new CoordinatePair(
            new LatitudeCoordinate(1, 2, BigDecimal.ONE, LatitudeDirection.NORTH),
            new LongitudeCoordinate(1, 2, BigDecimal.TEN, LongitudeDirection.EAST)
        ),
        new CoordinatePair(
            new LatitudeCoordinate(1, 2, BigDecimal.TEN, LatitudeDirection.NORTH),
            new LongitudeCoordinate(1, 2, BigDecimal.ONE, LongitudeDirection.EAST)
        )
        );

  }

  /**
   * Only exists to demo diffing functionality.
   */
  private PermanentDepositOverview getUpdatedDeposit(String reference, int entityId, List<String> pipelineRefs) {
    return new PermanentDepositOverview(
        entityId,
        MaterialType.GROUT_BAGS,
        reference,
        pipelineRefs,
        "January 2020",
        "December 2020",
        new StringWithTag(MaterialType.GROUT_BAGS.getDisplayText()),
        "15",
        false,
        "Some quite long reason for non degradable grout bags. This could get even longer. Some additional Words.",
        "A Quantity",
        "Some grout bag contingency",
        new CoordinatePair(
            new LatitudeCoordinate(1, 2, BigDecimal.ONE, LatitudeDirection.NORTH),
            new LongitudeCoordinate(1, 2, BigDecimal.TEN, LongitudeDirection.EAST)
        ),
        new CoordinatePair(
            new LatitudeCoordinate(1, 2, BigDecimal.TEN, LatitudeDirection.NORTH),
            new LongitudeCoordinate(1, 2, BigDecimal.ONE, LongitudeDirection.EAST)
        )
    );

  }
}
