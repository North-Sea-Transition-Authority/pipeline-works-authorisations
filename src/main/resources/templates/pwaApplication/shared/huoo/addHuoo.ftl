<#-- @ftlvariable name="errorList" type="java.util.Map<java.lang.String,java.util.List<java.lang.String,java.lang.String>>" -->

<#include '../../../layout.ftl'>

<@defaultPage htmlTitle="${screenActionType.actionText} a holder, user, operator, or owner" breadcrumbs=true>

    <#if errorList?has_content>
        <@fdsError.errorSummary errorItems=errorList errorTitle="Errors"/>
    </#if>

    <@fdsForm.htmlForm>
        <@fdsRadio.radioGroup path="form.huooType" labelText="What type of holder, user, operator or owner is being added?" fieldsetHeadingSize="h1" fieldsetHeadingClass="govuk-fieldset__legend--xl" hiddenContent=true>
            <@fdsDetails.details detailsTitle="When can I add a treaty agreement user?" 
                detailsText="When a treaty agreement is in place for a field development or pipeline(s) that crosses the UK Median line."/>
                        
            <#assign firstItem=false/>
            <#list huooTypes as name, displayText>
                <@fdsRadio.radioItem path="form.huooType" itemMap={name:displayText} isFirstItem=false>
                    <#if name == "PORTAL_ORG">
                        <@fdsSearchSelector.searchSelectorEnhanced path="form.organisationUnitId" options=portalOrgs labelText="Select the legal entity" nestingPath="form.huooType"/>
                        <@fdsDetails.details detailsTitle="The legal entity is not in the list" 
                            detailsText="If you are unable to find the legal entity you are looking for then provide the OGA with the holder company name, address, postcode and companies house registration number to add to the PWA service: ukop@ogauthority.co.uk"/>
                        <@fdsCheckbox.checkboxes path="form.huooRoles" checkboxes=huooRoles fieldsetHeadingText="Which roles will the legal entity have?" fieldsetHeadingSize="h2" fieldsetHeadingClass="govuk-fieldset__legend--m" nestingPath="form.huooType"/>
                    <#else>
                        <@fdsRadio.radio path="form.treatyAgreement" labelText="Which country is the treaty agreement with?" radioItems=treatyAgreements nestingPath="form.huooType" fieldsetHeadingSize="h2" fieldsetHeadingClass="govuk-fieldset__legend--m"/>
                    </#if>
                </@fdsRadio.radioItem>
                <#assign firstItem=true/>
            </#list>
        </@fdsRadio.radioGroup>


        <@fdsAction.submitButtons primaryButtonText="${screenActionType.submitButtonText} holder, user, operator, or owner" linkSecondaryAction=true secondaryLinkText="Cancel" linkSecondaryActionUrl=springUrl(backUrl)/>
    </@fdsForm.htmlForm>

</@defaultPage>