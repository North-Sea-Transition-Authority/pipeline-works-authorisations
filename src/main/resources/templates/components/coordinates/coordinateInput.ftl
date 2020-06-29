<#import 'locationInput.ftl' as locationInput/>

<#macro latitudeInput
degreesLocationPath
minutesLocationPath
secondsLocationPath
formId
directionList=[]
direction="NS"
directionPath=""
labelText=""
hintText="From 45 - 64 for degrees, 0 - 59 for minutes, and 0 - 59.99 for seconds"
optionalLabel="fromInterceptor"
nestingPath=""
fieldsetHeadingSize="h2"
fieldsetHeadingClass="govuk-fieldset__legend--m"
formGroupClass=""
caption=""
captionClass="govuk-caption-m">
    <@locationInput.locationInput
    degreesLocationPath=degreesLocationPath
    minutesLocationPath=minutesLocationPath
    secondsLocationPath=secondsLocationPath
    formId=formId
    directionList=directionList
    direction=direction
    directionPath=directionPath
    labelText=labelText
    hintText=hintText
    optionalLabel=optionalLabel
    nestingPath=nestingPath
    fieldsetHeadingSize=fieldsetHeadingSize
    fieldsetHeadingClass=fieldsetHeadingClass
    formGroupClass=formGroupClass
    caption=caption
    captionClass=captionClass/>
</#macro>

<#macro longitudeInput
degreesLocationPath
minutesLocationPath
secondsLocationPath
formId
directionList=[]
direction="NS"
directionPath=""
labelText=""
hintText="From 0 - 30 for degrees, 0 - 59 for minutes, and 0 - 59.99 for seconds"
optionalLabel="fromInterceptor"
nestingPath=""
fieldsetHeadingSize="h2"
fieldsetHeadingClass="govuk-fieldset__legend--m"
formGroupClass=""
caption=""
captionClass="govuk-caption-m">
    <@locationInput.locationInput
    degreesLocationPath=degreesLocationPath
    minutesLocationPath=minutesLocationPath
    secondsLocationPath=secondsLocationPath
    formId=formId
    directionList=directionList
    direction=direction
    directionPath=directionPath
    labelText=labelText
    hintText=hintText
    optionalLabel=optionalLabel
    nestingPath=nestingPath
    fieldsetHeadingSize=fieldsetHeadingSize
    fieldsetHeadingClass=fieldsetHeadingClass
    formGroupClass=formGroupClass
    caption=caption
    captionClass=captionClass/>
</#macro>