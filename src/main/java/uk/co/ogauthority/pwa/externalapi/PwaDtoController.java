package uk.co.ogauthority.pwa.externalapi;

import static org.apache.commons.lang3.EnumUtils.isValidEnum;
import static uk.co.ogauthority.pwa.externalapi.PwaDtoController.ENERGY_PORTAL_API_BASE_PATH;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.co.ogauthority.pwa.model.entity.enums.MasterPwaDetailStatus;

@RestController
@RequestMapping(ENERGY_PORTAL_API_BASE_PATH)
public class PwaDtoController {

  public static final String ENERGY_PORTAL_API_BASE_PATH = "/api/external/v1";
  static final int PAGE_SIZE = 1000;

  private final PwaDtoRepository pwaDtoRepository;

  @Autowired
  public PwaDtoController(PwaDtoRepository pwaDtoRepository) {
    this.pwaDtoRepository = pwaDtoRepository;
  }

  @GetMapping("/pwas")
  List<PwaDto> searchPwas(@RequestParam(name = "ids", required = false) List<Integer> ids,
                          @RequestParam(name = "reference", required = false) String reference,
                          @RequestParam(name = "status", required = false) String status,
                          @RequestParam(name = "page", required = false) Integer pageNumber) {

    if ((status != null && !isValidEnum(MasterPwaDetailStatus.class, status)) || pageNumber == null) {
      return List.of();
    }

    var masterPwaDetailStatus = status == null ? null : MasterPwaDetailStatus.valueOf(status);

    return pwaDtoRepository.searchPwas(ids, reference, masterPwaDetailStatus, PageRequest.of(pageNumber, PAGE_SIZE))
        .stream()
        .sorted(PwaDto::compareTo)
        .collect(Collectors.toList());
  }
}
