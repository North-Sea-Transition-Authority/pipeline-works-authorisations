<#include '../../layout.ftl'>
<#import 'identsSummary.ftl' as identsSummary/>

<!-- @ftlvariable name="projectInformationUrl" type="java.lang.String" -->

<@defaultPage htmlTitle="${pipelineView.pipelineNumber} idents" pageHeading="${pipelineView.pipelineNumber} idents" breadcrumbs=true>

    <@identsSummary.identsSummary pipelineView=pipelineView canEdit=true/>

</@defaultPage>