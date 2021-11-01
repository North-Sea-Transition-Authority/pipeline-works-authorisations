<#include '../../pwaLayoutImports.ftl'>

<#-- @ftlvariable name="sectionDisplayText" type="java.lang.String" -->
<#-- @ftlvariable name="medianLineAgreementView" type="uk.co.ogauthority.pwa.model.form.pwaapplications.views.MedianLineAgreementView" -->
<#-- @ftlvariable name="medianLineFiles" type="java.util.List<uk.co.ogauthority.pwa.model.form.files.UploadedFileView>" -->
<#-- @ftlvariable name="medianLineUrlFactory" type="uk.co.ogauthority.pwa.features.application.tasks.crossings.medianline.MedianLineCrossingUrlFactory" -->


<div class="pwa-application-summary-section">
    <h2 class="govuk-heading-l" id="medianLineAgreementDetails">${sectionDisplayText}</h2>

    <@medianLineAgreementDetails medianLineAgreementView/>
    <@medianLineAgreementDocuments medianLineFiles/>

</div>


<#macro medianLineAgreementDetails medianLineAgreementView>

    <h3 class="govuk-heading-m">Median line agreement</h3>

    <@fdsCheckAnswers.checkAnswers>

        <@fdsCheckAnswers.checkAnswersRow keyText="Status" actionUrl="" screenReaderActionText="" actionText="">
            <#if medianLineAgreementView.agreementStatus?has_content>
                ${medianLineAgreementView.agreementStatus.getDisplayText()!}
            </#if>
        </@fdsCheckAnswers.checkAnswersRow>

        <#if medianLineAgreementView.agreementStatus?has_content && medianLineAgreementView.agreementStatus != "NOT_CROSSED">
            <@fdsCheckAnswers.checkAnswersRow keyText="Name of negotiator" actionUrl="" screenReaderActionText="" actionText="">
                ${medianLineAgreementView.negotiatorName!}
            </@fdsCheckAnswers.checkAnswersRow>

            <@fdsCheckAnswers.checkAnswersRow keyText="Contact email for negotiator" actionUrl="" screenReaderActionText="" actionText="">
                ${medianLineAgreementView.negotiatorEmail!}
            </@fdsCheckAnswers.checkAnswersRow>
        </#if>

    </@fdsCheckAnswers.checkAnswers>

</#macro>


<#macro medianLineAgreementDocuments medianLineFiles>

    <h3 class="govuk-heading-m">Median line agreement documents</h3>
    <#if medianLineFiles?has_content>
        <@pwaFiles.uploadedFileList downloadUrl=springUrl(medianLineUrlFactory.getFileDownloadUrl()) existingFiles=medianLineFiles/>
    <#else>
        <@fdsInsetText.insetText>
            No median line crossing agreement documents have been added to this application.
        </@fdsInsetText.insetText>
    </#if>
</#macro>
