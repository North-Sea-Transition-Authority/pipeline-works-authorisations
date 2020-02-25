<#include '../../../layout.ftl'>

<@defaultPage htmlTitle="Add pipeline crossing agreement" pageHeading="Add a new pipeline crossing agreement" breadcrumbs=true>

    <@fdsForm.htmlForm>
        <@fdsFieldset.fieldset legendHeading="Pipeline crossing details">
            <@fdsTextInput.textInput path="form.pipelineNumber" labelText="Pipeline number"/>
            <@fdsTextInput.textInput path="form.ownerOfPipeline" labelText="Owner of pipeline"/>
        </@fdsFieldset.fieldset>

        <@fdsAction.button buttonText="Add pipeline crossing"/>

    </@fdsForm.htmlForm>

</@defaultPage>