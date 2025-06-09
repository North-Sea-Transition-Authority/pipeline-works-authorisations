package uk.co.ogauthority.pwa.features.webapp.devtools.testharness;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccount;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.external.WebUserAccountStatus;
import uk.co.ogauthority.pwa.integrations.energyportal.webuseraccount.internal.WebUserAccountRepository;

@Service
@Profile("test-harness")
public class TestHarnessUserRetrievalService {

  private final WebUserAccountRepository webUserAccountRepository;


  @Autowired
  public TestHarnessUserRetrievalService(WebUserAccountRepository webUserAccountRepository) {
    this.webUserAccountRepository = webUserAccountRepository;
  }

  public WebUserAccount getWebUserAccount(Integer wuaId) {

    return webUserAccountRepository.findAllByWuaIdAndAccountStatusIn(wuaId, List.of(WebUserAccountStatus.ACTIVE, WebUserAccountStatus.NEW))
        .stream()
        .findAny()
        .orElseThrow(() -> new PwaEntityNotFoundException("Could not find web user account with id: " + wuaId));
  }
}
