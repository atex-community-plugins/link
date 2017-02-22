package com.atex.plugins.link;

import com.atex.onecms.content.ContentResult;
import com.atex.onecms.content.ContentResultBuilder;
import com.atex.onecms.content.ContentWrite;
import com.atex.onecms.content.LegacyContentAdapter;
import com.atex.onecms.content.metadata.MetadataInfo;
import com.atex.plugins.baseline.policy.BaselinePolicy;
import com.polopoly.cm.app.policy.ContentReferencePolicy;
import com.polopoly.cm.app.policy.SelectableSubFieldPolicy;
import com.polopoly.cm.app.policy.SingleValuePolicy;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.policy.ContentPolicy;
import com.polopoly.cm.policymvc.PolicyModelDomain;
import com.polopoly.metadata.Metadata;
import com.polopoly.metadata.MetadataAware;
import com.polopoly.metadata.util.MetadataUtil;
import com.polopoly.model.ModelPathUtil;
import com.polopoly.model.pojo.FileReference;

/**
 * LinkResourcePolicy.
 *
 * @author mnova
 */
public class LinkResourcePolicy extends BaselinePolicy
        implements
            LegacyContentAdapter<LinkBean>,
            MetadataAware {

    public String getName()
            throws CMException
    {
        // As default, use the name of the link content itself

        String contentName = super.getName();

        if (null != contentName && contentName.trim().length() > 0) {
            return contentName;
        }

        // Fallback on the external URL or the name of the linked content

        SelectableSubFieldPolicy subFieldPolicy =
                (SelectableSubFieldPolicy) getChildPolicy("link");

        if (null != subFieldPolicy) {
            String selected = subFieldPolicy.getSelectedSubFieldName();

            if (null != selected) {
                ContentPolicy selectedFieldPolicy = (ContentPolicy) subFieldPolicy.getChildPolicy(selected);

                if ("internal".equals(selected)) {
                    ContentReferencePolicy contentRef =
                            (ContentReferencePolicy) selectedFieldPolicy.getChildPolicy("content");

                    return getInternalLinkName(contentRef);
                } else if ("external".equals(selected)) {
                    SingleValuePolicy urlPolicy =
                            (SingleValuePolicy) selectedFieldPolicy.getChildPolicy("href");

                    return getExternalLinkName(urlPolicy);
                }
            }
        }

        return "";
    }

    private String getExternalLinkName(SingleValuePolicy urlLinkPolicy)
            throws CMException {

        if (null != urlLinkPolicy) {
            String url = urlLinkPolicy.getValue();

            if (url.startsWith("http://")) {
                url = url.substring(7);
            }

            return url;
        }

        return "";
    }

    private String getInternalLinkName(ContentReferencePolicy contentSelectPolicy)
            throws CMException {

        if (null != contentSelectPolicy) {
            return getCMServer().getContent(
                    contentSelectPolicy.getReference()).getName();
        }

        return "";
    }

    @Override
    public ContentResult<LinkBean> legacyToNew(final PolicyModelDomain policyModelDomain) throws CMException
    {
        LinkBean bean = new LinkBean();

        bean.setName(getName());

        FileReference fileReference = new FileReference();
        fileReference.setMimeType("text/x-url");
        fileReference.setUrl((String) ModelPathUtil.get(policyModelDomain.getModel(this), "link/selected/href/value"));
        bean.setFileReference(fileReference);

        return new ContentResultBuilder<LinkBean>().mainAspectData(bean).build();
    }

    @Override
    public void newToLegacy(final ContentWrite<LinkBean> contentWrite) throws CMException
    {
        MetadataInfo metadata = (MetadataInfo) contentWrite.getAspect("metadata");
        if (metadata != null) {
            setMetadata(metadata.getMetadata());
        }

        LinkBean bean = contentWrite.getContentData();

        if (bean == null) {
            return;
        }

        this.setName(bean.getName());
        this.setComponent("link", "subField", "external");
        this.setComponent("link/external/href", "value", bean.getFileReference().getUrl());
    }

    @Override
    public Metadata getMetadata() {
        return getMetadataAware().getMetadata();
    }

    @Override
    public void setMetadata(final Metadata metadata) {
        getMetadataAware().setMetadata(metadata);
    }

    private MetadataAware getMetadataAware() {
        return MetadataUtil.getMetadataAware(this);
    }

}
