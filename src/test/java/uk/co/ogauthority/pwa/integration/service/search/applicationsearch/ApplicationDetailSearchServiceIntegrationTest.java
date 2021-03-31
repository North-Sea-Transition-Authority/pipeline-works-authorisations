package uk.co.ogauthority.pwa.integration.service.search.applicationsearch;


import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
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
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.model.dto.consultations.ConsulteeGroupId;
import uk.co.ogauthority.pwa.model.dto.organisations.OrganisationUnitId;
import uk.co.ogauthority.pwa.model.entity.appprocessing.consultations.consultees.ConsulteeGroup;
import uk.co.ogauthority.pwa.model.entity.consultations.ConsultationRequest;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwaTestUtil;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaAppAssignmentView;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailItemView;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailView;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailViewTestUtil;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.PadVersionLookup;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.PadVersionLookupTestUtil;
import uk.co.ogauthority.pwa.model.entity.search.consents.PwaHolderOrgUnitTestUtil;
import uk.co.ogauthority.pwa.model.view.search.SearchScreenView;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.users.UserType;
import uk.co.ogauthority.pwa.service.enums.workflow.assignment.WorkflowAssignment;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationDetailSearchService;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchContext;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchContextTestUtil;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchParameters;
import uk.co.ogauthority.pwa.service.search.applicationsearch.ApplicationSearchParametersBuilder;
import uk.co.ogauthority.pwa.testutils.PortalOrganisationTestUtils;

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
  // high valued default ids' so any new apps or app details generated within tests wont hit an id already in use.
  private static final int APP1_ID = 100;
  private static final int APP2_ID = 200;
  private static final int APP3_ID = 300;
  private static final int APP1_DETAIL_ID = 1000;
  private static final int APP2_V1_DETAIL_ID = 2000;
  private static final int APP2_V2_DETAIL_ID = 3000;
  private static final int APP3_V1_DETAIL_ID = 4000;

  private static final int VERSION1 = 1;
  private static final int VERSION2 = 2;

  public static final String APP_2_REFERENCE = "PAD/2";

  private final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

  private ApplicationDetailView app1Version1;
  private ApplicationDetailView app2Version1;
  private ApplicationDetailView app2Version2;
  private ApplicationDetailView app3Version1;

  private PadVersionLookup app1VersionLookup;
  private PadVersionLookup app2VersionLookup;
  private PadVersionLookup app3VersionLookup;

  private PortalOrganisationGroup portalOrgGroup1;
  private PortalOrganisationUnit portalOrgUnit1;

  private PortalOrganisationGroup portalOrgGroup2;
  private PortalOrganisationUnit portalOrgUnit2;

  private MasterPwa pwa1;

  private MasterPwa pwa2;

  private MasterPwa pwa3;// for initial PWA app testing

  private ConsulteeGroup consulteeGroup;
  private ConsultationRequest  consultationRequest;


  private ApplicationSearchContext searchContext;
  private ApplicationSearchParameters searchParams;

  @Autowired
  private ApplicationDetailSearchService applicationDetailSearchService;

  @Autowired
  private EntityManager entityManager;

  public void createDefaultAppDetailViews() {
    app1Version1 = ApplicationDetailViewTestUtil.createDraftDetailView(
        pwa1.getId(), PwaApplicationType.DEPOSIT_CONSENT, APP1_ID, APP1_DETAIL_ID, VERSION1, true
    );
    app1VersionLookup = PadVersionLookupTestUtil.createLookupForDraftOnlyApp(APP1_ID);

    app2Version1 = ApplicationDetailViewTestUtil.createSubmittedReviewDetailView(
        pwa1.getId(), PwaApplicationType.CAT_1_VARIATION, APP2_ID, APP2_V1_DETAIL_ID, VERSION1, false, clock.instant().minusSeconds(1000), null
    );
    app2Version2 = ApplicationDetailViewTestUtil.createSubmittedReviewDetailView(
        pwa1.getId(), PwaApplicationType.CAT_2_VARIATION, APP2_ID, APP2_V2_DETAIL_ID, VERSION2, true, clock.instant(), null
    );
    // only submitted - no ongoing update - not accepted
    app2VersionLookup = PadVersionLookupTestUtil.createLookupForSubmittedApp(
        APP2_ID,
        VERSION2,
        null,
        null
    );

    app3Version1 = ApplicationDetailViewTestUtil.createDraftDetailView(
        pwa2.getId(), PwaApplicationType.HUOO_VARIATION, APP3_ID, APP3_V1_DETAIL_ID, VERSION1, true
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

  private ApplicationSearchContext getIndustryAndOgaContext(OrganisationUnitId organisationUnitId) {
    return ApplicationSearchContextTestUtil.industryAndOgaContext(
        new AuthenticatedUserAccount(
            new WebUserAccount(),
            Collections.emptySet()
        ),
        Set.of(organisationUnitId)
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

  private ApplicationSearchContext getConsulteeContext(ConsulteeGroupId consulteeGroupId) {
    return ApplicationSearchContextTestUtil.consulteeContext(
        new AuthenticatedUserAccount(
            new WebUserAccount(),
            Collections.emptySet()
        ),
        Set.of(consulteeGroupId)
    );
  }

  private ApplicationDetailItemView createAndPersistViewFromAppDetail(PwaApplicationDetail pwaApplicationDetail){
    var detailView = ApplicationDetailViewTestUtil.createSubmittedReviewDetailView(
        pwaApplicationDetail.getMasterPwa().getId(),
        pwaApplicationDetail.getPwaApplicationType(),
        pwaApplicationDetail.getPwaApplication().getId(),
        pwaApplicationDetail.getId(),
        pwaApplicationDetail.getVersionNo(),
        pwaApplicationDetail.isTipFlag(),
        pwaApplicationDetail.getSubmittedTimestamp(),
        pwaApplicationDetail.getConfirmedSatisfactoryTimestamp()
    );

    entityManager.persist(detailView);
    return detailView;
  }

  private void setupDefaultPwaConsentsAndHolderOrgs() {
    portalOrgGroup1 = PortalOrganisationTestUtils.generateOrganisationGroup(1, "ORG GRP 1", "OG1");
    entityManager.persist(portalOrgGroup1);
    portalOrgUnit1 = PortalOrganisationTestUtils.generateOrganisationUnit(USER_HOLDER_ORG_UNIT_ID.asInt(), "ou1", portalOrgGroup1);
    entityManager.persist(portalOrgUnit1);

    portalOrgGroup2 = PortalOrganisationTestUtils.generateOrganisationGroup(2, "ORG GRP 2", "OG2");
    entityManager.persist(portalOrgGroup2);
    portalOrgUnit2 = PortalOrganisationTestUtils.generateOrganisationUnit(OTHER_HOLDER_ORG_UNIT_ID.asInt(), "ou2", portalOrgGroup2);
    entityManager.persist(portalOrgUnit2);

    pwa1 = MasterPwaTestUtil.create();
    pwa2 = MasterPwaTestUtil.create();
    pwa3 = MasterPwaTestUtil.create();

    entityManager.persist(pwa1);
    entityManager.persist(pwa2);
    entityManager.persist(pwa3); // for initial PWA app

    var pwa1Holder = PwaHolderOrgUnitTestUtil.createPwaHolderOrgUnit("pwa1-org1", pwa1.getId(), portalOrgUnit1);

    var pwa2Holder = PwaHolderOrgUnitTestUtil.createPwaHolderOrgUnit("pwa2-org2", pwa2.getId(), portalOrgUnit2);

    entityManager.persist(pwa1Holder);
    entityManager.persist(pwa2Holder);
  }

  private void setupDefaultData(){
    setupDefaultPwaConsentsAndHolderOrgs();
    createDefaultAppDetailViews();
    persistAppDetailViews();

  }

  /**
   * Test only submitted apps are returned for regulator users.
   */
  @Transactional
  @Test
  public void search_whenRegulatorUser_unfiltered_multipleSubmittedVersionsOfApp() {

    setupDefaultData();

    searchContext = getRegulatorContext();
    searchParams = ApplicationSearchParametersBuilder.createEmptyParams();

    var result = applicationDetailSearchService.search(searchParams, searchContext);

    var screenView = new SearchScreenView<>(1, List.of(app2Version2));

    assertThat(result).isEqualTo(screenView);

  }

  @Transactional
  @Test
  public void search_whenRegulatorUser_applicationReferenceFilter_partialMatch() {
    setupDefaultPwaConsentsAndHolderOrgs();
    createDefaultAppDetailViews();
    app2Version1.setPadReference(APP_2_REFERENCE);
    app2Version2.setPadReference(APP_2_REFERENCE);
    persistAppDetailViews();

    searchContext = getRegulatorContext();
    searchParams = new ApplicationSearchParametersBuilder()
        .setAppReference("d/2")
        .createApplicationSearchParameters();

    var result = applicationDetailSearchService.search(searchParams, searchContext);

    var screenView = new SearchScreenView<ApplicationDetailItemView>(1, List.of(app2Version2));

    assertThat(result).isEqualTo(screenView);

  }

  @Transactional
  @Test
  public void search_whenRegulatorUser_applicationReferenceFilter_fullMatch() {
    setupDefaultPwaConsentsAndHolderOrgs();
    createDefaultAppDetailViews();
    app2Version1.setPadReference(APP_2_REFERENCE);
    app2Version2.setPadReference(APP_2_REFERENCE);
    persistAppDetailViews();

    searchContext = getRegulatorContext();
    searchParams = new ApplicationSearchParametersBuilder()
        .setAppReference(APP_2_REFERENCE)
        .createApplicationSearchParameters();

    var results = applicationDetailSearchService.search(searchParams, searchContext);

    var screenView = new SearchScreenView<ApplicationDetailItemView>(1, List.of(app2Version2));

    assertThat(results).isEqualTo(screenView);

  }

  @Transactional
  @Test
  public void search_whenRegulatorUser_applicationReferenceFilter_noMatch() {
    setupDefaultPwaConsentsAndHolderOrgs();
    createDefaultAppDetailViews();
    app2Version1.setPadReference(APP_2_REFERENCE);
    app2Version2.setPadReference(APP_2_REFERENCE);
    persistAppDetailViews();

    searchContext = getRegulatorContext();
    searchParams = new ApplicationSearchParametersBuilder()
        .setAppReference("PAD/30")
        .createApplicationSearchParameters();

    var result = applicationDetailSearchService.search(searchParams, searchContext);

    var screenView = new SearchScreenView<ApplicationDetailItemView>(0, List.of());

    assertThat(result).isEqualTo(screenView);

  }


  @Transactional
  @Test
  public void search_whenIndustryUser_unfiltered_noCurrentPwasHeldByOrgGroupOrgUnits() {

    setupDefaultData();

    searchContext = getIndustryContext(new OrganisationUnitId(9999));
    searchParams = ApplicationSearchParametersBuilder.createEmptyParams();

    var results = applicationDetailSearchService.search(searchParams, searchContext);

    var screenView = new SearchScreenView<>(0, List.of());

    assertThat(results).isEqualTo(screenView);

  }

  @Transactional
  @Test
  public void search_whenIndustryUser_unfiltered_applicationsForPwaWhereUserInOrgGrpTeam_submittedAndUpdateAppsExist() {

    setupDefaultData();

    searchContext = getIndustryContext(USER_HOLDER_ORG_UNIT_ID);
    searchParams = ApplicationSearchParametersBuilder.createEmptyParams();

    var result = applicationDetailSearchService.search(searchParams, searchContext);

    var screenView = new SearchScreenView<>(2, List.of(app2Version2, app1Version1));

    assertThat(result).isEqualTo(screenView);

  }

  /**
   * Test that submitted initial PWA apps appear before they are consented
   */
  @Transactional
  @Test
  public void search_whenIndustryUser_unfiltered_submittedInitialPwaAppExists() {

    setupDefaultData();
    searchContext = getIndustryContext(USER_HOLDER_ORG_UNIT_ID);
    searchParams = ApplicationSearchParametersBuilder.createEmptyParams();
    var appId = 99999;
    var appDetailId = 99999;
    var app = new PwaApplication(pwa3, PwaApplicationType.INITIAL, 0);
    entityManager.persist(app);
    var appDetail = new PwaApplicationDetail(app, VERSION1, 1, clock.instant());
    appDetail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);
    appDetail.setSubmittedTimestamp(clock.instant());
    entityManager.persist(appDetail);
    var pwa3InitialAppHolderOrgUnit = PwaHolderOrgUnitTestUtil.createPwaHolderOrgUnit("org1CompId", pwa3.getId(), portalOrgUnit1);
    entityManager.persist(pwa3InitialAppHolderOrgUnit);

    var initialAppDetailView = createAndPersistViewFromAppDetail(appDetail);
    var initialAppLookup = PadVersionLookupTestUtil.createLookupForSubmittedApp(
        appDetail.getMasterPwaApplicationId(),
        VERSION1,
        null,
        null
    );
    entityManager.persist(initialAppLookup);

   var result = applicationDetailSearchService.search(searchParams, searchContext);

   var screenView = new SearchScreenView<>(3, List.of(app2Version2, app1Version1, initialAppDetailView));

    assertThat(result).isEqualTo(screenView);

  }

  @Transactional
  @Test
  public void search_whenIndustryUser_andOGAUser_unfiltered_draftInitialPwaAppExistsForDifferentHolder() {

    setupDefaultData();
    searchContext = getIndustryAndOgaContext(new OrganisationUnitId(portalOrgUnit1.getOuId()));
    searchParams = ApplicationSearchParametersBuilder.createEmptyParams();

    var app = new PwaApplication(pwa3, PwaApplicationType.INITIAL, 0);
    entityManager.persist(app);
    var appDetail = new PwaApplicationDetail(app, VERSION1, 1, clock.instant());
    appDetail.setStatus(PwaApplicationStatus.DRAFT);
    entityManager.persist(appDetail);
    var pwa3InitialAppHolderOrgUnit = PwaHolderOrgUnitTestUtil.createPwaHolderOrgUnit("org1CompId", pwa3.getId(), portalOrgUnit2);
    entityManager.persist(pwa3InitialAppHolderOrgUnit);

    var initialAppDetailView = createAndPersistViewFromAppDetail(appDetail);
    var initialAppLookup = PadVersionLookupTestUtil.createLookupForDraftOnlyApp(
        appDetail.getMasterPwaApplicationId()
    );
    entityManager.persist(initialAppLookup);

    var result = applicationDetailSearchService.search(searchParams, searchContext);

    var screenView = new SearchScreenView<>(2, List.of(app2Version2, app1Version1));

    assertThat(result).isEqualTo(screenView);
  }

  @Transactional
  @Test
  public void search_whenIndustryUser_andOGAUser_unfiltered_draftInitialPwaAppExistsForUserHolderOrg() {

    setupDefaultData();
    searchContext = getIndustryAndOgaContext(new OrganisationUnitId(portalOrgUnit1.getOuId()));
    searchParams = ApplicationSearchParametersBuilder.createEmptyParams();

    var app = new PwaApplication(pwa3, PwaApplicationType.INITIAL, 0);
    entityManager.persist(app);
    var appDetail = new PwaApplicationDetail(app, VERSION1, 1, clock.instant());
    appDetail.setStatus(PwaApplicationStatus.DRAFT);

    entityManager.persist(appDetail);
    var pwa3InitialAppHolderOrgUnit = PwaHolderOrgUnitTestUtil.createPwaHolderOrgUnit("org1CompId", pwa3.getId(), portalOrgUnit1);
    entityManager.persist(pwa3InitialAppHolderOrgUnit);

    var initialAppDetailView = createAndPersistViewFromAppDetail(appDetail);
    var initialAppLookup = PadVersionLookupTestUtil.createLookupForDraftOnlyApp(
        appDetail.getMasterPwaApplicationId()
    );
    entityManager.persist(initialAppLookup);

    var result = applicationDetailSearchService.search(searchParams, searchContext);

    var screenView = new SearchScreenView<>(3, List.of(app2Version2, app1Version1, initialAppDetailView));

    assertThat(result).isEqualTo(screenView);
  }

  /**
   * Test that submitted initial PWA apps appear before they are consented
   */
  @Transactional
  @Test
  public void search_whenIndustryUser_unfiltered_draftInitialPwaAppExists() {

    setupDefaultData();
    searchContext = getIndustryContext(USER_HOLDER_ORG_UNIT_ID);
    searchParams = ApplicationSearchParametersBuilder.createEmptyParams();

    var app = new PwaApplication(pwa3, PwaApplicationType.INITIAL, 0);
    entityManager.persist(app);
    var appDetail = new PwaApplicationDetail(app, VERSION1, 1, clock.instant());
    appDetail.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);
    appDetail.setSubmittedTimestamp(clock.instant());
    entityManager.persist(appDetail);
    var pwa3InitialAppHolderOrgUnit = PwaHolderOrgUnitTestUtil.createPwaHolderOrgUnit("org1CompId", pwa3.getId(), portalOrgUnit1);
    entityManager.persist(pwa3InitialAppHolderOrgUnit);

    var initialAppDetailView = createAndPersistViewFromAppDetail(appDetail);
    var initialAppLookup = PadVersionLookupTestUtil.createLookupForDraftOnlyApp(appDetail.getMasterPwaApplicationId());
    entityManager.persist(initialAppLookup);

    var result = applicationDetailSearchService.search(searchParams, searchContext);

    var screenView = new SearchScreenView<>(3, List.of(app2Version2, app1Version1, initialAppDetailView));

    assertThat(result).isEqualTo(screenView);

  }

  /**
   * Test that only last confirmed satisfactory version of applications the consultee user's group was consulted upon are returned.
   */
  @Transactional
  @Test
  public void search_whenConsulteeUser_unfiltered_limitedToApplicationsConsultedOn_lastSatisfactoryVersion() {

    setupDefaultData();
    // setup app to link to consultation request
    var consultedOnApp = new PwaApplication(pwa3, PwaApplicationType.INITIAL, 0);
    entityManager.persist(consultedOnApp);
    // have 2 submitted versions of app, one is satisfactory, one just submitted
    var appDetailVersion1IsSatisfactory = new PwaApplicationDetail(consultedOnApp, VERSION1, 1, clock.instant());
    appDetailVersion1IsSatisfactory.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);
    appDetailVersion1IsSatisfactory.setSubmittedTimestamp(clock.instant());
    appDetailVersion1IsSatisfactory.setConfirmedSatisfactoryTimestamp(clock.instant());
    appDetailVersion1IsSatisfactory.setTipFlag(false);

    var appDetailVersion2SubmittedOnly = new PwaApplicationDetail(consultedOnApp, VERSION2, 1, clock.instant());
    appDetailVersion2SubmittedOnly.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);
    appDetailVersion2SubmittedOnly.setSubmittedTimestamp(clock.instant());

    entityManager.persist(appDetailVersion1IsSatisfactory);
    entityManager.persist(appDetailVersion2SubmittedOnly);

    var v1IsSatisfactoryView = createAndPersistViewFromAppDetail(appDetailVersion1IsSatisfactory);
    // not used by needs to be persisted to test submitted version filtered out correctly
    var v2IsSubmittedView = createAndPersistViewFromAppDetail(appDetailVersion2SubmittedOnly);
    // version lookup to store latest confirmed satisfactory time
    var versionLookup = PadVersionLookupTestUtil.createLookupForSubmittedApp(
        appDetailVersion1IsSatisfactory.getMasterPwaApplicationId(),
        VERSION2,
        null,
        appDetailVersion1IsSatisfactory.getConfirmedSatisfactoryTimestamp()
    );

    entityManager.persist(versionLookup);

    // create consultee group and request linked to app
    consulteeGroup = new ConsulteeGroup();
    entityManager.persist(consulteeGroup);
    consultationRequest = new ConsultationRequest();
    consultationRequest.setPwaApplication(consultedOnApp);
    consultationRequest.setConsulteeGroup(consulteeGroup);
    entityManager.persist(consultationRequest);

    searchContext = getConsulteeContext(ConsulteeGroupId.from(consulteeGroup));
    searchParams = ApplicationSearchParametersBuilder.createEmptyParams();

    var result = applicationDetailSearchService.search(searchParams, searchContext);

    var screenView = new SearchScreenView<>(1, List.of(v1IsSatisfactoryView));

    assertThat(result).isEqualTo(screenView);

  }


  @Transactional
  @Test
  public void search_regulatorContext_emptySearchParam_completeAppDetailExistsForApp() {
    var app = new PwaApplication(pwa2, PwaApplicationType.CAT_1_VARIATION, 0);
    entityManager.persist(app);
    var appDetail = new PwaApplicationDetail(app, 1, 1, clock.instant());
    appDetail.setStatus(PwaApplicationStatus.COMPLETE);
    entityManager.persist(appDetail);

    setupDefaultPwaConsentsAndHolderOrgs();
    createDefaultAppDetailViews();
    app2Version1.setPadReference(APP_2_REFERENCE);
    app2Version1.setPwaApplicationId(app.getId());
    app2Version2.setPadReference(APP_2_REFERENCE);
    app2Version2.setPwaApplicationId(app.getId());
    persistAppDetailViews();

    searchContext = getRegulatorContext();
    searchParams = ApplicationSearchParametersBuilder.createEmptyParams();

    var result = applicationDetailSearchService.search(searchParams, searchContext);

    var screenView = new SearchScreenView<ApplicationDetailItemView>(0, List.of());

    assertThat(result).isEqualTo(screenView);

  }

  @Transactional
  @Test
  public void search_regulatorContext_includeCompletedWithdrawnApps_completeAppDetailExistsForApp() {
    var app = new PwaApplication(pwa2, PwaApplicationType.CAT_1_VARIATION, 0);
    entityManager.persist(app);
    var appDetail = new PwaApplicationDetail(app, 1, 1, clock.instant());
    appDetail.setStatus(PwaApplicationStatus.COMPLETE);
    entityManager.persist(appDetail);

    setupDefaultPwaConsentsAndHolderOrgs();
    createDefaultAppDetailViews();
    app2Version1.setPadReference(APP_2_REFERENCE);
    app2Version1.setPwaApplicationId(app.getId());
    app2Version2.setPadReference(APP_2_REFERENCE);
    app2Version2.setPwaApplicationId(app.getId());
    app2VersionLookup.setPwaApplicationId(app.getId());
    persistAppDetailViews();

    searchContext = getRegulatorContext();
    searchParams = new ApplicationSearchParametersBuilder()
        .includeCompletedOrWithdrawnApps(true)
        .createApplicationSearchParameters();

    var result = applicationDetailSearchService.search(searchParams, searchContext);

    var screenView = new SearchScreenView<ApplicationDetailItemView>(1, List.of(app2Version2));

    assertThat(result).isEqualTo(screenView);

  }

  @Transactional
  @Test
  public void search_regulatorContext_filterByCaseOfficer_completeAppDetailExistsForApp() {
    var app = new PwaApplication(pwa2, PwaApplicationType.CAT_1_VARIATION, 0);
    entityManager.persist(app);
    var appDetail = new PwaApplicationDetail(app, 1, 1, clock.instant());
    appDetail.setStatus(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW);
    entityManager.persist(appDetail);

    var caseOfficerPersonId = 123;

    setupDefaultPwaConsentsAndHolderOrgs();
    createDefaultAppDetailViews();
    app2Version1.setPadReference(APP_2_REFERENCE);
    app2Version1.setPwaApplicationId(app.getId());
    app2Version1.setCaseOfficerPersonId(123);
    app2Version2.setPadReference(APP_2_REFERENCE);
    app2Version2.setPwaApplicationId(app.getId());
    app2Version2.setCaseOfficerPersonId(123);
    app2VersionLookup.setPwaApplicationId(app.getId());
    persistAppDetailViews();

    var assignmentView = new PwaAppAssignmentView();
    assignmentView.setId(1);
    assignmentView.setPwaApplicationId(app2Version2.getPwaApplicationId());
    assignmentView.setAssignment(WorkflowAssignment.CASE_OFFICER);
    assignmentView.setAssigneePersonId(caseOfficerPersonId);
    entityManager.persist(assignmentView);


    searchContext = getRegulatorContext();
    searchParams = new ApplicationSearchParametersBuilder()
        .setCaseOfficerPersonId(caseOfficerPersonId)
        .createApplicationSearchParameters();

    var result = applicationDetailSearchService.search(searchParams, searchContext);

    var screenView = new SearchScreenView<ApplicationDetailItemView>(1, List.of(app2Version2));

    assertThat(result).isEqualTo(screenView);

  }

  @Transactional
  @Test
  public void search_regulatorContext_filterByCaseOfficer_noCaseOfficersAssignedToApps() {
    var app = new PwaApplication(pwa2, PwaApplicationType.CAT_1_VARIATION, 0);
    entityManager.persist(app);
    var appDetail = new PwaApplicationDetail(app, 1, 1, clock.instant());
    appDetail.setStatus(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW);
    entityManager.persist(appDetail);

    var caseOfficerPersonId = 123;
    var consulteeUserId = 456;

    setupDefaultPwaConsentsAndHolderOrgs();
    createDefaultAppDetailViews();
    app2Version1.setPadReference(APP_2_REFERENCE);
    app2Version1.setPwaApplicationId(app.getId());
    app2VersionLookup.setPwaApplicationId(app.getId());
    persistAppDetailViews();

    var assignmentView = new PwaAppAssignmentView();
    assignmentView.setId(1);
    assignmentView.setPwaApplicationId(app2Version1.getPwaApplicationId());
    assignmentView.setAssignment(WorkflowAssignment.CONSULTATION_RESPONDER);
    assignmentView.setAssigneePersonId(consulteeUserId);
    entityManager.persist(assignmentView);


    searchContext = getRegulatorContext();
    searchParams = new ApplicationSearchParametersBuilder()
        .setCaseOfficerPersonId(caseOfficerPersonId)
        .createApplicationSearchParameters();

    var result = applicationDetailSearchService.search(searchParams, searchContext);

    var screenView = new SearchScreenView<ApplicationDetailItemView>(0, List.of());

    assertThat(result).isEqualTo(screenView);

  }

  @Transactional
  @Test
  public void search_whenRegulatorUser_applicationTypeFilter_appTypeUnmatched() {
    setupDefaultPwaConsentsAndHolderOrgs();
    createDefaultAppDetailViews();
    persistAppDetailViews();

    searchContext = getRegulatorContext();
    searchParams = new ApplicationSearchParametersBuilder()
        .setPwaApplicationType(PwaApplicationType.OPTIONS_VARIATION)
        .createApplicationSearchParameters();

    var result = applicationDetailSearchService.search(searchParams, searchContext);

    var screenView = new SearchScreenView<ApplicationDetailItemView>(0, List.of());

    assertThat(result).isEqualTo(screenView);

  }

  @Transactional
  @Test
  public void search_whenRegulatorUser_applicationTypeFilter_appTypeMatched() {
    setupDefaultPwaConsentsAndHolderOrgs();
    createDefaultAppDetailViews();
    persistAppDetailViews();

    searchContext = getRegulatorContext();
    searchParams = new ApplicationSearchParametersBuilder()
        .setPwaApplicationType(PwaApplicationType.CAT_2_VARIATION)
        .createApplicationSearchParameters();

    var result = applicationDetailSearchService.search(searchParams, searchContext);

    var screenView = new SearchScreenView<ApplicationDetailItemView>(1, List.of(app2Version2));

    assertThat(result).isEqualTo(screenView);
  }

  @Transactional
  @Test
  public void search_whenIndustryUser_holderOrgUnitFilterMatches() {

    setupDefaultData();

    searchContext = getIndustryContext(USER_HOLDER_ORG_UNIT_ID);
    searchParams = new ApplicationSearchParametersBuilder()
      .setHolderOrgUnitId(USER_HOLDER_ORG_UNIT_ID.asInt())
      .createApplicationSearchParameters();

    var result = applicationDetailSearchService.search(searchParams, searchContext);

    var screenView = new SearchScreenView<>(2, List.of(app2Version2, app1Version1));

    assertThat(result).isEqualTo(screenView);

  }

  @Transactional
  @Test
  public void search_whenIndustryUser_holderOrgUnitFilterMatches_butNotForUser() {

    setupDefaultData();

    searchContext = getIndustryContext(USER_HOLDER_ORG_UNIT_ID);
    searchParams = new ApplicationSearchParametersBuilder()
        .setHolderOrgUnitId(OTHER_HOLDER_ORG_UNIT_ID.asInt())
        .createApplicationSearchParameters();

    var result = applicationDetailSearchService.search(searchParams, searchContext);

    var screenView = new SearchScreenView<>(0, List.of());

    assertThat(result).isEqualTo(screenView);

  }

}
