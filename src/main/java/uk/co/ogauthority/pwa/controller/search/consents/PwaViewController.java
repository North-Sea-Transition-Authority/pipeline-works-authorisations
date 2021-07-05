package uk.co.ogauthority.pwa.controller.search.consents;

import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.service.docgen.DocgenService;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentService;
import uk.co.ogauthority.pwa.service.pwacontext.PwaContext;
import uk.co.ogauthority.pwa.service.pwacontext.PwaPermission;
import uk.co.ogauthority.pwa.service.pwacontext.PwaPermissionCheck;
import uk.co.ogauthority.pwa.service.search.consents.PwaViewTab;
import uk.co.ogauthority.pwa.service.search.consents.SearchPwaBreadcrumbService;
import uk.co.ogauthority.pwa.service.search.consents.pwaviewtab.PwaViewTabService;
import uk.co.ogauthority.pwa.service.search.consents.pwaviewtab.PwaViewUrlFactory;
import uk.co.ogauthority.pwa.util.FileDownloadUtils;

@Controller
@RequestMapping("/consents/pwa-view/{pwaId}")
@PwaPermissionCheck(permissions = PwaPermission.VIEW_PWA)
public class PwaViewController {

  private final PwaViewTabService pwaViewTabService;
  private final SearchPwaBreadcrumbService searchPwaBreadcrumbService;
  private final DocgenService docgenService;
  private final PwaConsentService pwaConsentService;

  @Autowired
  public PwaViewController(PwaViewTabService pwaViewTabService,
                           SearchPwaBreadcrumbService searchPwaBreadcrumbService,
                           DocgenService docgenService,
                           PwaConsentService pwaConsentService) {
    this.pwaViewTabService = pwaViewTabService;
    this.searchPwaBreadcrumbService = searchPwaBreadcrumbService;
    this.docgenService = docgenService;
    this.pwaConsentService = pwaConsentService;
  }


  @GetMapping("/{tab}")
  public ModelAndView renderViewPwa(@PathVariable("pwaId") Integer pwaId,
                                    @PathVariable("tab") PwaViewTab tab,
                                    PwaContext pwaContext,
                                    AuthenticatedUserAccount authenticatedUserAccount) {

    Map<String, Object> tabContentModelMap = pwaViewTabService.getTabContentModelMap(pwaContext, tab);

    var modelAndView = new ModelAndView("search/consents/pwaView")
        .addObject("consentSearchResultView", pwaContext.getConsentSearchResultView())
        .addObject("availableTabs", PwaViewTab.stream().collect(Collectors.toList()))
        .addObject("currentProcessingTab", tab)
        .addObject("pwaViewUrlFactory", new PwaViewUrlFactory(pwaId))
        .addAllObjects(tabContentModelMap);

    searchPwaBreadcrumbService.fromPwaView(modelAndView, "View PWA");

    return modelAndView;

  }

  @GetMapping("/consent/{pwaConsentId}/download/{docgenRunId}")
  public ResponseEntity<Resource> downloadConsentDocument(@PathVariable("pwaId") Integer pwaId,
                                                          PwaContext pwaContext,
                                                          @PathVariable Integer pwaConsentId,
                                                          @PathVariable Long docgenRunId) {

    try {

      var docgenRun = docgenService.getDocgenRun(docgenRunId);

      var pwaConsent = pwaConsentService.getConsentById(pwaConsentId);

      pwaViewTabService.verifyConsentDocumentDownloadable(docgenRun, pwaConsent, pwaContext);

      var blob = docgenRun.getGeneratedDocument();

      var inputStream = blob.getBinaryStream();

      String filename = pwaConsent.getReference().replace("/", "-") + " consent document.pdf";
      return FileDownloadUtils.getResourceResponseEntity(blob, inputStream, filename);

    } catch (Exception e) {
      throw new RuntimeException("Error serving document", e);
    }

  }

}