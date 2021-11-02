package uk.co.ogauthority.pwa.features.application.summary.sectionsummarisers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.features.application.summary.ApplicationSectionSummariser;
import uk.co.ogauthority.pwa.features.application.summary.ApplicationSectionSummary;
import uk.co.ogauthority.pwa.features.application.tasklist.api.ApplicationTask;
import uk.co.ogauthority.pwa.features.application.tasklist.api.TaskListService;
import uk.co.ogauthority.pwa.features.application.tasks.fieldinfo.PadFieldService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.view.StringWithTagItem;
import uk.co.ogauthority.pwa.model.view.sidebarnav.SidebarSectionLink;
import uk.co.ogauthority.pwa.service.diff.DiffService;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaDetailFieldService;

/**
 * Construct summary of field links for a given application and associated PWA.
 */
@Service
public class FieldInformationSummaryService implements ApplicationSectionSummariser {

  private final TaskListService taskListService;
  private final PadFieldService padFieldService;
  private final MasterPwaDetailFieldService masterPwaDetailFieldService;
  private final DiffService diffService;

  @Autowired
  public FieldInformationSummaryService(TaskListService taskListService,
                                        PadFieldService padFieldService,
                                        MasterPwaDetailFieldService masterPwaDetailFieldService,
                                        DiffService diffService) {
    this.taskListService = taskListService;
    this.padFieldService = padFieldService;
    this.masterPwaDetailFieldService = masterPwaDetailFieldService;
    this.diffService = diffService;
  }

  @Override
  public boolean canSummarise(PwaApplicationDetail pwaApplicationDetail) {

    var taskFilter = Set.of(
        ApplicationTask.FIELD_INFORMATION);

    return taskListService.anyTaskShownForApplication(taskFilter, pwaApplicationDetail);
  }

  @Override
  public ApplicationSectionSummary summariseSection(PwaApplicationDetail pwaApplicationDetail,
                                                    String templateName) {

    var appDetailFieldLinks = padFieldService.getApplicationFieldLinksView(pwaApplicationDetail);
    var consentedFieldLinks = masterPwaDetailFieldService.getCurrentMasterPwaDetailFieldLinksView(
        pwaApplicationDetail.getPwaApplication()
    );

    var basicDiffedModel = diffService.diff(appDetailFieldLinks, consentedFieldLinks, Set.of("linkedFieldNames")
    );
    var diffedFieldNames = diffService.diffComplexLists(
        appDetailFieldLinks.getLinkedFieldNames(),
        consentedFieldLinks.getLinkedFieldNames(),
        StringWithTagItem::getStringWithTagHashcode,
        StringWithTagItem::getStringWithTagHashcode
    );

    var sectionDisplayText = ApplicationTask.FIELD_INFORMATION.getDisplayName();
    Map<String, Object> summaryModel = new HashMap<>();
    summaryModel.put("sectionDisplayText", sectionDisplayText);

    summaryModel.put("showFieldNames",
        BooleanUtils.isTrue(appDetailFieldLinks.getLinkedToFields()) || BooleanUtils.isTrue(consentedFieldLinks.getLinkedToFields())
    );

    summaryModel.put("hideFieldNamesOnLoad", BooleanUtils.isFalse(appDetailFieldLinks.getLinkedToFields()));

    summaryModel.put("showPwaLinkedToDesc",
        BooleanUtils.isFalse(appDetailFieldLinks.getLinkedToFields()) || BooleanUtils.isFalse(consentedFieldLinks.getLinkedToFields())
    );
    summaryModel.put("hidePwaLinkedToDescOnLoad", BooleanUtils.isTrue(appDetailFieldLinks.getLinkedToFields()));

    summaryModel.put("fieldLinks", diffedFieldNames);
    summaryModel.put("fieldLinkQuestions", basicDiffedModel);

    return new ApplicationSectionSummary(
        templateName,
        List.of(SidebarSectionLink.createAnchorLink(
            sectionDisplayText,
            "#fieldInformation"
        )),
        summaryModel
    );
  }




}
