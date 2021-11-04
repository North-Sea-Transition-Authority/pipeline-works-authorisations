<#include '../../pwaLayoutImports.ftl'>

<#-- @ftlvariable name="sectionDisplayText" type="java.lang.String" -->
<#-- @ftlvariable name="cableCrossingViews" type="java.util.List<uk.co.ogauthority.pwa.features.application.tasks.crossings.cable.PadCableCrossing.CableCrossingView>" -->
<#-- @ftlvariable name="cableCrossingFiles" type="java.util.List<uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadedFileView>" -->
<#-- @ftlvariable name="cableCrossingUrlFactory" type="uk.co.ogauthority.pwa.features.application.tasks.crossings.cable.CableCrossingUrlFactory" -->


<div class="pwa-application-summary-section">
    <h2 class="govuk-heading-l" id="cableCrossingDetails">${sectionDisplayText}</h2>

    <@cableCrossingDetails cableCrossingViews/>
    <@crossingAgreementDetails cableCrossingFiles/>

</div>


<#macro cableCrossingDetails cableCrossingViews>

    <h3 class="govuk-heading-m">Cable crossings</h3>

    <#list cableCrossingViews as cableCrossingView>

        <@fdsCheckAnswers.checkAnswers>

            <@fdsCheckAnswers.checkAnswersRow keyText="Cable name" actionUrl="" screenReaderActionText="" actionText="">
                ${cableCrossingView.cableName}
            </@fdsCheckAnswers.checkAnswersRow>

            <@fdsCheckAnswers.checkAnswersRow keyText="Cable owner" actionUrl="" screenReaderActionText="" actionText="">
                ${cableCrossingView.owner}
            </@fdsCheckAnswers.checkAnswersRow>

            <@fdsCheckAnswers.checkAnswersRow keyText="Location" actionUrl="" screenReaderActionText="" actionText="">
                ${cableCrossingView.location}
            </@fdsCheckAnswers.checkAnswersRow>

        </@fdsCheckAnswers.checkAnswers>

    </#list>

    <#if !cableCrossingViews?has_content>
        <@fdsInsetText.insetText>
            No cable crossings have been added to this application.
        </@fdsInsetText.insetText>
    </#if>

</#macro>


<#macro crossingAgreementDetails cableCrossingFiles>

    <h3 class="govuk-heading-m">Cable crossings agreements</h3>
    <#if cableCrossingFiles?has_content>
        <@pwaFiles.uploadedFileList downloadUrl=springUrl(cableCrossingUrlFactory.getFileDownloadUrl()) existingFiles=cableCrossingFiles/>
    <#else>
        <@fdsInsetText.insetText>
            No cable crossings agreement documents have been added to this application.
        </@fdsInsetText.insetText>
    </#if>
</#macro>


