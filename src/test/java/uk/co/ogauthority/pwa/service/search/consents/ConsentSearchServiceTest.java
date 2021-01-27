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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonTestUtil;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationGroup;
import uk.co.ogauthority.pwa.energyportal.model.entity.organisations.PortalOrganisationUnit;
import uk.co.ogauthority.pwa.model.entity.search.consents.ConsentSearchItem;
import uk.co.ogauthority.pwa.model.entity.search.consents.PwaHolderOrgGrp;
import uk.co.ogauthority.pwa.model.search.consents.ConsentSearchContext;
import uk.co.ogauthority.pwa.model.search.consents.ConsentSearchParams;
import uk.co.ogauthority.pwa.model.view.search.SearchScreenView;
import uk.co.ogauthority.pwa.model.view.search.consents.ConsentSearchResultView;
import uk.co.ogauthority.pwa.service.enums.users.UserType;
import uk.co.ogauthority.pwa.service.search.consents.predicates.ConsentSearchPredicateProvider;
import uk.co.ogauthority.pwa.testutils.ConsentSearchItemTestUtils;
import uk.co.ogauthority.pwa.testutils.PortalOrganisationTestUtils;

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

  private ConsentSearchItem pwa1Shell, pwa3Bp, pwa2ShellWintershall;

  private PortalOrganisationGroup shell, bp, wintershall;
  private PortalOrganisationUnit shellOrg1, shellOrg2, bpOrg, wintershallOrg;

  private AuthenticatedUserAccount industryUser = new AuthenticatedUserAccount(new WebUserAccount(1, PersonTestUtil.createDefaultPerson()), Set.of(
      PwaUserPrivilege.PWA_INDUSTRY, PwaUserPrivilege.PWA_CONSENT_SEARCH));

  private AuthenticatedUserAccount ogaUser = new AuthenticatedUserAccount(new WebUserAccount(2, PersonTestUtil.createPersonFrom(new PersonId(12))), Set.of(
      PwaUserPrivilege.PWA_REGULATOR, PwaUserPrivilege.PWA_CONSENT_SEARCH));

  @Before
  public void setUp() throws Exception {

    consentSearchService = new ConsentSearchService(entityManager, predicateProviders);

    shell = PortalOrganisationTestUtils.generateOrganisationGroup(1, "SHELL", "S");
    bp = PortalOrganisationTestUtils.generateOrganisationGroup(2, "BP", "B");
    wintershall = PortalOrganisationTestUtils.generateOrganisationGroup(3, "WINTERSHALL", "W");

    shellOrg1 = PortalOrganisationTestUtils.generateOrganisationUnit(1, "ShellOrg1", shell);
    shellOrg2 = PortalOrganisationTestUtils.generateOrganisationUnit(2, "ShellOrg2", shell);
    bpOrg = PortalOrganisationTestUtils.generateOrganisationUnit(3, "BpOrg1", bp);
    wintershallOrg = PortalOrganisationTestUtils.generateOrganisationUnit(4, "WinterOrg", wintershall);

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

  private void setUpSearchData() {

    pwa1Shell = ConsentSearchItemTestUtils.createSearchItem(1, "PENGUIN", "SHELL", Instant.now().minus(18, ChronoUnit.DAYS));
    pwa3Bp = ConsentSearchItemTestUtils.createSearchItem(3, "Interconnector", "BP", Instant.now().minus(365, ChronoUnit.DAYS));
    pwa2ShellWintershall = ConsentSearchItemTestUtils.createSearchItem(2, "GAWAIN", "SHELL, WINTERSHALL", Instant.now().minus(67, ChronoUnit.DAYS));

    var pwa1OrgGrp = new PwaHolderOrgGrp(1, 1, shell.getOrgGrpId());
    var pwa3OrgGrp = new PwaHolderOrgGrp(2, 3, bp.getOrgGrpId());
    var pwa2OrgGrpShell = new PwaHolderOrgGrp(3, 2, shell.getOrgGrpId());
    var pwa2OrgGrpWintershall = new PwaHolderOrgGrp(4, 2, wintershall.getOrgGrpId());

    var pwa1OrgUnit = ConsentSearchItemTestUtils.createPwaHolderOrgUnit(1, 1, shellOrg1);
    var pwa2OrgUnit1 = ConsentSearchItemTestUtils.createPwaHolderOrgUnit(2, 2, shellOrg2);
    var pwa2OrgUnit2 = ConsentSearchItemTestUtils.createPwaHolderOrgUnit(3, 2, wintershallOrg);
    var pwa3OrgUnit = ConsentSearchItemTestUtils.createPwaHolderOrgUnit(4, 3, bpOrg);

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

  }

}