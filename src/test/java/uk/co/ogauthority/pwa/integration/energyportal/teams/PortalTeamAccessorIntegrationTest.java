package uk.co.ogauthority.pwa.integration.energyportal.teams;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.energyportal.exceptions.teams.PortalTeamNotFoundException;
import uk.co.ogauthority.pwa.energyportal.model.dto.teams.PortalRoleDto;
import uk.co.ogauthority.pwa.energyportal.model.dto.teams.PortalSystemPrivilegeDto;
import uk.co.ogauthority.pwa.energyportal.model.dto.teams.PortalTeamDto;
import uk.co.ogauthority.pwa.energyportal.model.dto.teams.PortalTeamMemberDto;
import uk.co.ogauthority.pwa.energyportal.model.dto.teams.PortalTeamScopeDto;
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.energyportal.model.entity.teams.PortalTeamUsagePurpose;
import uk.co.ogauthority.pwa.energyportal.repository.PersonRepository;
import uk.co.ogauthority.pwa.energyportal.repository.teams.PortalTeamRepository;
import uk.co.ogauthority.pwa.energyportal.service.teams.PortalTeamAccessor;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureDataJpa
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles("integration-test")
@SuppressWarnings({"JpaQueryApiInspection", "SqlNoDataSourceInspection"}) // IJ seems to give spurious warnings when running with embedded H2
public class PortalTeamAccessorIntegrationTest {


  private final String WITH_SCOPE_SCOPED_WITHIN = "PARENT";
  private final String WITHOUT_SCOPE_SCOPED_WITHIN = "UNIVERSAL_SET";

  private final String SCOPED_TEAM_PORTAL_TYPE_TITLE = "Org1TeamTitle";
  private final String SCOPED_TEAM_PORTAL_TYPE_DESCRIPTION = "Org1TeamDescription";

  private final int UNKNOWN_RES_ID = 987654321;
  private final String NO_TEAMS_OF_PORTAL_TEAM_TYPE = "SOME_TEAM_TYPE_WITH_NO_TEAM_INSTANCES";

  private final int UNSCOPED_TEAM_RES_ID = 100;
  private final String UNSCOPED_TEAM_PORTAL_TYPE = "UNSCOPED_TEAM_TYPE";
  private final String UNSCOPED_TEAM_PORTAL_TYPE_TITLE = "RegulatorTeamTitle";
  private final String UNSCOPED_TEAM_PORTAL_TYPE_DESCRIPTION = "RegulatorTeamDescription";
  private final String UNSCOPED_TEAM_NAME = "PwaRegulatorTeam";
  private final String UNSCOPED_TEAM_DESCRIPTION = "RegulatorTeamDescription";

  private final String SCOPED_TEAM_PORTAL_TYPE = "SCOPED_TEAM_TYPE";
  private final int SCOPED_TEAM_RES_ID = 200;
  private final String SCOPED_TEAM_NAME = "Org1Team";
  private final String SCOPED_TEAM_DESCRIPTION = "Org1TeamDescription";
  private final String SCOPED_TEAM_UREF = constructOrgGroupUref(20);

  private final int NO_MEMBER_SCOPED_TEAM_RES_ID = 300;
  private final String NO_MEMBER_SCOPED_TEAM_NAME = "Org2Team";
  private final String NO_MEMBER_SCOPED_TEAM_DESCRIPTION = "Org2TeamDescription";
  private final String NO_MEMBER_SCOPED_TEAM_UREF = constructOrgGroupUref(30);

  @Autowired
  private PersonRepository personRepository;

  @Autowired
  private EntityManager entityManager;

  @Autowired
  private PortalTeamRepository portalTeamRepository;

  private PortalTeamAccessor portalTeamAccessor;

  private Person unscopedTeamMemberPerson_2Roles;
  private Person scopedTeamMemberPerson_2Roles;
  private Person scopedTeamMemberPerson_1Role;

