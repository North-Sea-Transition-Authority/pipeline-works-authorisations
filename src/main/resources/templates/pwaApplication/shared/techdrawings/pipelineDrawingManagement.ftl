<#include '../../../layout.ftl'>

<#-- @ftlvariable name="blockCrossings" type="java.util.List<uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.BlockCrossingView>" -->
<#-- @ftlvariable name="blockCrossingFileViews" type="java.util.List<uk.co.ogauthority.pwa.model.form.files.UploadedFileView>" -->
<#-- @ftlvariable name="urlFactory" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.BlockCrossingUrlFactory" -->
<#-- @ftlvariable name="crossingAgreementValidationResult" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.CrossingAgreementsValidationResult" -->

<#macro pipelineDrawingManagement urlFactory pipelineDrawingSummaryViews=[]>
  <h2 class="govuk-heading-l">
    Pipeline drawings
  </h2>
    <#if pipelineDrawingSummaryViews?has_content>
        <@fdsAction.link linkText="Add pipeline drawing" linkUrl=springUrl(urlFactory.getAddPipelineDrawingUrl()) linkClass="govuk-button govuk-button--blue"/>

        <#list pipelineDrawingSummaryViews as summary>
            <h3 class="govuk-heading-m">${summary.reference}</h3>
            <@fdsAction.link linkText="Edit" linkUrl=springUrl("#") linkClass="govuk-link govuk-!-font-size-19"/>&nbsp;
            <@fdsAction.link linkText="Remove" linkUrl=springUrl(urlFactory.getPipelineDrawingRemoveUrl(summary.drawingId)) linkClass="govuk-link govuk-!-font-size-19"/>
          <@fdsCheckAnswers.checkAnswers>
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
        </#list>
    <#else>
        <@fdsInsetText.insetText>
          No pipeline drawings have been added to this application.
        </@fdsInsetText.insetText>
        <@fdsAction.link linkText="Add pipeline drawing" linkUrl=springUrl(urlFactory.getAddPipelineDrawingUrl()) linkClass="govuk-button govuk-button--blue"/>
    </#if>

</#macro>