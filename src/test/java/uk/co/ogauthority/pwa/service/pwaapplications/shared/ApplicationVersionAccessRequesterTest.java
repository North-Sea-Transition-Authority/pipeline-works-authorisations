package uk.co.ogauthority.pwa.service.pwaapplications.shared;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContextTestUtil;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.permissions.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.PadVersionLookup;
import uk.co.ogauthority.pwa.repository.pwaapplications.search.PadVersionLookupRepository;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ApplicationVersionAccessRequesterTest {

  @Mock
  private PadVersionLookupRepository padVersionLookupRepository;

  @Mock
  private PwaApplicationDetailService pwaApplicationDetailService;

  private PwaAppProcessingContext context;

  private PwaApplicationDetail pwaApplicationDetail;
  private PadVersionLookup padVersionLookup;

  private ApplicationVersionAccessRequester applicationVersionAccessRequester;

  @BeforeEach
  void setUp() throws Exception {

    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    padVersionLookup = new PadVersionLookup();

    when(padVersionLookupRepository.findByPwaApplicationId(pwaApplicationDetail.getMasterPwaApplicationId()))
        .thenReturn(Optional.of(padVersionLookup));

    when(pwaApplicationDetailService.getDetailByVersionNo(any(), anyInt())).thenReturn(pwaApplicationDetail);

    applicationVersionAccessRequester = new ApplicationVersionAccessRequester(
        padVersionLookupRepository,
        pwaApplicationDetailService
    );
  }

  @Test
  void getAvailableAppVersionRequestTypesBy_noPermissions() {
    context = PwaAppProcessingContextTestUtil.withPermissions(pwaApplicationDetail, Set.of());

    assertThat(applicationVersionAccessRequester.getAvailableAppVersionRequestTypesBy(context)).isEmpty();

  }

  @Test
  void getAvailableAppVersionRequestTypesBy_consulteePriv_hasConfirmedSatisfactoryVersion() {
    context = PwaAppProcessingContextTestUtil.withPermissions(pwaApplicationDetail, Set.of(PwaAppProcessingPermission.CASE_MANAGEMENT_CONSULTEE));
    padVersionLookup.setLatestConfirmedSatisfactoryVersionNo(1);

    assertThat(applicationVersionAccessRequester.getAvailableAppVersionRequestTypesBy(context))
        .containsExactlyInAnyOrder(ApplicationVersionRequestType.LAST_SATISFACTORY);

  }

  @Test
  void getAvailableAppVersionRequestTypesBy_consulteePriv_noConfirmedSatisfactoryVersion() {
    context = PwaAppProcessingContextTestUtil.withPermissions(pwaApplicationDetail, Set.of(PwaAppProcessingPermission.CASE_MANAGEMENT_CONSULTEE));

    assertThat(applicationVersionAccessRequester.getAvailableAppVersionRequestTypesBy(context)).isEmpty();

  }

  @Test
  void getAvailableAppVersionRequestTypesBy_updateAppPriv_noDraftVersion() {
    context = PwaAppProcessingContextTestUtil.withPermissions(pwaApplicationDetail, Set.of(PwaAppProcessingPermission.UPDATE_APPLICATION));

    assertThat(applicationVersionAccessRequester.getAvailableAppVersionRequestTypesBy(context)).isEmpty();

  }

  @Test
  void getAvailableAppVersionRequestTypesBy_ogaOrIndustryPermissions_hasSubmittedVersion() {
    var lastSubmittedPermissions =  Set.of(PwaAppProcessingPermission.CASE_MANAGEMENT_OGA, PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY);
    for (PwaAppProcessingPermission permission : lastSubmittedPermissions) {
      context = PwaAppProcessingContextTestUtil.withPermissions(pwaApplicationDetail, Set.of(permission));
      padVersionLookup.setLatestSubmittedVersionNo(1);

      assertThat(applicationVersionAccessRequester.getAvailableAppVersionRequestTypesBy(context))
          .containsExactlyInAnyOrder(ApplicationVersionRequestType.LAST_SUBMITTED);
    }

  }

  @Test
  void getAvailableAppVersionRequestTypesBy_ogaOrIndustryPermissions_noSubmittedVersion() {
    context = PwaAppProcessingContextTestUtil.withPermissions(pwaApplicationDetail, Set.of(PwaAppProcessingPermission.UPDATE_APPLICATION));

    assertThat(applicationVersionAccessRequester.getAvailableAppVersionRequestTypesBy(context)).isEmpty();

  }

  @Test
  void getAvailableAppVersionRequestTypesBy_updateAppPriv_hasDraftVersion() {
    context = PwaAppProcessingContextTestUtil.withPermissions(pwaApplicationDetail, Set.of(PwaAppProcessingPermission.UPDATE_APPLICATION));
    padVersionLookup.setMaxDraftVersionNo(1);

    assertThat(applicationVersionAccessRequester.getAvailableAppVersionRequestTypesBy(context))
        .containsExactlyInAnyOrder(ApplicationVersionRequestType.CURRENT_DRAFT);

  }

  @Test
  void getPwaApplicationDetailWhenAvailable_retrievesExpectedVersionCurrentDraftRequest() {
    padVersionLookup.setMaxDraftVersionNo(4);

    var allPermissions = EnumSet.allOf(PwaAppProcessingPermission.class);
    context = PwaAppProcessingContextTestUtil.withPermissions(pwaApplicationDetail, allPermissions);

    var result = applicationVersionAccessRequester.getPwaApplicationDetailWhenAvailable(context, ApplicationVersionRequestType.CURRENT_DRAFT);
    verify(pwaApplicationDetailService).getDetailByVersionNo(pwaApplicationDetail.getPwaApplication(), padVersionLookup.getMaxDraftVersionNo());


  }

  @Test
  void getPwaApplicationDetailWhenAvailable_retrievesExpectedVersionLastSubmittedRequest() {

    padVersionLookup.setLatestSubmittedVersionNo(3);

    var allPermissions = EnumSet.allOf(PwaAppProcessingPermission.class);
    context = PwaAppProcessingContextTestUtil.withPermissions(pwaApplicationDetail, allPermissions);

    var result = applicationVersionAccessRequester.getPwaApplicationDetailWhenAvailable(context, ApplicationVersionRequestType.LAST_SUBMITTED);
    verify(pwaApplicationDetailService).getDetailByVersionNo(pwaApplicationDetail.getPwaApplication(), padVersionLookup.getLatestSubmittedVersionNo());

  }

  @Test
  void getPwaApplicationDetailWhenAvailable_retrievesExpectedVersionLastSatisfactoryRequest() {

    padVersionLookup.setLatestConfirmedSatisfactoryVersionNo(2);

    var allPermissions = EnumSet.allOf(PwaAppProcessingPermission.class);
    context = PwaAppProcessingContextTestUtil.withPermissions(pwaApplicationDetail, allPermissions);

    var result = applicationVersionAccessRequester.getPwaApplicationDetailWhenAvailable(context, ApplicationVersionRequestType.LAST_SATISFACTORY);
    verify(pwaApplicationDetailService).getDetailByVersionNo(pwaApplicationDetail.getPwaApplication(), padVersionLookup.getLatestConfirmedSatisfactoryVersionNo());

  }

  @Test
  void getPwaApplicationDetailWhenAvailable_noPermissionsForRequestedVersion() {

    context = PwaAppProcessingContextTestUtil.withPermissions(pwaApplicationDetail, EnumSet.noneOf(PwaAppProcessingPermission.class));

    var result = applicationVersionAccessRequester.getPwaApplicationDetailWhenAvailable(context, ApplicationVersionRequestType.LAST_SATISFACTORY);

    verifyNoInteractions(pwaApplicationDetailService);
    assertThat(result).isEmpty();

  }
}