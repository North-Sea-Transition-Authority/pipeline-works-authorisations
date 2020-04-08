package uk.co.ogauthority.pwa.util;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaContactService;

public class PwaApplicationEndpointTestBuilder {

  private MockMvc mockMvc;
  private Set<PwaApplicationType> allowedTypes = Set.of();
  private Set<PwaApplicationStatus> allowedStatuses = Set.of();
  private Set<PwaContactRole> rolesGivenAccess = Set.of();

  private PwaContactService pwaContactService;
  private PwaApplicationDetailService pwaApplicationDetailService;

  private BiFunction<PwaApplicationDetail, PwaApplicationType, String> endpointUrlProducer;

  private Map<String, String> requestParams = new HashMap<>();

  private WebUserAccount userWua;
  private AuthenticatedUserAccount user;
  private PwaApplicationDetail detail;

  private HttpMethod requestMethod;


  public PwaApplicationEndpointTestBuilder(MockMvc mockMvc,
                                           PwaContactService pwaContactService,
                                           PwaApplicationDetailService pwaApplicationDetailService) {
    this.mockMvc = mockMvc;
    this.pwaContactService = pwaContactService;
    this.pwaApplicationDetailService = pwaApplicationDetailService;

    this.userWua = new WebUserAccount(1);
    this.user = new AuthenticatedUserAccount(userWua, EnumSet.allOf(PwaUserPrivilege.class));
    this.detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);

    // setup mocks so basic app context checks can be executed
    when(this.pwaApplicationDetailService.getTipDetail(any())).thenReturn(detail);
    when(this.pwaContactService.getContactRoles(any(), any())).thenReturn(EnumSet.allOf(PwaContactRole.class));
  }

  public PwaApplicationEndpointTestBuilder setEndpointUrlProducer(
      BiFunction<PwaApplicationDetail, PwaApplicationType, String> endpointUrlProducer) {
    this.endpointUrlProducer = endpointUrlProducer;
    return this;
  }


  public PwaApplicationEndpointTestBuilder setRequestMethod(HttpMethod requestMethod) {
    if (!EnumSet.of(HttpMethod.GET, HttpMethod.POST).contains(requestMethod)) {
      throw new IllegalArgumentException("Only GET,  POST supported request types. Actual: " + requestMethod);
    }
    this.requestMethod = requestMethod;
    return this;
  }

  public PwaApplicationEndpointTestBuilder setAllowedTypes(
      PwaApplicationType... allowedTypes) {
    this.allowedTypes = Set.of(allowedTypes);
    return this;
  }

  public PwaApplicationEndpointTestBuilder setAllowedStatuses(
      PwaApplicationStatus... allowedStatuses) {
    this.allowedStatuses = Set.of(allowedStatuses);
    return this;
  }

  public PwaApplicationEndpointTestBuilder setAllowedRoles(
      PwaContactRole... rolesGivenAccess) {
    this.rolesGivenAccess = Set.of(rolesGivenAccess);
    return this;
  }

  public PwaApplicationEndpointTestBuilder addRequestParam(String key, String value) {
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
              .with(authenticatedUserAndSession(user))
              .params(paramMap)
      ).andExpect(resultMatcher);
    } else {
      this.mockMvc.perform(
          post(url)
              .with(authenticatedUserAndSession(user))
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

  public void performAppStatusChecks(ResultMatcher matchingTypeResultMatcher, ResultMatcher otherTypeResultMatcher) {

    for (PwaApplicationStatus status : PwaApplicationStatus.values()) {
      try {
        detail.setStatus(status);
        var expected = this.allowedStatuses.contains(status);

        performRequest(
            this.endpointUrlProducer.apply(detail, detail.getPwaApplicationType()),
            expected ? matchingTypeResultMatcher : otherTypeResultMatcher
        );

      } catch (AssertionError | Exception e) {
        throw new AssertionError("Failed at status:" + status + "\n" + e.getMessage(), e);
      }
    }
  }

  public void performAppTypeChecks(ResultMatcher matchingTypeResultMatcher, ResultMatcher otherTypeResultMatcher) {

    for (PwaApplicationType type : PwaApplicationType.values()) {
      try {
        detail.getPwaApplication().setApplicationType(type);
        var expected = this.allowedTypes.contains(type);
        performRequest(
            this.endpointUrlProducer.apply(detail, type),
            expected ? matchingTypeResultMatcher : otherTypeResultMatcher
        );

      } catch (AssertionError | Exception e) {
        throw new AssertionError("Failed at type:" + type + "\n" + e.getMessage(), e);
      }
    }
  }

  public void performAppContactRoleCheck(ResultMatcher matchingTypeResultMatcher,
                                         ResultMatcher otherTypeResultMatcher) {

    for (PwaContactRole role : PwaContactRole.values()) {
      try {
        var userAppRoles = Set.of(role);
        when(pwaContactService.getContactRoles(eq(detail.getPwaApplication()), any())).thenReturn(userAppRoles);
        // Based on required permission for endpoin, if role under test grants required permission
        var expected = this.rolesGivenAccess.contains(role);

        performRequest(
            this.endpointUrlProducer.apply(detail, detail.getPwaApplicationType()),
            expected ? matchingTypeResultMatcher : otherTypeResultMatcher
        );

      } catch (AssertionError | Exception e) {
        throw new AssertionError("Failed at Role:" + role + "\n" + e.getMessage(), e);
      }
    }
    // try when zero app contact roles
    try {
      when(pwaContactService.getContactRoles(eq(detail.getPwaApplication()), any())).thenReturn(Set.of());
      performRequest(
          this.endpointUrlProducer.apply(detail, detail.getPwaApplicationType()),
          otherTypeResultMatcher
      );

    } catch (AssertionError | Exception e) {
      throw new AssertionError("Failed when ZERO app roles\n" + e.getMessage(), e);
    }

    // try unauthenticated requests
    try {
      performUnauthenticatedRequest(this.endpointUrlProducer.apply(detail, detail.getPwaApplicationType()),
          status().is3xxRedirection());
    } catch (AssertionError | Exception e) {
      throw new AssertionError("Unauthenticated check expected 3xx redirect\n" + e.getMessage(), e);
    }

  }

}