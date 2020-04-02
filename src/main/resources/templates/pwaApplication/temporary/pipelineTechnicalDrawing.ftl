<#include "../../layout.ftl">
<#import "../../dummyFileUpload.ftl" as dummyFileUpload/>

<@defaultPage htmlTitle="Upload a technical drawing" pageHeading="Upload a technical drawing for ${pipelineNumber}" breadcrumbs=true>

    <@dummyFileUpload.dummyFileUpload id="technicalDrawing" uploadUrl="/" deleteUrl="/" downloadUrl="/" maxAllowedSize="500" allowedExtensions="png|jpg"/>

    <@fdsForm.htmlForm>
      <@fdsAction.button buttonText="Continue"/>
    </@fdsForm.htmlForm>

</@defaultPage>