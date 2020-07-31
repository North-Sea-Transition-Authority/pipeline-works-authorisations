<#include '../../../layout.ftl'>

<@defaultPage htmlTitle="Remove pipeline" pageHeading="Are you sure you want to remove this pipeline?" breadcrumbs=true>
  <@fdsCheckAnswers.checkAnswers>
    <@fdsCheckAnswers.checkAnswersRow keyText=pipeline.pipelineName
  </@fdsCheckAnswers.checkAnswers>
</@defaultPage>