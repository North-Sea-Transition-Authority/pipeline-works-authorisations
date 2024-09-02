package uk.co.ogauthority.pwa.externalapi;

import static uk.co.ogauthority.pwa.externalapi.PwaConsentDtoController.ENERGY_PORTAL_API_BASE_PATH;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ENERGY_PORTAL_API_BASE_PATH)
public class PwaConsentDtoController {

  public static final String ENERGY_PORTAL_API_BASE_PATH = "/api/external/v1";

  private final PwaConsentDtoRepository repository;

  @Autowired
  PwaConsentDtoController(PwaConsentDtoRepository repository) {
    this.repository = repository;
  }

  @GetMapping("/consents")
  List<PwaConsentDto> searchPwaConsents(@RequestParam(name = "pwaIds") List<Integer> pwaIds) {
    return repository.searchPwaConsents(pwaIds)
        .stream()
        .sorted(Comparator.comparing(PwaConsentDto::getConsentedDate))
        .collect(Collectors.toList());
  }
}
