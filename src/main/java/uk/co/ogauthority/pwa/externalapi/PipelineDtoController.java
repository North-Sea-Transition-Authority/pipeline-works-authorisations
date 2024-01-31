package uk.co.ogauthority.pwa.externalapi;

import static uk.co.ogauthority.pwa.externalapi.PipelineDtoController.ENERGY_PORTAL_API_BASE_PATH;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
                                    @RequestParam(name = "number", required = false) String pipelineNumber,
                                    @RequestParam(name = "pwaReference", required = false) String pwaReference)    {
    return pipelineDtoRepository.searchPipelines(ids, pipelineNumber, pwaReference);
  }
}
