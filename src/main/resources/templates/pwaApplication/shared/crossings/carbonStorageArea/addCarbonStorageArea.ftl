<#include '../../../../layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->
<#-- @ftlvariable name="orgUnits" type="java.util.Map<java.lang.String, java.lang.String>" -->
<#-- @ftlvariable name="backUrl" type="java.lang.String" -->
<#-- @ftlvariable name="crossedOwnerOptions" type="java.util.List<uk.co.ogauthority.pwa.service.pwaapplications.shared.crossings.CrossingOwner>" -->


<@defaultPage htmlTitle="Add carbon storage area" pageHeading="Add carbon storage area" breadcrumbs=true errorItems=errorList>

    <@fdsForm.htmlForm>
        <@fdsTextInput.textInput path="form.storageAreaRef" labelText="Which storage area does the pipeline cross?"/>
        <@fdsRadio.radioGroup
        path="form.crossingOwner"
        labelText="Who owns the area?"
        hintText="If the carbon storage area owner is not the PWA holder(s) you will be expected to provide a crossing agreement document"
        hiddenContent=true>
            <#assign firstItem=true/>
            <#list crossedOwnerOptions as crossedOwner>
                <@fdsRadio.radioItem path="form.crossingOwner" itemMap={crossedOwner : crossedOwner.getDisplayName()} isFirstItem=firstItem>
                    <#if crossedOwner == "PORTAL_ORGANISATION">
                        <@fdsSearchSelector.searchSelectorEnhanced path="form.ownersOuIdList" options=orgUnits labelText="Select owner" nestingPath="form.crossingOwner" />
                    </#if>
                </@fdsRadio.radioItem>
                <#assign firstItem=false/>
            </#list>
        </@fdsRadio.radioGroup>
        <@fdsAction.submitButtons linkSecondaryAction=true primaryButtonText="Add storage area" secondaryLinkText="Back to carbon storage areas" linkSecondaryActionUrl=springUrl(backUrl) />
    </@fdsForm.htmlForm>
</@defaultPage>
