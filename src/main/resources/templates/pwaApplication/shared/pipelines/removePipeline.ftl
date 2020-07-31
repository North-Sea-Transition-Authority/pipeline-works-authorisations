<#include '../../../layout.ftl'>

<@defaultPage htmlTitle="Remove pipeline" pageHeading="Are you sure you want to remove this pipeline?" breadcrumbs=true>
  <@fdsCheckAnswers.checkAnswers>
    <@fdsCheckAnswers.checkAnswersRow keyText="Pipeline name" actionUrl="" screenReaderActionText="">
      ${pipeline.pipelineName}
    </@fdsCheckAnswers.checkAnswersRow>
  </@fdsCheckAnswers.checkAnswers>

    <@fdsForm.htmlForm>
        <@fdsAction.submitButtons linkSecondaryAction=true primaryButtonText="Remove pipeline" secondaryLinkText="Back to overview" linkSecondaryActionUrl=springUrl(backUrl)/>
    </@fdsForm.htmlForm>
</@defaultPage>