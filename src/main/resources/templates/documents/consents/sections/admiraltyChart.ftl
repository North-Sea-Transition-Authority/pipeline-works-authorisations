<#include '../../../pwaLayoutImports.ftl'>
<#import '../fragments/consentDocImage.ftl' as pwaConsentDocImage>

<#-- @ftlvariable name="sectionName" type="String"-->
<#-- @ftlvariable name="admiraltyChartImgSource" type="String"-->

<div>

  <h2 class="govuk-heading-l" style="page-break-before: always">${sectionName}</h2>

  <@pwaConsentDocImage.img src=admiraltyChartImgSource />

</div>