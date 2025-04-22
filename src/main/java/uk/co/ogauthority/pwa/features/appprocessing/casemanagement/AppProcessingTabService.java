package uk.co.ogauthority.pwa.features.appprocessing.casemanagement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.features.appprocessing.authorisation.context.PwaAppProcessingContext;
import uk.co.ogauthority.pwa.teams.TeamQueryService;
import uk.co.ogauthority.pwa.teams.TeamType;

@Service
public class AppProcessingTabService {

  private final List<? extends AppProcessingTabContentService> tabContentServices;
  private final TeamQueryService teamQueryService;

  @Autowired
  public AppProcessingTabService(List<? extends AppProcessingTabContentService> tabContentServices,
                                 TeamQueryService teamQueryService) {
    this.tabContentServices = tabContentServices;
    this.teamQueryService = teamQueryService;
  }

  public List<AppProcessingTab> getTabsAvailableToUser(AuthenticatedUserAccount webUserAccount) {

    var tabList = new ArrayList<AppProcessingTab>();
    tabList.add(AppProcessingTab.TASKS);

    if (teamQueryService.userIsMemberOfStaticTeam((long) webUserAccount.getWuaId(), TeamType.REGULATOR)) {
      tabList.add(AppProcessingTab.CASE_HISTORY);
    }

    return tabList;

  }

  public Map<String, Object> getTabContentModelMap(PwaAppProcessingContext appProcessingContext,
                                              AppProcessingTab tab) {

    var modelMap = new HashMap<String, Object>();

    tabContentServices.forEach(tabContentService ->
        modelMap.putAll(tabContentService.getTabContent(appProcessingContext, tab)));

    return modelMap;

  }

}
