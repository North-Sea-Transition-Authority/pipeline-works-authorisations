package uk.co.ogauthority.pwa.service.appprocessing.tabs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.auth.AuthenticatedUserAccount;
import uk.co.ogauthority.pwa.auth.PwaUserPrivilege;
import uk.co.ogauthority.pwa.service.appprocessing.context.PwaAppProcessingContext;

@Service
public class AppProcessingTabService {

  private final List<? extends AppProcessingTabContentService> tabContentServices;

  @Autowired
  public AppProcessingTabService(List<? extends AppProcessingTabContentService> tabContentServices) {
    this.tabContentServices = tabContentServices;
  }

  public List<AppProcessingTab> getTabsAvailableToUser(AuthenticatedUserAccount webUserAccount) {

    var tabList = new ArrayList<AppProcessingTab>();
    tabList.add(AppProcessingTab.TASKS);

    var userPrivs = webUserAccount.getUserPrivileges();

    if (userPrivs.contains(PwaUserPrivilege.PWA_REGULATOR)) {
      tabList.add(AppProcessingTab.CASE_HISTORY);
    }

    if (userPrivs.contains(PwaUserPrivilege.PWA_INDUSTRY)) {
      tabList.add(AppProcessingTab.FIRS);
    }

    return tabList;

  }

  public Map<String, ?> getTabContentModelMap(PwaAppProcessingContext appProcessingContext,
                                              AppProcessingTab tab) {

    var modelMap = new HashMap<String, Object>();

    tabContentServices.forEach(tabContentService ->
        modelMap.putAll(tabContentService.getTabContent(appProcessingContext, tab)));

    return modelMap;

  }

}
