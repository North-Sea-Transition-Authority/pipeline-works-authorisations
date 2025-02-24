package uk.co.ogauthority.pwa.service.searchselector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.ogauthority.pwa.integrations.energyportal.devukfacilities.controller.DevukRestController;
import uk.co.ogauthority.pwa.integrations.energyportal.devukfacilities.external.DevukFacility;
import uk.co.ogauthority.pwa.model.form.fds.RestSearchItem;
import uk.co.ogauthority.pwa.model.searchselector.SearchSelectable;

class SearchSelectorServiceTest {

  private SearchSelectorService searchSelectorService;

  @BeforeEach
  void setUp() {
    searchSelectorService = new SearchSelectorService();
  }

  @Test
  void search_NoMatch() {
    var searchableResults = List.of(
        new DevukFacility(1, "facility")
    );
    var result = searchSelectorService.search("should not match", searchableResults);
    assertThat(result).isEmpty();
  }

  @Test
  void search_SearchableEmpty() {
    List<SearchSelectable> searchableResults = List.of();
    var result = searchSelectorService.search("should not match", searchableResults);
    assertThat(result).isEmpty();
  }

  @Test
  void search_Match() {
    var devukFacility = new DevukFacility(1, "facility");
    var searchableResults = List.of(devukFacility);
    var result = searchSelectorService.search("fa", searchableResults);
    assertThat(result).extracting(RestSearchItem::getId)
        .containsExactly(String.valueOf(devukFacility.getId()));
  }


  @Test
  void addManualEntry() {
    var searchableResults = searchSelectorService.addManualEntry("free_text", new ArrayList<>());
    assertThat(searchableResults).extracting(RestSearchItem::getId)
        .containsExactly(SearchSelectable.FREE_TEXT_PREFIX + "free_text");
  }

  @Test
  void addManualEntry_noFreeText() {
    var searchableResults = searchSelectorService.addManualEntry("free_text", new ArrayList<>(), ManualEntryAttribute.NO_FREE_TEXT_PREFIX);
    assertThat(searchableResults).extracting(RestSearchItem::getId)
        .containsExactly("free_text");
  }

  @Test
  void buildPrepopulatedSelections() {
    var prefix = SearchSelectable.FREE_TEXT_PREFIX;
    var selections = List.of(prefix + "Test", "1", "2");
    var resolvedMap = new HashMap<String, String>(){{
      put("1", "One");
      put("2", "Two");
    }};
    var result = searchSelectorService.buildPrepopulatedSelections(selections, resolvedMap);
    assertThat(result).containsExactly(
        entry(prefix + "Test", "Test"),
        entry("1", "One"),
        entry("2", "Two")
    );
  }

  @Test
  void removePrefix() {
    var str = SearchSelectable.FREE_TEXT_PREFIX + "Test";
    assertThat(searchSelectorService.removePrefix(str)).isEqualTo("Test");
  }

  @Test
  void route() {
    var routeOn = on(DevukRestController.class).searchFacilities(null);
    var route = SearchSelectorService.route(routeOn);
    assertThat(route).doesNotEndWith("term");
  }
}