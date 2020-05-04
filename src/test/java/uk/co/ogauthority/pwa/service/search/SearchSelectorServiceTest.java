package uk.co.ogauthority.pwa.service.search;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import uk.co.ogauthority.pwa.model.entity.devuk.DevukFacility;
import uk.co.ogauthority.pwa.model.form.fds.RestSearchItem;
import uk.co.ogauthority.pwa.model.search.SearchSelectable;

public class SearchSelectorServiceTest {

  private SearchSelectorService searchSelectorService;

  @Before
  public void setUp() {
    searchSelectorService = new SearchSelectorService();
  }

  @Test
  public void search_NoMatch() {
    var searchableResults = List.of(
        new DevukFacility(1, "facility")
    );
    var result = searchSelectorService.search("should not match", searchableResults);
    assertThat(result).isEmpty();
  }

  @Test
  public void search_SearchableEmpty() {
    List<SearchSelectable> searchableResults = List.of();
    var result = searchSelectorService.search("should not match", searchableResults);
    assertThat(result).isEmpty();
  }
  @Test
  public void search_Match() {
    var devukFacility = new DevukFacility(1, "facility");
    var searchableResults = List.of(devukFacility);
    var result = searchSelectorService.search("fa", searchableResults);
    assertThat(result).extracting(RestSearchItem::getId)
        .containsExactly(String.valueOf(devukFacility.getId()));
  }


  @Test
  public void addManualEntry() {
    var searchableResults = searchSelectorService.addManualEntry("free_text", new ArrayList<>());
    assertThat(searchableResults).extracting(RestSearchItem::getId)
        .containsExactly(SearchSelectable.FREE_TEXT_PREFIX + "free_text");
  }
}