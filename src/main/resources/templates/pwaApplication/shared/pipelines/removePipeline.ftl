<#include '../../../layout.ftl'>

<@defaultPage htmlTitle="Remove pipeline" pageHeading="Are you sure you want to remove this pipeline?" breadcrumbs=true>

  <@fdsCheckAnswers.checkAnswers>
    <@fdsCheckAnswers.checkAnswersRow keyText="Pipeline name" actionUrl="" screenReaderActionText="">
      ${pipeline.pipelineName}
    </@fdsCheckAnswers.checkAnswersRow>
      <@fdsCheckAnswers.checkAnswersRow keyText="Length" actionUrl="" screenReaderActionText="">
          ${pipeline.getLength()}m
      </@fdsCheckAnswers.checkAnswersRow>
      <@fdsCheckAnswers.checkAnswersRow keyText="From" actionUrl="" screenReaderActionText="">
          ${pipeline.getFromLocation()}
          <br/>
          <@pwaCoordinate.display coordinatePair=pipeline.getFromCoordinates() />
      </@fdsCheckAnswers.checkAnswersRow>
      <@fdsCheckAnswers.checkAnswersRow keyText="To" actionUrl="" screenReaderActionText="">
          ${pipeline.getToLocation()}
          <br/>
          <@pwaCoordinate.display coordinatePair=pipeline.getToCoordinates() />
      </@fdsCheckAnswers.checkAnswersRow>
      <@fdsCheckAnswers.checkAnswersRow keyText="Component parts" actionUrl="" screenReaderActionText="">
          ${pipeline.getComponentParts()}
      </@fdsCheckAnswers.checkAnswersRow>
      <@fdsCheckAnswers.checkAnswersRow keyText="Products to be conveyed" actionUrl="" screenReaderActionText="">
          ${pipeline.getProductsToBeConveyed()}
      </@fdsCheckAnswers.checkAnswersRow>
      <@fdsCheckAnswers.checkAnswersRow keyText="Number of idents" actionUrl="" screenReaderActionText="">
          ${pipeline.getNumberOfIdents()}
      </@fdsCheckAnswers.checkAnswersRow>
  </@fdsCheckAnswers.checkAnswers>

    <@fdsForm.htmlForm>
        <@fdsAction.submitButtons linkSecondaryAction=true primaryButtonText="Remove pipeline" secondaryLinkText="Back to overview" linkSecondaryActionUrl=springUrl(backUrl)/>
    </@fdsForm.htmlForm>
</@defaultPage>