package uk.co.ogauthority.pwa.features.filemanagement;

import static org.mockito.Mockito.doThrow;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.fivium.fileuploadlibrary.fds.FileDeleteResponse;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.controller.PwaApplicationContextAbstractControllerTest;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.service.PwaApplicationService;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

@WebMvcTest(controllers = AppFileManagementRestController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
class AppFileManagementRestControllerTest extends PwaApplicationContextAbstractControllerTest {
  private static final Integer PWA_ID = 1;
  private static final UUID FILE_ID = UUID.randomUUID();
  private static final Class<AppFileManagementRestController> CONTROLLER = AppFileManagementRestController.class;

  @MockBean
  private FileService fileService;

  @MockBean
  private AppFileManagementService appFileManagementService;

  @MockBean
  private PwaApplicationService pwaApplicationService;

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
  void download() throws Exception {
    when(pwaApplicationService.getApplicationFromId(PWA_ID)).thenReturn(pwaApplication);

    var uploadedFile = new UploadedFile();
    when(fileService.find(FILE_ID)).thenReturn(Optional.of(uploadedFile));

    when(fileService.download(uploadedFile)).thenReturn(ResponseEntity.ok().build());

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER)
            .download(PWA_ID, FILE_ID)))
            .with(user(user)))
        .andExpect(status().isOk());

    verify(appFileManagementService).throwIfFileDoesNotBelongToApplication(uploadedFile, pwaApplication);
    verify(fileService).download(uploadedFile);
  }

  @Test
  void download_invalidFileId() throws Exception {
    when(pwaApplicationService.getApplicationFromId(PWA_ID)).thenReturn(pwaApplication);

    when(fileService.find(FILE_ID)).thenReturn(Optional.empty());
    when(appFileManagementService.getFileNotFoundException(FILE_ID, pwaApplication))
        .thenReturn(new ResponseStatusException(HttpStatus.NOT_FOUND));

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER)
            .download(PWA_ID, FILE_ID)))
            .with(user(user)))
        .andExpect(status().isNotFound());
  }

  @Test
  void download_fileNotLinkedToApplication() throws Exception {
    when(pwaApplicationService.getApplicationFromId(PWA_ID)).thenReturn(pwaApplication);

    var uploadedFile = new UploadedFile();
    when(fileService.find(FILE_ID)).thenReturn(Optional.of(uploadedFile));

    doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND))
        .when(appFileManagementService)
        .throwIfFileDoesNotBelongToApplication(uploadedFile, pwaApplication);

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER)
            .download(PWA_ID, FILE_ID)))
            .with(user(user)))
        .andExpect(status().isNotFound());

    verify(appFileManagementService).throwIfFileDoesNotBelongToApplication(uploadedFile, pwaApplication);
  }

  @Test
  void delete() throws Exception {
    when(pwaApplicationService.getApplicationFromId(PWA_ID)).thenReturn(pwaApplication);

    var uploadedFile = new UploadedFile();
    when(fileService.find(FILE_ID)).thenReturn(Optional.of(uploadedFile));

    when(fileService.delete(uploadedFile)).thenReturn(FileDeleteResponse.success(FILE_ID));

    mockMvc.perform(post(ReverseRouter.route(on(CONTROLLER)
            .delete(PWA_ID, FILE_ID)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().isOk());

    verify(appFileManagementService).throwIfFileDoesNotBelongToApplication(uploadedFile, pwaApplication);
    verify(fileService).delete(uploadedFile);
  }

  @Test
  void delete_invalidFileId() throws Exception {
    when(pwaApplicationService.getApplicationFromId(PWA_ID)).thenReturn(pwaApplication);

    when(fileService.find(FILE_ID)).thenReturn(Optional.empty());
    when(appFileManagementService.getFileNotFoundException(FILE_ID, pwaApplication))
        .thenReturn(new ResponseStatusException(HttpStatus.NOT_FOUND));

    mockMvc.perform(post(ReverseRouter.route(on(CONTROLLER)
            .delete(PWA_ID, FILE_ID)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().isNotFound());
  }

  @Test
  void delete_fileNotLinkedToApplication() throws Exception {
    when(pwaApplicationService.getApplicationFromId(PWA_ID)).thenReturn(pwaApplication);

    var uploadedFile = new UploadedFile();
    when(fileService.find(FILE_ID)).thenReturn(Optional.of(uploadedFile));

    doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND))
        .when(appFileManagementService)
        .throwIfFileDoesNotBelongToApplication(uploadedFile, pwaApplication);

    mockMvc.perform(post(ReverseRouter.route(on(CONTROLLER)
            .delete(PWA_ID, FILE_ID)))
            .with(csrf())
            .with(user(user)))
        .andExpect(status().isNotFound());

    verify(appFileManagementService).throwIfFileDoesNotBelongToApplication(uploadedFile, pwaApplication);
  }
}