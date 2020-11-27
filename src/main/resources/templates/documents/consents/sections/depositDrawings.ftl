<#include '../../../pwaLayoutImports.ftl'>

<#-- @ftlvariable name="sectionName" type="String"-->
<#-- @ftlvariable name="drawingRefToFileIdMap" type="java.util.Map<String, String>"-->
<#-- @ftlvariable name="fileIdToBase64StringMap" type="java.util.Map<String, String>"-->

<div>

  <h2 class="govuk-heading-l">${sectionName}</h2>

  <#list drawingRefToFileIdMap as drawingRef, fileId>
    <p style="page-break-before: always">${drawingRef}</p>
    <img src="data:image/jpg;base64,${fileIdToBase64StringMap[fileId]}" alt="some-alt" class="deposit-image"/>
  </#list>

</div>