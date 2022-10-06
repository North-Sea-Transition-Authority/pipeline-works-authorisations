package uk.co.ogauthority.pwa.features.generalcase.pipelineview;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toUnmodifiableMap;

import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PhysicalPipelineState;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineOverview;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineStatus;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PadPipelineService;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.idents.PadPipelineIdentService;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentService;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailIdentViewService;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailService;

/**
 * Service whose  job to is collect data from both the consented model and applications model in order to give a consistent view
 * of pipelines across PWA within the appropriate PWA application context.
 */
@Service
public class PipelineAndIdentViewFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(PipelineAndIdentViewFactory.class);

  private final PadPipelineService padPipelineService;
  private final PadPipelineIdentService padPipelineIdentService;

  private final PipelineDetailService pipelineDetailService;
  private final PipelineDetailIdentViewService pipelineDetailIdentViewService;

  private final PwaConsentService pwaConsentService;
  private final Clock clock;

  @Autowired
  public PipelineAndIdentViewFactory(PadPipelineService padPipelineService,
                                     PadPipelineIdentService padPipelineIdentService,
                                     PipelineDetailService pipelineDetailService,
                                     PipelineDetailIdentViewService pipelineDetailIdentViewService,
                                     PwaConsentService pwaConsentService,
                                     @Qualifier("utcClock") Clock clock) {
    this.padPipelineService = padPipelineService;
    this.padPipelineIdentService = padPipelineIdentService;
    this.pipelineDetailService = pipelineDetailService;
    this.pipelineDetailIdentViewService = pipelineDetailIdentViewService;
    this.pwaConsentService = pwaConsentService;
    this.clock = clock;
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
      PwaApplicationDetail pwaApplicationDetail,
      ConsentedPipelineFilter consentedPipelineFilter) {
    // 1. get pipeline overviews for the PWA and application prioritising application data for pipelines
    // 2. Get a all Ident Views that we can from the application for every applicable pipeline.
    // 3. Get all Ident Views that we can from the consented model for every applicable pipeline.
    // 4. for every pipelineOverview for app and MasterPwa, combine the IdentView data to create a composite of the full pipeline's data.

    var allAppAndMasterPwaPipelineOverviewLookup = getAllPipelineOverviewsFromAppAndMasterPwa(
        pwaApplicationDetail, consentedPipelineFilter);

    LOGGER.debug("Found {} pipeline overviews for app and master pwa", allAppAndMasterPwaPipelineOverviewLookup.size());

    var allAppAndMasterPwaPipelineIds = allAppAndMasterPwaPipelineOverviewLookup.keySet();

    var applicationPipelineIdToIdentViewListMap = padPipelineIdentService.getApplicationIdentViewsForPipelines(
        pwaApplicationDetail, allAppAndMasterPwaPipelineIds
    );

    var totalAppIdentViewCount = applicationPipelineIdToIdentViewListMap.values().stream()
        .mapToLong(List::size)
        .sum();

    LOGGER.debug("Found {} ident views on application for {} pipelines",
        totalAppIdentViewCount, applicationPipelineIdToIdentViewListMap.size());

    var modifiableAppPipelineIdToIdentViewListMap = new HashMap<>(applicationPipelineIdToIdentViewListMap);
    // if pipeline added but no idents defined, add in empty entry to app pipelines ident lookup
    allAppAndMasterPwaPipelineOverviewLookup.forEach((pipelineId, pipelineOverview) -> {
      if (pipelineOverview.getPadPipelineId() != null) {
        modifiableAppPipelineIdToIdentViewListMap.putIfAbsent(pipelineId, List.of());
      }
    });

    var allConsentedPipelineIdtoIdentViewListMap = pipelineDetailIdentViewService.getSortedPipelineIdentViewsForPipelines(
        allAppAndMasterPwaPipelineIds
    );

    var totalConsentedIdentViewCount = allConsentedPipelineIdtoIdentViewListMap.values().stream()
        .mapToLong(List::size)
        .sum();

    LOGGER.debug("Found {} ident views in consented model for {} pipelines",
        totalConsentedIdentViewCount, allConsentedPipelineIdtoIdentViewListMap.size());

    var pipelineAndIdentViewListMap = new HashMap<PipelineId, List<IdentView>>();
    pipelineAndIdentViewListMap.putAll(modifiableAppPipelineIdToIdentViewListMap);

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
      PwaApplicationDetail pwaApplicationDetail,
      ConsentedPipelineFilter consentedPipelineFilter) {
    // 1. get pipeline overviews from pipelines represented within the application
    // 2. get pipeline overviews from consented model
    // 3. add consented pipelines to return map where the same pipeline does not exist in application
    // 4. add all application pipelines to return map

    Map<PipelineId, PipelineOverview> applicationPipelineIds = padPipelineService.getApplicationPipelineOverviews(pwaApplicationDetail)
        .stream()
        .collect(toMap(PipelineId::from, pipelineOverview -> pipelineOverview));

    // look for consented pipeline details that had the statuses we're looking for at the time of
    // consent, falling back to current details if app not consented
    var pipelineDetailTimestampToCheck = pwaConsentService.getConsentByPwaApplication(pwaApplicationDetail.getPwaApplication())
        .map(PwaConsent::getConsentInstant)
        .orElse(Instant.now(clock));

    Map<PipelineId, PipelineOverview> consentedPipelineIdentifiers = pipelineDetailService
        .getAllPipelineOverviewsForMasterPwaAndStatusAtInstant(
            pwaApplicationDetail.getPwaApplication().getMasterPwa(),
            consentedPipelineFilter.getPipelineStatusSet(),
            pipelineDetailTimestampToCheck
        )
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

  public Map<PipelineId, PipelineOverview> getAllPipelineOverviewsFromAppAndMasterPwaByPipelineIds(
      PwaApplicationDetail pwaApplicationDetail, List<PipelineId> pipelineIds) {

    var pipelineOverviews = getAllPipelineOverviewsFromAppAndMasterPwa(
        pwaApplicationDetail, ConsentedPipelineFilter.ALL_CURRENT_STATUS_PIPELINES);

    return pipelineIds.stream()
        .filter(pipelineId -> pipelineOverviews.get(pipelineId) != null)
        .collect(Collectors.toMap(Function.identity(), pipelineOverviews::get));
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
        pipelineDetailIdentViewService.getSortedPipelineIdentViewsForPipeline(pipelineId)
    );
  }

  public enum ConsentedPipelineFilter {
    ALL_CURRENT_STATUS_PIPELINES(PipelineStatus.currentStatusSet()),
    ONLY_ON_SEABED_PIPELINES(PipelineStatus.getStatusesWithState(PhysicalPipelineState.ON_SEABED));

    private final Set<PipelineStatus> pipelineStatusSet;

    ConsentedPipelineFilter(Set<PipelineStatus> pipelineStatusSet) {
      this.pipelineStatusSet = pipelineStatusSet;
    }

    Set<PipelineStatus> getPipelineStatusSet() {
      return pipelineStatusSet;
    }
  }

}
