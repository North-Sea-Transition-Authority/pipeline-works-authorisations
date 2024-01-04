<#include '../../pwaLayoutImports.ftl'>

<#-- @ftlvariable name="sectionDisplayText" type="java.lang.String" -->
<#-- @ftlvariable name="carbonStorageCrossingViews" type="java.util.List<uk.co.ogauthority.pwa.features.application.tasks.crossings.carbonstoragearea.CarbonStorageCrossingView>" -->
<#-- @ftlvariable name="carbonStorageCrossingFileViews" type="java.util.List<uk.co.ogauthority.pwa.features.mvcforms.fileupload.UploadedFileView>" -->
<#-- @ftlvariable name="isDocumentsRequired" type="java.lang.boolean" -->


<div class="pwa-application-summary-section">
    <h2 class="govuk-heading-l" id="carbonStorageCrossingDetails">${sectionDisplayText}</h2>

    <@carbonStorageCrossingDetails carbonStorageCrossingViews/>
    <#if isDocumentsRequired>
        <@crossingAgreementDetails carbonStorageCrossingFileViews/>
    </#if>

</div>


<#macro carbonStorageCrossingDetails crossingViews>

    <h3 class="govuk-heading-m">Carbon storage areas</h3>

    <#list crossingViews as crossingView>
        <@fdsCheckAnswers.checkAnswers>
            <@fdsCheckAnswers.checkAnswersRow keyText="Area reference" actionUrl="" screenReaderActionText="" actionText="">
                ${crossingView.storageAreaReference}
            </@fdsCheckAnswers.checkAnswersRow>
            <@fdsCheckAnswers.checkAnswersRow keyText="Area owner" actionUrl="" screenReaderActionText="" actionText="">
                <ul class="govuk-list">
                    <#if crossingView.ownedCompletelyByHolder>
                        <li>Holder owned</li>
                    <#elseif crossingView.operatorList?has_content>
                        <#list crossingView.operatorList as operator>
                          <li>${operator}</li>
                        </#list>
                    <#else>
                      <li>No operator</li>
                    </#if>
                </ul>
            </@fdsCheckAnswers.checkAnswersRow>

        </@fdsCheckAnswers.checkAnswers>

    </#list>

    <#if !crossingViews?has_content>
        <@fdsInsetText.insetText>
            No carbon storage areas have been added to this application.
        </@fdsInsetText.insetText>
    </#if>

</#macro>


<#macro crossingAgreementDetails crossingFileViews>

    <h3 class="govuk-heading-m">Carbon storage area crossing agreements</h3>
    <#if crossingFileViews?has_content>
        <@pwaFiles.uploadedFileList downloadUrl=springUrl(carbonStorageCrossingUrlFactory.getFileDownloadUrl()) existingFiles=crossingFileViews/>
    <#else>
        <@fdsInsetText.insetText>
            No carbon storage area crossing agreement documents have been added to this application.
        </@fdsInsetText.insetText>
    </#if>
</#macro>
