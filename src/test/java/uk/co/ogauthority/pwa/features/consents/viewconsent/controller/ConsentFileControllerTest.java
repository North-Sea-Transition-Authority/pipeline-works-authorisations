package uk.co.ogauthority.pwa.features.consents.viewconsent.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import javax.sql.rowset.serial.SerialBlob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpMethod;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.PwaContextAbstractControllerTest;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.features.consents.viewconsent.ConsentFileViewerService;
import uk.co.ogauthority.pwa.features.filemanagement.AppFileManagementService;
import uk.co.ogauthority.pwa.features.filemanagement.FileDocumentType;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.docgen.DocgenRun;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.docgen.DocgenService;
import uk.co.ogauthority.pwa.service.pwaconsents.PwaConsentService;
import uk.co.ogauthority.pwa.service.pwacontext.PwaContextService;
import uk.co.ogauthority.pwa.service.pwacontext.PwaPermission;
import uk.co.ogauthority.pwa.service.pwacontext.PwaPermissionService;
import uk.co.ogauthority.pwa.service.search.consents.pwaviewtab.PwaViewTabService;
import uk.co.ogauthority.pwa.testutils.PwaEndpointTestBuilder;

@WebMvcTest(controllers = ConsentFileController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
    PwaContextService.class}))
class ConsentFileControllerTest extends PwaContextAbstractControllerTest {

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

  @MockBean
  private FileService fileService;

  @MockBean
  private AppFileManagementService appFileManagementService;

  private MasterPwa masterPwa;
  private AuthenticatedUserAccount user;

  private PwaConsent consent;
  private DocgenRun docgenRun;
  private PwaApplication pwaApplication;

  @BeforeEach
  void setup() throws SQLException {
    endpointTester = new PwaEndpointTestBuilder(mockMvc, masterPwaService, pwaPermissionService, consentSearchService)
        .setAllowedProcessingPermissions(PwaPermission.VIEW_PWA);

    user = new AuthenticatedUserAccount(
        new WebUserAccount(1),
        Set.of(PwaUserPrivilege.PWA_ACCESS));

    this.masterPwa = new MasterPwa();
    this.masterPwa.setId(1);
    this.masterPwa.setCreatedTimestamp(Instant.MIN);
    when(masterPwaService.getMasterPwaById(masterPwa.getId())).thenReturn(masterPwa);

    when(pwaPermissionService.getPwaPermissions(masterPwa, user)).thenReturn(Set.of(PwaPermission.VIEW_PWA));

    pwaApplication = new PwaApplication();

    consent = new PwaConsent();
    consent.setId(1);
    consent.setReference("2/W/22");
    consent.setSourcePwaApplication(pwaApplication);
    when(pwaConsentService.getConsentById(any())).thenReturn(consent);

    docgenRun = new DocgenRun();
    docgenRun.setId(1L);
    docgenRun.setGeneratedDocument(new SerialBlob(new byte[1]));
    when(docgenService.getDocgenRun(anyLong())).thenReturn(docgenRun);
  }

  @Test
  void downloadConsentDocument_processingPermissionSmokeTest() {
    when(appFileManagementService.getUploadedFiles(pwaApplication, FileDocumentType.CONSENT_DOCUMENT)).thenReturn(List.of(new UploadedFile()));

    endpointTester.setRequestMethod(HttpMethod.GET)
        .setEndpointUrlProducer((masterPwa) ->
            ReverseRouter.route(on(ConsentFileController.class)
                .downloadConsentDocument(1, null, 1)));

    endpointTester.performProcessingPermissionCheck(status().isOk(), status().isForbidden());

  }

  @Test
  void downloadConsentDocument_success() throws Exception {

    var uploadedFile = new UploadedFile();

    when(appFileManagementService.getUploadedFiles(pwaApplication, FileDocumentType.CONSENT_DOCUMENT)).thenReturn(List.of(uploadedFile));

    mockMvc.perform(get(ReverseRouter.route(on(ConsentFileController.class)
        .downloadConsentDocument(1, null, 1)))
        .with(user(user))
        .with(csrf()))
        .andExpect(status().isOk());

    verify(fileService).download(uploadedFile);
  }

}
