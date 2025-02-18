package uk.co.ogauthority.pwa.integration.service.asbuilt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccountTestUtil;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationGroupStatus;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationWorkareaView;
import uk.co.ogauthority.pwa.model.entity.asbuilt.AsBuiltNotificationWorkareaViewTestUtil;
import uk.co.ogauthority.pwa.model.entity.search.consents.PwaHolderOrgUnit;
import uk.co.ogauthority.pwa.model.entity.search.consents.PwaHolderOrgUnitTestUtil;
import uk.co.ogauthority.pwa.model.teams.PwaOrganisationRole;
import uk.co.ogauthority.pwa.model.teams.PwaRegulatorRole;
import uk.co.ogauthority.pwa.repository.asbuilt.AsBuiltNotificationDtoRepositoryImpl;
import uk.co.ogauthority.pwa.service.teams.PwaHolderTeamService;
import uk.co.ogauthority.pwa.service.teams.PwaTeamService;
import uk.co.ogauthority.pwa.service.workarea.applications.WorkAreaPageServiceTestUtil;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureDataJpa
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("integration-test")
@SuppressWarnings({"JpaQueryApiInspection", "SqlNoDataSourceInspection"})
class AsBuiltNotificationWorkAreaIntegrationTest {

  @Autowired
  private AsBuiltNotificationDtoRepositoryImpl asBuiltNotificationDtoRepository;

  @Autowired
  private EntityManager entityManager;

  @MockBean
  private PwaHolderTeamService pwaHolderTeamService;

  @MockBean
  private PwaTeamService pwaTeamService;


  private final AuthenticatedUserAccount adminUser = AuthenticatedUserAccountTestUtil.createAllPrivUserAccount(1);
  private final AuthenticatedUserAccount industryUser = AuthenticatedUserAccountTestUtil.createAllPrivUserAccount(2);
  private final AuthenticatedUserAccount unrelatedUser = AuthenticatedUserAccountTestUtil.createNoPrivUserAccount(3);

  private AsBuiltNotificationWorkareaView view1;
  private AsBuiltNotificationWorkareaView view2;
  private AsBuiltNotificationWorkareaView view3;

  PortalOrganisationGroup group1 = PortalOrganisationTestUtils.generateOrganisationGroup(100, "Group A", "A");
  PortalOrganisationGroup group2 = PortalOrganisationTestUtils.generateOrganisationGroup(101, "Group B", "B");

  PortalOrganisationUnit portalOrganisationUnit1 = PortalOrganisationTestUtils.generateOrganisationUnit(50, "A Unit", group1);
  PortalOrganisationUnit portalOrganisationUnit2 = PortalOrganisationTestUtils.generateOrganisationUnit(51, "B Unit", group2);

  PwaHolderOrgUnit pwaHolderOrgUnit1;
  PwaHolderOrgUnit pwaHolderOrgUnit2;
  PwaHolderOrgUnit pwaHolderOrgUnit3;


  @BeforeEach
  void setup() {
    setupViews();
    setupPwaHolderUnits();
    persistItems();
    when(pwaTeamService.getPeopleWithRegulatorRole(PwaRegulatorRole.AS_BUILT_NOTIFICATION_ADMIN))
        .thenReturn(Set.of(adminUser.getLinkedPerson()));
  }

  @Transactional
  @Test
  void getAsBuiltNotifications_adminUser_getsAllNonCompleteAsBuiltNotifications() {
    when(pwaHolderTeamService.getPortalOrganisationGroupsWhereUserHasOrgRole(adminUser,
        PwaOrganisationRole.AS_BUILT_NOTIFICATION_SUBMITTER)).thenReturn(List.of(group1, group2));

    var result = asBuiltNotificationDtoRepository.findAllAsBuiltNotificationsForUser(
        adminUser,
        WorkAreaPageServiceTestUtil.getWorkAreaViewPageable(0, "deadlineDate")
    );

    assertThat(result.get()).containsExactly(
        view1, view3
    );
  }

  @Transactional
  @Test
  void getAsBuiltNotifications_industryUser_getsOnlyOwnOrganisationNonCompleteAsBuiltNotifications() {
    when(pwaHolderTeamService.getPortalOrganisationGroupsWhereUserHasOrgRole(industryUser,
        PwaOrganisationRole.AS_BUILT_NOTIFICATION_SUBMITTER)).thenReturn(List.of(group1));

    var result = asBuiltNotificationDtoRepository.findAllAsBuiltNotificationsForUser(
        industryUser,
        WorkAreaPageServiceTestUtil.getWorkAreaViewPageable(0, "deadlineDate")
    );

    assertThat(result.get()).containsExactly(view1);
  }

  @Transactional
  @Test
  void getAsBuiltNotifications_unrelatedUser_getsNoAsBuiltNotifications() {
    when(pwaHolderTeamService.getPortalOrganisationGroupsWhereUserHasOrgRole(unrelatedUser, PwaOrganisationRole.AS_BUILT_NOTIFICATION_SUBMITTER))
        .thenReturn(List.of());

    var result = asBuiltNotificationDtoRepository.findAllAsBuiltNotificationsForUser(
        industryUser,
        WorkAreaPageServiceTestUtil.getWorkAreaViewPageable(0, "deadlineDate")
    );

    assertThat(result.get()).isEmpty();
  }

  private void setupViews() {
    view1 = AsBuiltNotificationWorkareaViewTestUtil.createAsBuiltNotificationViewFrom(1, 10, "Antarctica project",
        AsBuiltNotificationGroupStatus.NOT_STARTED, LocalDate.of(2000, 10, 10));
    view2 = AsBuiltNotificationWorkareaViewTestUtil.createAsBuiltNotificationViewFrom(2, 20, "Canada project",
        AsBuiltNotificationGroupStatus.COMPLETE, LocalDate.of(2010, 10, 10));
    view3 = AsBuiltNotificationWorkareaViewTestUtil.createAsBuiltNotificationViewFrom(3, 30, "New Zealand project",
        AsBuiltNotificationGroupStatus.NOT_STARTED, LocalDate.of(2020, 10, 10));
  }

  private void setupPwaHolderUnits() {
    //link pwas to holder org units
    pwaHolderOrgUnit1 = PwaHolderOrgUnitTestUtil.createPwaHolderOrgUnit("90", view1.getPwaId(), portalOrganisationUnit1);
    pwaHolderOrgUnit2 = PwaHolderOrgUnitTestUtil.createPwaHolderOrgUnit("91", view2.getPwaId(), portalOrganisationUnit1);
    pwaHolderOrgUnit3 = PwaHolderOrgUnitTestUtil.createPwaHolderOrgUnit("92", view3.getPwaId(), portalOrganisationUnit2);
  }

  private void persistItems() {
    entityManager.persist(group1);
    entityManager.persist(group2);

    entityManager.persist(portalOrganisationUnit1);
    entityManager.persist(portalOrganisationUnit2);

    entityManager.persist(pwaHolderOrgUnit1);
    entityManager.persist(pwaHolderOrgUnit2);
    entityManager.persist(pwaHolderOrgUnit3);

    entityManager.persist(view1);
    entityManager.persist(view2);
    entityManager.persist(view3);
  }

}
