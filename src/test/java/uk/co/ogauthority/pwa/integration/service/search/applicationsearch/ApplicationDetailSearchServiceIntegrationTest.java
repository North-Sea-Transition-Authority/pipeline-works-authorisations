package uk.co.ogauthority.pwa.integration.service.search.applicationsearch;


import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Set;
import javax.persistence.EntityManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitId;
import uk.co.ogauthority.pwa.model.entity.enums.HuooRole;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaTestUtil;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailView;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailViewTestUtil;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.PadVersionLookup;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.PadVersionLookupTestUtil;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsent;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentOrganisationRoleTestUtil;
import uk.co.ogauthority.pwa.model.entity.pwaconsents.PwaConsentTestUtil;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.users.UserType;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationDetailSearchService;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchContext;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchContextTestUtil;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchParameters;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchParametersBuilder;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureDataJpa
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles("integration-test")
@SuppressWarnings({"JpaQueryApiInspection", "SqlNoDataSourceInspection"})
// IJ seems to give spurious warnings when running with embedded H2
public class ApplicationDetailSearchServiceIntegrationTest {

  private static final OrganisationUnitId USER_HOLDER_ORG_UNIT_ID = new OrganisationUnitId(10);
  private static final OrganisationUnitId OTHER_HOLDER_ORG_UNIT_ID = new OrganisationUnitId(20);

  private static final int APP1_ID = 1;
  private static final int APP2_ID = 2;
  private static final int APP3_ID = 3;

  private static final int VERSION1 = 1;
  private static final int VERSION2 = 2;

  private final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

  private ApplicationDetailView app1Version1;
  private ApplicationDetailView app2Version1;
  private ApplicationDetailView app2Version2;
  private ApplicationDetailView app3Version1;

  private PadVersionLookup app1VersionLookup;
  private PadVersionLookup app2VersionLookup;
  private PadVersionLookup app3VersionLookup;

  private int pwa1_id;
  private int pwa2_id;

  private MasterPwa pwa1;
  private PwaConsent pwa1Consent;

  private MasterPwa pwa2;
  private PwaConsent pwa2Consent;

  private ApplicationSearchContext searchContext;
  private ApplicationSearchParameters searchParams;

  @Autowired
  private ApplicationDetailSearchService applicationDetailSearchService;

  @Autowired
  private EntityManager entityManager;

  public void createDefaultAppDetailViews() {
    app1Version1 = ApplicationDetailViewTestUtil.createDraftDetailView(
        pwa1_id, PwaApplicationType.INITIAL, APP1_ID, 10, VERSION1, true
    );
    app1VersionLookup = PadVersionLookupTestUtil.createLookupForDraftOnlyApp(APP1_ID);

    app2Version1 = ApplicationDetailViewTestUtil.createSubmittedReviewDetailView(
        pwa1_id, PwaApplicationType.CAT_1_VARIATION, APP2_ID, 20, VERSION1, false, clock.instant().minusSeconds(1000)
    );
    app2Version2 = ApplicationDetailViewTestUtil.createSubmittedReviewDetailView(
        pwa1_id, PwaApplicationType.CAT_2_VARIATION, APP2_ID, 30, VERSION2, true, clock.instant()
    );
    // only submitted - no ongoing update - not accepted
    app2VersionLookup = PadVersionLookupTestUtil.createLookupForSubmittedApp(
        APP2_ID,
        clock.instant(),
        null,
        null
    );

    app3Version1 = ApplicationDetailViewTestUtil.createDraftDetailView(
        pwa2_id, PwaApplicationType.HUOO_VARIATION, APP3_ID, 40, VERSION1, true
    );
    app3VersionLookup = PadVersionLookupTestUtil.createLookupForDraftOnlyApp(APP3_ID);

  }

  public void persistAppDetailViews() {

    entityManager.persist(app1Version1);
    entityManager.persist(app2Version1);
    entityManager.persist(app2Version2);
    entityManager.persist(app3Version1);

    entityManager.persist(app1VersionLookup);
    entityManager.persist(app2VersionLookup);
    entityManager.persist(app3VersionLookup);
  }

  private ApplicationSearchContext getRegulatorContext() {
    return ApplicationSearchContextTestUtil.emptyUserContext(
        new AuthenticatedUserAccount(
            new WebUserAccount(),
            Collections.emptySet()
        ),
        UserType.OGA
    );
  }

  private ApplicationSearchContext getIndustryContext(OrganisationUnitId organisationUnitId) {
    return ApplicationSearchContextTestUtil.industryContext(
        new AuthenticatedUserAccount(
            new WebUserAccount(),
            Collections.emptySet()
        ),
        Set.of(organisationUnitId)
    );
  }

  @Transactional
  @Test
  /**
   * Test only submitted apps are returned for regulator users.
   */
  public void search_whenRegulatorUser_unfiltered_multipleSubmittedVersionsOfApp() {

    createDefaultAppDetailViews();
    persistAppDetailViews();

    searchContext = getRegulatorContext();
    searchParams = ApplicationSearchParametersBuilder.createEmptyParams();

    var results = applicationDetailSearchService.search(searchParams, searchContext);

    //assert null start date item is first
    assertThat(results).containsExactly(
        app2Version2
    );

  }

  private void setupDefaultIndustryData(){
    setupDefaultPwaConsentsAndHolder();
    createDefaultAppDetailViews();
    persistAppDetailViews();

  }

  @Transactional
  @Test
  /**
   * Test only submitted apps are returned for regulator users.
   */
  public void search_whenIndustryUser_unfiltered_noCurrentPwasHeldByOrgGroupOrgUnits() {

    setupDefaultIndustryData();

    searchContext = getIndustryContext(new OrganisationUnitId(9999));
    searchParams = ApplicationSearchParametersBuilder.createEmptyParams();

    var results = applicationDetailSearchService.search(searchParams, searchContext);

    //assert null start date item is first
    assertThat(results).isEmpty();

  }

  @Transactional
  @Test
  /**
   * Test only submitted apps are returned for regulator users.
   */
  public void search_whenIndustryUser_unfiltered_applicationsForPwaWhereUserInOrgGrpTeam_submittedAndDraftAppsExist() {

    setupDefaultIndustryData();

    searchContext = getIndustryContext(USER_HOLDER_ORG_UNIT_ID);
    searchParams = ApplicationSearchParametersBuilder.createEmptyParams();

    var results = applicationDetailSearchService.search(searchParams, searchContext);

    //assert null start date item is first
    assertThat(results).containsExactlyInAnyOrder(app1Version1, app2Version2);

  }


  private void setupDefaultPwaConsentsAndHolder() {
    pwa1 = MasterPwaTestUtil.create();
    pwa2 = MasterPwaTestUtil.create();

    entityManager.persist(pwa1);
    pwa1_id = pwa1.getId();
    entityManager.persist(pwa2);
    pwa2_id = pwa2.getId();

    pwa1Consent = PwaConsentTestUtil.createInitial(pwa1);
    pwa2Consent = PwaConsentTestUtil.createInitial(pwa2);

    entityManager.persist(pwa1Consent);
    entityManager.persist(pwa2Consent);

    var pwa1Holder = PwaConsentOrganisationRoleTestUtil.createOrganisationRole(
        pwa1Consent, USER_HOLDER_ORG_UNIT_ID, HuooRole.HOLDER
    );

    var pwa2Holder = PwaConsentOrganisationRoleTestUtil.createOrganisationRole(
        pwa2Consent, OTHER_HOLDER_ORG_UNIT_ID, HuooRole.HOLDER
    );

    entityManager.persist(pwa1Holder);
    entityManager.persist(pwa2Holder);
  }


}
