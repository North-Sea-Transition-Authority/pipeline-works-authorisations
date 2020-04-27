package uk.co.ogauthority.pwa.service.search;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.form.fds.RestSearchItem;
import uk.co.ogauthority.pwa.model.search.SearchSelectable;

@Service
public class SearchSelectorService {

  public List<RestSearchItem> search(String searchQuery, List<? extends SearchSelectable> selectableList) {
    return selectableList.stream()
        .filter(searchSelectable ->
            searchSelectable.getSelectionText()
                .toLowerCase()
                .contains(StringUtils.defaultIfBlank(searchQuery, "").toLowerCase()))
        .map(item -> new RestSearchItem(item.getSelectionId(), item.getSelectionText()))
        .collect(Collectors.toList());
  }

  public List<RestSearchItem> addManualEntry(String searchQuery, List<RestSearchItem> resultList) {
    if (!StringUtils.isBlank(searchQuery)) {
      resultList.add(new RestSearchItem("FT_" + searchQuery, searchQuery));
    }
    return resultList;
  }

}
