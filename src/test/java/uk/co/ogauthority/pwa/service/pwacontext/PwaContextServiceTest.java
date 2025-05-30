package uk.co.ogauthority.pwa.service.pwacontext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.domain.pwa.pipeline.model.PipelineId;
import uk.co.ogauthority.pwa.exception.AccessDeniedException;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.search.consents.ConsentSearchItem;
import uk.co.ogauthority.pwa.model.view.search.consents.ConsentSearchResultView;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineService;
import uk.co.ogauthority.pwa.service.search.consents.ConsentSearchService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PwaContextServiceTest {

  @Mock
  private PwaPermissionService pwaPermissionService;

  @Mock
  private MasterPwaService masterPwaService;

  @Mock
  private ConsentSearchService consentSearchService;

  @Mock
  private PipelineService pipelineService;

  private PwaContextService contextService;

  private MasterPwa masterPwa;
  private AuthenticatedUserAccount user;
  private ConsentSearchResultView consentSearchResultView;
  private Set<PwaPermission> validPermissions;

  private final static int PIPELINE_ID = 1;
  private final static int MASTER_PWA_ID1 = 1;

  @BeforeEach
  void setUp() {

    masterPwa = new MasterPwa();
    masterPwa.setId(MASTER_PWA_ID1);

    user = new AuthenticatedUserAccount(new WebUserAccount(1), Set.of());

    var consentSearchItem = new ConsentSearchItem();
    consentSearchItem.setFirstConsentTimestamp(Instant.now());
    consentSearchItem.setLatestConsentTimestamp(Instant.now());
    consentSearchItem.setResourceType(PwaResourceType.PETROLEUM);
    consentSearchResultView = ConsentSearchResultView.fromSearchItem(consentSearchItem);

    contextService = new PwaContextService(pwaPermissionService, masterPwaService, consentSearchService, pipelineService);

    when(masterPwaService.getMasterPwaById(masterPwa.getId())).thenReturn(masterPwa);
    when(consentSearchService.getConsentSearchResultView(masterPwa.getId())).thenReturn(consentSearchResultView);

    validPermissions = Set.of(PwaPermission.VIEW_PWA);
    when(pwaPermissionService.getPwaPermissions(masterPwa, user)).thenReturn(validPermissions);

    var pipeline = new Pipeline();
    pipeline.setId(PIPELINE_ID);
    pipeline.setMasterPwa(masterPwa);
    when(pipelineService.getPipelineFromId(new PipelineId(PIPELINE_ID))).thenReturn(pipeline);
  }


  @Test
  void validateAndCreate_userHasNoPermissions() {
    when(pwaPermissionService.getPwaPermissions(masterPwa, user)).thenReturn(Set.of());
    var contextParams = new PwaContextParams(MASTER_PWA_ID1, user);
    assertThrows(AccessDeniedException.class, () ->
      contextService.validateAndCreate(contextParams));
  }

  @Test
  void validateAndCreate_permissionRequired_userHasCorrectPermissions() {
    var contextParams = new PwaContextParams(MASTER_PWA_ID1, user).requiredPwaPermissions(Set.of(PwaPermission.VIEW_PWA));
    var context = contextService.validateAndCreate(contextParams);
    assertThat(context.getUser()).isEqualTo(user);
    assertThat(context.getPwaPermissions()).isEqualTo(validPermissions);
    assertThat(context.getConsentSearchResultView()).isEqualTo(consentSearchResultView);
  }

  @Test
  void validateAndCreate_withPipeline_valid() {
    var contextParams = new PwaContextParams(MASTER_PWA_ID1, user).withPipelineId(PIPELINE_ID);
    var context = contextService.validateAndCreate(contextParams);
    assertThat(context.getPipeline()).isNotNull();
  }

  @Test
  void validateAndCreate_withPipeline_pipelineAndPwaMismatch() {
    var masterPwaId2 = 2;
    var contextParams = new PwaContextParams(masterPwaId2, user).withPipelineId(PIPELINE_ID);
    assertThrows(AccessDeniedException.class, () ->
      contextService.validateAndCreate(contextParams));
  }

  @Test
  void validateAndCreate_noPipelineContextParam() {
    var builder = new PwaContextParams(MASTER_PWA_ID1, user);
    contextService.validateAndCreate(builder);
    verifyNoInteractions(pipelineService);
  }


}