  @Before
  public void setup() {
    portalTeamAccessor = new PortalTeamAccessor(portalTeamRepository, entityManager);

    insertPerson(10);
    unscopedTeamMemberPerson_2Roles = personRepository.findById(10).orElse(null);
    insertPerson(20);
    scopedTeamMemberPerson_2Roles = personRepository.findById(20).orElse(null);
    insertPerson(30);
    scopedTeamMemberPerson_1Role = personRepository.findById(30).orElse(null);

    insertPortalTeamTypes();
    insertPortalTeamTypeRoles();
    insertPortalTeamInstancesAndUsages();

    insertTeamMembers();
  }

  @Test
  @Transactional
  public void findPortalTeamById_whenTeamNotFound() {
    Optional<PortalTeamDto> portalTeamDto = portalTeamAccessor.findPortalTeamById(UNKNOWN_RES_ID);
    assertThat(portalTeamDto.isPresent()).isFalse();
  }

  @Test
  @Transactional
  public void findPortalTeamById_whenTeamFound_andIsScoped() {
    // tests for scoped and unscoped required because construction of team dto is done within method
    PortalTeamDto portalTeamDto = portalTeamAccessor.findPortalTeamById(SCOPED_TEAM_RES_ID)
        .orElseThrow(() -> new RuntimeException("Expected To find PwaTeam"));

    assertPortalTeamInstanceDtoMappingAsExpected(
        portalTeamDto,
        SCOPED_TEAM_UREF,
        SCOPED_TEAM_RES_ID,
        SCOPED_TEAM_PORTAL_TYPE,
        SCOPED_TEAM_NAME,
        SCOPED_TEAM_DESCRIPTION
    );
  }

  @Test
  @Transactional
  public void findPortalTeamById_whenTeamFound_andIsNotScoped() {
    // tests for scoped and unscoped required because construction of team dto is done within method
    PortalTeamDto portalTeamDto = portalTeamAccessor.findPortalTeamById(UNSCOPED_TEAM_RES_ID)
        .orElseThrow(() -> new RuntimeException("Expected To find PwaTeam"));

    assertPortalTeamInstanceDtoMappingAsExpected(
        portalTeamDto,
        null,
        UNSCOPED_TEAM_RES_ID,
        UNSCOPED_TEAM_PORTAL_TYPE,
        UNSCOPED_TEAM_NAME,
        UNSCOPED_TEAM_DESCRIPTION
    );
  }

  @Test(expected = PortalTeamNotFoundException.class)
  @Transactional
  public void getPortalTeamMembers_whenTeamNotFound() {
    portalTeamAccessor.getPortalTeamMembers(UNKNOWN_RES_ID);
  }

  @Test
  @Transactional
  public void getPortalTeamMembers_whenTeamFound_andZeroTeamMembers() {
    List<PortalTeamMemberDto> foundMembers = portalTeamAccessor.getPortalTeamMembers(NO_MEMBER_SCOPED_TEAM_RES_ID);
    assertThat(foundMembers).isEmpty();
  }

  @Test
  @Transactional
  public void getPortalTeamMembers_whenTeamFound_andHasTeamMembers() {
    List<PortalTeamMemberDto> foundTeamMembers = portalTeamAccessor.getPortalTeamMembers(SCOPED_TEAM_RES_ID);

    // expected number of team members?
    assertThat(foundTeamMembers).hasSize(2);

    PortalTeamMemberDto member_2Roles = foundTeamMembers.stream()
        .filter(tm -> tm.getPersonId().equals(scopedTeamMemberPerson_2Roles.getId()))
        .findFirst()
        .orElseThrow(() -> new RuntimeException("Expected to find team member A"));

    // does member_2Roles have predictable DTO?
    assertThat(member_2Roles.getRoles()).hasSize(2);
    assertPortalTeamMemberDtoHasExpectedRole(member_2Roles, ExampleTeamRole.ROLE_WITH_PRIV, SCOPED_TEAM_RES_ID);
    assertPortalTeamMemberDtoHasExpectedRole(member_2Roles, ExampleTeamRole.ROLE_WITHOUT_PRIV, SCOPED_TEAM_RES_ID);

    PortalTeamMemberDto member_1Role = foundTeamMembers.stream()
        .filter(tm -> tm.getPersonId().equals(scopedTeamMemberPerson_1Role.getId()))
        .findFirst()
        .orElseThrow(() -> new RuntimeException("Expected to find team member A"));
    // does member_1Role have predictable DTO?
    assertThat(member_1Role.getRoles()).hasSize(1);
    assertPortalTeamMemberDtoHasExpectedRole(member_1Role, ExampleTeamRole.ROLE_WITH_PRIV, SCOPED_TEAM_RES_ID);
  }

