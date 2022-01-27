<#include '../../../layout.ftl'>
<#import '../../../components/coordinates/coordinateInput.ftl' as coordinateInput/>

<#-- @ftlvariable name="pipelineTypes" type="java.util.Map<java.lang.String,java.lang.String>" -->
<#-- @ftlvariable name="longDirections" type="java.util.Map<java.lang.String,java.lang.String>" -->
<#-- @ftlvariable name="form" type="uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PipelineHeaderForm" -->
<#-- @ftlvariable name="cancelUrl" type="String" -->
<#-- @ftlvariable name="screenActionType" type="uk.co.ogauthority.pwa.model.form.enums.ScreenActionType" -->
<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.ogauthority.pwa.model.form.fds.ErrorItem>" -->
<#-- @ftlvariable name="pipelineNumber" type="String" -->
<#-- @ftlvariable name="requiredQuestions" type="java.util.Set<uk.co.ogauthority.pwa.features.application.tasks.pipelines.core.PipelineHeaderQuestion>" -->

<@defaultPage htmlTitle="${screenActionType.actionText} ${pipelineNumber!} pipeline" pageHeading="${screenActionType.actionText} ${pipelineNumber!} pipeline" breadcrumbs=true errorItems=errorList>

    <@fdsForm.htmlForm>

        <@fdsFieldset.fieldset legendHeading="Where does the pipeline start?" legendHeadingSize="h2" legendHeadingClass="govuk-fieldset__legend--l">

            <@fdsTextInput.textInput path="form.fromLocation" labelText="Structure" />

            <@coordinateInput.latitudeInput degreesLocationPath="form.fromCoordinateForm.latitudeDegrees"
            minutesLocationPath="form.fromCoordinateForm.latitudeMinutes"
            secondsLocationPath="form.fromCoordinateForm.latitudeSeconds"
            formId="fromLatitude"
            labelText="Start point latitude"/>

            <@coordinateInput.longitudeInput degreesLocationPath="form.fromCoordinateForm.longitudeDegrees"
            minutesLocationPath="form.fromCoordinateForm.longitudeMinutes"
            secondsLocationPath="form.fromCoordinateForm.longitudeSeconds"
            direction="EW"
            directionPath="form.fromCoordinateForm.longitudeDirection"
            directionList=longDirections
            formId="fromLongitude"
            labelText="Start point longitude"/>

        </@fdsFieldset.fieldset>

        <@fdsFieldset.fieldset legendHeading="Where does the pipeline finish?" legendHeadingSize="h2" legendHeadingClass="govuk-fieldset__legend--l">

            <@fdsTextInput.textInput path="form.toLocation" labelText="Structure" />

            <@coordinateInput.latitudeInput degreesLocationPath="form.toCoordinateForm.latitudeDegrees"
            minutesLocationPath="form.toCoordinateForm.latitudeMinutes"
            secondsLocationPath="form.toCoordinateForm.latitudeSeconds"
            formId="toLatitude"
            labelText="Finish point latitude"/>

            <@coordinateInput.longitudeInput degreesLocationPath="form.toCoordinateForm.longitudeDegrees"
            minutesLocationPath="form.toCoordinateForm.longitudeMinutes"
            secondsLocationPath="form.toCoordinateForm.longitudeSeconds"
            direction="EW"
            directionPath="form.toCoordinateForm.longitudeDirection"
            directionList=longDirections
            formId="toLongitude"
            labelText="Finish point longitude"/>

        </@fdsFieldset.fieldset>

        <@fdsFieldset.fieldset legendHeading="Pipeline information" legendHeadingSize="h2" legendHeadingClass="govuk-fieldset__legend--l">

            <@fdsSelect.select path="form.pipelineType" labelText="Pipeline type" options=pipelineTypes />

            <@fdsTextInput.textInput path="form.length" labelText="Length (m)" inputClass="govuk-input--width-5"/>

            <@fdsTextarea.textarea path="form.componentPartsDescription" labelText="Description of component parts of the pipeline" hintText="e.g. 10\" production flowline, electrical lead d B, 2 x 6\" Production Jumper within a Wellhead Bundle, 6\" flexible gas lift flowline, control umbilical etc" characterCount=true maxCharacterLength=maxCharacterLength?c/>

            <@fdsTextarea.textarea path="form.productsToBeConveyed" labelText="Products to be conveyed" characterCount=true maxCharacterLength=maxCharacterLength?c/>

            <@fdsRadio.radioGroup path="form.trenchedBuriedBackfilled" labelText="Will the pipeline be trenched and/or buried and/or backfilled?" hiddenContent=true>
                <@fdsRadio.radioYes path="form.trenchedBuriedBackfilled">
                    <@fdsTextarea.textarea path="form.trenchingMethods" labelText="Describe the methods to be deployed to execute the trenching and the target depth of trench" nestingPath="form.trenchedBuriedBackfilled" characterCount=true maxCharacterLength=maxCharacterLength?c/>
                </@fdsRadio.radioYes>
                <@fdsRadio.radioNo path="form.trenchedBuriedBackfilled"/>
            </@fdsRadio.radioGroup>


            <@fdsRadio.radioGroup path="form.pipelineFlexibility" labelText="Is this a flexible or rigid pipeline?" hiddenContent=true>
                <#assign firstItem=true/>
                <#list pipelineFlexibilityTypes as  pipelineFlexibilityTypeOption>
                    <@fdsRadio.radioItem path="form.pipelineFlexibility" itemMap={pipelineFlexibilityTypeOption : pipelineFlexibilityTypeOption.getDisplayText()} isFirstItem=firstItem/>
                    <#assign firstItem=false/>
                </#list>
            </@fdsRadio.radioGroup>


            <@fdsRadio.radioGroup path="form.pipelineMaterial" labelText="What materials were used to construct the pipeline?" hiddenContent=true>
                <#assign firstItem=true/>
                <#list pipelineMaterialTypes as  pipelineMaterialTypeOption>
                    <@fdsRadio.radioItem path="form.pipelineMaterial" itemMap={pipelineMaterialTypeOption : pipelineMaterialTypeOption.getDisplayText()} isFirstItem=firstItem>
                        <#if pipelineMaterialTypeOption == "OTHER">
                            <@fdsTextarea.textarea path="form.otherPipelineMaterialUsed" nestingPath="form.pipelineMaterial" labelText="Provide details of other materials used" characterCount=true maxCharacterLength=maxCharacterLength?c/>
                        </#if>
                    </@fdsRadio.radioItem>
                    <#assign firstItem=false/>
                </#list>
            </@fdsRadio.radioGroup>

            <@fdsTextInput.textInput path="form.pipelineDesignLife" labelText="What is the design life of the pipeline?" suffix="years" inputClass="govuk-input--width-5"/>

            <@fdsRadio.radioGroup path="form.pipelineInBundle" labelText="Is the full length of this pipeline in a bundle?" hiddenContent=true>
                <@fdsRadio.radioYes path="form.pipelineInBundle">
                    <@fdsSearchSelector.searchSelectorRest path="form.bundleName" restUrl=springUrl(bundleNameRestUrl) labelText="What is the name of the bundle?" hintText="Use the same bundle name on all pipelines in the same bundle. Each different bundle must be given a unique name." preselectedItems={"${form.bundleName!''}": "${form.bundleName!''}"}/>
                </@fdsRadio.radioYes>
                <@fdsRadio.radioNo path="form.pipelineInBundle">
                  <p class="govuk-body">If part of the pipeline is in a bundle add this information in the description of component part for the ident.</p>
                </@fdsRadio.radioNo>
            </@fdsRadio.radioGroup>

        </@fdsFieldset.fieldset>

        <#if requiredQuestions?seq_contains("OUT_OF_USE_ON_SEABED_REASON")>
            <@fdsTextarea.textarea path="form.whyNotReturnedToShore" labelText="Why is the pipeline not being returned to shore?" characterCount=true maxCharacterLength=maxCharacterLength?c/>
        </#if>

        <#if requiredQuestions?seq_contains("ALREADY_EXISTS_ON_SEABED")>
            <@fdsRadio.radioGroup path="form.alreadyExistsOnSeabed" labelText="Does this pipeline already exist on the seabed?" hiddenContent=true>
                <@fdsRadio.radioYes path="form.alreadyExistsOnSeabed">
                    <@fdsRadio.radioGroup path="form.pipelineInUse" labelText="Is the pipeline in use?">
                        <@fdsRadio.radioYes path="form.pipelineInUse"/>
                        <@fdsRadio.radioNo path="form.pipelineInUse"/>
                    </@fdsRadio.radioGroup>
                </@fdsRadio.radioYes>
                <@fdsRadio.radioNo path="form.alreadyExistsOnSeabed"/>
            </@fdsRadio.radioGroup>
        </#if>

        <@fdsTextarea.textarea path="form.footnote" labelText="Advise of any special features of the pipeline" maxCharacterLength=maxCharacterLength?c characterCount=true optionalLabel=true hintText="For example it replaces another pipeline, it can run reverse flow, it was removed from another pipeline, etc"/>
        <@fdsDetails.summaryDetails summaryTitle="Show me examples of special features to include">
            <p> The examples below are not an exhaustive list. You should replace the placeholder text identified inside square brackets with the information relevant to your pipeline. </p>
            <ul class="govuk-list govuk-list--bullet">
                <li> This Electrical Flying Lead replaces the functionality of failed Electrical Flying Lead in [PLUXXXX]. </li>
                <li> Removed from [PLXXXX] and laid alongside </li>
                <li> [PLXXXX] can run a reverse flow from [point x] to [point y] </li>
                <li> [PLXXXX] will be disconnected from [point x] to [point y]. It will be flushed and filled with filtered seawater and the ends protected with wooden covers. It will remain in situ on the seabed. </li>
                <li> Treaty between United Kingdom and the [named country] for the subject pipeline allows for gas flow in both directions. </li>
            </ul>
        </@fdsDetails.summaryDetails>


        <@fdsAction.submitButtons linkSecondaryAction=true primaryButtonText="${screenActionType.submitButtonText} pipeline" secondaryLinkText="Cancel" linkSecondaryActionUrl=springUrl(cancelUrl)/>

    </@fdsForm.htmlForm>
</@defaultPage>

