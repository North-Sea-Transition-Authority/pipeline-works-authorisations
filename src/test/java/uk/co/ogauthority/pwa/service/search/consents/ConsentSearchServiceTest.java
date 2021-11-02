package uk.co.ogauthority.pwa.service.search.consents;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationTestUtils;
import uk.co.ogauthority.pwa.integrations.energyportal.organisations.external.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.model.entity.masterpwas.MasterPwa;
import uk.co.ogauthority.pwa.model.entity.pipelines.Pipeline;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;
import uk.co.ogauthority.pwa.model.entity.search.consents.ConsentSearchItem;
import uk.co.ogauthority.pwa.model.entity.search.consents.PwaConsentView;
import uk.co.ogauthority.pwa.model.entity.search.consents.PwaHolderOrgGrp;
import uk.co.ogauthority.pwa.model.entity.search.consents.PwaHolderOrgUnitTestUtil;
import uk.co.ogauthority.pwa.model.search.consents.ConsentSearchContext;
import uk.co.ogauthority.pwa.model.search.consents.ConsentSearchParams;
import uk.co.ogauthority.pwa.model.view.search.SearchScreenView;
import uk.co.ogauthority.pwa.model.view.search.consents.ConsentSearchResultView;
import uk.co.ogauthority.pwa.repository.search.consents.ConsentSearchItemRepository;
import uk.co.ogauthority.pwa.service.enums.users.UserType;
import uk.co.ogauthority.pwa.service.search.consents.predicates.ConsentSearchPredicateProvider;
import uk.co.ogauthority.pwa.testutils.ConsentSearchItemTestUtils;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureDataJpa
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles("integration-test")
@SuppressWarnings({"JpaQueryApiInspection", "SqlNoDataSourceInspection"})
// IJ seems to give spurious warnings when running with embedded H2
public class ConsentSearchServiceTest {

  @Autowired
  private EntityManager entityManager;

  @Autowired
  private List<ConsentSearchPredicateProvider> predicateProviders;

  private ConsentSearchService consentSearchService;

  @Mock
  private ConsentSearchItemRepository consentSearchItemRepository;

  private ConsentSearchItem pwa1Shell, pwa3Bp, pwa2ShellWintershall;

  private List<String> pwa1ConsentRefs, pwa2ConsentRefs, pwa3ConsentRefs;

  private PortalOrganisationGroup shell, bp, wintershall;
  private PortalOrganisationUnit shellOrg1, shellOrg2, bpOrg, wintershallOrg;

  private AuthenticatedUserAccount industryUser = new AuthenticatedUserAccount(new WebUserAccount(1, PersonTestUtil.createDefaultPerson()), Set.of(
      PwaUserPrivilege.PWA_INDUSTRY, PwaUserPrivilege.PWA_CONSENT_SEARCH));

  private AuthenticatedUserAccount ogaUser = new AuthenticatedUserAccount(new WebUserAccount(2, PersonTestUtil.createPersonFrom(new PersonId(12))), Set.of(
      PwaUserPrivilege.PWA_REGULATOR, PwaUserPrivilege.PWA_CONSENT_SEARCH));

  @Before
  public void setUp() throws Exception {

    consentSearchService = new ConsentSearchService(entityManager, predicateProviders, consentSearchItemRepository);

    shell = PortalOrganisationTestUtils.generateOrganisationGroup(1, "SHELL", "S");
    bp = PortalOrganisationTestUtils.generateOrganisationGroup(2, "BP", "B");
    wintershall = PortalOrganisationTestUtils.generateOrganisationGroup(3, "WINTERSHALL", "W");

    shellOrg1 = PortalOrganisationTestUtils.generateOrganisationUnit(1, "ShellOrg1", shell);
    shellOrg2 = PortalOrganisationTestUtils.generateOrganisationUnit(2, "ShellOrg2", shell);
    bpOrg = PortalOrganisationTestUtils.generateOrganisationUnit(3, "BpOrg1", bp);
    wintershallOrg = PortalOrganisationTestUtils.generateOrganisationUnit(4, "WinterOrg", wintershall);

    pwa1ConsentRefs = List.of("1/V/20", "2/V/21");
    pwa2ConsentRefs = List.of("5/W/99", "5/W/96");
    pwa3ConsentRefs = List.of("10/D/01", "12/V/99");

  }

