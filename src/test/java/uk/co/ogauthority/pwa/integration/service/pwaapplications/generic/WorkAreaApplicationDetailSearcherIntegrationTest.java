package uk.co.ogauthority.pwa.integration.service.pwaapplications.generic;


import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
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
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.PublicNoticeStatus;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.WorkAreaApplicationDetailSearchItem;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.enums.workarea.WorkAreaFlag;
import uk.co.ogauthority.pwa.service.pwaapplications.search.WorkAreaApplicationDetailSearcher;
import uk.co.ogauthority.pwa.service.pwaapplications.search.WorkAreaApplicationSearchTestUtil;
import uk.co.ogauthority.pwa.service.workarea.applications.ApplicationWorkAreaSort;
import uk.co.ogauthority.pwa.service.workarea.applications.WorkAreaPageServiceTestUtil;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureDataJpa
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles("integration-test")
@SuppressWarnings({"JpaQueryApiInspection", "SqlNoDataSourceInspection"})
// IJ seems to give spurious warnings when running with embedded H2
public class WorkAreaApplicationDetailSearcherIntegrationTest {

  private PwaApplicationDetail detail1;
  private PwaApplicationDetail detail2;
  private PwaApplicationDetail detail3;
  private PwaApplicationDetail detail4;

  private WorkAreaApplicationDetailSearchItem detail1SearchItem;
  private WorkAreaApplicationDetailSearchItem detail2SearchItem;
  private WorkAreaApplicationDetailSearchItem detail3SearchItem;
  private WorkAreaApplicationDetailSearchItem detail4SearchItem;

  @Autowired
  private WorkAreaApplicationDetailSearcher workAreaApplicationDetailSearcher;

  @Autowired
  private EntityManager entityManager;

