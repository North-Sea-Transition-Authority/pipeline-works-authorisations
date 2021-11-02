package uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.internal.WebUserAccountRepository;

@Service
public class UserAccountService {

  private final int systemUserWuaId;

  private final WebUserAccountRepository webUserAccountRepository;

  @Autowired
  public UserAccountService(@Value("${pwa.global.system-user-wua-id}") int systemUserWuaId,
                            WebUserAccountRepository webUserAccountRepository) {
    this.systemUserWuaId = systemUserWuaId;
    this.webUserAccountRepository = webUserAccountRepository;
  }

  public WebUserAccount getWebUserAccount(int wuaId) {
    return webUserAccountRepository.findById(wuaId)
        .orElseThrow(() -> new PwaEntityNotFoundException("Cannot find wua id: " + wuaId));
  }

  public WebUserAccount getSystemWebUserAccount() {
    return getWebUserAccount(systemUserWuaId);
  }
}
