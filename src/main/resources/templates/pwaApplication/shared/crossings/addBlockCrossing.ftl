<#include '../../../layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->
<#-- @ftlvariable name="crossedBlockOwnerOptions" type="java.util.List<uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.CrossedBlockOwner>" -->
<#-- @ftlvariable name="orgUnits" type="java.util.Map<java.lang.String, java.lang.String>" -->
<#-- @ftlvariable name="blockSelectorUrl" type="java.lang.String" -->
<#-- @ftlvariable name="backUrl" type="java.lang.String" -->

<@defaultPage htmlTitle="Add block" pageHeading="Add block" breadcrumbs=true errorItems=errorList>

    <@fdsForm.htmlForm>
        <@fdsSearchSelector.searchSelectorRest path="form.pickedBlock" restUrl=springUrl(blockSelectorUrl) labelText="What block is the pipeline located in or crossing?" selectorMinInputLength=3 preselectedItems=preselectedBlock
        hintText="For example, 44/22a"/>
        <@fdsRadio.radioGroup
        path="form.crossedBlockOwner"
        labelText="Who owns the block?"
        hintText="If the block owner is not the PWA holder(s) you will be expected to provide a block crossing agreement document"
        hiddenContent=true>
            <#assign firstItem=true/>
            <#list crossedBlockOwnerOptions as crossedBlock>
                <@fdsRadio.radioItem path="form.crossedBlockOwner" itemMap={crossedBlock : crossedBlock.getDisplayName()} isFirstItem=firstItem>
                    <#if crossedBlock == "PORTAL_ORGANISATION">
                        <@fdsSearchSelector.searchSelectorEnhanced path="form.blockOwnersOuIdList" options=orgUnits labelText="Select block owner" nestingPath="form.crossedBlockOwner" />
                    </#if>
                </@fdsRadio.radioItem>
                <#assign firstItem=false/>
            </#list>
        </@fdsRadio.radioGroup>
        <@fdsAction.submitButtons linkSecondaryAction=true primaryButtonText="Add block" secondaryLinkText="Back to licence and blocks" linkSecondaryActionUrl=springUrl(backUrl) />
    </@fdsForm.htmlForm>
</@defaultPage>