<#include '../../../layout.ftl'>

<#-- @ftlvariable name="crossedBlockOwnerOptions" type="java.util.List<uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.CrossedBlockOwner>" -->
<#-- @ftlvariable name="orgUnits" type="java.util.Map<java.lang.String, java.lang.String>" -->
<#-- @ftlvariable name="pickableBlocks" type="java.util.Map<String, String>" -->


<@defaultPage htmlTitle="Edit block" pageHeading="Edit block" breadcrumbs=true>
    <@fdsError.errorSummary errorItems=errorList />
    <@fdsDataItems.dataItem>
        <@fdsDataItems.dataValues key="Block reference" value=blockReference/>
        <@fdsDataItems.dataValues key="Licence reference" value=licenceReference/>

    </@fdsDataItems.dataItem>


    <@fdsForm.htmlForm>
        <@fdsRadio.radioGroup
        path="form.crossedBlockOwner"
        labelText="Who owns the block?"
        hintText="If the block owner is not the PWA holder(s) you will be expected to provide a block crossing agreement document"
        hiddenContent=true
        >
            <#assign firstItem=true/>
            <#list crossedBlockOwnerOptions as crossedBlock>
                <@fdsRadio.radioItem path="form.crossedBlockOwner" itemMap={crossedBlock : crossedBlock.getDisplayName()} isFirstItem=firstItem>
                    <#if crossedBlock == "PORTAL_ORGANISATION">
                        <@fdsSelect.select path="form.blockOwnersOuIdList" options=orgUnits labelText="Select block owner" nestingPath="form.crossedBlockOwner"/>
                    </#if>
                </@fdsRadio.radioItem>
                <#assign firstItem=false/>
            </#list>
        </@fdsRadio.radioGroup>
        <@fdsAction.submitButtons linkSecondaryAction=true primaryButtonText="Save block crossing" secondaryLinkText="Back to licence and blocks" linkSecondaryActionUrl=springUrl(backUrl) />
    </@fdsForm.htmlForm>
</@defaultPage>