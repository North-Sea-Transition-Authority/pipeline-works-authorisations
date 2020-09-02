<#include '../../pwaLayoutImports.ftl'>

<#-- @ftlvariable name="sectionDisplayText" type="java.lang.String" -->


<div class="pwa-application-summary-section">
    <h2 class="govuk-heading-l" id="technicalDrawings">${sectionDisplayText}</h2>

    <@technicalDrawings admiraltyChartUrlFactory umbilicalUrlFactory admiraltyChartFileViews umbilicalFileViews />
</div>





<#macro technicalDrawings admiraltyChartUrlFactory umbilicalUrlFactory admiraltyChartFileViews=[] umbilicalFileViews=[]>
    <h3 class="govuk-heading-m"> Admiralty chart </h3>
    <#if admiraltyChartFileViews?has_content>
        <@fileUpload.uploadedFileList downloadUrl=springUrl(admiraltyChartUrlFactory.getDocumentsDownloadUrl()) existingFiles=admiraltyChartFileViews />
    <#else>
        <@fdsInsetText.insetText>
            No admiralty chart has been added to this application.
        </@fdsInsetText.insetText>
    </#if>

    <h3 class="govuk-heading-m"> Umbilical cross-section diagram </h3>
    <#if umbilicalFileViews?has_content>
        <@fileUpload.uploadedFileList downloadUrl=springUrl(umbilicalUrlFactory.getDocumentDownloadUrl()) existingFiles=umbilicalFileViews />
    <#else>
        <@fdsInsetText.insetText>
            No umbilical cross-section diagram has been added to this application.
        </@fdsInsetText.insetText>
    </#if>
</#macro>
