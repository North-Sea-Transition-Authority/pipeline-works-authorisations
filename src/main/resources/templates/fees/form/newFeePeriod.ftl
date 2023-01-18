<#include '../../layout.ftl'>

<#-- @ftlvariable name="applicationTypes" type="java.util.List<PwaApplicationType>" -->
<#-- @ftlvariable name="applicationFeeTypes" type="java.util.List<PwaApplicationFeeType>" -->
<#-- @ftlvariable name="errorList" type="java.util.Map<java.lang.String,java.util.List<java.lang.String,java.lang.String>>" -->
<#-- @ftlvariable name="cancelUrl" type="String" -->

<@defaultPage htmlTitle="Create new fee period"
fullWidthColumn=true
pageHeading="Create new fee period"
topNavigation=false
errorItems=errorList
errorCheck=errorList?has_content
backLinkUrl=""
backLinkText="Cancel">
    <@fdsForm.htmlForm>
      <#include 'feePeriodBaseForm.ftl'>
      <@fdsAction.submitButtons primaryButtonText="Create" secondaryLinkText="Cancel" linkSecondaryAction=true linkSecondaryActionUrl=springUrl(cancelUrl)/>
    </@fdsForm.htmlForm>
</@defaultPage>