  @Test
  @Transactional
  public void getPersonTeamMembership_whenNotATeamMember() {
    assertThat(portalTeamAccessor.getPersonTeamMembership(unscopedTeamMemberPerson_2Roles, SCOPED_TEAM_RES_ID)).isEmpty();
  }

  @Test
  @Transactional
  public void getPersonTeamMembership_whenATeamMember_dtoMappedAsExpected() {
    PortalTeamMemberDto unscopedTeamMember_2RolesDto = portalTeamAccessor.getPersonTeamMembership(
        unscopedTeamMemberPerson_2Roles,
        UNSCOPED_TEAM_RES_ID
    ).orElseThrow(RuntimeException::new);

    assertThat(unscopedTeamMember_2RolesDto.getRoles()).hasSize(2);
    assertPortalTeamMemberDtoHasExpectedRole(
        unscopedTeamMember_2RolesDto,
        ExampleTeamRole.ROLE_WITH_PRIV,
        UNSCOPED_TEAM_RES_ID
    );
    assertPortalTeamMemberDtoHasExpectedRole(
        unscopedTeamMember_2RolesDto,
        ExampleTeamRole.ROLE_WITHOUT_PRIV,
        UNSCOPED_TEAM_RES_ID
    );

  }

  @Test
  @Transactional
  public void getPortalTeamsByPortalTeamType_whenNoTeamsWithTypeFound() {
    assertThat(portalTeamAccessor.getPortalTeamsByPortalTeamType(NO_TEAMS_OF_PORTAL_TEAM_TYPE)).isEmpty();
  }

  @Test
  @Transactional
  public void getPortalTeamsByPortalTeamType_whenTeamsFound_andTeamsScoped() {
    // tests for scoped and unscoped required because construction of team dto is done within method
    List<PortalTeamDto> foundTeamsOfType = portalTeamAccessor.getPortalTeamsByPortalTeamType(SCOPED_TEAM_PORTAL_TYPE);

    assertThat(foundTeamsOfType).hasSize(2);

    PortalTeamDto withMembersTeamDto = foundTeamsOfType.stream()
        .filter(dto -> dto.getResId() == SCOPED_TEAM_RES_ID)
        .findFirst()
        .orElseThrow(RuntimeException::new);

    PortalTeamDto withoutMembersTeamDto = foundTeamsOfType.stream()
        .filter(dto -> dto.getResId() == NO_MEMBER_SCOPED_TEAM_RES_ID)
        .findFirst()
        .orElseThrow(RuntimeException::new);

    assertPortalTeamInstanceDtoMappingAsExpected(
        withMembersTeamDto,
        SCOPED_TEAM_UREF,
        SCOPED_TEAM_RES_ID,
        SCOPED_TEAM_PORTAL_TYPE,
        SCOPED_TEAM_NAME,
        SCOPED_TEAM_DESCRIPTION
    );

    assertPortalTeamInstanceDtoMappingAsExpected(
        withoutMembersTeamDto,
        NO_MEMBER_SCOPED_TEAM_UREF,
        NO_MEMBER_SCOPED_TEAM_RES_ID,
        SCOPED_TEAM_PORTAL_TYPE,
        NO_MEMBER_SCOPED_TEAM_NAME,
        NO_MEMBER_SCOPED_TEAM_DESCRIPTION
    );

  }

  @Test
  @Transactional
  public void getPortalTeamsByPortalTeamType_whenTeamsFound_andTeamsUnscoped() {
    // tests for scoped and unscoped required because construction of team dto is done within method
    List<PortalTeamDto> foundTeamsOfType = portalTeamAccessor.getPortalTeamsByPortalTeamType(UNSCOPED_TEAM_PORTAL_TYPE);

    assertThat(foundTeamsOfType).hasSize(1);
    assertPortalTeamInstanceDtoMappingAsExpected(
        foundTeamsOfType.get(0),
        null,
        UNSCOPED_TEAM_RES_ID,
        UNSCOPED_TEAM_PORTAL_TYPE,
        UNSCOPED_TEAM_NAME,
        UNSCOPED_TEAM_DESCRIPTION
    );

  }

