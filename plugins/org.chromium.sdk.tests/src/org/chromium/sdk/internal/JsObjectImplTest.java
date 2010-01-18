// Copyright (c) 2009 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.sdk.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.chromium.sdk.Browser;
import org.chromium.sdk.BrowserFactory;
import org.chromium.sdk.BrowserTab;
import org.chromium.sdk.DebugContext;
import org.chromium.sdk.JsValue.Type;
import org.chromium.sdk.internal.protocol.data.SomeRef;
import org.chromium.sdk.internal.tools.v8.V8ProtocolUtil;
import org.chromium.sdk.internal.transport.ChromeStub;
import org.chromium.sdk.internal.transport.FakeConnectionFactory;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.Before;
import org.junit.Test;

/**
 * A test for the JsVariable implementor.
 */
public class JsObjectImplTest {

  private ChromeStub messageResponder;
  private CallFrameImpl callFrame;

  private ValueMirror eventMirror;

  private final StubListener listener = new StubListener();

  @Before
  public void setUpBefore() throws Exception {
    this.messageResponder = new FixtureChromeStub();
    Browser browser = ((BrowserFactoryImpl) BrowserFactory.getInstance())
        .create(new FakeConnectionFactory(messageResponder));
    BrowserTab browserTab = browser.createTabFetcher().getTabs().get(0).attach(listener);

    listener.expectSuspendedEvent();
    messageResponder.sendSuspendedEvent();
    DebugContext debugContext = listener.getDebugContext();

    JSONObject valueObject = (JSONObject) JSONValue.parse(
        "{\"ref\":" + FixtureChromeStub.getNumber3Ref() +
        ",\"type\":\"number\",\"value\":3,\"text\":\"3\"}");
    SomeRef someRef = V8ProtocolUtil.getV8Parser().parse(valueObject, SomeRef.class);
    DataWithRef dataWithRef = DataWithRef.fromSomeRef(someRef);
    eventMirror = ValueMirror.createObject(
        11, new SubpropertiesMirror.ListBased(
            new PropertyReference("x", dataWithRef),
            new PropertyReference("y", dataWithRef)
        ), Type.TYPE_OBJECT, null).getValueMirror();

    InternalContext internalContext = ContextBuilder.getInternalContextForTests(debugContext);

    FrameMirror frameMirror = new FrameMirror(
        null,
        "fooscript", 12, FixtureChromeStub.getScriptId(),
        "foofunction");
    this.callFrame = new CallFrameImpl(frameMirror, 0, internalContext);
  }

  @Test
  public void testObjectData() throws Exception {
    JsObjectImpl jsObject = new JsObjectImpl(callFrame.getInternalContext(), "", eventMirror);
    assertNotNull(jsObject.asObject());
    assertNull(jsObject.asArray());
    Collection<JsVariableImpl> variables = jsObject.getProperties();
    assertEquals(2, variables.size()); // "x" and "y"
    Iterator<JsVariableImpl> it = variables.iterator();
    JsVariableImpl firstVar = it.next();
    JsVariableImpl secondVar = it.next();
    Set<String> names = new HashSet<String>();
    names.add("x"); //$NON-NLS-1$
    names.add("y"); //$NON-NLS-1$

    names.remove(firstVar.getName());
    names.remove(secondVar.getName());
    assertEquals(0, names.size());

    JsValueImpl firstVal = firstVar.getValue();
    JsValueImpl secondVal = firstVar.getValue();
    assertEquals("3", firstVal.getValueString()); //$NON-NLS-1$
    assertEquals("3", secondVal.getValueString()); //$NON-NLS-1$
    assertNull(firstVal.asObject());
    assertNull(secondVal.asObject());
  }
}
