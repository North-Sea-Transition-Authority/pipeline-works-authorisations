package uk.co.ogauthority.pwa.externalapi;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.controller.PwaApplicationContextAbstractControllerTest;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = PipelineDtoController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
public class PipelineDtoControllerTest extends PwaApplicationContextAbstractControllerTest {

  @MockBean
  private PipelineDtoRepository pipelineDtoRepository;

  private static final String PRE_SHARED_KEY = "testKey1";
  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Test
  public void searchPipelines() throws Exception {
    var pipelineId  = 1;
    var pipelineNumber = "PL123";
    var pwaReference = "10/W//12";

    var result = List.of(
        PipelineDtoTestUtil.builder()
            .withId(pipelineId)
            .withNumber(pipelineNumber)
            .withPwaReference(pwaReference)
            .build()
    );

    var resultJson = MAPPER.writeValueAsString(result);

    when(pipelineDtoRepository.searchPipelineDtos(
        List.of(pipelineId),
        pipelineNumber,
        pwaReference
    )).thenReturn(result);

    mockMvc.perform(get(
        ReverseRouter.route(on(PipelineDtoController.class).searchPipelines(
            Collections.singletonList(pipelineId),
            pipelineNumber,
            pwaReference
        ))).header("Authorization", String.format("Bearer %s", PRE_SHARED_KEY)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(resultJson));
  }
}