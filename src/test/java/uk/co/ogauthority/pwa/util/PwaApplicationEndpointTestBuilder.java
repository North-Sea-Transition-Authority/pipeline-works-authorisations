package uk.co.ogauthority.pwa.util;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.co.ogauthority.pwa.util.TestUserProvider.authenticatedUserAndSession;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.enums.pipelines.PipelineType;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.form.pipelines.PadPipeline;
import uk.co.ogauthority.pwa.model.location.CoordinatePair;
import uk.co.ogauthority.pwa.model.location.LatitudeCoordinate;
import uk.co.ogauthority.pwa.model.location.LongitudeCoordinate;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorRole;
import uk.co.ogauthority.pwa.model.teams.PwaRole;
import uk.co.ogauthority.pwa.model.teams.PwaTeamMember;
import uk.co.ogauthority.pwa.service.enums.location.LatitudeDirection;
import uk.co.ogauthority.pwa.service.enums.location.LongitudeDirection;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationDetailService;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaContactService;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PadPipelineService;
import uk.co.ogauthority.pwa.service.teams.TeamService;

public class PwaApplicationEndpointTestBuilder {

  private MockMvc mockMvc;
  private Set<PwaApplicationType> allowedTypes = Set.of();
  private Set<PwaApplicationStatus> allowedStatuses = Set.of();
  private Set<PwaContactRole> contactRolesGivenAccess = Set.of();
  private Set<PwaRegulatorRole> regulatorRolesGivenAccess = Set.of();

  private PwaContactService pwaContactService;
  private PwaApplicationDetailService pwaApplicationDetailService;

  private PadPipelineService padPipelineService;
  private TeamService teamService;

  private BiFunction<PwaApplicationDetail, PwaApplicationType, String> endpointUrlProducer;

  private Consumer<PwaApplicationDetail> preTestSetup;

  private Map<String, String> requestParams = new HashMap<>();

  private WebUserAccount userWua;
  private AuthenticatedUserAccount user;
  private PwaApplicationDetail detail;
  private PadPipeline pipeline;

  private HttpMethod requestMethod;


  public PwaApplicationEndpointTestBuilder(MockMvc mockMvc,
                                           PwaContactService pwaContactService,
                                           PwaApplicationDetailService pwaApplicationDetailService) {
    this.mockMvc = mockMvc;
    this.pwaContactService = pwaContactService;
    this.pwaApplicationDetailService = pwaApplicationDetailService;

    setupTestObjects();
    // do nothing by default
    this.preTestSetup = (detail) -> {};
  }

  public PwaApplicationEndpointTestBuilder(MockMvc mockMvc,
                                           PwaContactService pwaContactService,
                                           PwaApplicationDetailService pwaApplicationDetailService,
                                           PadPipelineService padPipelineService) {

    this.mockMvc = mockMvc;
    this.pwaContactService = pwaContactService;
    this.pwaApplicationDetailService = pwaApplicationDetailService;
    this.padPipelineService = padPipelineService;

    setupTestObjects();
    // do nothing by default
    this.preTestSetup = (detail) -> {};

  }

  public PwaApplicationEndpointTestBuilder(MockMvc mockMvc,
                                           TeamService teamService,
                                           PwaApplicationDetailService pwaApplicationDetailService) {

    this.mockMvc = mockMvc;
    this.teamService = teamService;
    this.pwaApplicationDetailService = pwaApplicationDetailService;

    setupTestObjects();
    // do nothing by default
    this.preTestSetup = (detail) -> {};

  }

