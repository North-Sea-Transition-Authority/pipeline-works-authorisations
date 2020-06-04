<#include '../../../layout.ftl'>

<#-- @ftlvariable name="backUrl" type="java.lang.String" -->

<@defaultPage htmlTitle="Remove pipeline drawing" pageHeading="Are you sure you want to remove this pipeline drawing?" breadcrumbs=true>

    <@fdsCheckAnswers.checkAnswers>
        <@fdsCheckAnswers.checkAnswersRow keyText="Drawing reference" actionUrl="" screenReaderActionText="" actionText="">
            ${summary.reference}
        </@fdsCheckAnswers.checkAnswersRow>
        <@fdsCheckAnswers.checkAnswersRow keyText="Schematic drawing" actionUrl="" screenReaderActionText="" actionText="">
            <@fdsAction.link linkText=summary.fileName linkUrl=springUrl(urlFactory.getPipelineDrawingDownloadUrl(summary.fileId)) linkClass="govuk-link" linkScreenReaderText="Download ${summary.fileName}" role=false start=false openInNewTab=true/>
        </@fdsCheckAnswers.checkAnswersRow>
        <@fdsCheckAnswers.checkAnswersRow keyText="Schematic description" actionUrl="" screenReaderActionText="" actionText="">
            ${summary.documentDescription}
        </@fdsCheckAnswers.checkAnswersRow>
        <@fdsCheckAnswers.checkAnswersRow keyText="Associated pipelines" actionUrl="" screenReaderActionText="" actionText="">
          <ul class="govuk-list">
              <#list summary.pipelineReferences as ref>
                <li>${ref}</li>
              </#list>
          </ul>
        </@fdsCheckAnswers.checkAnswersRow>
    </@fdsCheckAnswers.checkAnswers>

    <@fdsForm.htmlForm>
        <@fdsAction.submitButtons linkSecondaryAction=true primaryButtonText="Remove pipeline drawing" secondaryLinkText="Back to overview" linkSecondaryActionUrl=springUrl(backUrl)/>
    </@fdsForm.htmlForm>
</@defaultPage>