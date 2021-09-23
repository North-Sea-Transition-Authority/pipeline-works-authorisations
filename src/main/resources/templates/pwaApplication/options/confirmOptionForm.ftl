<#include '../../layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->
<#-- @ftlvariable name="confirmOptionList" type="java.util.Map<java.lang.String, java.lang.String>" -->
<#-- @ftlvariable name="pageHeading" type="java.lang.String" -->
<#-- @ftlvariable name="submitPrimaryButtonText" type="java.lang.String" -->
<#-- @ftlvariable name="submitSecondaryButtonText" type="java.lang.String" -->


<@defaultPage htmlTitle=pageHeading pageHeading="" breadcrumbs=true errorItems=errorList>

    <@fdsForm.htmlForm>
        <@fdsRadio.radioGroup
        labelText="What work was undertaken on this options variation?"
        path="form.confirmedOptionType"
        hiddenContent=true
        fieldsetHeadingClass="govuk-fieldset__legend--l"
        fieldsetHeadingSize="h1">
            <#assign isFirstItem = true/>
            <#list confirmOptionList as option, displayText>

                <@fdsRadio.radioItem path="form.confirmedOptionType" itemMap={option: displayText} isFirstItem=isFirstItem >
                    <#if option == "WORK_COMPLETE_AS_PER_OPTIONS">
                        <@fdsTextarea.textarea
                        path="form.optionCompletedDescription"
                        labelText="Describe the work undertaken"
                        characterCount=true
                        maxCharacterLength=maxCharacterLength?c
                        nestingPath="form.confirmedOptionType"/>

                    <#elseif option == "WORK_DONE_BUT_NOT_PRESENTED_AS_OPTION">
                        <@fdsTextarea.textarea
                        path="form.otherWorkDescription"
                        labelText="Describe the work undertaken"
                        characterCount=true
                        maxCharacterLength=maxCharacterLength?c
                        nestingPath="form.confirmedOptionType"/>
                    </#if>

                </@fdsRadio.radioItem>
                <#assign isFirstItem = false/>
            </#list>
        </@fdsRadio.radioGroup>

        <@fdsAction.submitButtons primaryButtonText=submitPrimaryButtonText secondaryButtonText=submitSecondaryButtonText/>

    </@fdsForm.htmlForm>

</@defaultPage>