package uk.co.ogauthority.pwa.service.pwaapplications;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
import java.util.concurrent.atomic.AtomicBoolean;
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
import uk.co.ogauthority.pwa.energyportal.model.entity.Person;
import uk.co.ogauthority.pwa.energyportal.model.entity.PersonId;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.enums.pipelineotherproperties.PropertyPhase;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplication;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.PwaApplicationDetail;
import uk.co.ogauthority.pwa.model.form.pwaapplications.shared.partnerletters.PartnerLettersForm;
import uk.co.ogauthority.pwa.repository.pwaapplications.PwaApplicationDetailRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationType;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.PadFastTrackService;
import uk.co.ogauthority.pwa.testutils.ObjectTestUtils;
import uk.co.ogauthority.pwa.testutils.PwaApplicationTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class PwaApplicationDetailServiceTest {

  private static final int WUA_ID_1 = 1;
  private static final int WUA_ID_2 = 2;
  private static final PersonId WUA_1_PERSON_ID = new PersonId(10);
  private static final PersonId WUA_2_PERSON_ID = new PersonId(20);
  private static int APP_ID = 1;

  @Mock
  private PwaApplicationDetailRepository applicationDetailRepository;

  @Mock
  private PadFastTrackService fastTrackService;

  @Captor
  private ArgumentCaptor<PwaApplicationDetail> detailCaptor;

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

    when(applicationDetailRepository.findByPwaApplicationIdAndStatusAndTipFlagIsTrue(APP_ID, PwaApplicationStatus.DRAFT))
        .thenReturn(Optional.of(pwaApplicationDetail));

    when(applicationDetailRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    pwaApplicationDetailService = new PwaApplicationDetailService(applicationDetailRepository, clock, fastTrackService);
  }

  @Test
  public void withDraftTipDetail() {
    AtomicBoolean functionApplied = new AtomicBoolean(false);
    pwaApplicationDetailService.withDraftTipDetail(APP_ID, user, detail -> {
      assertThat(detail).isEqualTo(pwaApplicationDetail);
      functionApplied.set(true);
      return null;
    });
    assertThat(functionApplied.get()).isEqualTo(true);
  }

  @Test
  public void getTipDetailWithStatus() {
    var detail = pwaApplicationDetailService.getTipDetailWithStatus(APP_ID, PwaApplicationStatus.DRAFT);
    assertThat(detail).isEqualTo(pwaApplicationDetail);
  }

  @Test
  public void setLinkedToFields_isLinked() {

    when(applicationDetailRepository.save(pwaApplicationDetail)).thenReturn(pwaApplicationDetail);

    var detail = pwaApplicationDetailService.setLinkedToFields(pwaApplicationDetail, true);
    assertThat(detail).isEqualTo(pwaApplicationDetail);
    assertThat(detail.getLinkedToField()).isTrue();
    assertNull(detail.getNotLinkedDescription());
  }

  @Test
  public void setLinkedToFields_notLinked() {
    pwaApplicationDetail.setNotLinkedDescription("test description");
    when(applicationDetailRepository.save(pwaApplicationDetail)).thenReturn(pwaApplicationDetail);

    var detail = pwaApplicationDetailService.setLinkedToFields(pwaApplicationDetail, false);
    assertThat(detail).isEqualTo(pwaApplicationDetail);
    assertThat(detail.getLinkedToField()).isFalse();
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
        alternativeWua
    );

    assertThat(submittedDetail.getStatusLastModifiedTimestamp()).isEqualTo(clock.instant());
    assertThat(submittedDetail.getStatusLastModifiedByWuaId()).isEqualTo(alternativeWua.getWuaId());
    assertThat(submittedDetail.getStatus()).isEqualTo(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW);
    assertThat(submittedDetail.getSubmittedTimestamp()).isEqualTo(clock.instant());
    assertThat(submittedDetail.getSubmittedByPersonId()).isEqualTo(wua2Person.getId());

    assertThat(submittedDetail.getSubmittedAsFastTrackFlag()).isTrue();

  }

  @Test
  public void setInitialReviewApproved() {

    var detail = PwaApplicationTestUtil.createDefaultApplicationDetail(PwaApplicationType.INITIAL);
    detail.setStatus(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW);

    pwaApplicationDetailService.setInitialReviewApproved(detail, user);

    verify(applicationDetailRepository, times(2)).save(detail);

    assertThat(detail.getInitialReviewApprovedByWuaId()).isEqualTo(user.getWuaId());
    assertThat(detail.getInitialReviewApprovedTimestamp()).isEqualTo(clock.instant());
    assertThat(detail.getStatus()).isEqualTo(PwaApplicationStatus.CASE_OFFICER_REVIEW);

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

    var newDetail = pwaApplicationDetailService.createNewTipDetail(pwaApplicationDetail, user);

    // Test Old Detail
    assertThat(pwaApplicationDetail.isTipFlag()).isFalse();

    // Test new detail

    assertThat(newDetail.getPwaApplication()).isEqualTo(pwaApplicationDetail.getPwaApplication());
    assertThat(newDetail.getId()).isNotEqualTo(pwaApplicationDetail.getId());
    assertThat(newDetail.isFirstVersion()).isFalse();
    assertThat(newDetail.isTipFlag()).isTrue();
    assertThat(newDetail.getCreatedTimestamp()).isAfter(pwaApplicationDetail.getCreatedTimestamp());
    assertThat(newDetail.getStatusLastModifiedTimestamp()).isEqualTo(newDetail.getCreatedTimestamp());
    assertThat(newDetail.getStatusLastModifiedByWuaId()).isNotEqualTo(pwaApplicationDetail.getStatusLastModifiedByWuaId());
    assertThat(newDetail.getCreatedByWuaId()).isNotEqualTo(pwaApplicationDetail.getCreatedByWuaId());
    // sets new detail to DRAFT
    assertThat(newDetail.getStatus()).isEqualTo(PwaApplicationStatus.DRAFT);

    var ignoredFields = List.of("id", "status", "tipFlag", "versionNo", "createdTimestamp", "statusLastModifiedTimestamp", "statusLastModifiedByWuaId", "createdByWuaId");

    var nullFields = List.of("id", "submittedByPersonId", "submittedTimestamp", "initialReviewApprovedByWuaId", "initialReviewApprovedTimestamp", "submittedAsFastTrackFlag");

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
    detail.setLinkedToField(true);
    detail.setNotLinkedDescription("NOT LINKED DESC");
    detail.setPipelinesCrossed(true);
    detail.setCablesCrossed(true);
    detail.setMedianLineCrossed(true);
    detail.setNumOfHolders(1);
    detail.setPipelinePhaseProperties(Set.of(PropertyPhase.OTHER));
    detail.setOtherPhaseDescription("OTHER PHASE DESC");
    detail.setPartnerLettersRequired(true);
    detail.setPartnerLettersConfirmed(true);
    detail.setCreatedByWuaId(wua.getWuaId());
    detail.setCreatedTimestamp(baseTime);
    detail.setSubmittedByPersonId(wua.getLinkedPerson().getId());
    detail.setSubmittedTimestamp(baseTime);
    detail.setStatusLastModifiedByWuaId(wua.getWuaId());
    detail.setStatusLastModifiedTimestamp(baseTime);
    detail.setInitialReviewApprovedByWuaId(wua.getWuaId());
    detail.setInitialReviewApprovedTimestamp(baseTime);
    detail.setSupplementaryDocumentsFlag(true);

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
      throw new AssertionError(String.format("Expected all fields to be given a value, but [%s] is/are null", String.join(",", nullFields)));
    }
  }

  @Test
  public void getLastSubmittedApplicationDetail_notFound(){

    assertThat(pwaApplicationDetailService.getLastSubmittedApplicationDetail(APP_ID)).isEmpty();
    verify(applicationDetailRepository, times(1)).findLastSubmittedApplicationDetail(APP_ID);
  }

  @Test
  public void getLastSubmittedApplicationDetail_found(){
    when(applicationDetailRepository.findLastSubmittedApplicationDetail(APP_ID)).thenReturn(Optional.of(pwaApplicationDetail));
    assertThat(pwaApplicationDetailService.getLastSubmittedApplicationDetail(APP_ID)).contains(pwaApplicationDetail);

  }

  private Object getFieldValue(Field field, Object object) {
    try {
      return FieldUtils.readField(field, object, true);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(
          String.format("Failed to access field '%s' on class '%s'", field.getName(), object.getClass()));
    }
  }
}