  @Test
  @Transactional
  public void getTeamsWherePersonMemberOfTeamTypeAndHasRoleMatching_whenPersonNotATeamMember() {
    List<PortalTeamDto> foundTeams = portalTeamAccessor.getTeamsWherePersonMemberOfTeamTypeAndHasRoleMatching(
        scopedTeamMemberPerson_1Role,
        UNSCOPED_TEAM_PORTAL_TYPE,
        ExampleTeamRole.getAllRoleNames()
    );

    assertThat(foundTeams).isEmpty();

  }

  @Test
  @Transactional
  public void getTeamsWherePersonMemberOfTeamTypeAndHasRoleMatching_whenPersonIsTeamMember_AndHasSearchForRole_andTeamScoped() {
    // tests for scoped and unscoped required because construction of team dto is done within method
    List<PortalTeamDto> foundTeams = portalTeamAccessor.getTeamsWherePersonMemberOfTeamTypeAndHasRoleMatching(
        scopedTeamMemberPerson_1Role,
        SCOPED_TEAM_PORTAL_TYPE,
        ExampleTeamRole.getAllRoleNames()
    );

    assertThat(foundTeams).hasSize(1);
    assertPortalTeamInstanceDtoMappingAsExpected(
        foundTeams.get(0),
        SCOPED_TEAM_UREF,
        SCOPED_TEAM_RES_ID,
        SCOPED_TEAM_PORTAL_TYPE,
        SCOPED_TEAM_NAME,
        SCOPED_TEAM_DESCRIPTION
    );

  }

  @Test
  @Transactional
  public void getTeamsWherePersonMemberOfTeamTypeAndHasRoleMatching_whenPersonIsTeamMember_AndHasSearchForRole_andTeamUnscoped() {
    // tests for scoped and unscoped required because construction of team dto is done within method
    List<PortalTeamDto> foundTeams = portalTeamAccessor.getTeamsWherePersonMemberOfTeamTypeAndHasRoleMatching(
        unscopedTeamMemberPerson_2Roles,
        UNSCOPED_TEAM_PORTAL_TYPE,
        ExampleTeamRole.getAllRoleNames()
    );

    assertThat(foundTeams).hasSize(1);
    assertPortalTeamInstanceDtoMappingAsExpected(
        foundTeams.get(0),
        null,
        UNSCOPED_TEAM_RES_ID,
        UNSCOPED_TEAM_PORTAL_TYPE,
        UNSCOPED_TEAM_NAME,
        UNSCOPED_TEAM_DESCRIPTION
    );

  }

  @Test
  @Transactional
  public void getAllPortalSystemPrivilegesForPerson_returnsExpectedSystemPrivs_whenPersonIsRoleWithPriv(){
    List<PortalSystemPrivilegeDto> privilegeDtoList = portalTeamAccessor.getAllPortalSystemPrivilegesForPerson(unscopedTeamMemberPerson_2Roles);
    assertThat(privilegeDtoList).allMatch(dto -> {

      assertThat(dto.getRoleName() ).isEqualTo(ExampleTeamRole.ROLE_WITH_PRIV.name());
      assertThat(dto.getGrantedPrivilege() ).isEqualTo(ExampleTeamRole.ROLE_WITH_PRIV.getExampleRolePriv());
      assertThat(dto.getPortalTeamType() ).isEqualTo(UNSCOPED_TEAM_PORTAL_TYPE);
      return true;
    });

  }

  @Test
  @Transactional
  public void personIsAMemberOfTeam_returnsTrueWhenPersonIsMember(){
    assertThat(
        portalTeamAccessor.personIsAMemberOfTeam(UNSCOPED_TEAM_RES_ID, unscopedTeamMemberPerson_2Roles)
    ).isTrue();

  }

  @Test
  @Transactional
  public void personIsAMemberOfTeam_returnsFalseWhenPersonIsNotMember(){
    assertThat(
        portalTeamAccessor.personIsAMemberOfTeam(NO_MEMBER_SCOPED_TEAM_RES_ID, unscopedTeamMemberPerson_2Roles)
    ).isFalse();

  }

