<#include '../../pwaLayoutImports.ftl'>


<#-- @ftlvariable name="sectionDisplayText" type="java.lang.String" -->
<#-- @ftlvariable name="canShowAdmiraltyChart" type="java.lang.Boolean" -->
<#-- @ftlvariable name="canShowUmbilicalCrossSection" type="java.lang.Boolean" -->


<div class="pwa-application-summary-section">
    <#if canShowAdmiraltyChart || canShowUmbilicalCrossSection>
        <h2 class="govuk-heading-l" id="technicalDrawings">${sectionDisplayText}</h2>
    
        <#if canShowAdmiraltyChart>
            <@admiraltyChart admiraltyChartUrlFactory  admiraltyChartFileViews />
        </#if>
        <#if canShowUmbilicalCrossSection>
            <@umbilicalFiles umbilicalUrlFactory umbilicalFileViews/>
        </#if>
    </#if>
</div>





<#macro admiraltyChart admiraltyChartUrlFactory  admiraltyChartFileViews=[]>
    <h3 class="govuk-heading-m"> Admiralty chart </h3>
    <#if admiraltyChartFileViews?has_content>
        <@pwaFiles.uploadedFileList downloadUrl=springUrl(admiraltyChartUrlFactory.getDocumentsDownloadUrl()) existingFiles=admiraltyChartFileViews />
    <#else>
        <@fdsInsetText.insetText>
            No admiralty chart has been added to this application.
        </@fdsInsetText.insetText>
    </#if>
</#macro>

<#macro umbilicalFiles umbilicalUrlFactory umbilicalFileViews=[]>
    <h3 class="govuk-heading-m"> Umbilical cross-section diagram </h3>
    <#if umbilicalFileViews?has_content>
        <@pwaFiles.uploadedFileList downloadUrl=springUrl(umbilicalUrlFactory.getDocumentDownloadUrl()) existingFiles=umbilicalFileViews />
    <#else>
        <@fdsInsetText.insetText>
            No umbilical cross-section diagram has been added to this application.
        </@fdsInsetText.insetText>
    </#if>
</#macro>