  @Test
  @Transactional
  public void search_ogaUser_unrestricted_sortedByIdDesc() {

    setUpSearchData();

    var context = new ConsentSearchContext(ogaUser, UserType.OGA);

    var result = consentSearchService.search(new ConsentSearchParams(), context);

    // sorted id desc
    var resultViewComparisonList = List.of(
        ConsentSearchResultView.fromSearchItem(pwa3Bp),
        ConsentSearchResultView.fromSearchItem(pwa2ShellWintershall),
        ConsentSearchResultView.fromSearchItem(pwa1Shell)
    );

    var screenView = new SearchScreenView<>(3, resultViewComparisonList);

    assertThat(result).isEqualTo(screenView);
    assertThat(result.resultsHaveBeenLimited()).isFalse();

  }

  @Test
  @Transactional
  public void search_industryUser_restrictedOrgGrpsReturned_sortedByIdDesc() {

    setUpSearchData();

    var context = new ConsentSearchContext(industryUser, UserType.INDUSTRY);
    context.setOrgGroupIdsUserInTeamFor(Set.of(shell.getOrgGrpId()));

    var result = consentSearchService.search(new ConsentSearchParams(), context);

    // sorted id desc
    var resultViewComparisonList = List.of(
        ConsentSearchResultView.fromSearchItem(pwa2ShellWintershall),
        ConsentSearchResultView.fromSearchItem(pwa1Shell)
    );

    var screenView = new SearchScreenView<>(2, resultViewComparisonList);

    assertThat(result).isEqualTo(screenView);
    assertThat(result.resultsHaveBeenLimited()).isFalse();

  }

  @Test
  @Transactional
  public void search_industryUser_filterByOrgUnitId_userInOrgGrp_filteredAndReturned() {

    setUpSearchData();

    var context = new ConsentSearchContext(industryUser, UserType.INDUSTRY);
    context.setOrgGroupIdsUserInTeamFor(Set.of(shell.getOrgGrpId()));

    var params = new ConsentSearchParams();
    params.setHolderOrgUnitId(shellOrg1.getOuId());
    var result = consentSearchService.search(params, context);

    var resultViewComparisonList = List.of(ConsentSearchResultView.fromSearchItem(pwa1Shell));

    var screenView = new SearchScreenView<>(1, resultViewComparisonList);

    assertThat(result).isEqualTo(screenView);
    assertThat(result.resultsHaveBeenLimited()).isFalse();

  }

  @Test
  @Transactional
  public void search_industryUser_filterByOrgUnitId_userNotInOrgGrp_notReturned() {

    setUpSearchData();

    var context = new ConsentSearchContext(industryUser, UserType.INDUSTRY);
    context.setOrgGroupIdsUserInTeamFor(Set.of(shell.getOrgGrpId()));

    var params = new ConsentSearchParams();
    params.setHolderOrgUnitId(bpOrg.getOuId());
    var result = consentSearchService.search(params, context);

    var screenView = new SearchScreenView<>(0, List.of());

    assertThat(result).isEqualTo(screenView);
    assertThat(result.resultsHaveBeenLimited()).isFalse();

  }

  @Test
  @Transactional
  public void search_ogaUser_filterByOrgUnitId_filteredAndReturned() {

    setUpSearchData();

    var context = new ConsentSearchContext(ogaUser, UserType.OGA);

    var params = new ConsentSearchParams();
    params.setHolderOrgUnitId(bpOrg.getOuId());
    var result = consentSearchService.search(params, context);

    var resultViewComparisonList = List.of(ConsentSearchResultView.fromSearchItem(pwa3Bp));

    var screenView = new SearchScreenView<>(1, resultViewComparisonList);

    assertThat(result).isEqualTo(screenView);
    assertThat(result.resultsHaveBeenLimited()).isFalse();

  }

  @Test
  @Transactional
  public void search_filterByConsentReference_smallRefFragment_filteredAndReturned() {

    setUpSearchData();

    var context = new ConsentSearchContext(ogaUser, UserType.OGA);

    var params = new ConsentSearchParams();
    params.setConsentReference("1");
    var result = consentSearchService.search(params, context);

    var resultViewComparisonList = List.of(ConsentSearchResultView.fromSearchItem(pwa1Shell), ConsentSearchResultView.fromSearchItem(pwa3Bp));

    var screenView = new SearchScreenView<>(2, resultViewComparisonList);

    assertThat(result.getFullResultCount()).isEqualTo(screenView.getFullResultCount());
    assertThat(result.getSearchResults()).containsExactlyInAnyOrderElementsOf(screenView.getSearchResults());
    assertThat(result.resultsHaveBeenLimited()).isFalse();

  }

