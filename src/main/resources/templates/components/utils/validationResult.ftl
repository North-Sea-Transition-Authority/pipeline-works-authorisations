<#include '../../layout.ftl'>

<#-- Helper file for summary screen validation-related frontend  -->

<#--
Get the object id to use in the card/check answers etc to allow error items in summary to click focus on them.
Params:
  SummaryScreensummaryValidationResult summaryValidationResult
  String id
-->
<#function constructObjectId summaryValidationResult id>
  <#return summaryValidationResult?has_content?then(summaryValidationResult.idPrefix + id, "") />
</#function>

<#--
Return true if the validation result exists and an error exists for the object id passed in.
Use to determine whether or not your card or check answers should be showing errors.
Params:
  SummaryScreensummaryValidationResult summaryValidationResult
  String objectId
-->
<#function hasErrors summaryValidationResult objectId>
  <#return summaryValidationResult?has_content
  && summaryValidationResult.getErrorItem(objectId)?has_content />
</#function>

<#--
Return the error message for an object id if one exists, otherwise empty string.
Use to show the error message on your card or check answers.
Params:
  SummaryScreensummaryValidationResult summaryValidationResult
  String objectId
-->
<#function errorMessageOrEmptyString summaryValidationResult objectId>
  <#return hasErrors(summaryValidationResult, objectId)?then(
    summaryValidationResult.getErrorItem(objectId)?has_content?then(
      summaryValidationResult.getErrorItem(objectId).errorMessage, "")
    , "") />
</#function>

<#--
If the validation result is present and has a section incomplete error, show it in a single error banner.
Params:
  SummaryScreensummaryValidationResult summaryValidationResult
-->
<#macro singleErrorSummary summaryValidationResult>
  <#if summaryValidationResult?has_content && summaryValidationResult.sectionIncompleteError?has_content>
    <@fdsError.singleErrorSummary errorMessage=summaryValidationResult.sectionIncompleteError />
  </#if>
</#macro>

<#--
If the validation result is present and has one or more error items, show them in a full error summary.
Params:
  SummaryScreensummaryValidationResult summaryValidationResult
-->
<#macro errorSummary summaryValidationResult>
  <#if summaryValidationResult?has_content && summaryValidationResult.errorItems?has_content>
    <@fdsError.errorSummary errorItems=summaryValidationResult.errorItems />
  </#if>
</#macro>