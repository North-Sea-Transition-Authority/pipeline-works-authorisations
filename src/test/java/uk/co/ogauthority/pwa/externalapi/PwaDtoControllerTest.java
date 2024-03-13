package uk.co.ogauthority.pwa.externalapi;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
@WebMvcTest(controllers = PwaDtoController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
public class PwaDtoControllerTest extends PwaApplicationContextAbstractControllerTest {

  @MockBean
  private PwaDtoRepository pwaDtoRepository;

  private static final String PRE_SHARED_KEY = "testKey1";
  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Test
  public void searchPwas_NoBearerToken_AssertForbidden_deprecated() throws Exception {
    mockMvc.perform(post(
            ReverseRouter.route(on(PwaDtoController.class)
                .searchPwas(null, null))))
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }

  @Test
  public void searchPwas() throws Exception {
    var id  = 1;
    var reference = "PL123";

    var result = List.of(
        PwaDtoTestUtil.builder()
            .withId(id)
            .withReference(reference)
            .build()
    );

    var resultJson = MAPPER.writeValueAsString(result);

    when(pwaDtoRepository.searchPwas(
        List.of(id),
        reference
    )).thenReturn(result);

    mockMvc.perform(get(
            ReverseRouter.route(on(PwaDtoController.class).searchPwas(
                Collections.singletonList(id),
                reference
            ))).header("Authorization", String.format("Bearer %s", PRE_SHARED_KEY)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(resultJson));
  }
}