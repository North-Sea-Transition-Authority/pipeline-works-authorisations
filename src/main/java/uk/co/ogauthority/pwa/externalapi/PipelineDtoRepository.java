package uk.co.ogauthority.pwa.externalapi;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.co.ogauthority.pwa.model.entity.pipelines.PipelineDetail;

@Repository
interface PipelineDtoRepository extends CrudRepository<PipelineDetail, Integer> {


  @Query("select new uk.co.ogauthority.pwa.externalapi.PipelineDto(p.id, pd.pipelineNumber, mpd.reference) " +
      "from PipelineDetail pd " +
      "join Pipeline p on pd.pipeline = p " +
      "join MasterPwaDetail mpd on p.masterPwa = mpd.masterPwa " +
      "where mpd.endInstant is null " +
      "and pd.tipFlag = true " +
      "and (mpd.reference like '%'||:reference||'%' or :reference is null) " +
      "and (pd.pipelineNumber like '%'||:pipelineNumber||'%' or :pipelineNumber is null) " +
      "and (p.id in (:ids) or COALESCE(:ids, null) is null ) "
  )
  List<PipelineDto> searchPipelines(List<Integer> ids, String pipelineNumber, String reference);
}
