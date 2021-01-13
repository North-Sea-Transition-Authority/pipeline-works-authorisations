package uk.co.ogauthority.pwa.service.search.consents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.ogauthority.pwa.model.entity.search.consents.ConsentSearchItem;
import uk.co.ogauthority.pwa.model.view.search.consents.ConsentSearchResultView;
import uk.co.ogauthority.pwa.testutils.ConsentSearchItemTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class ConsentSearchServiceTest {

  @Mock
  private ConsentSearcher consentSearcher;

  private ConsentSearchService consentSearchService;

  private ConsentSearchItem item1, item2, item3;

  @Before
  public void setUp() throws Exception {

    consentSearchService = new ConsentSearchService(consentSearcher);

    item1 = ConsentSearchItemTestUtils.createSearchItem(1, "PENGUIN", "SHELL", Instant.now().minus(18, ChronoUnit.DAYS));
    item2 = ConsentSearchItemTestUtils.createSearchItem(3, "Interconnector", "BP", Instant.now().minus(365, ChronoUnit.DAYS));
    item3 = ConsentSearchItemTestUtils.createSearchItem(2, "GAWAIN", "WINTERSHALL", Instant.now().minus(67, ChronoUnit.DAYS));

    when(consentSearcher.findAll()).thenReturn(List.of(item1, item2, item3));

  }

  @Test
  public void search_resultsNotLimited_sortedByIdDesc() {

    var results = consentSearchService.search();

    // sorted id desc
    var resultViewComparisonList = List.of(
        ConsentSearchResultView.fromSearchItem(item2),
        ConsentSearchResultView.fromSearchItem(item3),
        ConsentSearchResultView.fromSearchItem(item1)
    );

    assertThat(results).containsExactlyElementsOf(resultViewComparisonList);

  }

  @Test
  public void search_resultsLimited() {

    var bigList = new ArrayList<ConsentSearchItem>();

    IntStream.rangeClosed(1, ConsentSearchService.MAX_RESULTS_SIZE + 20).forEach(i -> {
      var item = ConsentSearchItemTestUtils.createSearchItem(i, "PENGUIN" + i, "SHELL" + i, Instant.now().minus(i, ChronoUnit.DAYS));
      bigList.add(item);
    });

    when(consentSearcher.findAll()).thenReturn(bigList);

    var results = consentSearchService.search();

    // results limited to max size
    assertThat(results.size()).isEqualTo(ConsentSearchService.MAX_RESULTS_SIZE);

  }

  @Test
  public void haveResultsBeenLimited_equalsMax_yes() {

    var bigList = new ArrayList<ConsentSearchItem>();

    IntStream.rangeClosed(1, ConsentSearchService.MAX_RESULTS_SIZE).forEach(i -> {
      var item = ConsentSearchItemTestUtils.createSearchItem(i, "PENGUIN" + i, "SHELL" + i, Instant.now().minus(i, ChronoUnit.DAYS));
      bigList.add(item);
    });

    when(consentSearcher.findAll()).thenReturn(bigList);

    var results = consentSearchService.search();

    assertThat(bigList.size()).isEqualTo(ConsentSearchService.MAX_RESULTS_SIZE);
    assertThat(consentSearchService.haveResultsBeenLimited(results)).isTrue();

  }

  @Test
  public void haveResultsBeenLimited_notEqualsMax_no() {

    var results = consentSearchService.search();

    assertThat(results.size()).isLessThan(ConsentSearchService.MAX_RESULTS_SIZE);
    assertThat(consentSearchService.haveResultsBeenLimited(results)).isFalse();

  }

}