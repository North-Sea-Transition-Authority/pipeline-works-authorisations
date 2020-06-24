<#include '../../../layout.ftl'>

<#-- @ftlvariable name="backUrl" type="java.lang.String" -->
<@defaultPage htmlTitle="Remove cable crossing" pageHeading="Are you sure you want to remove this cable crossing?" breadcrumbs=true>
    <#if errorList?has_content>
        <@fdsError.errorSummary errorItems=errorList errorTitle="Errors"/>
    </#if>

    <@fdsDataItems.dataItem>
      <@fdsDataItems.dataValues key="Cable name" value=cableCrossing.cableName/>
      <@fdsDataItems.dataValues key="Cable owner" value=cableCrossing.owner/>
      <@fdsDataItems.dataValues key="Location" value=cableCrossing.location/>
    </@fdsDataItems.dataItem>

    <@fdsForm.htmlForm>
        <@fdsAction.submitButtons linkSecondaryAction=true primaryButtonText="Remove cable crossing" secondaryLinkText="Back to cable crossings" linkSecondaryActionUrl=springUrl(backUrl)/>
    </@fdsForm.htmlForm>
</@defaultPage>