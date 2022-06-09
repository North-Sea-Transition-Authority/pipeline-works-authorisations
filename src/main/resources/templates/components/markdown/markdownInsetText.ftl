<#include '../../layout.ftl'>

<#macro text widgetDisplayName govNotifyMarkdown=false>
    <#if govNotifyMarkdown>
        <@fdsInsetText.insetText>The ${widgetDisplayName} below supports <@fdsAction.link linkText="Markdown" linkUrl=springUrl(govNotifyMarkdownGuidanceUrl) openInNewTab=true/> for text formatting.</@fdsInsetText.insetText>
    <#else>
        <@fdsInsetText.insetText>The ${widgetDisplayName} below supports <@fdsAction.link linkText="Markdown" linkUrl=springUrl(markdownGuidanceUrl) openInNewTab=true/> for text formatting.</@fdsInsetText.insetText>
    </#if>
</#macro>