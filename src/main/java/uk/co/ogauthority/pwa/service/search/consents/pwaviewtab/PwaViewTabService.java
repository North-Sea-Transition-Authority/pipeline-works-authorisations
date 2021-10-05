package uk.co.ogauthority.pwa.service.search.consents.pwaviewtab;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.model.docgen.DocgenRun;
import uk.co.ogauthority.pwa.model.entity.enums.documents.generation.DocGenType;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineStatus;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.repository.pwaconsents.PwaConsentApplicationDto;
import uk.co.ogauthority.pwa.repository.pwaconsents.PwaConsentDtoRepository;
import uk.co.ogauthority.pwa.service.asbuilt.view.AsBuiltViewerService;
import uk.co.ogauthority.pwa.service.pwaconsents.pipelines.PipelineDetailService;
import uk.co.ogauthority.pwa.service.pwacontext.PwaContext;
import uk.co.ogauthority.pwa.service.search.consents.PwaViewTab;
import uk.co.ogauthority.pwa.service.search.consents.tabcontentviews.PwaPipelineView;

@Service
public class PwaViewTabService {

  private final PipelineDetailService pipelineDetailService;
  private final PwaConsentDtoRepository pwaConsentDtoRepository;
  private final AsBuiltViewerService asBuiltViewerService;


  @Autowired
  public PwaViewTabService(PipelineDetailService pipelineDetailService,
                           PwaConsentDtoRepository pwaConsentDtoRepository,
                           AsBuiltViewerService asBuiltViewerService) {
    this.pipelineDetailService = pipelineDetailService;
    this.pwaConsentDtoRepository = pwaConsentDtoRepository;
    this.asBuiltViewerService = asBuiltViewerService;
  }


  public Map<String, Object> getTabContentModelMap(PwaContext pwaContext,
                                              PwaViewTab tab) {

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
    var pipelineOverviews = pipelineDetailService.getAllPipelineOverviewsForMasterPwaAndStatus(
        pwaContext.getMasterPwa(), pipelineStatusFilter);
    var consentedPipelineOverviews = asBuiltViewerService.getOverviewsWithAsBuiltStatus(pipelineOverviews);

    return consentedPipelineOverviews
        .stream().map(PwaPipelineView::new)
        .sorted()
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