  @Test
  @Transactional
  public void getAllPortalRolesForTeam_getsAllExpectedRoles(){
    List<PortalRoleDto> roles = portalTeamAccessor.getAllPortalRolesForTeam(UNSCOPED_TEAM_RES_ID);
    assertThat(roles).hasSize(2);
  }

@Test
@Transactional
public void getTeamsWherePersonMemberOfTeamTypeAndHasRoleMatching_whenPersonIsTeamMember_AndHasDoesntHaveRole() {
  // Tests for scoped and unscoped, required because construction of team dto is done within method
  List<PortalTeamDto> foundTeams = portalTeamAccessor.getTeamsWherePersonMemberOfTeamTypeAndHasRoleMatching(
      scopedTeamMemberPerson_1Role,
      SCOPED_TEAM_PORTAL_TYPE,
      List.of(ExampleTeamRole.ROLE_WITHOUT_PRIV.name())
  );

  assertThat(foundTeams).isEmpty();
}

  private void assertPortalTeamInstanceDtoMappingAsExpected(PortalTeamDto portalTeamDto,
                                                            String expectedScopeURef,
                                                            int expectedResId,
                                                            String expectedTeamType,
                                                            String expectedTeamName,
                                                            String expectedTeamDescription) {
    PortalTeamScopeDto expectedScopeDto = new PortalTeamScopeDto(expectedScopeURef);
    assertThat(portalTeamDto.getResId()).isEqualTo(expectedResId);
    assertThat(portalTeamDto.getType()).isEqualTo(expectedTeamType);
    assertThat(portalTeamDto.getName()).isEqualTo(expectedTeamName);
    assertThat(portalTeamDto.getDescription()).isEqualTo(expectedTeamDescription);
    assertThat(portalTeamDto.getScope()).isEqualTo(expectedScopeDto);
  }

  private void assertPortalTeamMemberDtoHasExpectedRole(PortalTeamMemberDto portalTeamMemberDto,
                                                        ExampleTeamRole exampleTeamRole, int memberTeamResId) {
    assertThat(portalTeamMemberDto.getRoles()).anySatisfy(portalRoleDto -> {
      assertThat(portalRoleDto.getResId()).isEqualTo(memberTeamResId);
      assertThat(portalRoleDto.getName()).isEqualTo(exampleTeamRole.name());
      assertThat(portalRoleDto.getDescription()).isEqualTo(exampleTeamRole.getDesc());
      assertThat(portalRoleDto.getDisplaySequence()).isEqualTo(exampleTeamRole.ordinal());
      assertThat(portalRoleDto.getTitle()).isEqualTo(exampleTeamRole.getTitle());
    });
  }


  /************ Helper methods to insert dummy data into fake H2 tables which represent the PortalTeamEntities ************/
  private void insertTeamMembers() {
    insertTeamMember(UNSCOPED_TEAM_RES_ID, unscopedTeamMemberPerson_2Roles.getId());
    insertTeamMemberRoleForTeamMember(
        UNSCOPED_TEAM_RES_ID,
        unscopedTeamMemberPerson_2Roles.getId(),
        UNSCOPED_TEAM_PORTAL_TYPE,
        ExampleTeamRole.ROLE_WITH_PRIV
    );
    insertTeamMemberRoleForTeamMember(
        UNSCOPED_TEAM_RES_ID,
        unscopedTeamMemberPerson_2Roles.getId(),
        UNSCOPED_TEAM_PORTAL_TYPE,
        ExampleTeamRole.ROLE_WITHOUT_PRIV
    );

    insertTeamMember(SCOPED_TEAM_RES_ID, scopedTeamMemberPerson_2Roles.getId());
    insertTeamMemberRoleForTeamMember(
        SCOPED_TEAM_RES_ID,
        scopedTeamMemberPerson_2Roles.getId(),
        SCOPED_TEAM_PORTAL_TYPE,
        ExampleTeamRole.ROLE_WITH_PRIV
    );
    insertTeamMemberRoleForTeamMember(
        SCOPED_TEAM_RES_ID,
        scopedTeamMemberPerson_2Roles.getId(),
        SCOPED_TEAM_PORTAL_TYPE,
        ExampleTeamRole.ROLE_WITHOUT_PRIV
    );

    insertTeamMember(SCOPED_TEAM_RES_ID, scopedTeamMemberPerson_1Role.getId());
    insertTeamMemberRoleForTeamMember(
        SCOPED_TEAM_RES_ID,
        scopedTeamMemberPerson_1Role.getId(),
        SCOPED_TEAM_PORTAL_TYPE,
        ExampleTeamRole.ROLE_WITH_PRIV
    );
  }

