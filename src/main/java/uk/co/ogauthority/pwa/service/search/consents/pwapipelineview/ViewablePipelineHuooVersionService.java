package uk.co.ogauthority.pwa.service.search.consents.pwapipelineview;

import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;
import static java.util.stream.Collectors.groupingBy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetailMigrationHuooData;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelinehuoo.views.huoosummary.DiffedAllOrgRolePipelineGroups;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentService;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailMigrationHuooDataService;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailService;
import uk.co.ogauthority.pwa.util.DateUtils;

@Service
public class ViewablePipelineHuooVersionService {

  private final PwaConsentService pwaConsentService;
  private final PipelineDetailMigrationHuooDataService pipelineDetailMigrationHuooDataService;
  private final PipelineDetailService pipelineDetailService;
  private final PwaHuooHistoryViewService pwaHuooHistoryViewService;


  @Autowired
  public ViewablePipelineHuooVersionService(PwaConsentService pwaConsentService,
                                            PipelineDetailMigrationHuooDataService pipelineDetailMigrationHuooDataService,
                                            PipelineDetailService pipelineDetailService,
                                            PwaHuooHistoryViewService pwaHuooHistoryViewService) {
    this.pwaConsentService = pwaConsentService;
    this.pipelineDetailMigrationHuooDataService = pipelineDetailMigrationHuooDataService;
    this.pipelineDetailService = pipelineDetailService;
    this.pwaHuooHistoryViewService = pwaHuooHistoryViewService;
  }



  private String createItemVersionOption(PwaHuooHistoryItemVersion itemVersion, Integer order) {

    var orderTagDisplay = order != null ? String.format(" (%s)", order) : "";
    var consentReferenceDisplay = itemVersion.getPwaHuooHistoryItemType().equals(PwaHuooHistoryItemType.PWA_CONSENT)
        && itemVersion.getReference() != null
        ? " - " + itemVersion.getReference() : "";
    return DateUtils.formatDate(itemVersion.getStartTimestamp()) + orderTagDisplay + consentReferenceDisplay;
  }

  private List<PipelineDetail> getPipelineDetailsWithMigratedHuoos(PipelineId pipelineId) {
    var pipelineDetails = pipelineDetailService.getAllPipelineDetailsForPipeline(pipelineId);
    return new ArrayList<>(
        pipelineDetailMigrationHuooDataService.getPipelineDetailMigratedHuoos(pipelineDetails)
            .stream()
            .collect(groupingBy(PipelineDetailMigrationHuooData::getPipelineDetail))
            .keySet());
  }

  public Map<String, String> getHuooHistorySearchSelectorItems(MasterPwa masterPwa, Integer pipelineId) {

    var pwaHuooHistoryItemVersions = new ArrayList<PwaHuooHistoryItemVersion>();
    pwaConsentService.getConsentsByMasterPwa(masterPwa)
        .stream()
        .map(PwaHuooHistoryItemVersion::fromConsent)
        .forEach(pwaHuooHistoryItemVersions::add);

    getPipelineDetailsWithMigratedHuoos(new PipelineId(pipelineId))
        .stream()
        .map(PwaHuooHistoryItemVersion::fromPipelineDetail)
        .forEach(pwaHuooHistoryItemVersions::add);


    //group all the huoo items by the day they were created (for easier order tagging of changes on the same day)
    var dateToHuooItemMap = pwaHuooHistoryItemVersions.stream()
        .sorted(Comparator.comparing(PwaHuooHistoryItemVersion::getStartTimestamp).reversed())
        .collect(groupingBy(huooItem ->
            DateUtils.instantToLocalDate(huooItem.getStartTimestamp()), LinkedHashMap::new, Collectors.toList()));

    Map<String, String> itemIdToOptionMap = new LinkedHashMap<>();
    dateToHuooItemMap.forEach((itemDate, itemsForDate) -> {
      //list of items are already ordered from newest, just need to sort by variation number (where applicable) for versions on the same day
      itemsForDate.sort(nullsFirst(Comparator.comparing(
          PwaHuooHistoryItemVersion::getVariationNumber, nullsFirst(naturalOrder())).reversed()));
      for (var x  = 0; x < itemsForDate.size(); x++) {
        var item = itemsForDate.get(x);
        var itemOrderTagNumber = itemsForDate.size() > 1 ? itemsForDate.size() - x : null;
        var itemKey = item.getPwaHuooHistoryItemType().equals(PwaHuooHistoryItemType.PIPELINE_DETAIL_MIGRATED_HUOO)
            ? PwaHuooHistoryItemType.PIPELINE_DETAIL_MIGRATED_HUOO.getItemPrefix() + item.getId()
            : PwaHuooHistoryItemType.PWA_CONSENT.getItemPrefix() + item.getId();
        itemIdToOptionMap.put(
            itemKey, createItemVersionOption(item, itemOrderTagNumber));
      }
    });

    var latestVersionEntryOpt = itemIdToOptionMap.entrySet().stream().findFirst();
    if (latestVersionEntryOpt.isPresent()) {
      var latestVersionEntry = latestVersionEntryOpt.get();
      latestVersionEntry.setValue(String.format("Latest version (%s)", latestVersionEntry.getValue()));
    }

    return itemIdToOptionMap;
  }


  private PwaHuooHistoryItemType getPwaHuooHistoryItemTypeFromHuooVersionId(String huooVersionId) {

    var prefix = huooVersionId.substring(0, huooVersionId.lastIndexOf("_") + 1);
    return PwaHuooHistoryItemType.PWA_CONSENT.getItemPrefix().equals(prefix) ? PwaHuooHistoryItemType.PWA_CONSENT
        : PwaHuooHistoryItemType.PIPELINE_DETAIL_MIGRATED_HUOO;
  }

  private Integer getEntityIdFromHuooVersionId(String huooVersionId) {
    var prefixEndIndex = huooVersionId.lastIndexOf("_");
    return Integer.parseInt(huooVersionId.substring(prefixEndIndex + 1));
  }


  public DiffedAllOrgRolePipelineGroups getDiffableOrgRolePipelineGroupsFromHuooVersionString(MasterPwa masterPwa,
                                                                                              PipelineId pipelineId,
                                                                                              String huooVersionId) {

    var selectedItemVersionType = getPwaHuooHistoryItemTypeFromHuooVersionId(
        huooVersionId);
    var selectedItemVersionEntityId = getEntityIdFromHuooVersionId(
        huooVersionId);

    DiffedAllOrgRolePipelineGroups diffedHuooSummary;
    if (PwaHuooHistoryItemType.PWA_CONSENT.equals(selectedItemVersionType)) {
      diffedHuooSummary = pwaHuooHistoryViewService.getDiffedHuooSummaryAtTimeOfConsentAndPipeline(
          selectedItemVersionEntityId, masterPwa, pipelineId);

    } else {
      diffedHuooSummary = pwaHuooHistoryViewService.getOrganisationRoleSummaryForHuooMigratedData(
          masterPwa, selectedItemVersionEntityId);
    }

    return diffedHuooSummary;
  }





}
