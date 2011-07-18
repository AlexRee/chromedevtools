// Copyright (c) 2009 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.sdk.internal.v8native.value;

import static org.chromium.sdk.tests.internal.JsonBuilderUtil.jsonObject;
import static org.chromium.sdk.tests.internal.JsonBuilderUtil.jsonProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Collection;
import java.util.SortedMap;

import org.chromium.sdk.Browser;
import org.chromium.sdk.BrowserFactory;
import org.chromium.sdk.BrowserTab;
import org.chromium.sdk.DebugContext;
import org.chromium.sdk.JsValue;
import org.chromium.sdk.JsVariable;
import org.chromium.sdk.internal.BrowserFactoryImpl;
import org.chromium.sdk.internal.BrowserFactoryImplTestGate;
import org.chromium.sdk.internal.browserfixture.FixtureChromeStub;
import org.chromium.sdk.internal.browserfixture.StubListener;
import org.chromium.sdk.internal.transport.ChromeStub;
import org.chromium.sdk.internal.transport.FakeConnectionFactory;
import org.chromium.sdk.internal.v8native.CallFrameImpl;
import org.chromium.sdk.internal.v8native.ContextBuilder;
import org.chromium.sdk.internal.v8native.InternalContext;
import org.chromium.sdk.internal.v8native.protocol.input.FrameObject;
import org.chromium.sdk.internal.v8native.protocol.input.V8ProtocolParserAccess;
import org.chromium.sdk.internal.v8native.protocol.input.data.ValueHandle;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.Before;
import org.junit.Test;

/**
 * A test for the JsVariable implementor.
 */
public class JsArrayImplTest {

  private ChromeStub messageResponder;
  private CallFrameImpl callFrame;

  private ValueMirror arrayMirror;

  private final StubListener listener = new StubListener();

  @Before
  public void setUpBefore() throws Exception {
    this.messageResponder = new FixtureChromeStub();
    Browser browser = BrowserFactoryImplTestGate.create(
        (BrowserFactoryImpl) BrowserFactory.getInstance(),
        new FakeConnectionFactory(messageResponder));
    BrowserTab browserTab = browser.createTabFetcher().getTabs().get(0).attach(listener);

    listener.expectSuspendedEvent();
    messageResponder.sendSuspendedEvent();
    DebugContext debugContext = listener.getDebugContext();

    String propertyRefText = "{'ref':" + FixtureChromeStub.getNumber3Ref() +
        ",'type':'number','value':3,'text':'3'}";

    String valueHandleJsonText = (
            "{'protoObject':{'ref':55516,'className':'Array','type':'object'}," +
            "'text':'#<an Array>'," +
            "'handle':5559,'" +
            "constructorFunction':{'ref':55515,'inferredName':''," +
            "'name':'Array','type':'function'}," +
            "'prototypeObject':{'ref':5553,'type':'undefined'}," +
            "'className':'Array','properties':[{'name':'length'," +
            "'value':{'ref':55517,'value':2,'type':'number'}}," +
            "{'name':1,'value':" + propertyRefText + "}," +
            "{'name':3,'value':"+ propertyRefText +"}],'type':'object'}"
        ).replace('\'', '"');
    JSONObject valueHandleJson = (JSONObject) JSONValue.parse(valueHandleJsonText);
    ValueHandle valueHandle =
        V8ProtocolParserAccess.get().parse(valueHandleJson, ValueHandle.class);


    InternalContext internalContext = ContextBuilder.getInternalContextForTests(debugContext);
    arrayMirror = internalContext.getValueLoader().addDataToMap(valueHandle);

    FrameObject frameObject;
    {
      JSONObject jsonObject = jsonObject(
          jsonProperty("line", 12L),
          jsonProperty("index", 0L),
          jsonProperty("sourceLineText", ""),
          jsonProperty("script",
              jsonObject(
                  jsonProperty("ref", Long.valueOf(FixtureChromeStub.getScriptId()))
              )
          ),
          jsonProperty("func",
              jsonObject(
                  jsonProperty("name", "foofunction")
              )
          )
      );
      frameObject = V8ProtocolParserAccess.get().parse(jsonObject, FrameObject.class);
    }

    this.callFrame = new CallFrameImpl(frameObject, internalContext);
  }

  @Test
  public void testArrayData() throws Exception {
    JsArrayImpl jsArray = new JsArrayImpl(callFrame.getInternalContext().getValueLoader(),
        "test_array", arrayMirror);
    assertNotNull(jsArray.asArray());
    Collection<JsVariableImpl> properties = jsArray.getProperties();
    assertEquals(2 + 1, properties.size()); // 2 array element properties and one length property.
    assertEquals(4, jsArray.length());
    SortedMap<Integer, ? extends JsVariable> sparseArray = jsArray.toSparseArray();
    assertEquals(2, sparseArray.size());
    JsVariable firstElement = sparseArray.get(1);
    JsVariable thirdElement = sparseArray.get(3);
    assertNull(jsArray.get(-1));
    assertNull(jsArray.get(0));
    assertEquals(firstElement, jsArray.get(1));
    assertEquals("[1]", firstElement.getName());
    assertEquals("test_array[1]", firstElement.getFullyQualifiedName());
    assertNull(jsArray.get(2));
    assertEquals(thirdElement, jsArray.get(3));
    assertEquals("[3]", thirdElement.getName());
    assertEquals("test_array[3]", thirdElement.getFullyQualifiedName());
    assertNull(jsArray.get(10));
    checkElementData(firstElement);
    checkElementData(thirdElement);
  }

  private static void checkElementData(JsVariable arrayElement) {
    assertNotNull(arrayElement);
    JsValue value = arrayElement.getValue();
    assertEquals(JsValue.Type.TYPE_NUMBER, value.getType());
    assertEquals("3", value.getValueString());
  }
}