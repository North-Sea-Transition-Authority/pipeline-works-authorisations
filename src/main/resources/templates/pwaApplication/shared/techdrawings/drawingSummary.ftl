<#include '../../../layout.ftl'>

<#-- @ftlvariable name="summary" type="uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings.PipelineDrawingSummaryView" -->
<#-- @ftlvariable name="urlFactory" type="uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings.PipelineDrawingUrlFactory" -->
<#-- @ftlvariable name="validatorFactory" type="uk.co.ogauthority.pwa.features.application.tasks.pipelinediagrams.pipelinetechdrawings.PipelineDrawingValidationFactory" -->

<#macro drawingSummary summary urlFactory validatorFactory showReferenceAsKey=false showActions=false>
    <#if !showReferenceAsKey>
      <h3 class="govuk-heading-m">${summary.reference}</h3>
        <#if validatorFactory?has_content && !validatorFactory.isValid(summary)>
          <span class="govuk-error-message">${validatorFactory.getErrorMessage(summary)}</span>
        </#if>
        <#if showActions>
            <@fdsAction.link linkText="Edit" linkUrl=springUrl(urlFactory.getPipelineDrawingEditUrl(summary.drawingId)) linkClass="govuk-link govuk-!-font-size-19" linkScreenReaderText=summary.reference/>&nbsp;
            <@fdsAction.link linkText="Remove" linkUrl=springUrl(urlFactory.getPipelineDrawingRemoveUrl(summary.drawingId)) linkClass="govuk-link govuk-!-font-size-19" linkScreenReaderText=summary.reference/>
        </#if>
    </#if>
    <@fdsCheckAnswers.checkAnswers>
        <#if showReferenceAsKey>
            <@fdsCheckAnswers.checkAnswersRow keyText="Schematic reference" actionUrl="" screenReaderActionText="" actionText="">
                ${summary.reference}
            </@fdsCheckAnswers.checkAnswersRow>
        </#if>
        <@fdsCheckAnswers.checkAnswersRow keyText="Schematic drawing" actionUrl="" screenReaderActionText="" actionText="">
            <#if summary.fileName?has_content>
                <@fdsAction.link linkText=summary.fileName linkUrl=springUrl(urlFactory.getPipelineDrawingDownloadUrl(summary.fileId)) linkClass="govuk-link" linkScreenReaderText="Download ${summary.fileName}" role=false start=false openInNewTab=true/>
            <#else>
              No drawing uploaded
            </#if>
        </@fdsCheckAnswers.checkAnswersRow>
        <@fdsCheckAnswers.checkAnswersRow keyText="Schematic description" actionUrl="" screenReaderActionText="" actionText="">
            <#if summary.documentDescription?has_content>
                ${summary.documentDescription}
            <#else>
              No drawing uploaded
            </#if>
        </@fdsCheckAnswers.checkAnswersRow>
        <@fdsCheckAnswers.checkAnswersRow keyText="Associated pipelines" actionUrl="" screenReaderActionText="" actionText="">
          <ul class="govuk-list">
              <#list summary.pipelineReferences as ref>
                <li>${ref}</li>
              </#list>
          </ul>
        </@fdsCheckAnswers.checkAnswersRow>
    </@fdsCheckAnswers.checkAnswers>
</#macro>