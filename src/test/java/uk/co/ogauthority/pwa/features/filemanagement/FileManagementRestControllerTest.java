package uk.co.ogauthority.pwa.features.filemanagement;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.ResponseEntity;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.fivium.fileuploadlibrary.fds.FileDeleteResponse;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.PwaApplicationContextAbstractControllerTest;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

@WebMvcTest(controllers = FileManagementRestController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
class FileManagementRestControllerTest extends PwaApplicationContextAbstractControllerTest {
  private static final UUID FILE_ID = UUID.randomUUID();
  private static final String WUA_ID = "1";
  private static final Class<FileManagementRestController> CONTROLLER = FileManagementRestController.class;

  @MockBean
  private FileService fileService;

  private AuthenticatedUserAccount user;

  @BeforeEach
  void setUp() {
    var webUserAccount = new WebUserAccount(1);
    user = new AuthenticatedUserAccount(webUserAccount, Set.of(PwaUserPrivilege.PWA_ACCESS));
  }

  @Test
  void download() throws Exception {
    var uploadedFile = new UploadedFile();
    uploadedFile.setUploadedBy(WUA_ID);
    when(fileService.find(FILE_ID)).thenReturn(Optional.of(uploadedFile));

    when(fileService.download(uploadedFile)).thenReturn(ResponseEntity.ok().build());

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER)
            .download(FILE_ID, null)))
            .with(user(user)))
        .andExpect(status().isOk());

    verify(fileService).download(uploadedFile);
  }

  @Test
  void download_invalidFileId() throws Exception {
    when(fileService.find(FILE_ID)).thenReturn(Optional.empty());

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER)
            .download(FILE_ID, null)))
            .with(user(user)))
        .andExpect(status().isNotFound());
  }

  @Test
  void download_userCannotAccessApplication() throws Exception {
    var uploadedFile = new UploadedFile();
    uploadedFile.setUploadedBy("2");

    when(fileService.find(FILE_ID)).thenReturn(Optional.of(uploadedFile));

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER)
            .download(FILE_ID, null)))
            .with(user(user)))
        .andExpect(status().isNotFound());
  }

  @Test
  void upload() throws Exception {
    mockMvc.perform(post(ReverseRouter.route(on(CONTROLLER)
            .upload(null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().isOk());
  }

  @Test
  void delete() throws Exception {
    var uploadedFile = new UploadedFile();
    uploadedFile.setUploadedBy(WUA_ID);
    when(fileService.find(FILE_ID)).thenReturn(Optional.of(uploadedFile));

    when(fileService.delete(uploadedFile)).thenReturn(FileDeleteResponse.success(FILE_ID));

    mockMvc.perform(post(ReverseRouter.route(on(CONTROLLER)
            .delete(FILE_ID, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().isOk());

    verify(fileService).delete(uploadedFile);
  }

  @Test
  void delete_invalidFileId() throws Exception {
    when(fileService.find(FILE_ID)).thenReturn(Optional.empty());

    mockMvc.perform(post(ReverseRouter.route(on(CONTROLLER)
            .delete(FILE_ID, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().isNotFound());
  }

  @Test
  void delete_userCannotAccessApplication() throws Exception {
    var uploadedFile = new UploadedFile();
    uploadedFile.setUploadedBy("2");

    when(fileService.find(FILE_ID)).thenReturn(Optional.of(uploadedFile));

    mockMvc.perform(post(ReverseRouter.route(on(CONTROLLER)
            .delete(FILE_ID, null)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().isNotFound());
  }
}