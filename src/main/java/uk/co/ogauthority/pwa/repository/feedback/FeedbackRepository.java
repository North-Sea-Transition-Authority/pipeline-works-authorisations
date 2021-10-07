package uk.co.ogauthority.pwa.repository.feedback;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.feedback.Feedback;

@Repository
public interface FeedbackRepository extends CrudRepository<Feedback, Integer> {
}