  private void insertTeamMemberRoleForTeamMember(int resId,
                                                 PersonId personId,
                                                 String resType,
                                                 ExampleTeamRole exampleTeamRole) {
    entityManager.createNativeQuery(
        "INSERT INTO portal_res_memb_current_roles (" +
            "  person_id " +
            ", res_id" +
            ", res_type " +
            ", role_name) " +
            "VALUES (:person_id" +
            ", :res_id " +
            ", :res_type " +
            ", :role_name )"
        )
        .setParameter("person_id", personId.asInt())
        .setParameter("res_id", resId)
        .setParameter("res_type", resType)
        .setParameter("role_name", exampleTeamRole.name())
        .executeUpdate();
  }

  private void insertRolePriv(String teamType, ExampleTeamRole exampleTeamRole){
    if(exampleTeamRole.getExampleRolePriv() != null){
      entityManager.createNativeQuery(
          "INSERT INTO portal_resource_type_role_priv (" +
              "  role_name" +
              ", res_type" +
              ", default_system_priv) " +
              "VALUES ( " +
              "  :role_name" +
              ", :res_type" +
              ", :priv_name) "
          )
          .setParameter("res_type", teamType)
          .setParameter("role_name", exampleTeamRole.name())
          .setParameter("priv_name", exampleTeamRole.getExampleRolePriv())
          .executeUpdate();
    }
  }

  private void insertTeamMember(int resId, PersonId personId) {
    entityManager.createNativeQuery(
        "INSERT INTO portal_res_members_current (" +
            "  res_id " +
            ", person_id) " +
            "VALUES (:res_id " +
            ", :person_id) "
        )
        .setParameter("person_id", personId.asInt())
        .setParameter("res_id", resId)
        .executeUpdate();
  }

  private void insertPerson(int personId) {
    entityManager.createNativeQuery(
        "INSERT INTO people (id)" +
        " VALUES (:rp_id)")
        .setParameter("rp_id", personId)
        .executeUpdate();
  }

  private void insertPortalTeamTypes() {
    insertPortalTeamType(
        UNSCOPED_TEAM_PORTAL_TYPE,
        UNSCOPED_TEAM_PORTAL_TYPE_TITLE,
        UNSCOPED_TEAM_PORTAL_TYPE_DESCRIPTION,
        WITHOUT_SCOPE_SCOPED_WITHIN
    );

    insertPortalTeamType(
        SCOPED_TEAM_PORTAL_TYPE,
        SCOPED_TEAM_PORTAL_TYPE_TITLE,
        SCOPED_TEAM_PORTAL_TYPE_DESCRIPTION,
        WITH_SCOPE_SCOPED_WITHIN
    );

  }

  private void insertPortalTeamInstanceUsage(int resId, String uref, String purpose) {
    entityManager.createNativeQuery(
        "INSERT INTO portal_resource_usages_current (" +
            "  res_id " +
            ", uref " +
            ", purpose) " +
            "VALUES (:res_id " +
            ", :uref " +
            ", :purpose)"
        )
        .setParameter("res_id", resId)
        .setParameter("uref", uref)
        .setParameter("purpose", purpose)
        .executeUpdate();
  }

  private void insertPortalTeamTypeRole(String type, String name, String title, String desc, int minMems, int maxMems,
                                        int displaySequence) {
    entityManager.createNativeQuery(
        "INSERT INTO portal_resource_type_roles (" +
            "  res_type " +
            ", role_name " +
            ", role_title " +
            ", role_description " +
            ", min_mems " +
            ", max_mems " +
            ", display_seq" +
            ") " +
            "VALUES ( :res_type " +
            ", :role_name " +
            ", :role_title " +
            ", :role_description " +
            ", :min_mems " +
            ", :max_mems " +
            ", :display_seq) "
        )
        .setParameter("res_type", type)
        .setParameter("role_name", name)
        .setParameter("role_title", title)
        .setParameter("role_description", desc)
        .setParameter("min_mems", minMems)
        .setParameter("max_mems", maxMems)
        .setParameter("display_seq", displaySequence)
        .executeUpdate();
  }


