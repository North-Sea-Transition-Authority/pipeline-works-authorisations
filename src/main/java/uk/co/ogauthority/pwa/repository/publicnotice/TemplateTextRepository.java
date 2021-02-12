package uk.co.ogauthority.pwa.repository.publicnotice;


import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.TemplateTextType;
import uk.co.ogauthority.pwa.model.entity.publicnotice.TemplateText;

@Repository
public interface TemplateTextRepository extends CrudRepository<TemplateText, Integer> {

  Optional<TemplateText> findByTextTypeAndEndTimestampIsNull(TemplateTextType textType);
}
