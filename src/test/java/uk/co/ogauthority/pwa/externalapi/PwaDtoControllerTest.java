package uk.co.ogauthority.pwa.externalapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.ogauthority.pwa.externalapi.PwaDtoController.PAGE_SIZE;

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
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import uk.co.ogauthority.pwa.config.ExternalApiWebSecurityConfiguration;
import uk.co.ogauthority.pwa.controller.PwaApplicationContextAbstractControllerTest;
import uk.co.ogauthority.pwa.features.application.authorisation.context.PwaApplicationContextService;
import uk.co.ogauthority.pwa.model.entity.enums.MasterPwaDetailStatus;
import uk.co.ogauthority.pwa.mvc.ReverseRouter;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = PwaDtoController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
@Import(ExternalApiWebSecurityConfiguration.class)
public class PwaDtoControllerTest extends PwaApplicationContextAbstractControllerTest {

  @MockBean
  private PwaDtoRepository pwaDtoRepository;

  private static final String PRE_SHARED_KEY = "testKey1";
  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Test
  public void searchPwas_NoBearerToken_AssertForbidden() throws Exception {
    mockMvc.perform(post(
            ReverseRouter.route(on(PwaDtoController.class)
                .searchPwas(null, null, null, 0))))
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }

  @Test
  public void searchPwas() throws Exception {
    var id = 1;
    var reference = "PL123";

    var result = List.of(
        PwaDtoTestUtil.builder()
            .withId(id)
            .withReference(reference)
            .withStatus(MasterPwaDetailStatus.CONSENTED)
            .build()
    );

    var resultJson = MAPPER.writeValueAsString(result);

    when(pwaDtoRepository.searchPwas(
        List.of(id),
        reference,
        MasterPwaDetailStatus.CONSENTED,
        PageRequest.of(0, PAGE_SIZE)
    )).thenReturn(result);

    mockMvc.perform(get(
            ReverseRouter.route(on(PwaDtoController.class).searchPwas(
                Collections.singletonList(id),
                reference,
                MasterPwaDetailStatus.CONSENTED.name(),
                0
            ))).header("Authorization", String.format("Bearer %s", PRE_SHARED_KEY)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(resultJson));
  }

  @Test
  public void searchPwas_assertSort() throws Exception {
    var firstPwa = PwaDtoTestUtil.builder()
        .withId(1)
        .withReference("1/W/02")
        .withStatus(MasterPwaDetailStatus.CONSENTED)
        .build();

    var secondPwa = PwaDtoTestUtil.builder()
        .withId(2)
        .withReference("2/W/02")
        .withStatus(MasterPwaDetailStatus.APPLICATION)
        .build();

    var thirdPwa = PwaDtoTestUtil.builder()
        .withId(3)
        .withReference("10/W/02")
        .withStatus(MasterPwaDetailStatus.CONSENTED)
        .build();

    var unsortedList = List.of(secondPwa, thirdPwa, firstPwa);

    when(pwaDtoRepository.searchPwas(
        List.of(1, 2, 3),
        null,
        null,
        PageRequest.of(0, PAGE_SIZE)
    )).thenReturn(unsortedList);

    var result = mockMvc.perform(get(
            ReverseRouter.route(on(PwaDtoController.class).searchPwas(
                List.of(1, 2, 3),
                null,
                null,
                0
            ))).header("Authorization", String.format("Bearer %s", PRE_SHARED_KEY)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andReturn();

    var encodedResponse = result.getResponse().getContentAsString();

    List<PwaDto> resultingPwas = new ArrayList<>(Arrays.asList(MAPPER.readValue(encodedResponse, PwaDto[].class)));

    assertThat(resultingPwas)
        .extracting(PwaDto::getId)
        .containsExactly(firstPwa.getId(), secondPwa.getId(), thirdPwa.getId());
  }

  @Test
  public void searchPwas_whenStatusIsInvalid() throws Exception {
    var result = mockMvc.perform(get(
            ReverseRouter.route(on(PwaDtoController.class).searchPwas(
                null,
                null,
                "invalid enum",
                0
            ))).header("Authorization", String.format("Bearer %s", PRE_SHARED_KEY)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andReturn();

    var encodedResponse = result.getResponse().getContentAsString();

    List<PwaDto> resultingPwas = new ArrayList<>(Arrays.asList(MAPPER.readValue(encodedResponse, PwaDto[].class)));

    assertThat(resultingPwas).isEmpty();
    verify(pwaDtoRepository, never()).searchPwas(any(), any(), any(), any());
  }

  @Test
  public void searchPwas_whenPageNumberIsNull() throws Exception {
    var result = mockMvc.perform(get(
            ReverseRouter.route(on(PwaDtoController.class).searchPwas(
                null,
                null,
                null,
                null
            ))).header("Authorization", String.format("Bearer %s", PRE_SHARED_KEY)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andReturn();

    var encodedResponse = result.getResponse().getContentAsString();

    List<PwaDto> resultingPwas = new ArrayList<>(Arrays.asList(MAPPER.readValue(encodedResponse, PwaDto[].class)));

    assertThat(resultingPwas).isEmpty();
    verify(pwaDtoRepository, never()).searchPwas(any(), any(), any(), any());
  }
}