  @Test
  @Transactional
  public void search_filterByConsentReference_fullRefFragment_filteredAndReturned() {

    setUpSearchData();

    var context = new ConsentSearchContext(ogaUser, UserType.OGA);

    var params = new ConsentSearchParams();
    params.setConsentReference("5/W/96");
    var result = consentSearchService.search(params, context);

    var resultViewComparisonList = List.of(ConsentSearchResultView.fromSearchItem(pwa2ShellWintershall));

    var screenView = new SearchScreenView<>(1, resultViewComparisonList);

    assertThat(result).isEqualTo(screenView);
    assertThat(result.resultsHaveBeenLimited()).isFalse();

  }

  @Test
  @Transactional
  public void search_filterByConsentReference_nothingFound() {

    setUpSearchData();

    var context = new ConsentSearchContext(ogaUser, UserType.OGA);

    var params = new ConsentSearchParams();
    params.setConsentReference("ABC");
    var result = consentSearchService.search(params, context);

    var resultViewComparisonList = List.of(ConsentSearchResultView.fromSearchItem(pwa2ShellWintershall));

    var screenView = new SearchScreenView<>(0, List.of());

    assertThat(result).isEqualTo(screenView);
    assertThat(result.resultsHaveBeenLimited()).isFalse();

  }


  @Test
  @Transactional
  public void search_filterByPipelineReference_likeMatch_filteredAndReturned() {

    var context = new ConsentSearchContext(ogaUser, UserType.OGA);
    var params = new ConsentSearchParams();
    params.setPipelineReference("pl1717");

    setupPipelineRefSearchData(params.getPipelineReference().toUpperCase());

    var result = consentSearchService.search(params, context);
    var resultViewComparisonList = List.of(ConsentSearchResultView.fromSearchItem(pwa1Shell));

    var screenView = new SearchScreenView<>(1, resultViewComparisonList);
    assertThat(result).isEqualTo(screenView);
  }

  @Test
  @Transactional
  public void search_filterByPipelineReference_exactMatch_filteredAndReturned() {

    var context = new ConsentSearchContext(ogaUser, UserType.OGA);
    var params = new ConsentSearchParams();
    params.setPipelineReference("PL1717");

    setupPipelineRefSearchData(params.getPipelineReference());

    var result = consentSearchService.search(params, context);
    var resultViewComparisonList = List.of(ConsentSearchResultView.fromSearchItem(pwa1Shell));

    var screenView = new SearchScreenView<>(1, resultViewComparisonList);
    assertThat(result).isEqualTo(screenView);
  }

  @Test
  @Transactional
  public void search_filterByPipelineReference_searchAndPersistedRefAreDifferent_nothingFound() {

    var context = new ConsentSearchContext(ogaUser, UserType.OGA);
    var params = new ConsentSearchParams();
    params.setPipelineReference("PL1184");

    setupPipelineRefSearchData("PL1717");

    var result = consentSearchService.search(params, context);

    var screenView = new SearchScreenView<>(0, List.of());

    assertThat(result).isEqualTo(screenView);
    assertThat(result.resultsHaveBeenLimited()).isFalse();

  }


  @Test
  @Transactional
  public void search_oga_resultsLimited() {

    // insert 20 more search items into the view than the max result size
    int start = 1000;
    int end = start + ConsentSearchService.MAX_RESULTS_SIZE + 20;
    IntStream.range(start, end).forEach(i -> {
      var item = ConsentSearchItemTestUtils.createSearchItem(i, "PENGUIN" + i, "SHELL" + i, Instant.now().minus(i, ChronoUnit.DAYS));
      entityManager.persist(item);
    });

    var context = new ConsentSearchContext(ogaUser, UserType.OGA);
    var result = consentSearchService.search(new ConsentSearchParams(), context);

    // results limited to max size, full count available
    assertThat(result.getFullResultCount()).isEqualTo(ConsentSearchService.MAX_RESULTS_SIZE + 20);
    assertThat(result.getSearchResults().size()).isEqualTo(ConsentSearchService.MAX_RESULTS_SIZE);
    assertThat(result.resultsHaveBeenLimited()).isTrue();

  }

  @Test
  @Transactional
  public void search_industry_resultsLimited() {

    // insert 20 more search items into the view than the max result size
    int start = 2000;
    int end = start + ConsentSearchService.MAX_RESULTS_SIZE + 20;
    IntStream.range(start, end).forEach(i -> {
      var item = ConsentSearchItemTestUtils.createSearchItem(i, "PENGUIN" + i, "SHELL" + i, Instant.now().minus(i, ChronoUnit.DAYS));
      var orgGrp = new PwaHolderOrgGrp(i, i, shell.getOrgGrpId());
      entityManager.persist(item);
      entityManager.persist(orgGrp);
    });

    var context = new ConsentSearchContext(industryUser, UserType.INDUSTRY);
    context.setOrgGroupIdsUserInTeamFor(Set.of(shell.getOrgGrpId()));
    var result = consentSearchService.search(new ConsentSearchParams(), context);

    // results limited to max size
    assertThat(result.getFullResultCount()).isEqualTo(ConsentSearchService.MAX_RESULTS_SIZE + 20);
    assertThat(result.getSearchResults().size()).isEqualTo(ConsentSearchService.MAX_RESULTS_SIZE);
    assertThat(result.resultsHaveBeenLimited()).isTrue();

  }

