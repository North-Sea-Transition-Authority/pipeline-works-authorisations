package uk.co.ogauthority.pwa.externalapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
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
@WebMvcTest(controllers = PwaConsentDtoController.class, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PwaApplicationContextService.class))
@Import(ExternalApiWebSecurityConfiguration.class)
public class PwaConsentDtoControllerTest extends PwaApplicationContextAbstractControllerTest {

  private static final Class<PwaConsentDtoController> CONTROLLER = PwaConsentDtoController.class;
  private static final String PRE_SHARED_KEY = "testKey1";
  private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

  @MockBean
  private PwaConsentDtoRepository pwaConsentDtoRepository;

  @Before
  public void setUp() {
    MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
  }

  @Test
  public void searchPwaConsents_noBearerToken_assertForbidden() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER).searchPwaConsents(List.of(1)))))
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON));
  }

  @Test
  public void searchPwaConsents_withNoPwaIds_assertBadRequest() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER).searchPwaConsents(null)))
        .header("Authorization", "Bearer " + PRE_SHARED_KEY)
    ).andExpect(status().isBadRequest());
  }

  @Test
  public void searchPwaConsents_withEmptyPwaIds_assertBadRequest() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER).searchPwaConsents(Collections.emptyList())))
        .header("Authorization", "Bearer " + PRE_SHARED_KEY)
    ).andExpect(status().isBadRequest());
  }

  @Test
  public void searchPwaConsents() throws Exception {
    var currentTime = Instant.now();
    var pwaIds = List.of(1);

    var firstPwaConsent = PwaConsentDtoTestUtil.newBuilder()
        .withPwaId(1)
        .withConsentedDate(currentTime.minus(30, ChronoUnit.DAYS))
        .build();
    var secondPwaConsent = PwaConsentDtoTestUtil.newBuilder()
        .withPwaId(1)
        .withConsentedDate(currentTime.minus(15, ChronoUnit.DAYS))
        .build();

    var result = List.of(firstPwaConsent, secondPwaConsent);
    var resultJson = MAPPER.writeValueAsString(result);

    when(pwaConsentDtoRepository.searchPwaConsents(pwaIds)).thenReturn(result);

    mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER).searchPwaConsents(pwaIds)))
        .header("Authorization", "Bearer " + PRE_SHARED_KEY)
    )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json(resultJson));
  }

  @Test
  public void searchPwaConsents_assertSort() throws Exception {
    var currentTime = Instant.now();
    var pwaIds = List.of(1, 2);

    var firstPwaConsent = PwaConsentDtoTestUtil.newBuilder()
        .withPwaId(2)
        .withId(10)
        .withConsentedDate(currentTime.minus(50, ChronoUnit.DAYS))
        .build();
    var secondPwaConsent = PwaConsentDtoTestUtil.newBuilder()
        .withPwaId(1)
        .withId(20)
        .withConsentedDate(currentTime.minus(30, ChronoUnit.DAYS))
        .build();
    var thirdPwaConsent = PwaConsentDtoTestUtil.newBuilder()
        .withPwaId(1)
        .withId(30)
        .withConsentedDate(currentTime.minus(15, ChronoUnit.DAYS))
        .build();

    var unsortedList = List.of(thirdPwaConsent, firstPwaConsent, secondPwaConsent);
    when(pwaConsentDtoRepository.searchPwaConsents(pwaIds)).thenReturn(unsortedList);

    var result = mockMvc.perform(get(ReverseRouter.route(on(CONTROLLER).searchPwaConsents(pwaIds)))
            .header("Authorization", "Bearer " + PRE_SHARED_KEY)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andReturn();

    var encodedResponse = result.getResponse().getContentAsString();

    List<PwaConsentDto> resultingPwaConsents = new ArrayList<>(Arrays.asList(
        MAPPER.readValue(encodedResponse, PwaConsentDto[].class)));

    assertThat(resultingPwaConsents)
        .extracting(PwaConsentDto::getId)
        .containsExactly(firstPwaConsent.getId(), secondPwaConsent.getId(), thirdPwaConsent.getId());
  }
}
