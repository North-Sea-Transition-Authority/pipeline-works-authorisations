package uk.co.ogauthority.pwa.teams.management.access;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerMapping;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.teams.Role;
import uk.co.ogauthority.pwa.teams.Team;
import uk.co.ogauthority.pwa.teams.TeamQueryService;
import uk.co.ogauthority.pwa.teams.TeamType;
import uk.co.ogauthority.pwa.teams.management.TeamManagementService;

@ExtendWith(MockitoExtension.class)
class TeamManagementHandlerInterceptorTest {

  @Mock
  private TeamManagementService teamManagementService;

  @Mock
  private TeamQueryService teamQueryService;

  @InjectMocks
  private TeamManagementHandlerInterceptor teamManagementHandlerInterceptor;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private HandlerMethod handlerMethod;

  private final AuthenticatedUserAccount invokingUser = new AuthenticatedUserAccount();


  @BeforeEach
  void setUp() {
    invokingUser.setWuaId(1);
    invokingUser.setForename("Test");
    invokingUser.setSurname("User");
    invokingUser.setEmailAddress("test@example.com");
  }

  @Test
  void preHandle_invokingUserHasStaticRole() throws Exception {
    var method = TestController.class.getDeclaredMethod("invokingUserHasStaticRole", UUID.class);
    when(handlerMethod.getMethod()).thenReturn(method);

    when(teamQueryService.userHasStaticRole((long) invokingUser.getWuaId(), TeamType.REGULATOR, Role.TEAM_ADMINISTRATOR))
        .thenReturn(true);

    assertThat(teamManagementHandlerInterceptor.preHandle(request, response, handlerMethod))
        .isTrue();
  }

  @Test
  void preHandle_invokingUserHasStaticRole_noAccess() throws Exception {
    var method = TestController.class.getDeclaredMethod("invokingUserHasStaticRole", UUID.class);
    when(handlerMethod.getMethod()).thenReturn(method);

    when(teamQueryService.userHasStaticRole((long) invokingUser.getWuaId(), TeamType.REGULATOR, Role.TEAM_ADMINISTRATOR))
        .thenReturn(false);

    assertThatExceptionOfType(ResponseStatusException.class)
        .isThrownBy(() -> teamManagementHandlerInterceptor.preHandle(request, response, handlerMethod))
        .extracting(ResponseStatusException::getStatusCode)
        .isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void preHandle_InvokingUserCanManageTeam_staticTeam() throws Exception {
    var method = TestController.class.getDeclaredMethod("invokingUserCanManageTeam", UUID.class);
    when(handlerMethod.getMethod()).thenReturn(method);

    var regTeam = new Team(UUID.randomUUID());
    regTeam.setTeamType(TeamType.REGULATOR);

    when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE))
        .thenReturn(Map.of("teamId", regTeam.getId().toString()));

    when(teamManagementService.getTeam(regTeam.getId()))
        .thenReturn(Optional.of(regTeam));

    when(teamManagementService.getStaticTeamOfTypeUserCanManage(regTeam.getTeamType(), (long) invokingUser.getWuaId()))
        .thenReturn(Optional.of(regTeam));

