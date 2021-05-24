package uk.co.ogauthority.pwa.controller.externalentry;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;
import uk.co.ogauthority.pwa.controller.search.consents.PwaPipelineViewController;
import uk.co.ogauthority.pwa.model.dto.pipelines.PipelineId;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;
import uk.co.ogauthority.pwa.service.pwaapplications.shared.pipelines.PipelineService;
import uk.co.ogauthority.pwa.service.search.consents.PwaPipelineViewTab;

@Controller
@RequestMapping
public class ExternalViewPipelineRouter {

  private final PipelineService pipelineService;

  private final String pwaUrlBase;
  private final String contextPath;


  @Autowired
  public ExternalViewPipelineRouter(PipelineService pipelineService,
                                    @Value("${pwa.url.base}") String pwaUrlBase,
                                    @Value("${context-path}") String contextPath) {
    this.pipelineService = pipelineService;
    this.pwaUrlBase = pwaUrlBase;
    this.contextPath = contextPath;
  }

  @GetMapping("/ext/view-pipeline/{pipelineId}")
  public RedirectView viewPipeline(@PathVariable("pipelineId") int pipelineId) {

    var urlRoot = pwaUrlBase + contextPath;
    var pipeline = pipelineService.getPipelineFromId(new PipelineId(pipelineId));

    return new RedirectView(
        urlRoot + ReverseRouter.route(on(PwaPipelineViewController.class).renderViewPwaPipeline(
            pipeline.getMasterPwa().getId(),
            pipelineId,
            PwaPipelineViewTab.PIPELINE_HISTORY,
            null, null, null, null, null)
        )
    );
  }
}
