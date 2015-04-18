package com.atex.plugins.link;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.path.ContentPathCreator;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.cm.servlet.RequestPreparator;
import com.polopoly.cm.servlet.URLBuilder;
import com.polopoly.model.ModelPathUtil;
import com.polopoly.model.ModelWrite;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;
import com.polopoly.siteengine.mvc.RenderControllerBase;

/**
 * Render controller for {@link LinkResourcePolicy}.
 *
 * <p>
 * If the request attribute ATTR_NAME_LINK_TEXT is present,
 * it is used as link text.
 *
 * <p>
 * If the request attribute ATTR_NAME_LINK_ATTRS is present, all entries
 * in this map will be uses as attributes on the a tag. Note that the href
 * attribute will never be inherited.
 *
 * @author mnova
 */
public class RenderControllerLinkResource extends RenderControllerBase {

    static final String ATTR_NAME_LINK_TEXT = "linkText";
    static final String ATTR_NAME_LINK_ATTRS = "linkAttrs";

    private static final Logger LOG = Logger.getLogger(RenderControllerLinkResource.class.getName());

    @SuppressWarnings("unchecked")
    @Override
    public void populateModelBeforeCacheKey(final RenderRequest request,
                                            final TopModel m,
                                            final ControllerContext context) {

        final ModelWrite localModel = m.getLocal();

        // Get link name
        String linkText = (String) request.getAttribute(ATTR_NAME_LINK_TEXT);

        if (linkText == null) {
            linkText = (String) ModelPathUtil.get(localModel, "content/name");
        }

        final Map<String, String> attrs = new HashMap<String, String>();

        // Inherit attributes
        final Map<String, String> inheritedAttrs = (Map<String, String>) request.getAttribute(ATTR_NAME_LINK_ATTRS);

        if (inheritedAttrs != null) {
            attrs.putAll(inheritedAttrs);
        }

        // Get/create href and put in attrs
        final String linkType = (String) ModelPathUtil.get(localModel, "content/link/selectedName");
        String href = null;

        if (!"internal".equals(linkType)) {
            href = (String) ModelPathUtil.get(localModel, "content/link/selected/href/value");

            if (linkText == null) {
                linkText = href;
            }
        } else {
            final CmClient cmClient = getCmClient(context);

            final HttpServletRequest httpRequest = (HttpServletRequest) request;
            final URLBuilder urlBuilder = RequestPreparator.getURLBuilder(httpRequest);
            final PolicyCMServer cmServer = cmClient.getPolicyCMServer();

            final ContentId contentId = (ContentId) ModelPathUtil.get(localModel, "content/link/selected/content/contentId");
            ContentPathCreator path = RequestPreparator.getPathCreator(httpRequest);

            try {
                href = urlBuilder.createUrl(
                        path.createPath(contentId, cmServer),
                        null,
                        httpRequest);

                if (linkText == null) {
                    linkText = cmServer.getContent(contentId).getName();
                }
            } catch (CMException e) {
                LOG.log(Level.WARNING, "Could not create internal link.", e);
            }
        }

        attrs.put("href", href);

        // Get title and put in attrs
        if (attrs.get("title") == null) {
            String title = (String) ModelPathUtil.get(localModel, "content/title/value");

            if (title != null) {
                attrs.put("title", title);
            }
        }

        // Put link text and attributes in model
        ModelPathUtil.set(localModel, "text", linkText);
        ModelPathUtil.set(localModel, "attrs", attrs);
    }

}
