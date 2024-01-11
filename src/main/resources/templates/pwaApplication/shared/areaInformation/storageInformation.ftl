<#include '../../../layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->
<#-- @ftlvariable name="startingSize" type"java.lang.Integer" -->

<@defaultPage htmlTitle="Storage area information" breadcrumbs=true errorItems=errorList>

    <@fdsForm.htmlForm>

        <@fdsRadio.radioGroup path="form.linkedToArea" labelText="Is this PWA related to a carbon storage area?" fieldsetHeadingSize="h1" fieldsetHeadingClass="govuk-fieldset__legend--l" hiddenContent=true>
            <@fdsRadio.radioYes path="form.linkedToArea">
                <@fdsAddAField.addAField path="form.linkedAreas" actionLinkText="Add another storage licence number" fieldListSize=preSelectedItems?size fieldLabelText="Which storage licences are covered by this PWA?"/>
            </@fdsRadio.radioYes>
            <@fdsRadio.radioNo path="form.linkedToArea">
                <@fdsTextarea.textarea path="form.noLinkedAreaDescription" labelText="What is this PWA related to?" characterCount=true maxCharacterLength=maxCharacterLength?c/>
            </@fdsRadio.radioNo>
        </@fdsRadio.radioGroup>

        <@fdsAction.submitButtons primaryButtonText=submitPrimaryButtonText secondaryButtonText=submitSecondaryButtonText/>

    </@fdsForm.htmlForm>

</@defaultPage>
