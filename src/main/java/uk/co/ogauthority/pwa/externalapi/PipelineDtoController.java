package uk.co.ogauthority.pwa.externalapi;

import static uk.co.ogauthority.pwa.externalapi.PipelineDtoController.ENERGY_PORTAL_API_BASE_PATH;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(ENERGY_PORTAL_API_BASE_PATH)
public class PipelineDtoController {

  public static final String ENERGY_PORTAL_API_BASE_PATH = "/api/external/v1";

  private final PipelineDtoRepository pipelineDtoRepository;

  @Autowired
  PipelineDtoController(PipelineDtoRepository pipelineDtoRepository) {
    this.pipelineDtoRepository = pipelineDtoRepository;
  }

  @GetMapping("/pipelines")
  List<PipelineDto> searchPipelines(@RequestParam(name = "ids", required = false) List<Integer> ids,
                                    @RequestParam(name = "pipelineNumber", required = false) String pipelineNumber,
                                    @RequestParam(name = "pwaIds", required = false) List<Integer> pwaIds) {
    if (CollectionUtils.isEmpty(ids) && StringUtils.isBlank(pipelineNumber) && CollectionUtils.isEmpty(pwaIds)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one request parameter must be non-null");
    }

    return pipelineDtoRepository.searchPipelines(ids, pipelineNumber, pwaIds)
        .stream()
        .sorted(PipelineDto::compareTo)
        .collect(Collectors.toList());
  }
}
