<#include '../../../layout.ftl'>

<#-- @ftlvariable name="minNumber" type="java.lang.Integer" -->
<#-- @ftlvariable name="maxNumber" type="java.lang.Integer" -->

<@defaultPage htmlTitle="Set pipeline number" pageHeading="Set pipeline reference" breadcrumbs=true errorItems=errorList twoThirdsColumn=true>

    <@fdsInsetText.insetText>Use this page to assign a custom pipeline number to new pipelines that have already had their pipeline number determined.</@fdsInsetText.insetText>

    <@fdsForm.htmlForm>
        <@fdsTextInput.textInput path="form.pipelineNumber" labelText="Pipeline number" hintText="The pipeline number must be between ${minNumber?c} and ${maxNumber?c} and in a format like [PL(U)]XXXX[.XX]" maxCharacterLength="100"/>

        <@fdsAction.button buttonText="Set pipeline number" buttonValue="SET_PIPELINE_NUMBER" />
    </@fdsForm.htmlForm>

</@defaultPage>