    assertThat(teamManagementHandlerInterceptor.preHandle(request, response, handlerMethod))
        .isTrue();
  }

  @Test
  void preHandle_InvokingUserCanManageTeam_staticTeam_noAccess() throws Exception {
    var method = TestController.class.getDeclaredMethod("invokingUserCanManageTeam", UUID.class);
    when(handlerMethod.getMethod()).thenReturn(method);

    var regTeam = new Team(UUID.randomUUID());
    regTeam.setTeamType(TeamType.REGULATOR);

    when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE))
        .thenReturn(Map.of("teamId", regTeam.getId().toString()));

    when(teamManagementService.getTeam(regTeam.getId()))
        .thenReturn(Optional.of(regTeam));

    when(teamManagementService.getStaticTeamOfTypeUserCanManage(regTeam.getTeamType(), (long) invokingUser.getWuaId()))
        .thenReturn(Optional.empty());

    assertThatExceptionOfType(ResponseStatusException.class)
        .isThrownBy(() -> teamManagementHandlerInterceptor.preHandle(request, response, handlerMethod))
        .extracting(ResponseStatusException::getStatusCode)
        .isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void preHandle_InvokingUserCanManageTeam_scopedTeam() throws Exception {
    var method = TestController.class.getDeclaredMethod("invokingUserCanManageTeam", UUID.class);
    when(handlerMethod.getMethod()).thenReturn(method);

    var orgTeam = new Team(UUID.randomUUID());
    orgTeam.setTeamType(TeamType.ORGANISATION);

    when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE))
        .thenReturn(Map.of("teamId", orgTeam.getId().toString()));

    when(teamManagementService.getTeam(orgTeam.getId()))
        .thenReturn(Optional.of(orgTeam));

    when(teamManagementService.getScopedTeamsOfTypeUserCanManage(orgTeam.getTeamType(), (long) invokingUser.getWuaId()))
        .thenReturn(List.of(orgTeam));

    assertThat(teamManagementHandlerInterceptor.preHandle(request, response, handlerMethod))
        .isTrue();
  }

  @Test
  void preHandle_InvokingUserCanManageTeam_scopedTeam_noAccess() throws Exception {
    var method = TestController.class.getDeclaredMethod("invokingUserCanManageTeam", UUID.class);
    when(handlerMethod.getMethod()).thenReturn(method);

    var orgTeam = new Team(UUID.randomUUID());
    orgTeam.setTeamType(TeamType.ORGANISATION);

    when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE))
        .thenReturn(Map.of("teamId", orgTeam.getId().toString()));

    when(teamManagementService.getTeam(orgTeam.getId()))
        .thenReturn(Optional.of(orgTeam));

    when(teamManagementService.getScopedTeamsOfTypeUserCanManage(orgTeam.getTeamType(), (long) invokingUser.getWuaId()))
        .thenReturn(List.of());

    assertThatExceptionOfType(ResponseStatusException.class)
        .isThrownBy(() -> teamManagementHandlerInterceptor.preHandle(request, response, handlerMethod))
        .extracting(ResponseStatusException::getStatusCode)
        .isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void preHandle_InvokingUserCanManageTeam_noTeamIdInPath() throws Exception {
    var method = TestController.class.getDeclaredMethod("invokingUserCanManageTeam", UUID.class);
    when(handlerMethod.getMethod()).thenReturn(method);

    when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE))
        .thenReturn(Map.of());

    assertThatExceptionOfType(ResponseStatusException.class)
        .isThrownBy(() -> teamManagementHandlerInterceptor.preHandle(request, response, handlerMethod))
        .extracting(ResponseStatusException::getStatusCode)
        .isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void preHandle_InvokingUserCanManageTeam_malformedTeamIdUuid() throws Exception {
    var method = TestController.class.getDeclaredMethod("invokingUserCanManageTeam", UUID.class);
    when(handlerMethod.getMethod()).thenReturn(method);

    when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE))
        .thenReturn(Map.of("teamId", "not-a-uid"));

    assertThatExceptionOfType(ResponseStatusException.class)
        .isThrownBy(() -> teamManagementHandlerInterceptor.preHandle(request, response, handlerMethod))
        .extracting(ResponseStatusException::getStatusCode)
        .isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void preHandle_invokingUserCanViewTeam_whenNoTeamIdInPath_thenBadRequest() throws Exception {

    var method = TestController.class.getDeclaredMethod("invokingUserCanViewTeam", UUID.class);
    when(handlerMethod.getMethod()).thenReturn(method);

    when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE))
        .thenReturn(Map.of());

    assertThatExceptionOfType(ResponseStatusException.class)
        .isThrownBy(() -> teamManagementHandlerInterceptor.preHandle(request, response, handlerMethod))
        .extracting(ResponseStatusException::getStatusCode)
        .isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void preHandle_invokingUserCanViewTeam_whenMalformedTeamIdUuid_thenBadRequest() throws Exception {

    var method = TestController.class.getDeclaredMethod("invokingUserCanViewTeam", UUID.class);
    when(handlerMethod.getMethod()).thenReturn(method);

    when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE))
        .thenReturn(Map.of("teamId", "not-a-uid"));

    assertThatExceptionOfType(ResponseStatusException.class)
        .isThrownBy(() -> teamManagementHandlerInterceptor.preHandle(request, response, handlerMethod))
        .extracting(ResponseStatusException::getStatusCode)
        .isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void preHandle_invokingUserCanViewTeam_whenIsMemberOfTeam_thenOk() throws Exception {

    var method = TestController.class.getDeclaredMethod("invokingUserCanViewTeam", UUID.class);
    when(handlerMethod.getMethod()).thenReturn(method);

    var team = new Team(UUID.randomUUID());

    when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE))
        .thenReturn(Map.of("teamId", team.getId().toString()));

    when(teamManagementService.getTeam(team.getId()))
        .thenReturn(Optional.of(team));

    when(teamManagementService.isMemberOfTeam(team, (long) invokingUser.getWuaId()))
        .thenReturn(true);

    assertThat(teamManagementHandlerInterceptor.preHandle(request, response, handlerMethod)).isTrue();
  }

  @Test
  void preHandle_invokingUserCanViewTeam_whenNotMemberOfTeam_thenForbidden() throws Exception {

    var method = TestController.class.getDeclaredMethod("invokingUserCanViewTeam", UUID.class);
    when(handlerMethod.getMethod()).thenReturn(method);

    var team = new Team(UUID.randomUUID());

    when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE))
        .thenReturn(Map.of("teamId", team.getId().toString()));

    when(teamManagementService.getTeam(team.getId()))
        .thenReturn(Optional.of(team));

    when(teamManagementService.isMemberOfTeam(team, (long) invokingUser.getWuaId()))
        .thenReturn(false);

    assertThatExceptionOfType(ResponseStatusException.class)
        .isThrownBy(() -> teamManagementHandlerInterceptor.preHandle(request, response, handlerMethod))
        .extracting(ResponseStatusException::getStatusCode)
        .isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void preHandle_invokingUserCanViewTeam_whenOrganisationTeam_andNotMemberOfTeam_andNotManageAnyOrganisationTeamRole_thenForbidden() throws Exception {

    var method = TestController.class.getDeclaredMethod("invokingUserCanViewTeam", UUID.class);
    when(handlerMethod.getMethod()).thenReturn(method);

    var organisationTeam = new Team(UUID.randomUUID());
    organisationTeam.setTeamType(TeamType.ORGANISATION);

    when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE))
        .thenReturn(Map.of("teamId", organisationTeam.getId().toString()));

    when(teamManagementService.getTeam(organisationTeam.getId()))
        .thenReturn(Optional.of(organisationTeam));

    when(teamManagementService.isMemberOfTeam(organisationTeam, (long) invokingUser.getWuaId()))
        .thenReturn(false);

    when(teamManagementService.userCanManageAnyOrganisationTeam((long) invokingUser.getWuaId()))
        .thenReturn(false);

    assertThatExceptionOfType(ResponseStatusException.class)
        .isThrownBy(() -> teamManagementHandlerInterceptor.preHandle(request, response, handlerMethod))
        .extracting(ResponseStatusException::getStatusCode)
        .isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void preHandle_invokingUserCanViewTeam_whenOrganisationTeam_andNotMemberOfTeam_andHasManageAnyOrganisationTeamRole_thenOk() throws Exception {

    var method = TestController.class.getDeclaredMethod("invokingUserCanViewTeam", UUID.class);
    when(handlerMethod.getMethod()).thenReturn(method);

    var organisationTeam = new Team(UUID.randomUUID());
    organisationTeam.setTeamType(TeamType.ORGANISATION);

    when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE))
        .thenReturn(Map.of("teamId", organisationTeam.getId().toString()));

    when(teamManagementService.getTeam(organisationTeam.getId()))
        .thenReturn(Optional.of(organisationTeam));

    when(teamManagementService.isMemberOfTeam(organisationTeam, (long) invokingUser.getWuaId()))
        .thenReturn(false);

    when(teamManagementService.userCanManageAnyOrganisationTeam((long) invokingUser.getWuaId()))
        .thenReturn(true);

    assertThat(teamManagementHandlerInterceptor.preHandle(request, response, handlerMethod)).isTrue();
  }

  @Test
  void preHandle_noAnnotation() throws Exception {
    var method = TestController.class.getDeclaredMethod("noAnnotation", UUID.class);
    when(handlerMethod.getMethod()).thenReturn(method);

    assertThat(teamManagementHandlerInterceptor.preHandle(request, response, handlerMethod))
        .isTrue();
  }

  @Controller
  static class TestController {

    @GetMapping("/{teamId}/foo")
    @InvokingUserHasStaticRole(teamType = TeamType.REGULATOR, role = Role.TEAM_ADMINISTRATOR)
    String invokingUserHasStaticRole(@PathVariable UUID teamId) {
      return "ok";
    }

    @GetMapping("/{teamId}/bar")
    @InvokingUserCanManageTeam
    String invokingUserCanManageTeam(@PathVariable UUID teamId) {
      return "ok";
    }

    @GetMapping("/{teamId}/can-view-team")
    @InvokingUserCanViewTeam
    String invokingUserCanViewTeam(@PathVariable UUID teamId) {
      return "ok";
    }

    @GetMapping("/{teamId}/baz")
    String noAnnotation(@PathVariable UUID teamId) {
      return "ok";
    }
  }
}
