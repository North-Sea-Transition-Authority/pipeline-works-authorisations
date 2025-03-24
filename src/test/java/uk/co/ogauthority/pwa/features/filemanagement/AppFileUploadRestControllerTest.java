package uk.co.ogauthority.pwa.features.filemanagement;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.mock.web.MockMultipartFile;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.core.FileSource;
import uk.co.fivium.fileuploadlibrary.fds.FileUploadResponse;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.PwaApplicationContextAbstractControllerTest;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.service.PwaApplicationService;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.files.AppFilePurpose;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.fileupload.AppFileService;

@WebMvcTest(controllers = AppFileUploadRestController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
class AppFileUploadRestControllerTest extends PwaApplicationContextAbstractControllerTest {
  private static final Integer PWA_ID = 1;
  private static final UUID FILE_ID = UUID.randomUUID();
  private static final Class<AppFileUploadRestController> CONTROLLER = AppFileUploadRestController.class;

  @MockBean
  private FileService fileService;

  @MockBean
  private PwaApplicationService pwaApplicationService;

  @MockBean
  private AppFileService appFileService;

  private AuthenticatedUserAccount user;

  private PwaApplication pwaApplication;

  @BeforeEach
  void setUp() {
    var webUserAccount = new WebUserAccount(1);
    user = new AuthenticatedUserAccount(webUserAccount, Set.of(PwaUserPrivilege.PWA_ACCESS));

    pwaApplication = new PwaApplication();
    pwaApplication.setId(PWA_ID);
  }

  @Test
  void upload() throws Exception {
    when(pwaApplicationService.getApplicationFromId(PWA_ID)).thenReturn(pwaApplication);

    var fileUploadResponse =
        FileUploadResponse.success(FILE_ID, FileSource.fromMultipartFile(new MockMultipartFile("test", new byte[]{})));

    when(fileService.upload(any())).thenReturn(fileUploadResponse);

    mockMvc.perform(post(ReverseRouter.route(on(CONTROLLER)
            .upload(PWA_ID, AppFilePurpose.PUBLIC_NOTICE.name(), null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().isOk());

    verify(appFileService).processInitialUpload(any(), eq(pwaApplication), eq(AppFilePurpose.PUBLIC_NOTICE));
  }

}