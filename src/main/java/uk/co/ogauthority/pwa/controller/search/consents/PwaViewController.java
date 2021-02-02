package uk.co.ogauthority.pwa.controller.search.consents;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.service.pwacontext.PwaContext;
import uk.co.ogauthority.pwa.service.pwacontext.PwaPermission;
import uk.co.ogauthority.pwa.service.pwacontext.PwaPermissionCheck;

@Controller
@RequestMapping("/consents/pwa-view")
@PwaPermissionCheck(permissions = PwaPermission.VIEW_PWA)
public class PwaViewController {


  @GetMapping("{pwaId}")
  public ModelAndView renderViewPwa(@PathVariable("pwaId") Integer pwaId,
                                    PwaContext pwaContext,
                                    AuthenticatedUserAccount authenticatedUserAccount) {

    return new ModelAndView("search/consents/pwaView")
        .addObject("consentSearchResultView", pwaContext.getConsentSearchResultView());
  }



}