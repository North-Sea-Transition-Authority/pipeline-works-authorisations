package uk.co.ogauthority.pwa.integration.energyportal.teams;


import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.integrations.energyportal.people.internal.PersonRepository;
import uk.co.ogauthority.pwa.integrations.energyportal.teams.external.PortalSystemPrivilegeDto;
import uk.co.ogauthority.pwa.integrations.energyportal.teams.external.PortalTeamAccessor;
import uk.co.ogauthority.pwa.integrations.energyportal.teams.internal.entity.PortalTeamUsagePurpose;
import uk.co.ogauthority.pwa.model.teams.PwaTeamType;

// TODO: Remove in PWARE-60

// IJ seems to give spurious warnings when running with embedded H2
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureDataJpa
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles("integration-test")
@SuppressWarnings({"JpaQueryApiInspection", "SqlNoDataSourceInspection"})
class PortalTeamAccessorIntegrationTest {


  private final String WITH_SCOPE_SCOPED_WITHIN = "PARENT";
  private final String WITHOUT_SCOPE_SCOPED_WITHIN = "UNIVERSAL_SET";

  private final String SCOPED_TEAM_PORTAL_TYPE_TITLE = "Org1TeamTitle";
  private final String SCOPED_TEAM_PORTAL_TYPE_DESCRIPTION = "Org1TeamDescription";

  private final int UNSCOPED_TEAM_RES_ID = 100;
  private final String UNSCOPED_TEAM_PORTAL_TYPE = "UNSCOPED_TEAM_TYPE";
  private final String UNSCOPED_TEAM_PORTAL_TYPE_TITLE = "RegulatorTeamTitle";
  private final String UNSCOPED_TEAM_PORTAL_TYPE_DESCRIPTION = "RegulatorTeamDescription";
  private final String UNSCOPED_TEAM_NAME = "PwaRegulatorTeam";
  private final String UNSCOPED_TEAM_DESCRIPTION = "RegulatorTeamDescription";

  private final PortalOrganisationGroup PORTAL_ORGANISATION_GROUP =
      PortalOrganisationTestUtils.generateOrganisationGroup(1, "name", "short name");

  private final String SCOPED_TEAM_PORTAL_TYPE = PwaTeamType.ORGANISATION.getPortalTeamType();
  private final int SCOPED_TEAM_RES_ID = 200;
  private final String SCOPED_TEAM_NAME = "Org1Team";
  private final String SCOPED_TEAM_DESCRIPTION = "Org1TeamDescription";
  private final String SCOPED_TEAM_UREF = constructOrgGroupUref(PORTAL_ORGANISATION_GROUP.getOrgGrpId());

  private final int NO_MEMBER_SCOPED_TEAM_RES_ID = 300;
  private final String NO_MEMBER_SCOPED_TEAM_NAME = "Org2Team";
  private final String NO_MEMBER_SCOPED_TEAM_DESCRIPTION = "Org2TeamDescription";
  private final String NO_MEMBER_SCOPED_TEAM_UREF = constructOrgGroupUref(30);

  @Autowired
  private PersonRepository personRepository;

  @Autowired
  private EntityManager entityManager;

  private PortalTeamAccessor portalTeamAccessor;

  private Person unscopedTeamMemberPerson_2Roles;
  private Person scopedTeamMemberPerson_2Roles;
  private Person scopedTeamMemberPerson_1Role;

  @BeforeEach
  void setup() {

    portalTeamAccessor = new PortalTeamAccessor(entityManager);

    insertPerson(10);
    unscopedTeamMemberPerson_2Roles = personRepository.findById(10).orElse(null);
    insertPerson(20);
    scopedTeamMemberPerson_2Roles = personRepository.findById(20).orElse(null);
    insertPerson(30);
    scopedTeamMemberPerson_1Role = personRepository.findById(30).orElse(null);

    insertPortalOrganisationGroup(PORTAL_ORGANISATION_GROUP);

    insertPortalTeamTypes();
    insertPortalTeamTypeRoles();
    insertPortalTeamInstancesAndUsages();

    insertTeamMembers();

  }


  @Test
  @Transactional
  void getAllPortalSystemPrivilegesForPerson_returnsExpectedSystemPrivs_whenPersonIsRoleWithPriv(){
    List<PortalSystemPrivilegeDto> privilegeDtoList = portalTeamAccessor.getAllPortalSystemPrivilegesForPerson(unscopedTeamMemberPerson_2Roles);

    assertThat(privilegeDtoList).isNotEmpty().allMatch(dto -> {
      assertThat(dto.getRoleName() ).isEqualTo(ExampleTeamRole.ROLE_WITH_PRIV.name());
      assertThat(dto.getGrantedPrivilege() ).isEqualTo(ExampleTeamRole.ROLE_WITH_PRIV.getExampleRolePriv());
      assertThat(dto.getPortalTeamType() ).isEqualTo(UNSCOPED_TEAM_PORTAL_TYPE);
      return true;
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


  private void insertPortalOrganisationGroup(PortalOrganisationGroup portalOrganisationGroup) {
    entityManager.createNativeQuery(
        "INSERT INTO portal_organisation_groups (org_grp_id, name, short_name, uref_value) VALUES (" +
            "  :org_grp_id" +
            ", :name" +
            ", :short_name" +
            ", :uref_value)"
    )
        .setParameter("org_grp_id", portalOrganisationGroup.getOrgGrpId())
        .setParameter("name", portalOrganisationGroup.getName())
        .setParameter("short_name", portalOrganisationGroup.getShortName())
        .setParameter("uref_value", constructOrgGroupUref(portalOrganisationGroup.getOrgGrpId()))
        .executeUpdate();
  }

}
