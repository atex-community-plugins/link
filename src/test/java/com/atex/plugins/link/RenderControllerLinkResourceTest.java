package com.atex.plugins.link;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.polopoly.model.ModelBase;
import com.polopoly.model.ModelPathUtil;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;

/**
 * Unit test for {@link RenderControllerLinkResource}.
 *
 * @author mnova
 */
@RunWith(MockitoJUnitRunner.class)
public class RenderControllerLinkResourceTest {

    @Mock
    private TopModel topModel;

    private RenderControllerLinkResource target;

    @Mock
    private RenderRequest request;

    @Mock
    private ControllerContext context;

    private ModelBase localModel;

    private Map<String, String> attrsToInherit;

    @Before
    public void before() throws Exception {

        target = new RenderControllerLinkResource();

        localModel = new ModelBase();
        attrsToInherit = new HashMap<String, String>();

        Mockito.when(topModel.getLocal()).thenReturn(localModel);
    }

    @Test
    public void testShouldUseInheritedLinkTextIfPresent() throws Exception {

        Mockito.when(request.getAttribute(RenderControllerLinkResource.ATTR_NAME_LINK_TEXT)).thenReturn("inherited");

        target.populateModelBeforeCacheKey(request, topModel, context);

        Assert.assertTrue("Link did not use inherited text", localModel.getAttribute("text")
                                                                       .equals("inherited"));
    }

    @Test
    public void testShouldUseAllInheritedAttributesIfPresent() throws Exception {

        attrsToInherit.put("class", "right");
        attrsToInherit.put("style", "padding: 4px;");
        attrsToInherit.put("polopoly:contentid", "1.100.1000");

        Mockito.when(request.getAttribute(RenderControllerLinkResource.ATTR_NAME_LINK_ATTRS)).thenReturn(attrsToInherit);

        target.populateModelBeforeCacheKey(request, topModel, context);

        Assert.assertTrue("Link did not preserve html-attribute",
                ((String) ((Map<?, ?>) (localModel.getAttribute("attrs"))).get("class"))
                        .indexOf("right") != -1);

        Assert.assertTrue("Link did not preserve html-attribute",
                ((String) ((Map<?, ?>) (localModel.getAttribute("attrs"))).get("style"))
                        .indexOf("padding: 4px;") != -1);

        Assert.assertTrue("Link did not preserve polopoly-attribute",
                ((String) ((Map<?, ?>) (localModel.getAttribute("attrs"))).get("polopoly:contentid"))
                        .indexOf("1.100.1000") != -1);
    }

    @Test
    public void testShouldNeverUseInheritedHrefIfPresent() throws Exception {

        attrsToInherit.put("href", "http://www.polopoly.com");
        ModelPathUtil.set(localModel, "content/link/selected/href/value", "http://www.atex.com");

        Mockito.when(request.getAttribute(RenderControllerLinkResource.ATTR_NAME_LINK_ATTRS)).thenReturn(attrsToInherit);

        target.populateModelBeforeCacheKey(request, topModel, context);

        Assert.assertTrue("Link did not use local href",
                ((String) ((Map<?, ?>) (localModel.getAttribute("attrs"))).get("href"))
                        .indexOf("http://www.atex.com") != -1);
    }

    @Test
    public void testShouldUseInheritedTitleEvenIfLocalIsPresent() throws Exception {

        attrsToInherit.put("title", "inheritedTitle");

        ModelPathUtil.set(localModel, "content/title/value", "localTitle");

        Mockito.when(request.getAttribute(RenderControllerLinkResource.ATTR_NAME_LINK_ATTRS)).thenReturn(attrsToInherit);

        target.populateModelBeforeCacheKey(request, topModel, context);

        Assert.assertTrue("Link did not use inherited title",
                ((String) ((Map<?, ?>) (localModel.getAttribute("attrs"))).get("title"))
                        .indexOf("inheritedTitle") != -1);
    }

}