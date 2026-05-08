package uk.co.ogauthority.pwa.controller.search.consents;

import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.service.pwacontext.PwaContext;
import uk.co.ogauthority.pwa.service.pwacontext.PwaPermission;
import uk.co.ogauthority.pwa.service.pwacontext.PwaPermissionCheck;
import uk.co.ogauthority.pwa.service.search.consents.PwaViewTab;
import uk.co.ogauthority.pwa.service.search.consents.SearchPwaBreadcrumbService;
import uk.co.ogauthority.pwa.service.search.consents.pwaviewtab.PwaViewTabService;
import uk.co.ogauthority.pwa.service.search.consents.pwaviewtab.PwaViewUrlFactory;
import uk.co.ogauthority.pwa.teams.TeamQueryService;
import uk.co.ogauthority.pwa.teams.TeamType;

@Controller
@RequestMapping("/consents/pwa-view/{pwaId}")
@PwaPermissionCheck(permissions = PwaPermission.VIEW_PWA)
public class PwaViewController {

  private final PwaViewTabService pwaViewTabService;
  private final SearchPwaBreadcrumbService searchPwaBreadcrumbService;
  private final TeamQueryService teamQueryService;

  @Autowired
  public PwaViewController(PwaViewTabService pwaViewTabService,
                           SearchPwaBreadcrumbService searchPwaBreadcrumbService,
                           TeamQueryService teamQueryService) {
    this.pwaViewTabService = pwaViewTabService;
    this.searchPwaBreadcrumbService = searchPwaBreadcrumbService;
    this.teamQueryService = teamQueryService;
  }


  @GetMapping("/{tab}")
  public ModelAndView renderViewPwa(@PathVariable("pwaId") Integer pwaId,
                                    @PathVariable("tab") PwaViewTab tab,
                                    PwaContext pwaContext,
                                    AuthenticatedUserAccount authenticatedUserAccount,
                                    @RequestParam(required = false) Boolean showBreadcrumbs) {
    var onlyShowPipelinesTab = teamQueryService.isUserOnlyPartOfStaticTeam(authenticatedUserAccount.getWuaId(),
        TeamType.SECONDARY_REGULATOR);
    throwIfHiddenTabIsAccessed(onlyShowPipelinesTab, tab, pwaId, authenticatedUserAccount);

    Map<String, Object> tabContentModelMap = pwaViewTabService.getTabContentModelMap(pwaContext, tab);

    List<PwaViewTab> availableTabs;

    if (onlyShowPipelinesTab) {
      availableTabs = List.of(PwaViewTab.PIPELINES);
    } else {
      availableTabs = PwaViewTab.stream().toList();
    }

    var modelAndView = new ModelAndView("search/consents/pwaView")
        .addObject("consentSearchResultView", pwaContext.getConsentSearchResultView())
        .addObject("availableTabs", availableTabs)
        .addObject("currentProcessingTab", tab)
        .addObject("pwaViewUrlFactory", new PwaViewUrlFactory(pwaId))
        .addObject("showBreadcrumbs", BooleanUtils.isTrue(showBreadcrumbs))
        .addAllObjects(tabContentModelMap);

    searchPwaBreadcrumbService.fromPwaView(modelAndView, pwaContext.getConsentSearchResultView().getPwaReference());

    return modelAndView;
  }

  private void throwIfHiddenTabIsAccessed(boolean onlyShowPipelinesTab, PwaViewTab tab,
                                          Integer pwaId, AuthenticatedUserAccount authenticatedUserAccount) {
    if (!PwaViewTab.PIPELINES.equals(tab) && onlyShowPipelinesTab) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN,
          "Cannot view the %s tab for pwa %s for user %s"
              .formatted(tab.getDisplayName(), pwaId, authenticatedUserAccount.getWuaId())
      );
    }
  }

}