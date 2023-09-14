package uk.co.ogauthority.pwa.service.search.consents.pwaviewtab;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineOverview;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineStatus;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.features.application.tasks.pipelines.transfers.PadPipelineTransferService;
import uk.co.ogauthority.pwa.model.docgen.DocgenRun;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocGenType;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.repository.pwaconsents.PwaConsentApplicationDto;
import uk.co.ogauthority.pwa.repository.pwaconsents.PwaConsentDtoRepository;
import uk.co.ogauthority.pwa.service.asbuilt.view.AsBuiltViewerService;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailService;
import uk.co.ogauthority.pwa.service.pwacontext.PwaContext;
import uk.co.ogauthority.pwa.service.search.consents.PwaViewTab;
import uk.co.ogauthority.pwa.service.search.consents.TransferHistoryView;
import uk.co.ogauthority.pwa.service.search.consents.tabcontentviews.PwaPipelineView;
import uk.co.ogauthority.pwa.util.StreamUtils;
import uk.co.ogauthority.pwa.util.pipelines.PipelineNumberSortingUtil;

@Service
public class PwaViewTabService {

  private final PipelineDetailService pipelineDetailService;
  private final PwaConsentDtoRepository pwaConsentDtoRepository;
  private final AsBuiltViewerService asBuiltViewerService;
  private final Clock clock;
  private final PadPipelineTransferService padPipelineTransferService;


  @Autowired
  public PwaViewTabService(PipelineDetailService pipelineDetailService,
                           PwaConsentDtoRepository pwaConsentDtoRepository,
                           AsBuiltViewerService asBuiltViewerService,
                           @Qualifier("utcClock") Clock clock, PadPipelineTransferService padPipelineTransferService) {
    this.pipelineDetailService = pipelineDetailService;
    this.pwaConsentDtoRepository = pwaConsentDtoRepository;
    this.asBuiltViewerService = asBuiltViewerService;
    this.clock = clock;
    this.padPipelineTransferService = padPipelineTransferService;
  }


  public Map<String, Object> getTabContentModelMap(PwaContext pwaContext, PwaViewTab tab) {

    Map<String, Object> tabContentMap = new HashMap<>();

    if (tab == PwaViewTab.PIPELINES) {
      tabContentMap.put("pwaPipelineViews", getPipelineTabContent(pwaContext));

    } else if (tab == PwaViewTab.CONSENT_HISTORY) {
      tabContentMap.put("pwaConsentHistoryViews", getConsentHistoryTabContent(pwaContext));
    }

    return tabContentMap;
  }


  private List<PwaPipelineView> getPipelineTabContent(PwaContext pwaContext) {
    var pipelineStatusFilter = EnumSet.allOf(PipelineStatus.class);
    var pipelineOverviews = pipelineDetailService
        .getAllPipelineOverviewsForMasterPwaAndStatusAtInstant(pwaContext.getMasterPwa(), pipelineStatusFilter, Instant.now(clock));
    var consentedPipelineOverviews = asBuiltViewerService.getOverviewsWithAsBuiltStatus(pipelineOverviews);

    var pipelineIds = consentedPipelineOverviews.stream()
        .map(PipelineOverview::getPipelineId)
        .collect(Collectors.toList());

    var transferHistoryViews = padPipelineTransferService.getTransferHistoryViews(pipelineIds).stream()
        .collect(StreamUtils.toLinkedHashMap(TransferHistoryView::getOriginalPipelineId, Function.identity()));

    return consentedPipelineOverviews
        .stream().map(pipelineOverview -> new PwaPipelineView(pipelineOverview, transferHistoryViews.get(pipelineOverview.getPipelineId())))
        .sorted((view1, view2) -> PipelineNumberSortingUtil.compare(view1.getPipelineNumber(), view2.getPipelineNumber()))
        .collect(Collectors.toList());
  }

  public Optional<PwaConsentApplicationDto> getConsentHistoryTabContentForConsentId(Integer consentId) {
    return pwaConsentDtoRepository.getConsentAndApplicationDto(consentId);
  }

  private List<PwaConsentApplicationDto> getConsentHistoryTabContent(PwaContext pwaContext) {
    return pwaConsentDtoRepository.getConsentAndApplicationDtos(pwaContext.getMasterPwa())
        .stream()
        .sorted(Comparator.comparing(PwaConsentApplicationDto::getConsentInstant).reversed())
        .collect(Collectors.toList());
  }

  public void verifyConsentDocumentDownloadable(DocgenRun docgenRun,
                                                PwaConsent pwaConsent,
                                                PwaContext pwaContext) {

    boolean docgenRunLinkedToConsentOnPwa = getConsentHistoryTabContent(pwaContext).stream()
        .anyMatch(consentAppDto -> consentIdAndDocgenRunMatches(consentAppDto, docgenRun, pwaConsent));

    if (!docgenRunLinkedToConsentOnPwa) {
      throw new AccessDeniedException(
          String.format("User tried to access docgen run (ID: %s) for a consent on a different PWA than the one they are viewing (ID: %s)",
              docgenRun.getId(), pwaContext.getMasterPwa().getId()));
    }

    if (docgenRun.getDocGenType() != DocGenType.FULL) {
      throw new AccessDeniedException(String.format(
          "User tried to access a non-FULL docgen run (ID: %s) using the consent doc endpoint for PWA with ID: %s", docgenRun.getId(),
          pwaContext.getMasterPwa().getId()));
    }

  }

  private boolean consentIdAndDocgenRunMatches(PwaConsentApplicationDto consentAppDto,
                                               DocgenRun docgenRun,
                                               PwaConsent pwaConsent) {

    boolean consentIsOnPwa = pwaConsent.getId() == consentAppDto.getConsentId();
    boolean consentDocgenRunIsRequestedRun = consentAppDto.getDocgenRunId()
        .map(runId -> Objects.equals(runId, docgenRun.getId()))
        .orElse(false);

    return consentIsOnPwa && consentDocgenRunIsRequestedRun;

  }

}
