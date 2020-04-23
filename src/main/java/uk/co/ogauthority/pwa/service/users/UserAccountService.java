package uk.co.ogauthority.pwa.service.users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.energyportal.model.entity.WebUserAccount;
import uk.co.ogauthority.pwa.energyportal.repository.WebUserAccountRepository;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;

@Service
public class UserAccountService {

  private final WebUserAccountRepository webUserAccountRepository;

  @Autowired
  public UserAccountService(WebUserAccountRepository webUserAccountRepository) {
    this.webUserAccountRepository = webUserAccountRepository;
  }

  public WebUserAccount getWebUserAccount(int wuaId) {
    return webUserAccountRepository.findById(wuaId)
        .orElseThrow(() -> new PwaEntityNotFoundException("Cannot find wua id: " + wuaId));
  }
}
