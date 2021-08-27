package uk.co.ogauthority.pwa.controller.search.consents;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.sql.SQLException;
import java.time.Instant;
import java.util.Set;
import javax.sql.rowset.serial.SerialBlob;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.PwaContextAbstractControllerTest;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.model.docgen.DocgenRun;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.appprocessing.prepareconsent.ConsentFileViewerService;
import uk.co.ogauthority.pwa.service.docgen.DocgenService;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentService;
import uk.co.ogauthority.pwa.service.pwacontext.PwaContextService;
import uk.co.ogauthority.pwa.service.pwacontext.PwaPermission;
import uk.co.ogauthority.pwa.service.pwacontext.PwaPermissionService;
import uk.co.ogauthority.pwa.service.search.consents.pwaviewtab.PwaViewTabService;
import uk.co.ogauthority.pwa.testutils.PwaEndpointTestBuilder;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = ConsentFileController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
    PwaContextService.class}))
public class ConsentFileControllerTest extends PwaContextAbstractControllerTest {

  private PwaEndpointTestBuilder endpointTester;

  @MockBean
  private ConsentFileViewerService consentFileViewerService;

  @MockBean
  protected PwaPermissionService pwaPermissionService;

  @MockBean
  protected PwaViewTabService pwaViewTabService;

  @MockBean
  private DocgenService docgenService;

  @MockBean
  private PwaConsentService pwaConsentService;

  private MasterPwa masterPwa;
  private AuthenticatedUserAccount user;

  private PwaConsent consent;
  private DocgenRun docgenRun;

  @Before
  public void setup() throws SQLException {
    endpointTester = new PwaEndpointTestBuilder(mockMvc, masterPwaService, pwaPermissionService, consentSearchService)
        .setAllowedProcessingPermissions(PwaPermission.VIEW_PWA);

    user = new AuthenticatedUserAccount(
        new WebUserAccount(1),
        Set.of(PwaUserPrivilege.PWA_REGULATOR));

    this.masterPwa = new MasterPwa();
    this.masterPwa.setId(1);
    this.masterPwa.setCreatedTimestamp(Instant.MIN);
    when(masterPwaService.getMasterPwaById(masterPwa.getId())).thenReturn(masterPwa);

    when(pwaPermissionService.getPwaPermissions(masterPwa, user)).thenReturn(Set.of(PwaPermission.VIEW_PWA));

    consent = new PwaConsent();
    consent.setId(1);
    consent.setReference("2/W/22");
    when(pwaConsentService.getConsentById(any())).thenReturn(consent);

    docgenRun = new DocgenRun();
    docgenRun.setId(1L);
    docgenRun.setGeneratedDocument(new SerialBlob(new byte[1]));
    when(docgenService.getDocgenRun(anyLong())).thenReturn(docgenRun);
  }

  @Test
  public void downloadConsentDocument_processingPermissionSmokeTest() {

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((masterPwa) ->
            ReverseRouter.route(on(ConsentFileController.class)
                .downloadConsentDocument(1, null, 1, 1L)));

    endpointTester.performProcessingPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  public void downloadConsentDocument_success() throws Exception {

    var blob = docgenRun.getGeneratedDocument();

    mockMvc.perform(get(ReverseRouter.route(on(ConsentFileController.class)
        .downloadConsentDocument(1, null, 1, 1L)))
        .with(authenticatedUserAndSession(user))
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(content().bytes(blob.getBytes(1, (int) blob.length())))
        .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
        .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"2-W-22 consent document.pdf\""));

  }

  @Test
  public void downloadConsentDocument_notAllowed() throws Exception {

    doThrow(new AccessDeniedException(""))
        .when(pwaViewTabService).verifyConsentDocumentDownloadable(eq(docgenRun), eq(consent), any());

    mockMvc.perform(get(ReverseRouter.route(on(ConsentFileController.class)
        .downloadConsentDocument(1, null, 1, 1L)))
        .with(authenticatedUserAndSession(user))
        .with(csrf()))
        .andExpect(status().isForbidden());

  }

}
