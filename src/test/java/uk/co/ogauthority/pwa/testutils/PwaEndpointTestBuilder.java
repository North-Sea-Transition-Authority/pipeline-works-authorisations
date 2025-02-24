package uk.co.ogauthority.pwa.testutils;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.co.ogauthority.pwa.util.TestUserProvider.user;

import java.time.Instant;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaResourceType;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.search.consents.ConsentSearchItem;
import uk.co.ogauthority.pwa.model.view.search.consents.ConsentSearchResultView;
import uk.co.ogauthority.pwa.service.masterpwas.MasterPwaService;
import uk.co.ogauthority.pwa.service.pwacontext.PwaPermission;
import uk.co.ogauthority.pwa.service.pwacontext.PwaPermissionService;
import uk.co.ogauthority.pwa.service.search.consents.ConsentSearchService;

public class PwaEndpointTestBuilder {

  private MockMvc mockMvc;
  private Set<PwaPermission> allowedProcessingPermissions = Set.of();

  private MasterPwaService masterPwaService;

  private ConsentSearchService consentSearchService;

  private PwaPermissionService pwaPermissionService;

  private Function<MasterPwa, String> endpointUrlProducer;

  private Consumer<MasterPwa> preTestSetup;

  private Map<String, String> requestParams = new HashMap<>();

  private WebUserAccount userWua;
  private AuthenticatedUserAccount user;
  private Set<PwaUserPrivilege> userPrivileges;
  private MasterPwa masterPwa;

  private HttpMethod requestMethod;

  public PwaEndpointTestBuilder(MockMvc mockMvc,
                                MasterPwaService masterPwaService) {
    this.mockMvc = mockMvc;
    this.masterPwaService = masterPwaService;

    setupTestObjects();
    // do nothing by default
    this.preTestSetup = (pwa) -> {};
  }

  public PwaEndpointTestBuilder(MockMvc mockMvc,
                                MasterPwaService masterPwaService,
                                PwaPermissionService pwaPermissionService,
                                ConsentSearchService consentSearchService) {
    this.mockMvc = mockMvc;
    this.masterPwaService = masterPwaService;
    this.pwaPermissionService = pwaPermissionService;
    this.consentSearchService = consentSearchService;

    setupTestObjects();
    // do nothing by default
    this.preTestSetup = (pwa) -> {};

  }

  public PwaEndpointTestBuilder setPreTestSetupMethod(Consumer<MasterPwa> setup){
    this.preTestSetup = setup;
    return this;
  }

  public PwaEndpointTestBuilder setEndpointUrlProducer(
      Function<MasterPwa, String> endpointUrlProducer) {
    this.endpointUrlProducer = endpointUrlProducer;
    return this;
  }

  public PwaEndpointTestBuilder setUserPrivileges(PwaUserPrivilege... privileges) {
    this.userPrivileges = Arrays.stream(privileges).collect(Collectors.toSet());
    return this;
  }

  public PwaEndpointTestBuilder setRequestMethod(HttpMethod requestMethod) {
    if (requestMethod != HttpMethod.GET && requestMethod != HttpMethod.POST) {
      throw new IllegalArgumentException("Only GET,  POST supported request types. Actual: " + requestMethod);
    }
    this.requestMethod = requestMethod;
    return this;
  }

  public PwaEndpointTestBuilder setAllowedProcessingPermissions(PwaPermission... permissions) {
    this.allowedProcessingPermissions = Set.of(permissions);
    return this;
  }

  public PwaEndpointTestBuilder addRequestParam(String key, String value) {
    this.requestParams.put(key, value);
    return this;
  }


  /**
   * Generate request params from set builder values
   */
  private MultiValueMap<String, String> generateRequestParams() {
    MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
    for (Map.Entry entry : this.requestParams.entrySet()) {
      paramMap.add(entry.getKey().toString(), entry.getValue().toString());
    }

    return paramMap;
  }