  private void insertExamplePortalTeamTypeRole(String teamType, ExampleTeamRole exampleTeamRole) {
    insertPortalTeamTypeRole(
        teamType,
        exampleTeamRole.name(),
        exampleTeamRole.getTitle(),
        exampleTeamRole.getDesc(),
        exampleTeamRole.getMinMembers(),
        exampleTeamRole.getMaxMembers(),
        exampleTeamRole.ordinal());

    insertRolePriv(teamType, exampleTeamRole);
  }

  private void insertPortalTeamTypeRoles() {
    insertExamplePortalTeamTypeRole(UNSCOPED_TEAM_PORTAL_TYPE, ExampleTeamRole.ROLE_WITH_PRIV);
    insertExamplePortalTeamTypeRole(UNSCOPED_TEAM_PORTAL_TYPE, ExampleTeamRole.ROLE_WITHOUT_PRIV);

    insertExamplePortalTeamTypeRole(SCOPED_TEAM_PORTAL_TYPE, ExampleTeamRole.ROLE_WITH_PRIV);
    insertExamplePortalTeamTypeRole(SCOPED_TEAM_PORTAL_TYPE, ExampleTeamRole.ROLE_WITHOUT_PRIV);

  }

  private void insertPortalTeamInstancesAndUsages() {
    insertPortalTeamInstance(UNSCOPED_TEAM_RES_ID, UNSCOPED_TEAM_PORTAL_TYPE, UNSCOPED_TEAM_NAME,
        UNSCOPED_TEAM_DESCRIPTION);

    insertPortalTeamInstance(SCOPED_TEAM_RES_ID, SCOPED_TEAM_PORTAL_TYPE, SCOPED_TEAM_NAME, SCOPED_TEAM_DESCRIPTION);
    insertPortalTeamInstanceUsage(SCOPED_TEAM_RES_ID, SCOPED_TEAM_UREF, PortalTeamUsagePurpose.PRIMARY_DATA.name());

    insertPortalTeamInstance(
        NO_MEMBER_SCOPED_TEAM_RES_ID,
        SCOPED_TEAM_PORTAL_TYPE,
        NO_MEMBER_SCOPED_TEAM_NAME,
        NO_MEMBER_SCOPED_TEAM_DESCRIPTION
    );
    insertPortalTeamInstanceUsage(
        NO_MEMBER_SCOPED_TEAM_RES_ID,
        NO_MEMBER_SCOPED_TEAM_UREF,
        PortalTeamUsagePurpose.PRIMARY_DATA.name()
    );

  }

  private void insertPortalTeamType(String type, String title, String desc, String scopedWithin) {
    entityManager.createNativeQuery(
        "INSERT INTO portal_resource_types (" +
            "  res_type " +
            ", res_type_title " +
            ", res_type_description " +
            ", scoped_within) " +
            "VALUES ( :type" +
            ", :title " +
            ", :desc " +
            ", :scopedWithin ) "
        )
        .setParameter("type", type)
        .setParameter("title", title)
        .setParameter("desc", desc)
        .setParameter("scopedWithin", scopedWithin)
        .executeUpdate();
  }

  private void insertPortalTeamInstance(int resId, String type, String name, String desc) {
    entityManager.createNativeQuery(
        "INSERT INTO portal_resources (res_id, res_type, res_name, description) VALUES (" +
            "  :res_id" +
            ", :res_type" +
            ", :res_name" +
            ", :desc)"
        )
        .setParameter("res_id", resId)
        .setParameter("res_type", type)
        .setParameter("res_name", name)
        .setParameter("desc", desc)
        .executeUpdate();
  }

  private String constructOrgGroupUref(int id) {
    return id + PortalOrganisationGroup.UREF_TYPE;
  }

}
