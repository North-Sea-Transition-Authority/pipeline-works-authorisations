<#include '../../pwaLayoutImports.ftl'>

<#-- @ftlvariable name="sectionDisplayText" type="java.lang.String" -->
<#-- @ftlvariable name="blockCrossingViews" type="java.util.List<uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.BlockCrossingView>" -->
<#-- @ftlvariable name="blockCrossingFileViews" type="java.util.List<uk.co.ogauthority.pwa.model.form.files.UploadedFileView>" -->
<#-- @ftlvariable name="blockCrossingFileViews" type="java.util.List<uk.co.ogauthority.pwa.model.form.files.UploadedFileView>" -->
<#-- @ftlvariable name="isDocumentsRequired" type="java.lang.boolean" -->


<div class="pwa-application-summary-section">
    <h2 class="govuk-heading-l" id="licenceBlockDetails">${sectionDisplayText}</h2>

    <@licenceBlockDetails blockCrossingViews/>
    <#if isDocumentsRequired>
        <@crossingAgreementDetails blockCrossingFileViews/>
    </#if>

</div>


<#macro licenceBlockDetails blockCrossingViews>

    <h3 class="govuk-heading-m">Blocks</h3>

    <#list blockCrossingViews as blockCrossingView>

        <h4 class="govuk-heading-s"> ${blockCrossingView.blockReference} </h4>

        <@fdsCheckAnswers.checkAnswers>

            <@fdsCheckAnswers.checkAnswersRow keyText="Licence" actionUrl="" screenReaderActionText="" actionText="">
                ${blockCrossingView.licenceReference}
            </@fdsCheckAnswers.checkAnswersRow>

            <@fdsCheckAnswers.checkAnswersRow keyText="Block operator" actionUrl="" screenReaderActionText="" actionText="">
                <ul class="govuk-list">
                    <#if blockCrossingView.blockOwnedCompletelyByHolder>
                        <li>Holder owned</li>
                    <#elseif blockCrossingView.licenceReference == "Unlicensed">
                        <li>No operator</li>
                    <#else>
                        <#list blockCrossingView.blockOperatorList as operator>
                            <li>${operator}</li>
                        </#list>
                    </#if>
                </ul>
            </@fdsCheckAnswers.checkAnswersRow>

        </@fdsCheckAnswers.checkAnswers>

    </#list>

    <#if !blockCrossingViews?has_content>
        <@fdsInsetText.insetText>
            No blocks have been added to this application.
        </@fdsInsetText.insetText>
    </#if>

</#macro>


<#macro crossingAgreementDetails blockCrossingFileViews>

    <h3 class="govuk-heading-m">Block crossing agreements</h3>
    <#if blockCrossingFileViews?has_content>
        <@pwaFiles.uploadedFileList downloadUrl=springUrl(blockCrossingUrlFactory.getFileDownloadUrl()) existingFiles=blockCrossingFileViews/>
    <#else>
        <@fdsInsetText.insetText>
            No block crossing agreement documents have been added to this application.
        </@fdsInsetText.insetText>
    </#if>
</#macro>