  public PwaApplicationEndpointTestBuilder setPreTestSetupMethod(Consumer<PwaApplicationDetail> setup){
    this.preTestSetup = setup;
    return this;
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

  public PwaApplicationEndpointTestBuilder setAllowedContactRoles(
      PwaContactRole... rolesGivenAccess) {
    this.contactRolesGivenAccess = Set.of(rolesGivenAccess);
    return this;
  }

  public PwaApplicationEndpointTestBuilder setAllowedRegulatorRoles(PwaRegulatorRole... roles) {
    this.regulatorRolesGivenAccess = Set.of(roles);
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

  private void setupTestObjects() {
    var defaultStatus = this.allowedStatuses.stream().findAny().orElse(PwaApplicationStatus.DRAFT);
    var defaultType = this.allowedTypes.stream().findAny().orElse(PwaApplicationType.INITIAL);

    this.userWua = new WebUserAccount(1);
    this.user = new AuthenticatedUserAccount(userWua, EnumSet.allOf(PwaUserPrivilege.class));
    this.detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    this.pipeline = new PadPipeline();
    pipeline.setId(99);
    pipeline.setPwaApplicationDetail(detail);
    pipeline.setPipelineRef("TEMPORARY_1");
    pipeline.setFromLocation("from");
    pipeline.setToLocation("to");
    pipeline.setFromCoordinates(new CoordinatePair(
        new LatitudeCoordinate(45, 2, BigDecimal.valueOf(2.2), LatitudeDirection.NORTH),
        new LongitudeCoordinate(12, 1, BigDecimal.valueOf(1), LongitudeDirection.EAST)
    ));
    pipeline.setToCoordinates(new CoordinatePair(
        new LatitudeCoordinate(46, 2, BigDecimal.valueOf(2.2), LatitudeDirection.NORTH),
        new LongitudeCoordinate(12, 1, BigDecimal.valueOf(1), LongitudeDirection.EAST)
    ));
    pipeline.setProductsToBeConveyed("prod");
    pipeline.setPipelineType(PipelineType.PRODUCTION_FLOWLINE);
    pipeline.setComponentPartsDescription("comp");
    pipeline.setLength(BigDecimal.valueOf(200));

    detail.setStatus(defaultStatus);
    detail.getPwaApplication().setApplicationType(defaultType);
    when(pwaApplicationDetailService.getTipDetail(detail.getMasterPwaApplicationId())).thenReturn(detail);

    var defaultContactRoles = EnumSet.allOf(PwaContactRole.class);

    if (pwaContactService != null) {
      when(pwaContactService.getContactRoles(eq(detail.getPwaApplication()), any())).thenReturn(defaultContactRoles);
    }

    if (padPipelineService != null) {
      when(padPipelineService.getById(pipeline.getId())).thenReturn(pipeline);
    }

    var defaultRegulatorRoles = EnumSet.allOf(PwaRegulatorRole.class);

    if (teamService != null) {
      when(teamService.getMembershipOfPersonInTeam(teamService.getRegulatorTeam(), user.getLinkedPerson())).thenReturn(
          Optional.of(new PwaTeamMember(null, user.getLinkedPerson(), convertRegRolesToPwaRoles(defaultRegulatorRoles))));
    }

  }

  private Set<PwaRole> convertRegRolesToPwaRoles(Set<PwaRegulatorRole> roles) {
    return roles.stream()
        .map(role -> new PwaRole(role.getPortalTeamRoleName(), role.name(), null, 1))
        .collect(Collectors.toSet());
  }

  public void performAppStatusChecks(ResultMatcher matchingTypeResultMatcher, ResultMatcher otherTypeResultMatcher) {
    setupTestObjects();

    for (PwaApplicationStatus status : PwaApplicationStatus.values()) {
      try {

        detail.setStatus(status);
        preTestSetup.accept(detail);
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
    setupTestObjects();
    for (PwaApplicationType type : PwaApplicationType.values()) {
      try {
        detail.getPwaApplication().setApplicationType(type);
        preTestSetup.accept(detail);
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
    setupTestObjects();
    for (PwaContactRole role : PwaContactRole.values()) {
      try {
        var userAppRoles = Set.of(role);
        preTestSetup.accept(detail);
        when(pwaContactService.getContactRoles(eq(detail.getPwaApplication()), any())).thenReturn(userAppRoles);
        // Based on required permission for endpoint, if role under test grants required permission
        var expected = this.contactRolesGivenAccess.contains(role);

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

  public void performRegulatorRoleCheck(ResultMatcher matchingTypeResultMatcher,
                                        ResultMatcher otherTypeResultMatcher) {
    setupTestObjects();
    for (PwaRegulatorRole role : PwaRegulatorRole.values()) {
      try {
        var userRegRoles = Set.of(role);
        preTestSetup.accept(detail);
        when(teamService.getMembershipOfPersonInTeam(teamService.getRegulatorTeam(), user.getLinkedPerson())).thenReturn(
            Optional.of(new PwaTeamMember(null, user.getLinkedPerson(), convertRegRolesToPwaRoles(userRegRoles))));
        // Based on required permission for endpoint, if role under test grants required permission
        var expected = this.regulatorRolesGivenAccess.contains(role);

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
      when(teamService.getMembershipOfPersonInTeam(teamService.getRegulatorTeam(), user.getLinkedPerson())).thenReturn(Optional.empty());
      performRequest(
          this.endpointUrlProducer.apply(detail, detail.getPwaApplicationType()),
          otherTypeResultMatcher
      );

    } catch (AssertionError | Exception e) {
      throw new AssertionError("Failed when ZERO reg roles\n" + e.getMessage(), e);
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