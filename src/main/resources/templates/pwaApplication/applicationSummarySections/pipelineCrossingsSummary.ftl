<#include '../../pwaLayoutImports.ftl'>

<#-- @ftlvariable name="sectionDisplayText" type="java.lang.String" -->
<#-- @ftlvariable name="pipelineCrossingViews" type="java.util.List<uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.pipeline.PipelineCrossingView>" -->
<#-- @ftlvariable name="pipelineCrossingFiles" type="java.util.List<uk.co.ogauthority.pwa.model.form.files.UploadedFileView>" -->
<#-- @ftlvariable name="pipelineCrossingUrlFactory" type="uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.pipeline.PipelineCrossingUrlFactory" -->


<div class="pwa-application-summary-section">
    <h2 class="govuk-heading-l" id="pipelineCrossingDetails">${sectionDisplayText}</h2>

    <@pipelineCrossingDetails pipelineCrossingViews/>
    <@crossingAgreementDetails pipelineCrossingFiles/>

</div>


<#macro pipelineCrossingDetails pipelineCrossingViews>

    <h3 class="govuk-heading-m">Pipeline crossings</h3>

    <#list pipelineCrossingViews as pipelineCrossingView>

        <h4 class="govuk-heading-s"> ${pipelineCrossingView.reference} </h4>

        <@fdsCheckAnswers.checkAnswers>

            <@fdsCheckAnswers.checkAnswersRow keyText="Owners" actionUrl="" screenReaderActionText="" actionText="">
                ${pipelineCrossingView.owners}
            </@fdsCheckAnswers.checkAnswersRow>

        </@fdsCheckAnswers.checkAnswers>

    </#list>

    <#if !pipelineCrossingViews?has_content>
        <@fdsInsetText.insetText>
            No pipeline crossings have been added to this application.
        </@fdsInsetText.insetText>
    </#if>

</#macro>


<#macro crossingAgreementDetails pipelineCrossingFiles>

    <h3 class="govuk-heading-m">Pipeline crossing agreements</h3>
    <#if pipelineCrossingFiles?has_content>
        <@pwaFiles.uploadedFileList downloadUrl=springUrl(pipelineCrossingUrlFactory.getFileDownloadUrl()) existingFiles=pipelineCrossingFiles/>
    <#else>
        <@fdsInsetText.insetText>
            No pipeline crossing agreement documents have been added to this application.
        </@fdsInsetText.insetText>
    </#if>
</#macro>
