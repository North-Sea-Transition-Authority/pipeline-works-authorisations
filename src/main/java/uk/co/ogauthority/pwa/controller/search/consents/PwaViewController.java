package uk.co.ogauthority.pwa.controller.search.consents;

import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.service.pwacontext.PwaContext;
import uk.co.ogauthority.pwa.service.pwacontext.PwaPermission;
import uk.co.ogauthority.pwa.service.pwacontext.PwaPermissionCheck;
import uk.co.ogauthority.pwa.service.search.consents.PwaViewTab;
import uk.co.ogauthority.pwa.service.search.consents.SearchPwaBreadcrumbService;
import uk.co.ogauthority.pwa.service.search.consents.pwaviewtab.PwaViewTabService;
import uk.co.ogauthority.pwa.service.search.consents.pwaviewtab.PwaViewUrlFactory;

@Controller
@RequestMapping("/consents/pwa-view/{pwaId}")
@PwaPermissionCheck(permissions = PwaPermission.VIEW_PWA)
public class PwaViewController {

  private final PwaViewTabService pwaViewTabService;
  private final SearchPwaBreadcrumbService searchPwaBreadcrumbService;

  @Autowired
  public PwaViewController(PwaViewTabService pwaViewTabService,
                           SearchPwaBreadcrumbService searchPwaBreadcrumbService) {
    this.pwaViewTabService = pwaViewTabService;
    this.searchPwaBreadcrumbService = searchPwaBreadcrumbService;
  }


  @GetMapping("/{tab}")
  public ModelAndView renderViewPwa(@PathVariable("pwaId") Integer pwaId,
                                    @PathVariable("tab") PwaViewTab tab,
                                    PwaContext pwaContext,
                                    AuthenticatedUserAccount authenticatedUserAccount,
                                    @RequestParam(required = false) Boolean showBreadcrumbs) {

    Map<String, Object> tabContentModelMap = pwaViewTabService.getTabContentModelMap(pwaContext, tab);

    var modelAndView = new ModelAndView("search/consents/pwaView")
        .addObject("consentSearchResultView", pwaContext.getConsentSearchResultView())
        .addObject("availableTabs", PwaViewTab.stream().collect(Collectors.toList()))
        .addObject("currentProcessingTab", tab)
        .addObject("pwaViewUrlFactory", new PwaViewUrlFactory(pwaId))
        .addObject("showBreadcrumbs", BooleanUtils.isTrue(showBreadcrumbs))
        .addAllObjects(tabContentModelMap);

    searchPwaBreadcrumbService.fromPwaView(modelAndView, pwaContext.getConsentSearchResultView().getPwaReference());

    return modelAndView;
  }

}