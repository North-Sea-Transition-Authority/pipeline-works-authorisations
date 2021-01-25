package uk.co.ogauthority.pwa.service.pwaapplications.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import uk.co.ogauthority.pwa.repository.pwaapplications.search.WorkAreaApplicationDetailSearchItemRepository;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;

@RunWith(MockitoJUnitRunner.class)
public class WorkAreaApplicationDetailSearcherTest {

  private static final int PERSON_ID = 10;
  private static final int APP_ID = 20;

  private static final int PAGE_REQUESTED = 0;
  private static final int PAGE_SIZE = 10;

  @Mock
  private EntityManager entityManager;

  @Mock
  private WorkAreaApplicationDetailSearchItemRepository workAreaApplicationDetailSearchItemRepository;

  private WorkAreaApplicationDetailSearcher workAreaApplicationDetailSearcher;
  private Pageable pageable;

  @Before
  public void setup() {

    workAreaApplicationDetailSearcher = new WorkAreaApplicationDetailSearcher(entityManager,
        workAreaApplicationDetailSearchItemRepository);
    pageable = PageRequest.of(PAGE_REQUESTED, PAGE_SIZE);
  }


  @Test
  public void searchByStatus_whenApplicationSearchItemFound() {
    var result = WorkAreaApplicationSearchTestUtil.getSearchDetailItem(PwaApplicationStatus.DRAFT);

    var fakePageResult = WorkAreaApplicationSearchTestUtil.setupFakeApplicationSearchResultPage(
        List.of(result),
        pageable
    );
    var statusFilter = Set.of(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW);

    when(workAreaApplicationDetailSearchItemRepository.findAllByTipFlagIsTrueAndPadStatusIn(any(),
        eq(Set.of(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW))))
        .thenReturn(fakePageResult);

    var resultPage = workAreaApplicationDetailSearcher.searchByStatus(pageable, statusFilter);

    verify(workAreaApplicationDetailSearchItemRepository, times(1))
        .findAllByTipFlagIsTrueAndPadStatusIn(pageable, Set.of(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW));

    assertThat(resultPage).isEqualTo(fakePageResult);
  }

  @Test
  public void searchByStatus_whenNoStatusInFilter() {

    var resultPage = workAreaApplicationDetailSearcher.searchByStatus(pageable, Set.of());

    verifyNoInteractions(workAreaApplicationDetailSearchItemRepository);

    assertThat(resultPage).isEqualTo(Page.empty(pageable));
  }

  @Test
  public void searchByStatusOrApplicationIdsAndWhereAllProcessingWaitFlagsFalse_serviceInteractions_whenFiltersHaveContent() {

    var resultPage = workAreaApplicationDetailSearcher.searchByStatusOrApplicationIdsAndWhereTipSatisfactoryFlagIsFalseOrAllProcessingWaitFlagsFalse(
        pageable,
        Set.of(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW),
        Set.of(1, 2, 3)
    );

    verify(workAreaApplicationDetailSearchItemRepository, times(1)).findAllByPadStatusInOrPwaApplicationIdInAndWhereTipSatisfactoryFlagEqualsOrAllWaitFlagsMatch(
        pageable,
        Set.of(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW),
        Set.of(1, 2, 3),
        false,
        false,
        false,
        false
    );
  }

  @Test
  public void searchByStatusOrApplicationIdsAndWhereAllProcessingWaitFlagsFalse_serviceInteractions_whenFiltersHaveNoContent() {

    var resultPage = workAreaApplicationDetailSearcher.searchByStatusOrApplicationIdsAndWhereTipSatisfactoryFlagIsFalseOrAllProcessingWaitFlagsFalse(
        pageable,
        Set.of(),
        Set.of()
    );

    verify(workAreaApplicationDetailSearchItemRepository, times(0)).findAllByPadStatusInOrPwaApplicationIdIn(
        any(), any(), any()
    );

    assertThat(resultPage).isEqualTo(Page.empty(pageable));
  }
}