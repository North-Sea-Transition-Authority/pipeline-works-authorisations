package uk.co.ogauthority.pwa.service.rendering;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import freemarker.template.Configuration;
import freemarker.template.Template;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TemplateRenderingServiceTest {

  @Mock
  private Configuration mockFreemarkerConfig;

  @Mock
  private Template template;

  private TemplateRenderingService templateRenderingService;

  private String templateName = "TEMPLATE.ftl";
  private Map<String, Object> templateModel = new HashMap<>();

  @BeforeEach
  void setup(){
    templateRenderingService = new TemplateRenderingService(mockFreemarkerConfig);

  }

  /**
   * Very basic test to ensure it doesnt throw an arbitrary error. Tests on specific templates containing expected output are more likely integration tests
   * @throws IOException when failed to load template
   */
  @Test
  void render() throws IOException {
    when(mockFreemarkerConfig.getTemplate(ArgumentMatchers.eq(templateName), any(Locale.class))).thenReturn(template);
    String renderedTemplate = templateRenderingService.render(templateName, templateModel, false);
  }

}
