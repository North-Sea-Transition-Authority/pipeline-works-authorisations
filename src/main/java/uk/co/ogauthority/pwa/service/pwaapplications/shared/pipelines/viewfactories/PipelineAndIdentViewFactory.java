package uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.viewfactories;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toUnmodifiableMap;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.views.PipelineOverview;
import uk.co.ogauthority.pwa.model.view.PipelineAndIdentView;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.IdentView;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineIdentService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineService;
import uk.co.ogauthority.pwa.service.pwaconsents.PipelineDetailIdentService;
import uk.co.ogauthority.pwa.service.pwaconsents.PipelineDetailService;

/**
 * Service whose  job to is collect data from both the consented model and applications model in order to give a consistent view
 * of pipelines across PWA within the appropriate application or Master PWA context.
 */
@Service
public class PipelineAndIdentViewFactory {

  private final PadPipelineService padPipelineService;
  private final PadPipelineIdentService padPipelineIdentService;

  private final PipelineDetailService pipelineDetailService;
  private PipelineDetailIdentService pipelineDetailIdentService;

  @Autowired
  public PipelineAndIdentViewFactory(PadPipelineService padPipelineService,
                                     PadPipelineIdentService padPipelineIdentService,
                                     PipelineDetailService pipelineDetailService,
                                     PipelineDetailIdentService pipelineDetailIdentService) {
    this.padPipelineService = padPipelineService;
    this.padPipelineIdentService = padPipelineIdentService;
    this.pipelineDetailService = pipelineDetailService;
    this.pipelineDetailIdentService = pipelineDetailIdentService;
  }

  /**
   * <p>Pipelines and Ident views from both the application and PWA as a whole prioritising details from the application.
   * If an application updates a consented PWA pipeline, we want the detail to show the application details and not the
   * consented details.</p>
   *
   * <p>The returned List will have not have any pipeline splits represented as they only exist in the
   * context of an application's/Master PWA's HUOO roles and pipeline links.</p>
   */
  public List<PipelineAndIdentView> getAllAppAndMasterPwaPipelineAndIdentViews(
      PwaApplicationDetail pwaApplicationDetail) {
    // 1. get pipeline overviews for the PWA and application prioritising application data for pipelines
    // 2. Get a all Ident Views that we can from the application for every applicable pipeline.
    // 3. Get all Ident Views that we can from the consented model for every applicable pipeline.
    // 4. for every pipelineOverview for app and MasterPwa, combine the IdentView data to create a composite of the full pipeline's data.

    var allAppAndMasterPwaPipelineOverviewLookup = getAllPipelineOverviewsFromAppAndMasterPwa(
        pwaApplicationDetail);
    var allAppAndMasterPwaPipelineIds = allAppAndMasterPwaPipelineOverviewLookup.keySet();

    var applicationPipelineIdToIdentViewListMap = padPipelineIdentService.getApplicationIdentViewsForPipelines(
        allAppAndMasterPwaPipelineIds
    );
    // if pipeline added but no idents defined, add in empty entry to app pipelines ident lookup
    allAppAndMasterPwaPipelineOverviewLookup.forEach((pipelineId, pipelineOverview) -> {
      if (pipelineOverview.getPadPipelineId() != null) {
        applicationPipelineIdToIdentViewListMap.putIfAbsent(pipelineId, List.of());
      }
    });

    var allConsentedPipelineIdtoIdentViewListMap = pipelineDetailIdentService.getSortedPipelineIdentViewsForPipelines(
        allAppAndMasterPwaPipelineIds
    );

    var pipelineAndIdentViewListMap = new HashMap<PipelineId, List<IdentView>>();
    pipelineAndIdentViewListMap.putAll(applicationPipelineIdToIdentViewListMap);

    // if app version of the pipeline doesnt exist, add in entry using consented version
    allConsentedPipelineIdtoIdentViewListMap.forEach(pipelineAndIdentViewListMap::putIfAbsent);

    // for each app and master pwa pipeline over, combine the relevant ident views and pipeline number to create output list.
    return allAppAndMasterPwaPipelineOverviewLookup.entrySet()
        .stream()
        .map(pipelineIdPipelineOverviewEntry -> new PipelineAndIdentView(
                pipelineIdPipelineOverviewEntry.getValue(),
                pipelineAndIdentViewListMap.get(pipelineIdPipelineOverviewEntry.getKey())
            )
        )
        .collect(Collectors.toUnmodifiableList());

  }


  /**
   * <p>Pipelines from both the application and PWA as a whole. If an application updates a consented PWA
   * pipeline, we want the detail to show the application details and not the consented details.</p>
   *
   * <p>The returned map will have not have any pipeline splits represented as they only exist in the
   * context of an applications/Master PWA's HUOO roles.</p>
   */
  public Map<PipelineId, PipelineOverview> getAllPipelineOverviewsFromAppAndMasterPwa(
      PwaApplicationDetail pwaApplicationDetail) {
    // 1. get pipeline overviews from pipelines represented within the application
    // 2. get pipeline overviews from consented model
    // 3. add consented pipelines to return map where the same pipeline does not exist in application
    // 4. add all application pipelines to return map

    Map<PipelineId, PipelineOverview> applicationPipelineIds = padPipelineService.getApplicationPipelineOverviews(
        pwaApplicationDetail)
        .stream()
        .collect(toMap(PipelineId::from, pipelineOverview -> pipelineOverview));

    Map<PipelineId, PipelineOverview> consentedPipelineIdentifiers = pipelineDetailService
        .getAllPipelineOverviewsForMasterPwa(pwaApplicationDetail.getPwaApplication().getMasterPwa())
        .stream()
        .collect(toUnmodifiableMap(PipelineId::from, pipelineOverview -> pipelineOverview));

    Map<PipelineId, PipelineOverview> pipelineOverviewSummary = new HashMap<>();

    consentedPipelineIdentifiers.forEach((key, value) -> {
      if (!applicationPipelineIds.containsKey(key)) {
        pipelineOverviewSummary.put(key, value);
      }
    });

    pipelineOverviewSummary.putAll(applicationPipelineIds);

    return Collections.unmodifiableMap(pipelineOverviewSummary);
  }


  /**
   * Provide ident views for pipeline from either the current application or consented model if not imported.
   */
  public List<IdentView> getPipelineSortedIdentViews(PwaApplicationDetail pwaApplicationDetail,
                                                     PipelineId pipelineId) {
    var padPipeline = padPipelineService.findByPwaApplicationDetailAndPipelineId(pwaApplicationDetail, pipelineId);
    if (padPipeline.isPresent()) {
      return padPipelineIdentService.getIdentViews(padPipeline.get()).stream()
          .sorted(Comparator.comparing(IdentView::getIdentNumber))
          .collect(Collectors.toUnmodifiableList());
    }

    return Collections.unmodifiableList(
        pipelineDetailIdentService.getSortedPipelineIdentViewsForPipeline(pipelineId)
    );
  }

}
