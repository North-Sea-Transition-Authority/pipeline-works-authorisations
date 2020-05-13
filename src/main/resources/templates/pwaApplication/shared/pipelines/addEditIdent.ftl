<#include '../../../layout.ftl'>

<#-- @ftlvariable name="longDirections" type="java.util.Map<java.lang.String,java.lang.String>" -->
<#-- @ftlvariable name="form" type="uk.co.ogauthority.pwa.model.form.pwaapplications.shared.pipelines.PipelineIdentForm" -->
<#-- @ftlvariable name="cancelUrl" type="String" -->
<#-- @ftlvariable name="screenActionType" type="uk.co.ogauthority.pwa.model.form.enums.ScreenActionType" -->
<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->

<@defaultPage htmlTitle="${screenActionType.actionText} ident" pageHeading="${screenActionType.actionText} ident" breadcrumbs=true>

    <#if errorList?has_content>
        <@fdsError.errorSummary errorItems=errorList />
    </#if>

    <@fdsForm.htmlForm>

        <@fdsFieldset.fieldset legendHeading="Where does the ident start?" legendHeadingSize="h2" legendHeadingClass="govuk-fieldset__legend--l">

            <@fdsTextInput.textInput path="form.fromLocation" labelText="Structure" />

            <@pwaLocationInput.locationInput degreesLocationPath="form.fromCoordinateForm.latitudeDegrees"
                                          minutesLocationPath="form.fromCoordinateForm.latitudeMinutes"
                                          secondsLocationPath="form.fromCoordinateForm.latitudeSeconds"
                                          optionalLabel="true"
                                          hintText="Provide coordinates if this is a key point along the pipeline route"
                                          formId="fromLatitude"
                                          labelText="Start point latitude"/>

            <@pwaLocationInput.locationInput degreesLocationPath="form.fromCoordinateForm.longitudeDegrees"
                                          minutesLocationPath="form.fromCoordinateForm.longitudeMinutes"
                                          secondsLocationPath="form.fromCoordinateForm.longitudeSeconds"
                                          optionalLabel="true"
                                          hintText="Provide coordinates if this is a key point along the pipeline route"
                                          direction="EW"
                                          directionPath="form.fromCoordinateForm.longitudeDirection"
                                          directionList=longDirections
                                          formId="fromLongitude"
                                          labelText="Start point longitude"/>

        </@fdsFieldset.fieldset>

        <@fdsFieldset.fieldset legendHeading="Where does the ident finish?" legendHeadingSize="h2" legendHeadingClass="govuk-fieldset__legend--l">

            <@fdsTextInput.textInput path="form.toLocation" labelText="Structure" />

            <@pwaLocationInput.locationInput degreesLocationPath="form.toCoordinateForm.latitudeDegrees"
                                          minutesLocationPath="form.toCoordinateForm.latitudeMinutes"
                                          secondsLocationPath="form.toCoordinateForm.latitudeSeconds"
                                          optionalLabel="true"
                                          hintText="Provide coordinates if this is a key point along the pipeline route"
                                          formId="toLatitude"
                                          labelText="Finish point latitude"/>

            <@pwaLocationInput.locationInput degreesLocationPath="form.toCoordinateForm.longitudeDegrees"
                                          minutesLocationPath="form.toCoordinateForm.longitudeMinutes"
                                          secondsLocationPath="form.toCoordinateForm.longitudeSeconds"
                                          optionalLabel="true"
                                          hintText="Provide coordinates if this is a key point along the pipeline route"
                                          direction="EW"
                                          directionPath="form.toCoordinateForm.longitudeDirection"
                                          directionList=longDirections
                                          formId="toLongitude"
                                          labelText="Finish point longitude"/>

        </@fdsFieldset.fieldset>

        <@fdsFieldset.fieldset legendHeading="Ident information" legendHeadingSize="h2" legendHeadingClass="govuk-fieldset__legend--l">

            <@fdsTextInput.textInput path="form.length" labelText="Length (m)" inputClass="govuk-input--width-5"/>

            <@fdsTextarea.textarea path="form.dataForm.componentPartsDescription" labelText="Description of component parts" hintText="Some guidance text here."/>

            <@fdsTextInput.textInput path="form.dataForm.externalDiameter" labelText="External diameter" inputClass="govuk-input--width-5" suffix="mm" suffixScreenReaderPrompt="mm"/>
            <@fdsTextInput.textInput path="form.dataForm.internalDiameter" labelText="Internal diameter" inputClass="govuk-input--width-5" suffix="mm" suffixScreenReaderPrompt="mm"/>
            <@fdsTextInput.textInput path="form.dataForm.wallThickness" labelText="Wall thickness" inputClass="govuk-input--width-5" suffix="mm" suffixScreenReaderPrompt="mm"/>
            <@fdsTextInput.textInput path="form.dataForm.maop" labelText="MAOP" inputClass="govuk-input--width-5" suffix="barg" suffixScreenReaderPrompt="barg"/>

            <@fdsTextarea.textarea path="form.dataForm.insulationCoatingType" labelText="Insulation / coating type"/>

            <@fdsTextarea.textarea path="form.dataForm.productsToBeConveyed" labelText="Products to be conveyed"/>

        </@fdsFieldset.fieldset>

        <@fdsAction.submitButtons linkSecondaryAction=true primaryButtonText="${screenActionType.submitButtonText} ident" secondaryLinkText="Cancel" linkSecondaryActionUrl=springUrl(cancelUrl)/>

    </@fdsForm.htmlForm>
</@defaultPage>