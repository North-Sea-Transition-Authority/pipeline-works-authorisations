package uk.co.ogauthority.pwa.externalapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.config.ExternalApiWebSecurityConfiguration;
import uk.co.ogauthority.pwa.controller.PwaApplicationContextAbstractControllerTest;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = PipelineDtoController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
@Import(ExternalApiWebSecurityConfiguration.class)
public class PipelineDtoControllerTest extends PwaApplicationContextAbstractControllerTest {

  @MockBean
  private PipelineDtoRepository pipelineDtoRepository;

  private static final String PRE_SHARED_KEY = "testKey1";
  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Test
  public void searchPipelines() throws Exception {
    var pipelineId  = 1;
    var pipelineNumber = "PL123";
    var pwaId = 2;

    var result = List.of(
        PipelineDtoTestUtil.builder()
            .withId(pipelineId)
            .withNumber(pipelineNumber)
            .withPwaId(pwaId)
            .build()
    );

    var resultJson = MAPPER.writeValueAsString(result);

    when(pipelineDtoRepository.searchPipelines(
        List.of(pipelineId),
        pipelineNumber,
        List.of(pwaId)
    )).thenReturn(result);

    mockMvc.perform(get(
            ReverseRouter.route(on(PipelineDtoController.class).searchPipelines(
                Collections.singletonList(pipelineId),
                pipelineNumber,
                List.of(pwaId)
            ))).header("Authorization", String.format("Bearer %s", PRE_SHARED_KEY)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(resultJson));
  }

  @Test
  public void searchPipelines_assertSort() throws Exception {
    var firstPipeline = PipelineDtoTestUtil.builder()
        .withId(1)
        .withNumber("PL1")
        .build();

    var secondPipeline = PipelineDtoTestUtil.builder()
        .withId(2)
        .withNumber("PL2")
        .build();

    var thirdPipeline = PipelineDtoTestUtil.builder()
        .withId(2)
        .withNumber("PL10")
        .build();

    var unsortedList = List.of(secondPipeline, thirdPipeline, firstPipeline);

    when(pipelineDtoRepository.searchPipelines(
        List.of(1, 2, 3),
        null,
        null
    )).thenReturn(unsortedList);

    var result = mockMvc.perform(get(
            ReverseRouter.route(on(PipelineDtoController.class).searchPipelines(
                List.of(1, 2, 3),
                null,
                null
            ))).header("Authorization", String.format("Bearer %s", PRE_SHARED_KEY)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andReturn();

    var encodedResponse = result.getResponse().getContentAsString();

    List<PipelineDto> resultingPipelines = new ArrayList<>(Arrays.asList(MAPPER.readValue(encodedResponse, PipelineDto[].class)));

    assertThat(resultingPipelines)
        .extracting(PipelineDto::getId)
        .containsExactly(firstPipeline.getId(), secondPipeline.getId(), thirdPipeline.getId());
  }

  @Test
  public void searchPipelines_NoBearerToken_AssertForbidden() throws Exception {
    mockMvc.perform(post(
            ReverseRouter.route(on(PipelineDtoController.class)
                .searchPipelines(null, null, null))))
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }
}