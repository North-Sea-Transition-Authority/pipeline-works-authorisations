<#-- @ftlvariable name="errorList" type="java.util.Map<java.lang.String,java.util.List<java.lang.String,java.lang.String>>" -->

<#include '../../../layout.ftl'>

<@defaultPage htmlTitle="${screenActionType.actionText} a holder, user, operator, or owner" pageHeading="${screenActionType.actionText} a holder, user, operator, or owner" breadcrumbs=true>

    <#if errorList?has_content>
        <@fdsError.errorSummary errorItems=errorList errorTitle="Errors"/>
    </#if>

    <@fdsForm.htmlForm>
        <#if huooType == "PORTAL_ORG">
            <@fdsSearchSelector.searchSelectorEnhanced path="form.organisationUnitId" options=portalOrgs labelText="Select the legal entity" nestingPath="form.huooType"/>
            <@fdsCheckbox.checkboxes path="form.huooRoles" checkboxes=huooRoles fieldsetHeadingText="Which roles will the legal entity have?" nestingPath="form.huooType"/>
        <#elseif huooType == "TREATY_AGREEMENT">
            <@fdsRadio.radio path="form.treatyAgreement" labelText="Which country is the treaty agreement with?" radioItems=treatyAgreements nestingPath="form.huooType"/>
        </#if>

        <@fdsDetails.details detailsText="If you are unable to find the legal entity or treaty agreement you are looking for, please contact the OGA." detailsTitle="I can't find a legal entity"/>

        <@fdsAction.submitButtons primaryButtonText="${screenActionType.submitButtonText} holder, user, operator, or owner" linkSecondaryAction=true secondaryLinkText="Cancel" linkSecondaryActionUrl=springUrl(backUrl)/>
    </@fdsForm.htmlForm>

</@defaultPage>