  /**
   * perform a GET or POST request upon a given url and with an expected result
   */
  private void performRequest(String url, ResultMatcher resultMatcher) throws Exception {
    var paramMap = generateRequestParams();
    if (this.requestMethod == HttpMethod.GET) {
      this.mockMvc.perform(
          get(url)
              .with(user(user))
              .params(paramMap)
      ).andExpect(resultMatcher);
    } else {
      this.mockMvc.perform(
          post(url)
              .with(user(user))
              .with(csrf())
              .params(paramMap)
      )
          .andExpect(resultMatcher);
    }
  }

  /**
   * perform a GET or POST request upon a given url and with an expected result
   */
  private void performUnauthenticatedRequest(String url, ResultMatcher resultMatcher) throws Exception {
    var paramMap = generateRequestParams();
    if (this.requestMethod == HttpMethod.GET) {
      this.mockMvc.perform(
          get(url)
              .params(paramMap)
      ).andExpect(resultMatcher);
    } else {
      this.mockMvc.perform(
          post(url)
              .with(csrf())
              .params(paramMap)
      )
          .andExpect(resultMatcher);
    }
  }

  private void setupTestObjects() {

    this.userWua = new WebUserAccount(1);

    var privs = Optional.ofNullable(userPrivileges).orElse(EnumSet.allOf(PwaUserPrivilege.class));
    this.user = new AuthenticatedUserAccount(userWua, privs);

    var pwaId = 1;
    this.masterPwa = new MasterPwa();
    this.masterPwa.setId(pwaId);
    this.masterPwa.setCreatedTimestamp(Instant.MIN);
    when(masterPwaService.getMasterPwaById(masterPwa.getId())).thenReturn(masterPwa);

    var consentSearchItem = new ConsentSearchItem();
    consentSearchItem.setPwaId(pwaId);
    consentSearchItem.setPwaReference("1/W/02");
    consentSearchItem.setResourceType(PwaResourceType.PETROLEUM);

    consentSearchItem.setFirstConsentTimestamp(Instant.now());
    consentSearchItem.setLatestConsentTimestamp(Instant.now());
    consentSearchItem.setLatestConsentReference("latest consent reference");
    var consentSearchResultView = ConsentSearchResultView.fromSearchItem(consentSearchItem);
    when(consentSearchService.getConsentSearchResultView(pwaId)).thenReturn(consentSearchResultView);

    var defaultPermissions = EnumSet.allOf(PwaPermission.class);
    if (pwaPermissionService != null) {
      when(pwaPermissionService.getPwaPermissions(masterPwa, user)).thenReturn(defaultPermissions);
    }

  }



  public void performProcessingPermissionCheck(ResultMatcher matchingTypeResultMatcher,
                                               ResultMatcher otherTypeResultMatcher) {
    setupTestObjects();
    for (PwaPermission permission : PwaPermission.values()) {
      try {

        preTestSetup.accept(masterPwa);

        // Based on required permission for endpoint, if role under test grants required permission
        when(pwaPermissionService.getPwaPermissions(masterPwa, user)).thenReturn(Set.of(permission));

        var expected = this.allowedProcessingPermissions.contains(permission);

        performRequest(
            this.endpointUrlProducer.apply(masterPwa),
            expected ? matchingTypeResultMatcher : otherTypeResultMatcher
        );

      } catch (AssertionError | Exception e) {
        throw new AssertionError("Failed at permission:" + permission + "\n" + e.getMessage(), e);
      }
    }
    // try when zero permissions
    try {

      when(pwaPermissionService.getPwaPermissions(any(), any())).thenReturn(Set.of());
      performRequest(
          this.endpointUrlProducer.apply(masterPwa),
          otherTypeResultMatcher
      );

    } catch (AssertionError | Exception e) {
      throw new AssertionError("Failed when ZERO permissions\n" + e.getMessage(), e);
    }

    // try unauthenticated requests
    try {
      performUnauthenticatedRequest(this.endpointUrlProducer.apply(masterPwa),
          status().is3xxRedirection());
    } catch (AssertionError | Exception e) {
      throw new AssertionError("Unauthenticated check expected 3xx redirect\n" + e.getMessage(), e);
    }

  }

  public MasterPwa getMasterPwa() {
    return masterPwa;
  }
}
