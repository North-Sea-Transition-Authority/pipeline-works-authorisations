package uk.co.ogauthority.pwa.externalapi;

import static org.apache.commons.lang3.EnumUtils.isValidEnum;
import static uk.co.ogauthority.pwa.externalapi.PwaDtoController.ENERGY_PORTAL_API_BASE_PATH;

import io.micrometer.common.util.StringUtils;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.co.ogauthority.pwa.model.entity.enums.MasterPwaDetailStatus;

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
                          @RequestParam(name = "reference", required = false) String reference,
                          @RequestParam(name = "status", required = false) String status) {

    if (CollectionUtils.isEmpty(ids) && StringUtils.isBlank(reference) && StringUtils.isBlank(status)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one request parameter must be non-null");
    }

    if ((status != null && !isValidEnum(MasterPwaDetailStatus.class, status))) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status of %s provided".formatted(status));
    }

    var masterPwaDetailStatus = status == null ? null : MasterPwaDetailStatus.valueOf(status);

    return pwaDtoRepository.searchPwas(ids, reference, masterPwaDetailStatus)
        .stream()
        .sorted(PwaDto::compareTo)
        .collect(Collectors.toList());
  }
}
