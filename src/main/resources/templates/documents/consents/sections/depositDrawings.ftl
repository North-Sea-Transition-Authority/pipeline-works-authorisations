<#include '../../../pwaLayoutImports.ftl'>

<#-- @ftlvariable name="sectionName" type="String"-->
<#-- @ftlvariable name="drawingRefToFileIdMap" type="java.util.Map<String, String>"-->
<#-- @ftlvariable name="fileIdToImgSourceMap" type="java.util.Map<String, String>"-->

<div>

  <h2 class="govuk-heading-l" style="page-break-before: always">${sectionName}</h2>

  <#list drawingRefToFileIdMap as drawingRef, fileId>
    <p style="page-break-before: always">${drawingRef}</p>
    <div>
      <img src="${fileIdToImgSourceMap[fileId]}" alt="some-alt" class="deposit-image"/>
    </div>
  </#list>

</div>