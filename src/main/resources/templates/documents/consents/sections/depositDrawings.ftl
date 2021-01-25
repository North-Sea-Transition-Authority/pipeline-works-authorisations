<#include '../../../pwaLayoutImports.ftl'>
<#import '../fragments/consentDocImage.ftl' as pwaConsentDocImage>

<#-- @ftlvariable name="sectionName" type="String"-->
<#-- @ftlvariable name="drawingRefToFileIdMap" type="java.util.Map<String, String>"-->
<#-- @ftlvariable name="fileIdToImgSourceMap" type="java.util.Map<String, String>"-->

<div>

  <h2 class="govuk-heading-l" style="page-break-before: always">${sectionName}</h2>

  <#list drawingRefToFileIdMap as drawingRef, fileId>
    <p style="page-break-before: always">${drawingRef}</p>
    <@pwaConsentDocImage.img src=fileIdToImgSourceMap[fileId] />
  </#list>

</div>