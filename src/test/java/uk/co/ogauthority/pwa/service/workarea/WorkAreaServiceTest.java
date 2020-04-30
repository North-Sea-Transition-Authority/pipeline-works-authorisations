package uk.co.ogauthority.pwa.service.workarea;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.model.entity.pwaapplications.search.ApplicationDetailSearchItem;
import uk.co.ogauthority.pwa.service.enums.masterpwas.contacts.PwaContactRole;
import uk.co.ogauthority.pwa.service.enums.pwaapplications.PwaApplicationStatus;
import uk.co.ogauthority.pwa.service.pwaapplications.PwaApplicationRedirectService;
import uk.co.ogauthority.pwa.service.pwaapplications.contacts.PwaContactService;
import uk.co.ogauthority.pwa.service.pwaapplications.search.ApplicationDetailSearcher;
import uk.co.ogauthority.pwa.service.pwaapplications.search.ApplicationSearchTestUtil;

@RunWith(MockitoJUnitRunner.class)
public class WorkAreaServiceTest {

  @Mock
  private PwaContactService pwaContactService;
  @Mock
  private ApplicationDetailSearcher applicationDetailSearcher;
  @Mock
  private PwaApplicationRedirectService pwaApplicationRedirectService;

  private WorkAreaService workAreaService;

  private WebUserAccount wua = new WebUserAccount(10);

  @Before
  public void setup() {

    workAreaService = new WorkAreaService(
        pwaContactService,
        applicationDetailSearcher,
        pwaApplicationRedirectService);

  }

  @Test
  public void getWorkAreaResultPage_zeroResults() {

    var requestedPage = 0;

    var fakePage = new PageImpl<ApplicationDetailSearchItem>(List.of(), getDefaultWorkAreaPageable(requestedPage), 0);
    when(applicationDetailSearcher.search(any(), any())).thenReturn(fakePage);

    var workareaPage = workAreaService.getWorkAreaResultPage(wua, WorkAreaTab.OPEN, requestedPage);
    assertThat(workareaPage.getTotalElements()).isEqualTo(0);
    verify(pwaContactService, times(1)).getPwaContactRolesForWebUserAccount(
        wua,
        EnumSet.of(PwaContactRole.PREPARER)
    );

    verify(applicationDetailSearcher, times(1)).search(
        getDefaultWorkAreaPageable(requestedPage),
        Set.of()
    );

    verifyNoInteractions(pwaApplicationRedirectService);
  }

  @Test
  public void getWorkAreaResultPage_viewUrlWhenApplicationStatusDraft() {

    var requestedPage = 0;
    var searchItem = ApplicationSearchTestUtil.getSearchDetailItem(PwaApplicationStatus.DRAFT);

    setupFakeApplicationSearchResultPage(List.of(searchItem), requestedPage);

    var workareaPage = workAreaService.getWorkAreaResultPage(wua, WorkAreaTab.OPEN, requestedPage);
    assertThat(workareaPage.getTotalElements()).isEqualTo(1);
    verify(pwaContactService, times(1)).getPwaContactRolesForWebUserAccount(
        wua,
        EnumSet.of(PwaContactRole.PREPARER)
    );

    verify(applicationDetailSearcher, times(1)).search(
        getDefaultWorkAreaPageable(requestedPage),
        Set.of()
    );

    verify(pwaApplicationRedirectService, times(1))
        .getTaskListRoute(searchItem.getPwaApplicationId(), searchItem.getApplicationType());

  }

  @Test
  public void getWorkAreaResultPage_viewUrlWhenApplicationStatusInitialSubmission() {

    var requestedPage = 0;
    var searchItem = ApplicationSearchTestUtil.getSearchDetailItem(PwaApplicationStatus.INITIAL_SUBMISSION_REVIEW);

    setupFakeApplicationSearchResultPage(List.of(searchItem), requestedPage);

    var workareaPage = workAreaService.getWorkAreaResultPage(wua, WorkAreaTab.OPEN, requestedPage);
    assertThat(workareaPage.getTotalElements()).isEqualTo(1);
    verify(pwaContactService, times(1)).getPwaContactRolesForWebUserAccount(
        wua,
        EnumSet.of(PwaContactRole.PREPARER)
    );

    verify(applicationDetailSearcher, times(1)).search(
        getDefaultWorkAreaPageable(requestedPage),
        Set.of()
    );

    verifyNoInteractions(pwaApplicationRedirectService);

  }

  private Pageable getDefaultWorkAreaPageable(int requestedPage) {
    return PageRequest.of(requestedPage, WorkAreaService.PAGE_SIZE,
        Sort.by(Sort.Direction.DESC, "padCreatedTimestamp"));
  }

  private Page<ApplicationDetailSearchItem> setupFakeApplicationSearchResultPage(List<ApplicationDetailSearchItem> results, int page){
    var fakePage = new PageImpl<>(
        results,
        getDefaultWorkAreaPageable(page),
        results.size());

    when(applicationDetailSearcher.search(any(), any())).thenReturn(fakePage);

    return fakePage;
  }

}