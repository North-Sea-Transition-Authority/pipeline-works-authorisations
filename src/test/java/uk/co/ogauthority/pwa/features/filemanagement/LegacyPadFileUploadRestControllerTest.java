package uk.co.ogauthority.pwa.features.filemanagement;

import static org.mockito.ArgumentMatchers.any;
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
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.features.application.files.ApplicationDetailFilePurpose;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

@WebMvcTest(controllers = LegacyPadFileUploadRestController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
class LegacyPadFileUploadRestControllerTest extends PwaApplicationContextAbstractControllerTest {
  private static final UUID FILE_ID = UUID.randomUUID();

  @MockBean
  private FileService fileService;

  private AuthenticatedUserAccount user;

  @BeforeEach
  void setUp() {
    var webUserAccount = new WebUserAccount(1);
    user = new AuthenticatedUserAccount(webUserAccount, Set.of((PwaUserPrivilege.PWA_ACCESS)));
  }

  @Test
  void upload() throws Exception {
    var pwaApplicationDetail = new PwaApplicationDetail();

    when(pwaApplicationDetailService.getDetailByDetailId(1)).thenReturn(pwaApplicationDetail);

    var fileUploadResponse =
        FileUploadResponse.success(FILE_ID, FileSource.fromMultipartFile(new MockMultipartFile("test", new byte[]{})));

    when(fileService.upload(any())).thenReturn(fileUploadResponse);

    mockMvc.perform(post(ReverseRouter.route(on(LegacyPadFileUploadRestController.class)
            .upload(1, ApplicationDetailFilePurpose.PIPELINE_DRAWINGS.name(),null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().isOk());

    verify(padFileService).savePadFileIfValid(fileUploadResponse, pwaApplicationDetail, ApplicationDetailFilePurpose.PIPELINE_DRAWINGS);
  }

}