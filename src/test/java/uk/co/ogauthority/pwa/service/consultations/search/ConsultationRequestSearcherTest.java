package uk.co.ogauthority.pwa.service.consultations.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import uk.co.ogauthority.pwa.repository.consultations.search.ConsultationRequestSearchItemRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.ConsultationRequestStatus;
import uk.co.ogauthority.pwa.testutils.ConsulteeGroupTestingUtils;

@ExtendWith(MockitoExtension.class)
class ConsultationRequestSearcherTest {

  private static final int PERSON_ID = 10;
  private static final int APP_ID = 20;

  private static final int PAGE_REQUESTED = 0;
  private static final int PAGE_SIZE = 10;

  @Mock
  private ConsultationRequestSearchItemRepository consultationRequestSearchItemRepository;

  private ConsultationRequestSearcher consultationRequestSearcher;
  private Pageable pageable;

  @BeforeEach
  void setup() {

    consultationRequestSearcher = new ConsultationRequestSearcher(consultationRequestSearchItemRepository);
    pageable = PageRequest.of(PAGE_REQUESTED, PAGE_SIZE);
  }

  @Test
  void searchByAllocationForGroupIdsOrConsultationRequestIds_whenNoIds() {
    var resultPage = consultationRequestSearcher.searchByStatusForGroupIdsOrConsultationRequestIds(pageable,
        ConsultationRequestStatus.ALLOCATION, null, Set.of());
    assertThat(resultPage).isEqualTo(Page.empty(PageRequest.of(PAGE_REQUESTED, PAGE_SIZE)));
  }

  @Test
  void searchByAllocationForGroupIdsOrConsultationRequestIds_idsProvided() {

    var result = ConsultationRequestSearchTestUtil.getSearchDetailItem(ConsultationRequestStatus.ALLOCATION);

    var fakePageResult = ConsultationRequestSearchTestUtil.setupFakeConsultationSearchResultPage(
        List.of(result),
        pageable
    );

    var groupDetail = ConsulteeGroupTestingUtils.createConsulteeGroup("test", "t");
    var cgId = groupDetail.getConsulteeGroupId();

    when(consultationRequestSearchItemRepository.getAllByConsultationRequestStatusIsAndConsulteeGroupIdEqualsOrConsultationRequestIdIn(
        any(),
        eq(ConsultationRequestStatus.ALLOCATION),
        eq(groupDetail.getConsulteeGroupId()),
        eq(Set.of(APP_ID)))).thenReturn(fakePageResult);

    var resultPage = consultationRequestSearcher.searchByStatusForGroupIdsOrConsultationRequestIds(pageable,
        ConsultationRequestStatus.ALLOCATION, cgId, Set.of(APP_ID));

    verify(consultationRequestSearchItemRepository, times(1))
        .getAllByConsultationRequestStatusIsAndConsulteeGroupIdEqualsOrConsultationRequestIdIn(
            pageable,
            ConsultationRequestStatus.ALLOCATION,
            cgId,
            Set.of(APP_ID));

    assertThat(resultPage).isEqualTo(fakePageResult);

  }

}