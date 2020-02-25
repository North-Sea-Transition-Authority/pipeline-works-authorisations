<#include '../../../layout.ftl'>

<@defaultPage htmlTitle="Add telecommunication cable crossing agreement" pageHeading="Add a new telecommunication cable crossing agreement" breadcrumbs=true>

    <@fdsForm.htmlForm>
        <@fdsFieldset.fieldset legendHeading="Telecommunication cable crossing details">
            <@fdsTextInput.textInput path="form.cableNameOrLocation" labelText="Cable name/location"/>
            <@fdsTextInput.textInput path="form.holderOfCable" labelText="Holder of cable"/>
        </@fdsFieldset.fieldset>

        <@fdsAction.button buttonText="Add telecommunication cable crossing"/>

    </@fdsForm.htmlForm>

</@defaultPage>