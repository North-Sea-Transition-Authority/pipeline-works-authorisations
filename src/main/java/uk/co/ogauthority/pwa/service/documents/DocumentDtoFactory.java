package uk.co.ogauthority.pwa.service.documents;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uk.co.ogauthority.pwa.model.documents.DocumentTemplateDto;
import uk.co.ogauthority.pwa.model.documents.SectionDto;
import uk.co.ogauthority.pwa.model.documents.TemplateSectionClauseVersionDto;
import uk.co.ogauthority.pwa.model.entity.documents.templates.DocumentTemplateSection;
import uk.co.ogauthority.pwa.model.entity.documents.templates.DocumentTemplateSectionClauseVersion;

@Service
public class DocumentDtoFactory {

  public DocumentTemplateDto create(Map<DocumentTemplateSection, List<DocumentTemplateSectionClauseVersion>> sectionToClauseListMap) {

    var doc = new DocumentTemplateDto(sectionToClauseListMap.keySet().iterator().next().getDocumentTemplate());

    sectionToClauseListMap.forEach((section, clauseVersions) -> {

      var sectionDto = new SectionDto();
      sectionDto.setName(section.getName());

      var clauseVersionDtos = clauseVersions.stream()
          .map(TemplateSectionClauseVersionDto::from)
          .collect(Collectors.toList());

      sectionDto.setClauses(clauseVersionDtos);

      doc.getSections().add(sectionDto);

    });

    return doc;

  }
}
