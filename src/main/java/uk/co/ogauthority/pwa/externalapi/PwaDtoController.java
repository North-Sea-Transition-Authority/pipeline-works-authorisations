package uk.co.ogauthority.pwa.externalapi;

import static uk.co.ogauthority.pwa.externalapi.PwaDtoController.ENERGY_PORTAL_API_BASE_PATH;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ENERGY_PORTAL_API_BASE_PATH)
public class PwaDtoController {

  public static final String ENERGY_PORTAL_API_BASE_PATH = "/api/external/v1";

  private final PwaDtoRepository pwaDtoRepository;

  @Autowired
  public PwaDtoController(PwaDtoRepository pwaDtoRepository) {
    this.pwaDtoRepository = pwaDtoRepository;
  }

  @GetMapping("/pwas")
  List<PwaDto> searchPwas(@RequestParam(name = "ids", required = false) List<Integer> ids,
                          @RequestParam(name = "reference", required = false) String reference) {
    return pwaDtoRepository.searchPwas(ids, reference);
  }
}
