package uk.co.ogauthority.pwa.service.rendering;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.RequestContext;
import uk.co.ogauthority.pwa.exception.TemplateRenderingException;

@Service
public class TemplateRenderingService {

  private final Configuration freemarkerConfig;

  @Autowired
  public TemplateRenderingService(Configuration freemarkerConfig) {
    this.freemarkerConfig = freemarkerConfig;
  }

  public String render(String template, Map<String, Object> viewModel,
                       boolean addSpringRequestSupport) throws TemplateRenderingException {

    // This is a bit of a hack to get access to spring.ftl macros to work if they are included in the template.
    // This prevents rendering from working async as there will be no active request.
    if (addSpringRequestSupport) {
      HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
      viewModel.put("springMacroRequestContext", new RequestContext(request, null, null, null));
    }
    String renderedTemplate;
    try {
      Template t = freemarkerConfig.getTemplate(template, Locale.ENGLISH);
      renderedTemplate = FreeMarkerTemplateUtils.processTemplateIntoString(t, viewModel);
      return renderedTemplate;
    } catch (IOException | TemplateException e) {
      throw new TemplateRenderingException("Could not render template: " + template, e);
    }
  }


}
