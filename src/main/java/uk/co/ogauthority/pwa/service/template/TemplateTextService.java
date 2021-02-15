package uk.co.ogauthority.pwa.service.template;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.exception.PwaEntityNotFoundException;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.TemplateTextType;
import uk.co.ogauthority.pwa.repository.template.TemplateTextRepository;

@Service
public class TemplateTextService {

  private final TemplateTextRepository templateTextRepository;

  @Autowired
  public TemplateTextService(TemplateTextRepository templateTextRepository) {
    this.templateTextRepository = templateTextRepository;
  }


  public String getLatestVersionTextByType(TemplateTextType textType) {
    return templateTextRepository.findByTextTypeAndEndTimestampIsNull(textType)
        .orElseThrow(() -> new PwaEntityNotFoundException("Text not found for type: " + textType.name())).getText();
  }

}