  public void setupSearchItems() {
    detail1 = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, 1, 10);
    detail2 = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.CAT_1_VARIATION, 2, 20);
    detail3 = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.CAT_2_VARIATION, 3, 30);
    detail4 = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.HUOO_VARIATION, 4, 40);

    detail1SearchItem = WorkAreaApplicationSearchTestUtil.getSearchDetailItem(detail1, Instant.now().minus(4, ChronoUnit.DAYS));
    detail2SearchItem = WorkAreaApplicationSearchTestUtil.getSearchDetailItem(detail2, Instant.now().minus(3, ChronoUnit.DAYS));
    detail3SearchItem = WorkAreaApplicationSearchTestUtil.getSearchDetailItem(detail3, Instant.now().minus(2, ChronoUnit.DAYS));
    detail4SearchItem = WorkAreaApplicationSearchTestUtil.getSearchDetailItem(detail4, Instant.now().minus(1, ChronoUnit.DAYS));

  }

  public void persistSearchItems() {

    entityManager.persist(detail1SearchItem);
    entityManager.persist(detail2SearchItem);
    entityManager.persist(detail3SearchItem);
    entityManager.persist(detail4SearchItem);
  }

  @Transactional
  @Test
  public void searchWhereApplicationIdInAndWhereStatusInOrOpenUpdateRequest_whenAllMatchStatus_andHaveNonNullProposedStartDate() {

    setupSearchItems();
    detail4SearchItem = WorkAreaApplicationSearchTestUtil.getSearchDetailItem(detail4, null);

    persistSearchItems();

    var result = workAreaApplicationDetailSearcher.searchWhereApplicationIdInAndWhereStatusInOrOpenUpdateRequest(
        WorkAreaPageServiceTestUtil.getWorkAreaViewPageable(0, ApplicationWorkAreaSort.PROPOSED_START_DATE_ASC),
        Set.of(1, 2, 3, 4),
        Set.of(PwaApplicationStatus.DRAFT),
        true
    );

    //assert null start date item is first
    assertThat(result.get()).containsExactly(
        detail4SearchItem,
        detail1SearchItem,
        detail2SearchItem,
        detail3SearchItem
    );


  }

  @Transactional
  @Test
  public void searchWhereApplicationIdInAndWhereStatusInOrOpenUpdateRequest_whenAllMatchStatus_andNullProposedStartDateExists() {

    setupSearchItems();

    persistSearchItems();

    var result = workAreaApplicationDetailSearcher.searchWhereApplicationIdInAndWhereStatusInOrOpenUpdateRequest(
        WorkAreaPageServiceTestUtil.getWorkAreaViewPageable(0, ApplicationWorkAreaSort.PROPOSED_START_DATE_ASC),
        Set.of(1, 2, 3, 4),
        Set.of(PwaApplicationStatus.DRAFT),
        true
    );

    assertThat(result.get()).containsExactly(
        detail1SearchItem,
        detail2SearchItem,
        detail3SearchItem,
        detail4SearchItem
    );


  }

  @Transactional
  @Test
  public void searchWhereApplicationIdInAndWhereStatusInOrOpenUpdateRequest_whenDoesNotMatchStatus_andOpenUpdateFlagSet() {

    setupSearchItems();
    detail4.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);
    detail4SearchItem = WorkAreaApplicationSearchTestUtil.getSearchDetailItem(detail4, Instant.now().minus(1, ChronoUnit.DAYS));
    detail4SearchItem.setOpenUpdateRequestFlag(true);
    persistSearchItems();

    var result = workAreaApplicationDetailSearcher.searchWhereApplicationIdInAndWhereStatusInOrOpenUpdateRequest(
        WorkAreaPageServiceTestUtil.getWorkAreaViewPageable(0, ApplicationWorkAreaSort.PROPOSED_START_DATE_ASC),
        Set.of(4),
        Set.of(PwaApplicationStatus.DRAFT),
        true
    );

    assertThat(result.get()).containsExactly(
        detail4SearchItem
    );

  }

  @Transactional
  @Test
  public void searchWhereApplicationIdInAndWhereStatusInOrOpenUpdateRequest_whenDoesNotMatchStatus_andOpenUpdateFlagNotSet() {

    setupSearchItems();
    detail4.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);
    detail4SearchItem = WorkAreaApplicationSearchTestUtil.getSearchDetailItem(detail4, Instant.now().minus(1, ChronoUnit.DAYS));
    detail4SearchItem.setOpenUpdateRequestFlag(false);
    persistSearchItems();

    var result = workAreaApplicationDetailSearcher.searchWhereApplicationIdInAndWhereStatusInOrOpenUpdateRequest(
        WorkAreaPageServiceTestUtil.getWorkAreaViewPageable(0, ApplicationWorkAreaSort.PROPOSED_START_DATE_ASC),
        Set.of(4),
        Set.of(PwaApplicationStatus.DRAFT),
        true
    );

    assertThat(result.get()).isEmpty();
  }


  @Transactional
  @Test
  public void searchWhereApplicationIdInAndWhereStatusInOrOpenUpdateRequest_zeroApps() {

    setupSearchItems();
    detail4.setStatus(PwaApplicationStatus.CASE_OFFICER_REVIEW);
    detail4SearchItem = WorkAreaApplicationSearchTestUtil.getSearchDetailItem(detail4, Instant.now().minus(1, ChronoUnit.DAYS));
    detail4SearchItem.setOpenUpdateRequestFlag(false);
    persistSearchItems();

    var result = workAreaApplicationDetailSearcher.searchWhereApplicationIdInAndWhereStatusInOrOpenUpdateRequest(
        WorkAreaPageServiceTestUtil.getWorkAreaViewPageable(0, ApplicationWorkAreaSort.PROPOSED_START_DATE_ASC),
        Set.of(),
        Set.of(PwaApplicationStatus.DRAFT),
        true
    );

    assertThat(result.get()).isEmpty();
  }

  @Transactional
  @Test
  public void searchByStatusOrApplicationIdsAndWhereTipSatisfactoryFlagIsFalseOrAllProcessingWaitFlagsFalse_satisfactory_noWaitFlags_forAttention() {

    setupSearchItems();

    detail3SearchItem.setTipVersionSatisfactoryFlag(true);
    detail1SearchItem.setPublicNoticeStatus(PublicNoticeStatus.MANAGER_APPROVAL);
    detail2SearchItem.setPublicNoticeStatus(PublicNoticeStatus.MANAGER_APPROVAL);
    detail3SearchItem.setPublicNoticeStatus(PublicNoticeStatus.MANAGER_APPROVAL);
    detail4SearchItem.setPublicNoticeStatus(PublicNoticeStatus.MANAGER_APPROVAL);
    persistSearchItems();

    var flagMap = getFlagMapWithDefaultValue(false);

    var result = workAreaApplicationDetailSearcher.searchByStatusOrApplicationIdsAndWhereTipSatisfactoryFlagEqualsOrAllProcessingWaitFlagsEqual(
        WorkAreaPageServiceTestUtil.getWorkAreaViewPageable(0, ApplicationWorkAreaSort.PROPOSED_START_DATE_ASC),
        Set.of(),
        Set.of(PublicNoticeStatus.MANAGER_APPROVAL),
        true,
        Set.of(1, 2, 3, 4),
        flagMap
    );

    assertThat(result).containsOnly(detail1SearchItem, detail2SearchItem, detail3SearchItem, detail4SearchItem);

  }

  @Transactional
  @Test
  public void searchByStatusOrApplicationIdsAndWhereTipSatisfactoryFlagIsFalseOrAllProcessingWaitFlagsFalse_notSatisfactory_available() {

    setupSearchItems();
    persistSearchItems();

    var flagMap = getFlagMapWithDefaultValue(false);

    var result = workAreaApplicationDetailSearcher.searchByStatusOrApplicationIdsAndWhereTipSatisfactoryFlagEqualsOrAllProcessingWaitFlagsEqual(
        WorkAreaPageServiceTestUtil.getWorkAreaViewPageable(0, ApplicationWorkAreaSort.PROPOSED_START_DATE_ASC),
        Set.of(),
        Set.of(),
        false,
        Set.of(1, 2, 3, 4),
        flagMap
    );

    assertThat(result).containsOnly(detail1SearchItem, detail2SearchItem, detail3SearchItem, detail4SearchItem);

  }

  @Transactional
  @Test
  public void searchByStatusOrApplicationIdsAndWhereTipSatisfactoryFlagIsFalseOrAllProcessingWaitFlagsFalse_satisfactory_waitFlags_notAvailable() {

    setupSearchItems();

    detail3SearchItem.setOpenUpdateRequestFlag(true);
    detail3SearchItem.setTipVersionSatisfactoryFlag(true);
    persistSearchItems();

    var flagMap = getFlagMapWithDefaultValue(false);

    var result = workAreaApplicationDetailSearcher.searchByStatusOrApplicationIdsAndWhereTipSatisfactoryFlagEqualsOrAllProcessingWaitFlagsEqual(
        WorkAreaPageServiceTestUtil.getWorkAreaViewPageable(0, ApplicationWorkAreaSort.PROPOSED_START_DATE_ASC),
        Set.of(),
        Set.of(),
        Set.of(1, 2, 3, 4),
        flagMap
    );

    assertThat(result).containsOnly(detail1SearchItem, detail2SearchItem, detail4SearchItem);

  }

  @Transactional
  @Test
  public void searchByStatusOrApplicationIdsAndWhereTipSatisfactoryFlagIsFalseOrAllProcessingWaitFlagsFalse_consentReview_consentReviewForegroundFlag_available() {

    setupSearchItems();

    detail3SearchItem.setTipVersionSatisfactoryFlag(true);
    detail3SearchItem.setOpenConsentReviewFlag(true);
    persistSearchItems();

    var flagMap = getFlagMapWithDefaultValue(false);
    flagMap.put(WorkAreaFlag.OPEN_CONSENT_REVIEW_FOREGROUND_FLAG, true);

    var result = workAreaApplicationDetailSearcher.searchByStatusOrApplicationIdsAndWhereTipSatisfactoryFlagEqualsOrAllProcessingWaitFlagsEqual(
        WorkAreaPageServiceTestUtil.getWorkAreaViewPageable(0, ApplicationWorkAreaSort.PROPOSED_START_DATE_ASC),
        Set.of(),
        Set.of(),
        Set.of(1, 2, 3, 4),
        flagMap
    );

    assertThat(result).containsOnly(detail1SearchItem, detail2SearchItem, detail3SearchItem, detail4SearchItem);

  }

  @Transactional
  @Test
  public void searchByStatusOrApplicationIdsAndWhereTipSatisfactoryFlagIsFalseOrAllProcessingWaitFlagsFalse_consentReview_noWaitFlags_notAvailable() {

    setupSearchItems();

    detail3SearchItem.setTipVersionSatisfactoryFlag(true);
    detail3SearchItem.setOpenConsentReviewFlag(true);
    persistSearchItems();

    var flagMap = getFlagMapWithDefaultValue(false);

    var result = workAreaApplicationDetailSearcher.searchByStatusOrApplicationIdsAndWhereTipSatisfactoryFlagEqualsOrAllProcessingWaitFlagsEqual(
        WorkAreaPageServiceTestUtil.getWorkAreaViewPageable(0, ApplicationWorkAreaSort.PROPOSED_START_DATE_ASC),
        Set.of(),
        Set.of(),
        Set.of(1, 2, 3, 4),
        flagMap
    );

    assertThat(result).containsOnly(detail1SearchItem, detail2SearchItem, detail4SearchItem);

  }

  @Transactional
  @Test
  public void searchByStatusOrApplicationIdsAndWhereTipSatisfactoryFlagIsFalseOrAllProcessingWaitFlagsFalse_noConsentReview_noWaitFlags_available() {

    setupSearchItems();

    detail3SearchItem.setTipVersionSatisfactoryFlag(true);
    detail3SearchItem.setOpenConsentReviewFlag(false);
    persistSearchItems();

    var flagMap = getFlagMapWithDefaultValue(false);

    var result = workAreaApplicationDetailSearcher.searchByStatusOrApplicationIdsAndWhereTipSatisfactoryFlagEqualsOrAllProcessingWaitFlagsEqual(
        WorkAreaPageServiceTestUtil.getWorkAreaViewPageable(0, ApplicationWorkAreaSort.PROPOSED_START_DATE_ASC),
        Set.of(),
        Set.of(),
        Set.of(1, 2, 3, 4),
        flagMap
    );

    assertThat(result).containsOnly(detail1SearchItem, detail2SearchItem, detail3SearchItem, detail4SearchItem);

  }

  @Transactional
  @Test
  public void searchByStatusOrApplicationIdsAndWhereTipSatisfactoryFlagIsFalseOrAllProcessingWaitFlagsFalse_noConsentReview_consentReviewForegroundFlag_notAvailable() {

    setupSearchItems();

    detail3SearchItem.setTipVersionSatisfactoryFlag(true);
    detail3SearchItem.setOpenConsentReviewFlag(false);
    persistSearchItems();

    var flagMap = getFlagMapWithDefaultValue(false);
    flagMap.put(WorkAreaFlag.OPEN_CONSENT_REVIEW_FOREGROUND_FLAG, true);

    var result = workAreaApplicationDetailSearcher.searchByStatusOrApplicationIdsAndWhereTipSatisfactoryFlagEqualsOrAllProcessingWaitFlagsEqual(
        WorkAreaPageServiceTestUtil.getWorkAreaViewPageable(0, ApplicationWorkAreaSort.PROPOSED_START_DATE_ASC),
        Set.of(),
        Set.of(),
        false,
        Set.of(1, 2, 3, 4),
        flagMap
    );

    assertThat(result).containsOnly(detail1SearchItem, detail2SearchItem, detail4SearchItem);

  }

  @Transactional
  @Test
  public void searchByStatusOrApplicationIdsAndWhereTipSatisfactoryFlagIsTrueAndAnyProcessingWaitFlagTrue_notSatisfactory_notAvailable() {

    setupSearchItems();

    detail3SearchItem.setTipVersionSatisfactoryFlag(false);
    persistSearchItems();

    var flagMap = getFlagMapWithDefaultValue(true);

    var result = workAreaApplicationDetailSearcher.searchByStatusOrApplicationIdsAndWhereTipSatisfactoryFlagEqualsAndAnyProcessingWaitFlagEqual(
        WorkAreaPageServiceTestUtil.getWorkAreaViewPageable(0, ApplicationWorkAreaSort.PROPOSED_START_DATE_ASC),
        Set.of(),
        Set.of(),
        false,
        Set.of(1, 2, 3, 4),
        flagMap
    );

    assertThat(result).isEmpty();

  }

  @Transactional
  @Test
  public void searchByStatusOrApplicationIdsAndWhereTipSatisfactoryFlagIsTrueAndAnyProcessingWaitFlagTrue_satisfactory_noWaitFlags_notAvailable() {

    setupSearchItems();

    detail3SearchItem.setTipVersionSatisfactoryFlag(true);
    persistSearchItems();

    var flagMap = getFlagMapWithDefaultValue(true);

    var result = workAreaApplicationDetailSearcher.searchByStatusOrApplicationIdsAndWhereTipSatisfactoryFlagEqualsAndAnyProcessingWaitFlagEqual(
        WorkAreaPageServiceTestUtil.getWorkAreaViewPageable(0, ApplicationWorkAreaSort.PROPOSED_START_DATE_ASC),
        Set.of(),
        Set.of(),
        false,
        Set.of(1, 2, 3, 4),
        flagMap
    );

    assertThat(result).isEmpty();

  }

  @Transactional
  @Test
  public void searchByStatusOrApplicationIdsAndWhereTipSatisfactoryFlagIsTrueAndAnyProcessingWaitFlagTrue_satisfactory_oneWaitFlag_available() {

    setupSearchItems();

    detail3SearchItem.setTipVersionSatisfactoryFlag(true);
    detail3SearchItem.setOpenConsultationRequestFlag(true);
    persistSearchItems();

    var flagMap = getFlagMapWithDefaultValue(true);

    var result = workAreaApplicationDetailSearcher.searchByStatusOrApplicationIdsAndWhereTipSatisfactoryFlagEqualsAndAnyProcessingWaitFlagEqual(
        WorkAreaPageServiceTestUtil.getWorkAreaViewPageable(0, ApplicationWorkAreaSort.PROPOSED_START_DATE_ASC),
        Set.of(),
        Set.of(),
        false,
        Set.of(1, 2, 3, 4),
        flagMap
    );

    assertThat(result).containsOnly(detail3SearchItem);

  }

  @Transactional
  @Test
  public void searchByStatusOrApplicationIdsAndWhereTipSatisfactoryFlagIsTrueAndAnyProcessingWaitFlagTrue_consentReviewOpen_oneWaitFlag_available() {

    setupSearchItems();

    detail3SearchItem.setTipVersionSatisfactoryFlag(true);
    detail3SearchItem.setOpenConsentReviewFlag(true);
    persistSearchItems();

    var flagMap = getFlagMapWithDefaultValue(true);
    flagMap.put(WorkAreaFlag.OPEN_CONSENT_REVIEW_FOREGROUND_FLAG, false);

    var result = workAreaApplicationDetailSearcher.searchByStatusOrApplicationIdsAndWhereTipSatisfactoryFlagEqualsAndAnyProcessingWaitFlagEqual(
        WorkAreaPageServiceTestUtil.getWorkAreaViewPageable(0, ApplicationWorkAreaSort.PROPOSED_START_DATE_ASC),
        Set.of(),
        Set.of(),
        Set.of(1, 2, 3, 4),
        flagMap
    );

    assertThat(result).containsOnly(detail3SearchItem);

  }

  @Transactional
  @Test
  public void searchByStatusOrApplicationIdsAndWhereTipSatisfactoryFlagIsTrueAndAnyProcessingWaitFlagTrue_consentReviewOpen_noWaitFlags_notAvailable() {

    setupSearchItems();

    detail3SearchItem.setTipVersionSatisfactoryFlag(true);
    detail3SearchItem.setOpenConsentReviewFlag(true);
    persistSearchItems();

    var flagMap = getFlagMapWithDefaultValue(true);

    var result = workAreaApplicationDetailSearcher.searchByStatusOrApplicationIdsAndWhereTipSatisfactoryFlagEqualsAndAnyProcessingWaitFlagEqual(
        WorkAreaPageServiceTestUtil.getWorkAreaViewPageable(0, ApplicationWorkAreaSort.PROPOSED_START_DATE_ASC),
        Set.of(),
        Set.of(),
        Set.of(1, 2, 3, 4),
        flagMap
    );

    assertThat(result).isEmpty();

  }

  private Map<WorkAreaFlag, Boolean> getFlagMapWithDefaultValue(Boolean value) {

    return WorkAreaFlag.stream()
        .collect(Collectors.toMap(Function.identity(), val -> value));

  }

}
