<#include '../../../../layout.ftl'>

<#-- @ftlvariable name="backUrl" type="java.lang.String" -->

<@defaultPage htmlTitle="Remove pipeline crossing" pageHeading="Are you sure you want to remove this pipeline crossing?" breadcrumbs=true>

    <@fdsCheckAnswers.checkAnswers summaryListClass="">

      <@fdsCheckAnswers.checkAnswersRow keyText="Pipeline reference" actionText="" actionUrl="" screenReaderActionText="">
        ${view.reference}
      </@fdsCheckAnswers.checkAnswersRow>

      <@fdsCheckAnswers.checkAnswersRow keyText="Pipeline owners" actionText="" actionUrl="" screenReaderActionText="">
        ${view.owners}
      </@fdsCheckAnswers.checkAnswersRow>

    </@fdsCheckAnswers.checkAnswers>

    <@fdsForm.htmlForm>
        <@fdsAction.submitButtons linkSecondaryAction=true primaryButtonText="Remove pipeline crossing" primaryButtonClass="govuk-button govuk-button--warning" secondaryLinkText="Back to pipeline crossings" linkSecondaryActionUrl=springUrl(backUrl)/>
    </@fdsForm.htmlForm>
</@defaultPage>