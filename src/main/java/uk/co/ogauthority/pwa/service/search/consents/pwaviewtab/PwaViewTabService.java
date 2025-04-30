package uk.co.ogauthority.pwa.service.search.consents.pwaviewtab;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.controller.search.consents.PwaViewController;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineOverview;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineStatus;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.repository.pwaconsents.PwaConsentApplicationDto;
import uk.co.ogauthority.pwa.repository.pwaconsents.PwaConsentDtoRepository;
import uk.co.ogauthority.pwa.service.asbuilt.view.AsBuiltViewerService;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaService;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailService;
import uk.co.ogauthority.pwa.service.pwacontext.PwaContext;
import uk.co.ogauthority.pwa.service.search.consents.PwaViewTab;
import uk.co.ogauthority.pwa.service.search.consents.tabcontentviews.PwaPipelineView;
import uk.co.ogauthority.pwa.teams.TeamQueryService;
import uk.co.ogauthority.pwa.teams.TeamType;
import uk.co.ogauthority.pwa.util.pipelines.PipelineNumberSortingUtil;

@Service
public class PwaViewTabService {

  private final PipelineDetailService pipelineDetailService;
  private final PwaConsentDtoRepository pwaConsentDtoRepository;
  private final AsBuiltViewerService asBuiltViewerService;
  private final Clock clock;
  private final MasterPwaService masterPwaService;
  private final TeamQueryService teamQueryService;

  @Autowired
  public PwaViewTabService(PipelineDetailService pipelineDetailService,
                           PwaConsentDtoRepository pwaConsentDtoRepository,
                           AsBuiltViewerService asBuiltViewerService,
                           @Qualifier("utcClock") Clock clock,
                           MasterPwaService masterPwaService, TeamQueryService teamQueryService) {
    this.pipelineDetailService = pipelineDetailService;
    this.pwaConsentDtoRepository = pwaConsentDtoRepository;
    this.asBuiltViewerService = asBuiltViewerService;
    this.clock = clock;
    this.masterPwaService = masterPwaService;
    this.teamQueryService = teamQueryService;
  }


  public Map<String, Object> getTabContentModelMap(PwaContext pwaContext, PwaViewTab tab) {

    Map<String, Object> tabContentMap = new HashMap<>();

    if (tab == PwaViewTab.PIPELINES) {
      tabContentMap.put("pwaPipelineViews", getPipelineTabContent(pwaContext));

    } else if (tab == PwaViewTab.CONSENT_HISTORY) {
      tabContentMap.put("pwaConsentHistoryViews", getConsentHistoryTabContent(pwaContext));
    }

    tabContentMap.put("transferLinksVisible", isUserIsRegulator(pwaContext));

    return tabContentMap;
  }

  private boolean isUserIsRegulator(PwaContext pwaContext) {
    return teamQueryService.userIsMemberOfStaticTeam((long) pwaContext.getUser().getWuaId(), TeamType.REGULATOR);
  }

  private List<PwaPipelineView> getPipelineTabContent(PwaContext pwaContext) {

    var pipelineStatusFilter = EnumSet.allOf(PipelineStatus.class);
    var pipelineOverviews = pipelineDetailService
        .getAllPipelineOverviewsForMasterPwaAndStatusAtInstant(pwaContext.getMasterPwa(), pipelineStatusFilter, Instant.now(clock));
    var consentedPipelineOverviews = asBuiltViewerService.getOverviewsWithAsBuiltStatus(pipelineOverviews);

    var pipelineIds = consentedPipelineOverviews.stream()
        .map(PipelineOverview::getPipelineId)
        .collect(Collectors.toList());

    var pipelineIdToDetailMap = pipelineDetailService.getLatestPipelineDetailsForIds(pipelineIds).stream()
        .collect(Collectors.toMap(detail -> detail.getPipelineId().asInt(), Function.identity()));

    var transferImplicatedPwas = new ArrayList<MasterPwa>();
    pipelineIdToDetailMap.values().stream()
        .filter(detail -> detail.getTransferredFromPipeline() != null || detail.getTransferredToPipeline() != null)
        .forEach(transferredDetail -> {

          if (transferredDetail.getTransferredFromPipeline() != null) {
            transferImplicatedPwas.add(transferredDetail.getTransferredFromPipeline().getMasterPwa());
          }

          if (transferredDetail.getTransferredToPipeline() != null) {
            transferImplicatedPwas.add(transferredDetail.getTransferredToPipeline().getMasterPwa());
          }

        });

    var pwaToReferenceMap = masterPwaService.findAllCurrentDetailsIn(transferImplicatedPwas).stream()
        .collect(Collectors.toMap(MasterPwaDetail::getMasterPwa, Function.identity()));

    return consentedPipelineOverviews
        .stream()
        .map(pipelineOverview -> {
          var detail = pipelineIdToDetailMap.get(pipelineOverview.getPipelineId());
          String transferredFromPwaRef = null;
          String transferredFromPwaUrl = null;
          String transferredToPwaRef = null;
          String transferredToPwaUrl = null;
          if (detail.getTransferredFromPipeline() != null) {
            var pwaDetail = pwaToReferenceMap.get(detail.getTransferredFromPipeline().getMasterPwa());
            transferredFromPwaRef = pwaDetail.getReference();
            transferredFromPwaUrl = ReverseRouter.route(on(PwaViewController.class)
                .renderViewPwa(pwaDetail.getMasterPwaId(), PwaViewTab.PIPELINES, null, null, false));
          }

          if (detail.getTransferredToPipeline() != null) {
            var pwaDetail = pwaToReferenceMap.get(detail.getTransferredToPipeline().getMasterPwa());
            transferredToPwaRef = pwaDetail.getReference();
            transferredToPwaUrl = ReverseRouter.route(on(PwaViewController.class)
                  .renderViewPwa(pwaDetail.getMasterPwaId(), PwaViewTab.PIPELINES, null, null, false));
          }

          return new PwaPipelineView(
              pipelineOverview,
              transferredFromPwaRef,
              transferredFromPwaUrl,
              transferredToPwaRef,
              transferredToPwaUrl
          );
        })
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

}
