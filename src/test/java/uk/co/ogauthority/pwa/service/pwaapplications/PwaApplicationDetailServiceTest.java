package uk.co.ogauthority.pwa.service.pwaapplications;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplication;
import uk.co.ogauthority.pwa.domain.pwa.application.model.PwaApplicationType;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.features.application.tasks.fasttrack.PadFastTrackService;
import uk.co.ogauthority.pwa.features.application.tasks.othertechprops.PropertyPhase;
import uk.co.ogauthority.pwa.features.application.tasks.partnerletters.PartnerLettersForm;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.initialreview.InitialReviewPaymentDecision;
import uk.co.ogauthority.pwa.features.appprocessing.tasks.initialreview.PadInitialReviewService;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.Person;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonId;
import uk.co.ogauthority.pwa.integrations.energyportal.people.external.PersonTestUtil;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.repository.pwaapplications.PwaApplicationDetailRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ApplicationState;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.users.UserType;
import uk.co.ogauthority.pwa.service.users.UserTypeService;
import uk.co.ogauthority.pwa.testutils.ObjectTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PwaApplicationDetailServiceTest {

  private static final int WUA_ID_1 = 1;
  private static final int WUA_ID_2 = 2;
  private static final PersonId WUA_1_PERSON_ID = new PersonId(10);
  private static final PersonId WUA_2_PERSON_ID = new PersonId(20);
  private static final int APP_ID = 1;

  private static final PwaApplicationStatus DEFAULT_SUBMISSION_STATUS = PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW;

  @Mock
  private PwaApplicationDetailRepository applicationDetailRepository;

  @Mock
  private PadFastTrackService fastTrackService;

  @Mock
  private UserTypeService userTypeService;

  @Mock
  private PadInitialReviewService initialReviewService;

  @Mock
  private Consumer<PwaApplicationDetail> detailHandlerFunction;

  @Captor
  private ArgumentCaptor<PwaApplicationDetail> detailCaptor;

  @Captor
  private ArgumentCaptor<List<PwaApplicationDetail>> detailsCaptor;


  private PwaApplicationDetailService pwaApplicationDetailService;
  private PwaApplicationDetail pwaApplicationDetail;
  private WebUserAccount webUserAccount1;
  private WebUserAccount webUserAccount2;
  private Person wua1Person = new Person(WUA_1_PERSON_ID.asInt(), "Industry", "Person", "industry@pwa.co.uk", null);
  private Person wua2Person = new Person(WUA_2_PERSON_ID.asInt(), "Industry2", "Person2", "industry@pwa.co.uk", null);
  private AuthenticatedUserAccount user;

  private Clock clock;

  @Before
  public void setUp() {
    pwaApplicationDetail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, APP_ID);
    webUserAccount1 = new WebUserAccount(WUA_ID_1, wua1Person);
    webUserAccount2 = new WebUserAccount(WUA_ID_2, wua2Person);
    user = new AuthenticatedUserAccount(webUserAccount1, List.of());

    var fixedInstant = LocalDate
        .of(2020, 2, 6)
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant();

    clock = Clock.fixed(fixedInstant, ZoneId.systemDefault());

    when(applicationDetailRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    pwaApplicationDetailService = new PwaApplicationDetailService(applicationDetailRepository, clock, fastTrackService,
        userTypeService, initialReviewService);
  }

  @Test
  public void setLinkedToFields_isLinked() {

    when(applicationDetailRepository.save(pwaApplicationDetail)).thenReturn(pwaApplicationDetail);

    var detail = pwaApplicationDetailService.setLinkedToFields(pwaApplicationDetail, true);
    assertThat(detail).isEqualTo(pwaApplicationDetail);
    assertThat(detail.getLinkedToArea()).isTrue();
    assertNull(detail.getNotLinkedDescription());
  }

  @Test
  public void setLinkedToFields_notLinked() {
    pwaApplicationDetail.setNotLinkedDescription("test description");
    when(applicationDetailRepository.save(pwaApplicationDetail)).thenReturn(pwaApplicationDetail);

    var detail = pwaApplicationDetailService.setLinkedToFields(pwaApplicationDetail, false);
    assertThat(detail).isEqualTo(pwaApplicationDetail);
    assertThat(detail.getLinkedToArea()).isFalse();
    assertEquals("test description", detail.getNotLinkedDescription());
  }

  @Test
  public void createFirstDetail_attributesSetAsExpected() {

    var master = new PwaApplication();
    var detail = pwaApplicationDetailService.createFirstDetail(master, user, 1L);
    assertThat(detail.getPwaApplication()).isEqualTo(master);
    assertThat(detail.getStatus()).isEqualTo(PwaApplicationStatus.DRAFT);
    assertThat(detail.isTipFlag()).isTrue();
    assertThat(detail.getVersionNo()).isEqualTo(1);
    assertThat(detail.getCreatedByWuaId()).isEqualTo(user.getWuaId());
    assertThat(detail.getCreatedTimestamp()).isEqualTo(clock.instant());
    assertThat(detail.getSubmittedByPersonId()).isNull();
    assertThat(detail.getSubmittedTimestamp()).isNull();
    assertThat(detail.getStatusLastModifiedByWuaId()).isEqualTo(user.getWuaId());
    assertThat(detail.getStatusLastModifiedTimestamp()).isEqualTo(clock.instant());

  }

  @Test
  public void updateStatus_statusModifiedDataSet() {
    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.CAT_1_VARIATION);
    detail.setStatus(PwaApplicationStatus.DRAFT);

    var alternativeWua = new WebUserAccount(1000);
    var updatedDetail = pwaApplicationDetailService.updateStatus(
        detail,
        PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW,
        alternativeWua
    );

    assertThat(updatedDetail.getStatusLastModifiedTimestamp()).isEqualTo(clock.instant());
    assertThat(updatedDetail.getStatusLastModifiedByWuaId()).isEqualTo(alternativeWua.getWuaId());
    assertThat(updatedDetail.getStatus()).isEqualTo(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW);

  }

  @Test
  public void setSubmitted_allStatusColumnsSetAsExpected() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.CAT_1_VARIATION);
    detail.setStatus(PwaApplicationStatus.DRAFT);

    var alternativeWua = new WebUserAccount(1000, wua2Person);

    when(fastTrackService.isFastTrackRequired(detail)).thenReturn(true);

    var submittedDetail = pwaApplicationDetailService.setSubmitted(
        detail,
        alternativeWua,
        DEFAULT_SUBMISSION_STATUS
    );

    assertThat(submittedDetail.getStatusLastModifiedTimestamp()).isEqualTo(clock.instant());
    assertThat(submittedDetail.getStatusLastModifiedByWuaId()).isEqualTo(alternativeWua.getWuaId());
    assertThat(submittedDetail.getStatus()).isEqualTo(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW);
    assertThat(submittedDetail.getSubmittedTimestamp()).isEqualTo(clock.instant());
    assertThat(submittedDetail.getSubmittedByPersonId()).isEqualTo(wua2Person.getId());

    assertThat(submittedDetail.getSubmittedAsFastTrackFlag()).isTrue();

  }

  @Test
  public void setInitialReviewApproved_paymentWaived() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    detail.setStatus(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW);

    pwaApplicationDetailService.setInitialReviewApproved(detail, user, InitialReviewPaymentDecision.PAYMENT_WAIVED);

    verify(applicationDetailRepository, times(2)).save(detail);

    assertThat(detail.getStatus()).isEqualTo(PwaApplicationStatus.CASE_OFFICER_REVIEW);

  }

  @Test
  public void setInitialReviewApproved_paymentRequired() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    detail.setStatus(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW);

    pwaApplicationDetailService.setInitialReviewApproved(detail, user, InitialReviewPaymentDecision.PAYMENT_REQUIRED);

    verify(applicationDetailRepository, times(2)).save(detail);

    assertThat(detail.getStatus()).isEqualTo(PwaApplicationStatus.AWAITING_APPLICATION_PAYMENT);

  }

  @Test
  public void updatePartnerLetters_lettersRequired() {
    var form = new PartnerLettersForm();
    form.setPartnerLettersRequired(true);
    form.setPartnerLettersConfirmed(true);
    pwaApplicationDetailService.updatePartnerLetters(pwaApplicationDetail, form);
    assertTrue(pwaApplicationDetail.getPartnerLettersRequired());
    assertTrue(pwaApplicationDetail.getPartnerLettersConfirmed());
  }

  @Test
  public void updatePartnerLetters_lettersNotRequired() {
    var form = new PartnerLettersForm();
    form.setPartnerLettersRequired(false);
    pwaApplicationDetailService.updatePartnerLetters(pwaApplicationDetail, form);
    assertFalse(pwaApplicationDetail.getPartnerLettersRequired());
    assertNull(pwaApplicationDetail.getPartnerLettersConfirmed());
  }

  @Test
  public void setWithdrawn() {

    var withdrawalTimestamp = Instant.now(clock);
    pwaApplicationDetail.setWithdrawalTimestamp(withdrawalTimestamp);
    Person withdrawingUser = PersonTestUtil.createDefaultPerson();
    String withdrawalReason = "reason";

    pwaApplicationDetailService.setWithdrawn(pwaApplicationDetail, withdrawingUser, withdrawalReason);

    var captor = ArgumentCaptor.forClass(PwaApplicationDetail.class);
    verify(applicationDetailRepository, times(1)).save(captor.capture());

    assertThat(captor.getValue().getStatus()).isEqualTo(PwaApplicationStatus.WITHDRAWN);
    assertThat(captor.getValue().getWithdrawalReason()).isEqualTo(withdrawalReason);
    assertThat(captor.getValue().getWithdrawalTimestamp()).isEqualTo(withdrawalTimestamp);
    assertThat(captor.getValue().getWithdrawingPersonId()).isEqualTo(withdrawingUser.getId());
  }

  @Test
  public void setDeleted() {

    var deletedTimestamp = Instant.now(clock);
    Person deletingUser = PersonTestUtil.createDefaultPerson();

    pwaApplicationDetailService.setDeleted(pwaApplicationDetail, deletingUser);

    var captor = ArgumentCaptor.forClass(PwaApplicationDetail.class);
    verify(applicationDetailRepository, times(1)).save(captor.capture());

    assertThat(captor.getValue().getStatus()).isEqualTo(PwaApplicationStatus.DELETED);
    assertThat(captor.getValue().getDeletedTimestamp()).isEqualTo(deletedTimestamp);
    assertThat(captor.getValue().getDeletingPersonId()).isEqualTo(deletingUser.getId());
  }

  @Test
  public void setNotLinkedFieldDescription() {

    assertThat(pwaApplicationDetail.getNotLinkedDescription()).isNull();

    pwaApplicationDetailService.setNotLinkedFieldDescription(pwaApplicationDetail, "testDesc");

    assertThat(pwaApplicationDetail.getNotLinkedDescription()).isEqualTo("testDesc");

    verify(applicationDetailRepository, times(1)).save(pwaApplicationDetail);

  }

  @Test
  public void createNewTipDetail_setsOldValueAsNotTip_andSetsAttributesOnNewDetailAsExpected() {

    setAllPwaAppDetailFields(pwaApplicationDetail, PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW, webUserAccount2);
    pwaApplicationDetail.setStatus(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW);

    var newDetail = pwaApplicationDetailService
        .createNewTipDetail(pwaApplicationDetail, PwaApplicationStatus.UPDATE_REQUESTED, user);

    verify(initialReviewService, times(1))
        .carryForwardInitialReview(pwaApplicationDetail, newDetail);

    // Test Old Detail
    assertThat(pwaApplicationDetail.isTipFlag()).isFalse();

    // Test new detail

    assertThat(newDetail.getPwaApplication()).isEqualTo(pwaApplicationDetail.getPwaApplication());
    assertThat(newDetail.getId()).isNotEqualTo(pwaApplicationDetail.getId());
    assertThat(newDetail.isFirstVersion()).isFalse();
    assertThat(newDetail.isTipFlag()).isTrue();
    assertThat(newDetail.getCreatedTimestamp()).isAfter(pwaApplicationDetail.getCreatedTimestamp());
    assertThat(newDetail.getStatusLastModifiedTimestamp()).isEqualTo(newDetail.getCreatedTimestamp());
    assertThat(newDetail.getStatusLastModifiedByWuaId()).isNotEqualTo(
        pwaApplicationDetail.getStatusLastModifiedByWuaId());
    assertThat(newDetail.getCreatedByWuaId()).isNotEqualTo(pwaApplicationDetail.getCreatedByWuaId());
    // sets new detail to UPDATE_REQUESTED
    assertThat(newDetail.getStatus()).isEqualTo(PwaApplicationStatus.UPDATE_REQUESTED);

    var ignoredFields = List.of("id", "status", "tipFlag", "versionNo", "createdTimestamp",
        "statusLastModifiedTimestamp", "statusLastModifiedByWuaId", "createdByWuaId");

    var nullFields = List.of("id",
        "submittedByPersonId",
        "submittedTimestamp",
        "initialReviewApprovedByWuaId",
        "initialReviewApprovedTimestamp",
        "submittedAsFastTrackFlag",
        "confirmedSatisfactoryByPersonId",
        "confirmedSatisfactoryReason",
        "confirmedSatisfactoryTimestamp",
        "withdrawalTimestamp",
        "withdrawalReason",
        "withdrawingPersonId",
        "deletedTimestamp",
        "deletingPersonId");

    var ignoredForEqualsComparison = new ArrayList<String>();
    ignoredForEqualsComparison.addAll(ignoredFields);
    ignoredForEqualsComparison.addAll(nullFields);

    ObjectTestUtils.assertValuesEqual(pwaApplicationDetail, newDetail, ignoredForEqualsComparison);
    ObjectTestUtils.assertAllExpectedFieldsHaveValue(newDetail, nullFields);

  }

  @Test
  public void setSupplementaryDocumentsFlag() {

    var detail = new PwaApplicationDetail();
    assertThat(detail.getSupplementaryDocumentsFlag()).isNull();

    pwaApplicationDetailService.setSupplementaryDocumentsFlag(detail, true);

    verify(applicationDetailRepository, times(1)).save(detailCaptor.capture());

    var savedDetail = detailCaptor.getValue();

    assertThat(savedDetail.getSupplementaryDocumentsFlag()).isTrue();

  }

  @Test
  public void setSupplementaryDocumentsFlag_nullValue_doesntError() {

    var detail = new PwaApplicationDetail();
    assertThat(detail.getSupplementaryDocumentsFlag()).isNull();

    pwaApplicationDetailService.setSupplementaryDocumentsFlag(detail, null);

    verify(applicationDetailRepository, times(1)).save(detailCaptor.capture());

    var savedDetail = detailCaptor.getValue();

    assertThat(savedDetail.getSupplementaryDocumentsFlag()).isNull();

  }

  private void setAllPwaAppDetailFields(PwaApplicationDetail detail, PwaApplicationStatus status, WebUserAccount wua) {
    // This should not be setting any value as null. That will defeat the purpose of this method.
    var baseTime = Instant.ofEpochSecond(
        LocalDateTime.of(2000, 12, 31, 0, 59).toEpochSecond(ZoneOffset.UTC)
    );
    detail.setStatus(status);
    detail.setLinkedToArea(true);
    detail.setNotLinkedDescription("NOT LINKED DESC");
    detail.setPipelinesCrossed(true);
    detail.setCablesCrossed(true);
    detail.setMedianLineCrossed(true);
    detail.setCsaCrossed(true);
    detail.setNumOfHolders(1);
    detail.setPipelinePhaseProperties(Set.of(PropertyPhase.OTHER));
    detail.setOtherPhaseDescription("OTHER PHASE DESC");
    detail.setOtherFluidDescription("OTHER FLUID DESC");
    detail.setPartnerLettersRequired(true);
    detail.setPartnerLettersConfirmed(true);
    detail.setCreatedByWuaId(wua.getWuaId());
    detail.setCreatedTimestamp(baseTime);
    detail.setSubmittedByPersonId(wua.getLinkedPerson().getId());
    detail.setSubmittedTimestamp(baseTime);
    detail.setStatusLastModifiedByWuaId(wua.getWuaId());
    detail.setStatusLastModifiedTimestamp(baseTime);
    detail.setSupplementaryDocumentsFlag(true);
    detail.setConfirmedSatisfactoryTimestamp(Instant.now());
    detail.setConfirmedSatisfactoryReason("reason");
    detail.setConfirmedSatisfactoryByPersonId(new PersonId(1));
    detail.setWithdrawingPersonId(wua.getLinkedPerson().getId());
    detail.setWithdrawalTimestamp(baseTime);
    detail.setWithdrawalReason("reason");
    detail.setDeletingPersonId(wua.getLinkedPerson().getId());
    detail.setDeletedTimestamp(baseTime);

    // want to make sure that this method always gives every pwaApplicationDetail attribute a value.
    // This should ensure that tests are updated if attributes added later on.
    var nullFields = Arrays.stream(FieldUtils.getAllFields(PwaApplicationDetail.class))
        .filter(field -> Objects.isNull(getFieldValue(field, detail)))
        .map(Field::getName)
        .collect(Collectors.toSet());
    // If this has values, need to make sure some value is set, and that creating a new version of a detail deals with that attribute.
    try {
      assertThat(nullFields).isEmpty();
    } catch (AssertionError e) {
      throw new AssertionError(
          String.format("Expected all fields to be given a value, but [%s] is/are null", String.join(",", nullFields)));
    }
  }

  @Test
  public void getLatestDetailForUser_industry_firstDraft() {

    var draftDetail = new PwaApplicationDetail();
    draftDetail.setStatus(PwaApplicationStatus.DRAFT);
    draftDetail.setTipFlag(true);
    draftDetail.setVersionNo(1);

    when(applicationDetailRepository.findByPwaApplicationId(APP_ID)).thenReturn(List.of(draftDetail));
    when(userTypeService.getUserTypes(user)).thenReturn(Set.of(UserType.INDUSTRY));

    var latestDetailOpt = pwaApplicationDetailService.getLatestDetailForUser(APP_ID, user);

    assertThat(latestDetailOpt)
        .isPresent()
        .contains(draftDetail);

  }

  private PwaApplicationDetail createDetail(PwaApplicationStatus pwaApplicationStatus, int versionNumber) {
    var detail = new PwaApplicationDetail();
    detail.setVersionNo(versionNumber);
    detail.setStatus(pwaApplicationStatus);
    detail.setTipFlag(false);
    return detail;
  }

  @Test
  public void getLatestDetailForUser_industry_submitted() {

    var draftDetail = createDetail(PwaApplicationStatus.DRAFT, 1);
    var submittedDetail1 = createDetail(PwaApplicationStatus.CASE_OFFICER_REVIEW, 2);
    submittedDetail1.setSubmittedTimestamp(Instant.now().minusSeconds(86400));
    var submittedDetail2 = createDetail(PwaApplicationStatus.CASE_OFFICER_REVIEW, 3);
    submittedDetail2.setSubmittedTimestamp(Instant.now());
    submittedDetail2.setTipFlag(true);

    when(applicationDetailRepository.findByPwaApplicationId(APP_ID)).thenReturn(
        List.of(draftDetail, submittedDetail1, submittedDetail2));
    when(userTypeService.getUserTypes(user)).thenReturn(Set.of(UserType.INDUSTRY));

    var latestDetailOpt = pwaApplicationDetailService.getLatestDetailForUser(APP_ID, user);

    assertThat(latestDetailOpt)
        .isPresent()
        .contains(submittedDetail2);

  }

  @Test
  public void getLatestDetailForUser_industry_andOga_submittedVersionExists() {

    var draftDetail = createDetail(PwaApplicationStatus.DRAFT, 1);
    var submittedDetail1 = createDetail(PwaApplicationStatus.CASE_OFFICER_REVIEW, 2);
    submittedDetail1.setSubmittedTimestamp(Instant.now().minusSeconds(86400));
    var submittedDetail2 = createDetail(PwaApplicationStatus.CASE_OFFICER_REVIEW, 3);
    submittedDetail2.setSubmittedTimestamp(Instant.now());
    submittedDetail2.setTipFlag(true);

    when(applicationDetailRepository.findByPwaApplicationId(APP_ID)).thenReturn(
        List.of(draftDetail, submittedDetail1, submittedDetail2));
    when(userTypeService.getUserTypes(user)).thenReturn(Set.of(UserType.INDUSTRY, UserType.OGA));

    var latestDetailOpt = pwaApplicationDetailService.getLatestDetailForUser(APP_ID, user);

    assertThat(latestDetailOpt)
        .isPresent()
        .contains(submittedDetail2);

  }

  @Test
  public void getLatestDetailForUser_industry_andOga_submittedVersions_draftVersionIsTip() {

    var submittedDetail1 = createDetail(PwaApplicationStatus.CASE_OFFICER_REVIEW, 1);
    var submittedDetail2 = createDetail(PwaApplicationStatus.CASE_OFFICER_REVIEW, 2);
    submittedDetail2.setSubmittedTimestamp(Instant.now().minusSeconds(86400));
    var draftDetail = createDetail(PwaApplicationStatus.UPDATE_REQUESTED, 3);
    draftDetail.setTipFlag(true);

    when(applicationDetailRepository.findByPwaApplicationId(APP_ID)).thenReturn(
        List.of(submittedDetail1, submittedDetail2, draftDetail));
    when(userTypeService.getUserTypes(user)).thenReturn(Set.of(UserType.INDUSTRY, UserType.OGA));

    var latestDetailOpt = pwaApplicationDetailService.getLatestDetailForUser(APP_ID, user);

    assertThat(latestDetailOpt)
        .isPresent()
        .contains(submittedDetail2);

  }

  @Test
  public void getLatestDetailForUser_industry_andOga_firstDraftVersionIsTip() {

    var draftDetail = createDetail(PwaApplicationStatus.DRAFT, 1);
    draftDetail.setTipFlag(true);

    when(applicationDetailRepository.findByPwaApplicationId(APP_ID)).thenReturn(List.of(draftDetail));
    when(userTypeService.getUserTypes(user)).thenReturn(Set.of(UserType.INDUSTRY, UserType.OGA));

    var latestDetailOpt = pwaApplicationDetailService.getLatestDetailForUser(APP_ID, user);

    assertThat(latestDetailOpt)
        .isPresent()
        .contains(draftDetail);

  }

  @Test
  public void getLatestDetailForUser_oga_submitted() {

    var draftDetail = createDetail(PwaApplicationStatus.DRAFT, 1);
    var submittedDetail1 = createDetail(PwaApplicationStatus.CASE_OFFICER_REVIEW, 2);
    submittedDetail1.setSubmittedTimestamp(Instant.now().minusSeconds(86400));
    var submittedDetail2 = createDetail(PwaApplicationStatus.CASE_OFFICER_REVIEW, 3);
    submittedDetail2.setSubmittedTimestamp(Instant.now());
    submittedDetail2.setTipFlag(true);

    when(applicationDetailRepository.findByPwaApplicationId(APP_ID)).thenReturn(
        List.of(draftDetail, submittedDetail1, submittedDetail2));
    when(userTypeService.getUserTypes(user)).thenReturn(Set.of(UserType.OGA));

    var latestDetailOpt = pwaApplicationDetailService.getLatestDetailForUser(APP_ID, user);

    assertThat(latestDetailOpt)
        .isPresent()
        .contains(submittedDetail2);

  }

  @Test
  public void getLatestDetailForUser_consultee_satisfactory() {

    var draftDetail = createDetail(PwaApplicationStatus.DRAFT, 1);
    var satisfactoryDetail1 = createDetail(PwaApplicationStatus.CASE_OFFICER_REVIEW, 2);
    satisfactoryDetail1.setSubmittedTimestamp(Instant.now().minusSeconds(86400));
    satisfactoryDetail1.setConfirmedSatisfactoryTimestamp(Instant.now().minusSeconds(86400));
    var satisfactoryDetail2 = createDetail(PwaApplicationStatus.CASE_OFFICER_REVIEW, 3);
    satisfactoryDetail2.setSubmittedTimestamp(Instant.now().minusSeconds(43200));
    satisfactoryDetail2.setConfirmedSatisfactoryTimestamp(Instant.now().minusSeconds(43200));
    var submittedDetail = createDetail(PwaApplicationStatus.CASE_OFFICER_REVIEW, 4);
    submittedDetail.setSubmittedTimestamp(Instant.now());
    submittedDetail.setTipFlag(true);

    when(applicationDetailRepository.findByPwaApplicationId(APP_ID))
        .thenReturn(List.of(draftDetail, satisfactoryDetail1, satisfactoryDetail2, submittedDetail));
    when(userTypeService.getUserTypes(user)).thenReturn(Set.of(UserType.CONSULTEE));

    var latestDetailOpt = pwaApplicationDetailService.getLatestDetailForUser(APP_ID, user);

    assertThat(latestDetailOpt)
        .isPresent()
        .contains(satisfactoryDetail2);

  }

  @Test
  public void setConfirmedSatisfactoryData_allProvided() {

    var detail = new PwaApplicationDetail();

    pwaApplicationDetailService.setConfirmedSatisfactoryData(detail, "reason", wua1Person);

    verify(applicationDetailRepository, times(1)).save(detailCaptor.capture());

    var checkDetail = detailCaptor.getValue();

    assertThat(checkDetail.getConfirmedSatisfactoryByPersonId()).isEqualTo(wua1Person.getId());
    assertThat(checkDetail.getConfirmedSatisfactoryReason()).isEqualTo("reason");
    assertThat(checkDetail.getConfirmedSatisfactoryTimestamp()).isEqualTo(clock.instant());

  }

  @Test
  public void setConfirmedSatisfactoryData_noReason() {

    var detail = new PwaApplicationDetail();

    pwaApplicationDetailService.setConfirmedSatisfactoryData(detail, null, wua1Person);

    verify(applicationDetailRepository, times(1)).save(detailCaptor.capture());

    var checkDetail = detailCaptor.getValue();

    assertThat(checkDetail.getConfirmedSatisfactoryByPersonId()).isEqualTo(wua1Person.getId());
    assertThat(checkDetail.getConfirmedSatisfactoryReason()).isNull();
    assertThat(checkDetail.getConfirmedSatisfactoryTimestamp()).isEqualTo(clock.instant());

  }

  private Object getFieldValue(Field field, Object object) {
    try {
      return FieldUtils.readField(field, object, true);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(
          String.format("Failed to access field '%s' on class '%s'", field.getName(), object.getClass()));
    }
  }

  @Test
  public void getAllWithdrawnApplicationDetailsForApplication() {
    pwaApplicationDetailService.getAllWithdrawnApplicationDetailsForApplication(
        pwaApplicationDetail.getPwaApplication());
    verify(applicationDetailRepository, times(1))
        .findByPwaApplicationAndStatus(pwaApplicationDetail.getPwaApplication(), PwaApplicationStatus.WITHDRAWN);
  }

  @Test
  public void getOpenApplicationIds() {
    var pwaAppId1 = 1;
    var pwaAppId2 = 2;
    var detail1 = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, pwaAppId1);
    var detail2 = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL, pwaAppId2);

    var openStatuses = ApplicationState.IN_PROGRESS.getStatuses();
    when(applicationDetailRepository.findLastSubmittedAppDetailsWithStatusIn(openStatuses)).thenReturn(
        List.of(detail1, detail2));

    var openApplicationIds = pwaApplicationDetailService.getInProgressApplicationIds();
    assertThat(openApplicationIds).isEqualTo(List.of(
        detail1.getPwaApplication().getId(), detail2.getPwaApplication().getId()));
  }

  @Test
  public void getAllDetailsForApplication() {

    var app = new PwaApplication();
    pwaApplicationDetailService.getAllDetailsForApplication(app);
    verify(applicationDetailRepository, times(1)).findByPwaApplication(app);

  }

  @Test
  public void getAllSubmittedApplicationDetailsForApplication() {

    var app = new PwaApplication();
    pwaApplicationDetailService.getAllSubmittedApplicationDetailsForApplication(app);
    verify(applicationDetailRepository, times(1)).findByPwaApplicationAndSubmittedTimestampIsNotNull(app);

  }

  @Test
  public void getLatestSubmittedDetail_detailsFound() {

    var app = new PwaApplication();
    var subFirst = new PwaApplicationDetail();
    subFirst.setSubmittedTimestamp(Instant.now().minusSeconds(5));
    var subSecond = new PwaApplicationDetail();
    subSecond.setSubmittedTimestamp(Instant.now());

    when(applicationDetailRepository.findByPwaApplicationAndSubmittedTimestampIsNotNull(app))
        .thenReturn(List.of(subFirst, subSecond));

    var result = pwaApplicationDetailService.getLatestSubmittedDetail(app);

    assertThat(result).contains(subSecond);

  }

  @Test
  public void getLatestSubmittedDetail_noDetailsFound() {

    var app = new PwaApplication();

    when(applicationDetailRepository.findByPwaApplicationAndSubmittedTimestampIsNotNull(app)).thenReturn(List.of());

    var result = pwaApplicationDetailService.getLatestSubmittedDetail(app);

    assertThat(result).isEmpty();

  }

  @Test
  public void getDetailById_verifyRepoInteraction() {
    when(applicationDetailRepository.findById(1)).thenReturn(Optional.of(pwaApplicationDetail));
    pwaApplicationDetailService.getDetailByDetailId(1);
    verify(applicationDetailRepository).findById(1);
  }

  @Test(expected = PwaEntityNotFoundException.class)
  public void getDetailById_entityNotFound() {
    pwaApplicationDetailService.getDetailByDetailId(1);
  }

  @Test
  public void getLatestDetailsForApplications() {
    when(applicationDetailRepository.findByPwaApplicationIsInAndTipFlagIsTrue(
        List.of(pwaApplicationDetail.getPwaApplication())))
        .thenReturn(List.of(pwaApplicationDetail));
    assertThat(
        pwaApplicationDetailService.getLatestDetailsForApplications(List.of(pwaApplicationDetail.getPwaApplication())))
        .isEqualTo(List.of(pwaApplicationDetail));

    verify(applicationDetailRepository).findByPwaApplicationIsInAndTipFlagIsTrue(
        List.of(pwaApplicationDetail.getPwaApplication()));
  }

  @Test
  public void transferTipFlag_existingTipDetailIsNoLongerTip_otherTipDetailIsNowTip() {

    var currentTipDetail = new PwaApplicationDetail();
    currentTipDetail.setTipFlag(true);
    var otherDetail = new PwaApplicationDetail();
    otherDetail.setTipFlag(false);

    pwaApplicationDetailService.transferTipFlag(currentTipDetail, otherDetail);

    verify(applicationDetailRepository, times(1)).saveAll(detailsCaptor.capture());

    var details = detailsCaptor.getValue();
    assertThat(details).hasSize(2);

    assertThat(details.get(0).isTipFlag()).isFalse();
    assertThat(details.get(1).isTipFlag()).isTrue();
  }

  @Test
  public void doWithLastSubmittedDetailIfExists_lastSubmittedDetailExists_verifyDetailHandlingFunctionCalled() {

    var pwaApplication = new PwaApplication();
    var lastSubmittedDetail = new PwaApplicationDetail();
    when(applicationDetailRepository.findByPwaApplicationAndSubmittedTimestampIsNotNull(pwaApplication)).thenReturn(
        List.of(lastSubmittedDetail));

    pwaApplicationDetailService.doWithLastSubmittedDetailIfExists(pwaApplication, detailHandlerFunction);

    verify(detailHandlerFunction).accept(lastSubmittedDetail);
  }

  @Test
  public void doWithLastSubmittedDetailIfExists_firstDraft_lastSubmittedDetailDoesNotExists_noDetailProcessingDone() {

    var pwaApplication = new PwaApplication();
    pwaApplicationDetailService.doWithLastSubmittedDetailIfExists(pwaApplication, detailHandlerFunction);
    verifyNoInteractions(detailHandlerFunction);
  }

  @Test
  public void doWithCurrentUpdateRequestedDetailIfExists_tipDetailHasUpdateRequest_verifyDetailHandlingFunctionCalled() {

    var pwaApplication = new PwaApplication();
    pwaApplication.setId(1);
    var tipDetail = new PwaApplicationDetail();
    tipDetail.setStatus(PwaApplicationStatus.UPDATE_REQUESTED);
    when(applicationDetailRepository.findByPwaApplicationIdAndTipFlagIsTrue(pwaApplication.getId()))
        .thenReturn(Optional.of(tipDetail));

    pwaApplicationDetailService.doWithCurrentUpdateRequestedDetailIfExists(pwaApplication, detailHandlerFunction);

    verify(detailHandlerFunction).accept(tipDetail);
  }


  @Test
  public void doWithCurrentUpdateRequestedDetailIfExists_tipDetailDoesNotHaveUpdateRequest_noDetailProcessingDone() {

    var pwaApplication = new PwaApplication();
    pwaApplication.setId(1);
    var tipDetail = new PwaApplicationDetail();
    tipDetail.setStatus(PwaApplicationStatus.DRAFT);
    when(applicationDetailRepository.findByPwaApplicationIdAndTipFlagIsTrue(pwaApplication.getId()))
        .thenReturn(Optional.of(tipDetail));

    pwaApplicationDetailService.doWithCurrentUpdateRequestedDetailIfExists(pwaApplication, detailHandlerFunction);

    verifyNoInteractions(detailHandlerFunction);
  }

}
