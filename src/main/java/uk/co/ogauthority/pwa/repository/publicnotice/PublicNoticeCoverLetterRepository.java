package uk.co.ogauthority.pwa.repository.publicnotice;


import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import uk.co.ogauthority.pwa.model.entity.enums.publicnotice.CoverLetterTextType;
import uk.co.ogauthority.pwa.model.entity.publicnotice.PublicNoticeCoverLetter;


public interface PublicNoticeCoverLetterRepository extends CrudRepository<PublicNoticeCoverLetter, Integer> {

  Optional<PublicNoticeCoverLetter> findByTextTypeAndEndTimestampIsNull(CoverLetterTextType textType);
}
