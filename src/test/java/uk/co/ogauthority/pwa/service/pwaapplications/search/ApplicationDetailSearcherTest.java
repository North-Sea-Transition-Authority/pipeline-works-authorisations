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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import uk.co.ogauthority.pwa.repository.pwaapplications.search.ApplicationDetailSearchItemRepository;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaApplicationContactRoleDto;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationDetailSearcherTest {

  private static final int PERSON_ID = 10;
  private static final int APP_ID = 20;

  private static final int PAGE_REQUESTED = 0;
  private static final int PAGE_SIZE = 10;

  @Mock
  private ApplicationDetailSearchItemRepository applicationDetailSearchItemRepository;

  private ApplicationDetailSearcher applicationDetailSearcher;
  private Pageable pageable;

  @Before
  public void setup() {

    applicationDetailSearcher = new ApplicationDetailSearcher(applicationDetailSearchItemRepository);
    pageable = PageRequest.of(PAGE_REQUESTED, PAGE_SIZE);
  }

  @Test
  public void searchByPwaContacts_whenNoContactRoles() {
    var resultPage = applicationDetailSearcher.searchByPwaContacts(pageable, Set.of());
    assertThat(resultPage).isEqualTo(Page.empty(PageRequest.of(PAGE_REQUESTED, PAGE_SIZE)));

  }

  @Test
  public void searchByPwaContacts_whenContactRolesProvided() {
    var result = ApplicationSearchTestUtil.getSearchDetailItem(PwaApplicationStatus.DRAFT);

    var fakePageResult = ApplicationSearchTestUtil.setupFakeApplicationSearchResultPage(
        List.of(result),
        pageable

    );
    var contactRoles = Set.of(new PwaApplicationContactRoleDto(PERSON_ID, APP_ID, PwaContactRole.PREPARER));
    when(applicationDetailSearchItemRepository.findAllByTipFlagIsTrueAndPwaApplicationIdIn(any(), eq(Set.of(APP_ID))))
        .thenReturn(fakePageResult);

    var resultPage = applicationDetailSearcher.searchByPwaContacts(pageable, contactRoles);


    verify(applicationDetailSearchItemRepository, times(1))
        .findAllByTipFlagIsTrueAndPwaApplicationIdIn(pageable, Set.of(APP_ID));
    assertThat(resultPage).isEqualTo(fakePageResult);
  }

  @Test
  public void searchByStatus_whenApplicationSearchItemFound() {
    var result = ApplicationSearchTestUtil.getSearchDetailItem(PwaApplicationStatus.DRAFT);

    var fakePageResult = ApplicationSearchTestUtil.setupFakeApplicationSearchResultPage(
        List.of(result),
        pageable
    );
    var statusFilter = Set.of(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW);

    when(applicationDetailSearchItemRepository.findAllByTipFlagIsTrueAndAndPadStatusIn(any(), eq(Set.of(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW))))
        .thenReturn(fakePageResult);

    var resultPage = applicationDetailSearcher.searchByStatus(pageable, statusFilter);

    verify(applicationDetailSearchItemRepository, times(1))
        .findAllByTipFlagIsTrueAndAndPadStatusIn(pageable, Set.of(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW));

    assertThat(resultPage).isEqualTo(fakePageResult);
  }

  @Test
  public void searchByStatus_whenNoStatusInFilter() {

    var resultPage = applicationDetailSearcher.searchByStatus(pageable, Set.of());

    verifyNoInteractions(applicationDetailSearchItemRepository);

    assertThat(resultPage).isEqualTo(Page.empty(pageable));
  }
}