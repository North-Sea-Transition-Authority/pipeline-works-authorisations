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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.PadVersionLookup;
import uk.co.ogauthority.pwa.repository.pwaapplications.search.PadVersionLookupRepository;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContextTestUtil;
import uk.co.ogauthority.pwa.service.enums.appprocessing.PwaAppProcessingPermission;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationVersionAccessRequesterTest {

  @Mock
  private PadVersionLookupRepository padVersionLookupRepository;

  @Mock
  private PwaApplicationDetailService pwaApplicationDetailService;

  private PwaAppProcessingContext context;

  private PwaApplicationDetail pwaApplicationDetail;
  private PadVersionLookup padVersionLookup;

  private ApplicationVersionAccessRequester applicationVersionAccessRequester;

  @Before
  public void setUp() throws Exception {

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
  public void getAvailableAppVersionRequestTypesBy_noPermissions() {
    context = PwaAppProcessingContextTestUtil.withPermissions(pwaApplicationDetail, Set.of());

    assertThat(applicationVersionAccessRequester.getAvailableAppVersionRequestTypesBy(context)).isEmpty();

  }

  @Test
  public void getAvailableAppVersionRequestTypesBy_consulteePriv_hasConfirmedSatisfactoryVersion() {
    context = PwaAppProcessingContextTestUtil.withPermissions(pwaApplicationDetail, Set.of(PwaAppProcessingPermission.CASE_MANAGEMENT_CONSULTEE));
    padVersionLookup.setLatestConfirmedSatisfactoryVersionNo(1);

    assertThat(applicationVersionAccessRequester.getAvailableAppVersionRequestTypesBy(context))
        .containsExactlyInAnyOrder(ApplicationVersionRequestType.LAST_SATISFACTORY);

  }

  @Test
  public void getAvailableAppVersionRequestTypesBy_consulteePriv_noConfirmedSatisfactoryVersion() {
    context = PwaAppProcessingContextTestUtil.withPermissions(pwaApplicationDetail, Set.of(PwaAppProcessingPermission.CASE_MANAGEMENT_CONSULTEE));

    assertThat(applicationVersionAccessRequester.getAvailableAppVersionRequestTypesBy(context)).isEmpty();

  }

  @Test
  public void getAvailableAppVersionRequestTypesBy_updateAppPriv_noDraftVersion() {
    context = PwaAppProcessingContextTestUtil.withPermissions(pwaApplicationDetail, Set.of(PwaAppProcessingPermission.UPDATE_APPLICATION));

    assertThat(applicationVersionAccessRequester.getAvailableAppVersionRequestTypesBy(context)).isEmpty();

  }

  @Test
  public void getAvailableAppVersionRequestTypesBy_ogaOrIndustryPermissions_hasSubmittedVersion() {
    var lastSubmittedPermissions =  Set.of(PwaAppProcessingPermission.CASE_MANAGEMENT_OGA, PwaAppProcessingPermission.CASE_MANAGEMENT_INDUSTRY);
    for (PwaAppProcessingPermission permission : lastSubmittedPermissions) {
      context = PwaAppProcessingContextTestUtil.withPermissions(pwaApplicationDetail, Set.of(permission));
      padVersionLookup.setLatestSubmittedVersionNo(1);

      assertThat(applicationVersionAccessRequester.getAvailableAppVersionRequestTypesBy(context))
          .containsExactlyInAnyOrder(ApplicationVersionRequestType.LAST_SUBMITTED);
    }

  }

  @Test
  public void getAvailableAppVersionRequestTypesBy_ogaOrIndustryPermissions_noSubmittedVersion() {
    context = PwaAppProcessingContextTestUtil.withPermissions(pwaApplicationDetail, Set.of(PwaAppProcessingPermission.UPDATE_APPLICATION));

    assertThat(applicationVersionAccessRequester.getAvailableAppVersionRequestTypesBy(context)).isEmpty();

  }

  @Test
  public void getAvailableAppVersionRequestTypesBy_updateAppPriv_hasDraftVersion() {
    context = PwaAppProcessingContextTestUtil.withPermissions(pwaApplicationDetail, Set.of(PwaAppProcessingPermission.UPDATE_APPLICATION));
    padVersionLookup.setMaxDraftVersionNo(1);

    assertThat(applicationVersionAccessRequester.getAvailableAppVersionRequestTypesBy(context))
        .containsExactlyInAnyOrder(ApplicationVersionRequestType.CURRENT_DRAFT);

  }

  @Test
  public void getPwaApplicationDetailWhenAvailable_retrievesExpectedVersionCurrentDraftRequest() {
    padVersionLookup.setMaxDraftVersionNo(4);

    var allPermissions = EnumSet.allOf(PwaAppProcessingPermission.class);
    context = PwaAppProcessingContextTestUtil.withPermissions(pwaApplicationDetail, allPermissions);

    var result = applicationVersionAccessRequester.getPwaApplicationDetailWhenAvailable(context, ApplicationVersionRequestType.CURRENT_DRAFT);
    verify(pwaApplicationDetailService).getDetailByVersionNo(pwaApplicationDetail.getPwaApplication(), padVersionLookup.getMaxDraftVersionNo());


  }

  @Test
  public void getPwaApplicationDetailWhenAvailable_retrievesExpectedVersionLastSubmittedRequest() {

    padVersionLookup.setLatestSubmittedVersionNo(3);

    var allPermissions = EnumSet.allOf(PwaAppProcessingPermission.class);
    context = PwaAppProcessingContextTestUtil.withPermissions(pwaApplicationDetail, allPermissions);

    var result = applicationVersionAccessRequester.getPwaApplicationDetailWhenAvailable(context, ApplicationVersionRequestType.LAST_SUBMITTED);
    verify(pwaApplicationDetailService).getDetailByVersionNo(pwaApplicationDetail.getPwaApplication(), padVersionLookup.getLatestSubmittedVersionNo());

  }

  @Test
  public void getPwaApplicationDetailWhenAvailable_retrievesExpectedVersionLastSatisfactoryRequest() {

    padVersionLookup.setLatestConfirmedSatisfactoryVersionNo(2);

    var allPermissions = EnumSet.allOf(PwaAppProcessingPermission.class);
    context = PwaAppProcessingContextTestUtil.withPermissions(pwaApplicationDetail, allPermissions);

    var result = applicationVersionAccessRequester.getPwaApplicationDetailWhenAvailable(context, ApplicationVersionRequestType.LAST_SATISFACTORY);
    verify(pwaApplicationDetailService).getDetailByVersionNo(pwaApplicationDetail.getPwaApplication(), padVersionLookup.getLatestConfirmedSatisfactoryVersionNo());

  }

  @Test
  public void getPwaApplicationDetailWhenAvailable_noPermissionsForRequestedVersion() {

    context = PwaAppProcessingContextTestUtil.withPermissions(pwaApplicationDetail, EnumSet.noneOf(PwaAppProcessingPermission.class));

    var result = applicationVersionAccessRequester.getPwaApplicationDetailWhenAvailable(context, ApplicationVersionRequestType.LAST_SATISFACTORY);

    verifyNoInteractions(pwaApplicationDetailService);
    assertThat(result).isEmpty();

  }
}