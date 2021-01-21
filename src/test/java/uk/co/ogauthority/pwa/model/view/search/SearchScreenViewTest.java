package uk.co.ogauthority.pwa.model.view.search;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.stream.IntStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.co.ogauthority.pwa.model.view.search.consents.ConsentSearchResultView;
import uk.co.ogauthority.pwa.testutils.ConsentSearchItemTestUtils;

@RunWith(SpringJUnit4ClassRunner.class)
public class SearchScreenViewTest {

  @Test
  public void haveResultsBeenLimited_fullCountEqualsResultsSize_no() {

    var bigList = new ArrayList<ConsentSearchResultView>();

    // 10 results
    IntStream.rangeClosed(1, 10).forEach(i -> {
      var item = ConsentSearchItemTestUtils.createSearchItem(i, "PENGUIN" + i, "SHELL" + i, Instant.now().minus(i, ChronoUnit.DAYS));
      bigList.add(ConsentSearchResultView.fromSearchItem(item));
    });

    var screenView = new SearchScreenView(10, bigList);

    assertThat(screenView.resultsHaveBeenLimited()).isFalse();

  }

  @Test
  public void haveResultsBeenLimited_fullCountMoreThanResults_yes() {

    var bigList = new ArrayList<ConsentSearchResultView>();

    // 10 results
    IntStream.rangeClosed(1, 10).forEach(i -> {
      var item = ConsentSearchItemTestUtils.createSearchItem(i, "PENGUIN" + i, "SHELL" + i, Instant.now().minus(i, ChronoUnit.DAYS));
      bigList.add(ConsentSearchResultView.fromSearchItem(item));
    });

    var screenView = new SearchScreenView(100, bigList);

    assertThat(screenView.resultsHaveBeenLimited()).isTrue();

  }

}