  private void setupPipelineRefSearchData(String pipelineReferenceToPersist) {

    var masterPwa = new MasterPwa();
    entityManager.persist(masterPwa);

    var pipeline = new Pipeline();
    pipeline.setMasterPwa(masterPwa);
    entityManager.persist(pipeline);

    var pipelineDetail = new PipelineDetail();
    pipelineDetail.setPipelineNumber(pipelineReferenceToPersist);
    pipelineDetail.setTipFlag(true);
    pipelineDetail.setPipeline(pipeline);
    entityManager.persist(pipelineDetail);

    pwa1Shell = ConsentSearchItemTestUtils.createSearchItem(masterPwa.getId(), "PENGUIN", "SHELL", Instant.now().minus(18, ChronoUnit.DAYS));
    entityManager.persist(pwa1Shell);
  }

  private void setUpSearchData() {

    pwa1Shell = ConsentSearchItemTestUtils.createSearchItem(1, "PENGUIN", "SHELL", Instant.now().minus(18, ChronoUnit.DAYS));
    pwa3Bp = ConsentSearchItemTestUtils.createSearchItem(3, "Interconnector", "BP", Instant.now().minus(365, ChronoUnit.DAYS));
    pwa2ShellWintershall = ConsentSearchItemTestUtils.createSearchItem(2, "GAWAIN", "SHELL, WINTERSHALL", Instant.now().minus(67, ChronoUnit.DAYS));

    var pwa1OrgGrp = new PwaHolderOrgGrp(1, 1, shell.getOrgGrpId());
    var pwa3OrgGrp = new PwaHolderOrgGrp(2, 3, bp.getOrgGrpId());
    var pwa2OrgGrpShell = new PwaHolderOrgGrp(3, 2, shell.getOrgGrpId());
    var pwa2OrgGrpWintershall = new PwaHolderOrgGrp(4, 2, wintershall.getOrgGrpId());

    var pwa1OrgUnit = PwaHolderOrgUnitTestUtil.createPwaHolderOrgUnit("1", 1, shellOrg1);
    var pwa2OrgUnit1 = PwaHolderOrgUnitTestUtil.createPwaHolderOrgUnit("2", 2, shellOrg2);
    var pwa2OrgUnit2 = PwaHolderOrgUnitTestUtil.createPwaHolderOrgUnit("3", 2, wintershallOrg);
    var pwa3OrgUnit = PwaHolderOrgUnitTestUtil.createPwaHolderOrgUnit("4", 3, bpOrg);

    entityManager.persist(pwa1Shell);
    entityManager.persist(pwa3Bp);
    entityManager.persist(pwa2ShellWintershall);

    entityManager.persist(pwa1OrgGrp);
    entityManager.persist(pwa3OrgGrp);
    entityManager.persist(pwa2OrgGrpShell);
    entityManager.persist(pwa2OrgGrpWintershall);

    entityManager.persist(pwa1OrgUnit);
    entityManager.persist(pwa2OrgUnit1);
    entityManager.persist(pwa2OrgUnit2);
    entityManager.persist(pwa3OrgUnit);

    pwa1ConsentRefs.forEach(ref -> {
      var consentView = new PwaConsentView();
      consentView.setRowId(pwa1Shell.getPwaId() + ref);
      consentView.setPwaId(pwa1Shell.getPwaId());
      consentView.setConsentReference(ref);
      entityManager.persist(consentView);
    });

    pwa2ConsentRefs.forEach(ref -> {
      var consentView = new PwaConsentView();
      consentView.setRowId(pwa2ShellWintershall.getPwaId() + ref);
      consentView.setPwaId(pwa2ShellWintershall.getPwaId());
      consentView.setConsentReference(ref);
      entityManager.persist(consentView);
    });

    pwa3ConsentRefs.forEach(ref -> {
      var consentView = new PwaConsentView();
      consentView.setRowId(pwa3Bp.getPwaId() + ref);
      consentView.setPwaId(pwa3Bp.getPwaId());
      consentView.setConsentReference(ref);
      entityManager.persist(consentView);
    });

  }

}