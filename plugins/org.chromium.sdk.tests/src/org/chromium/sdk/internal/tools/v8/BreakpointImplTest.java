// Copyright (c) 2009 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.sdk.internal.tools.v8;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;

import org.chromium.sdk.Breakpoint;
import org.chromium.sdk.JavascriptVm.BreakpointCallback;
import org.chromium.sdk.RelayOk;
import org.chromium.sdk.SyncCallback;
import org.chromium.sdk.internal.TestUtil;
import org.chromium.sdk.internal.browserfixture.AbstractAttachedTest;
import org.chromium.sdk.internal.transport.FakeConnection;
import org.chromium.sdk.internal.v8native.BreakpointImpl;
import org.chromium.sdk.internal.v8native.BreakpointManager;
import org.chromium.sdk.internal.v8native.DebugSession;
import org.junit.Test;

/**
 * A BreakpointImpl test.
 */
public class BreakpointImplTest extends AbstractAttachedTest<FakeConnection> {

  public boolean isBreakpointChanged;
  public boolean isBreakpointCleared;

  private class TestBreakpointManager extends BreakpointManager {

    public TestBreakpointManager(DebugSession debugSession) {
      super(debugSession);
    }

    @Override
    public RelayOk changeBreakpoint(BreakpointImpl breakpointImpl, BreakpointCallback callback,
        SyncCallback syncCallback) {
      BreakpointImplTest.this.isBreakpointChanged = true;
      return super.changeBreakpoint(breakpointImpl, callback, syncCallback);
    }

    @Override
    public RelayOk clearBreakpoint(BreakpointImpl breakpointImpl, BreakpointCallback callback,
        SyncCallback syncCallback, long originalId) {
      BreakpointImplTest.this.isBreakpointCleared = true;
      return super.clearBreakpoint(breakpointImpl, callback, syncCallback, originalId);
    }

  }

  @Test(timeout = 5000)
  public void testCreateChange() throws Exception {
    final String[] resultMessage = new String[1];
    final Breakpoint[] resultBreakpoint = new Breakpoint[1];
    Breakpoint breakpoint;
    {
      final CountDownLatch latch = new CountDownLatch(1);

      // The "create" part
      browserTab.setBreakpoint(new Breakpoint.Target.ScriptName("1"), 4, 1, true, "false",
          new BreakpointCallback() {
            public void failure(String errorMessage) {
              resultMessage[0] = errorMessage;
              latch.countDown();
            }

            public void success(Breakpoint breakpoint) {
              resultBreakpoint[0] = breakpoint;
              latch.countDown();
            }
          },
          null);
      latch.await();
      assertNull(resultMessage[0], resultMessage[0]);
      assertNotNull(resultBreakpoint[0]);

      breakpoint = resultBreakpoint[0];
      assertEquals("false", breakpoint.getCondition());
      assertTrue(breakpoint.isEnabled());
    }

    // The "change" part
    breakpoint.setCondition("true");
    breakpoint.setEnabled(false);
    resultBreakpoint[0] = null;

    final CountDownLatch latch2 = new CountDownLatch(1);
    breakpoint.flush(new BreakpointCallback() {

      public void failure(String errorMessage) {
        resultMessage[0] = errorMessage;
        latch2.countDown();
      }

      public void success(Breakpoint breakpoint) {
        resultBreakpoint[0] = breakpoint;
        latch2.countDown();
      }

    },
    null);
    latch2.await();
    assertNull(resultMessage[0], resultMessage[0]);
    assertNotNull(resultBreakpoint[0]);
    TestUtil.assertBreakpointsEqual(breakpoint, resultBreakpoint[0]);
  }

  @Test(timeout = 5000)
  public void testClear() throws Exception {
    BreakpointImpl bp = new BreakpointImpl(1, new Breakpoint.Target.ScriptName("abc.js"),
        10, true, null,
        new TestBreakpointManager(browserTab.getDebugSession()));
    final CountDownLatch latch = new CountDownLatch(1);
    final String[] resultMessage = new String[1];
    final Breakpoint[] resultBreakpoint = new Breakpoint[1];

    bp.clear(new BreakpointCallback() {

      public void failure(String errorMessage) {
        resultMessage[0] = errorMessage;
        latch.countDown();
      }

      public void success(Breakpoint breakpoint) {
        resultBreakpoint[0] = breakpoint;
        latch.countDown();
      }

    },
    null);
    latch.await();
    assertNull(resultMessage[0], resultMessage[0]);
    assertTrue(isBreakpointCleared);
  }

  @Test
  public void testNonDirtyChanges() throws Exception {
    String condition = "true";
    int ignoreCount = 3;
    boolean enabled = true;
    BreakpointImpl bp = new BreakpointImpl(1, new Breakpoint.Target.ScriptName("abc.js"), 10,
        enabled, condition, new TestBreakpointManager(browserTab.getDebugSession()));

    bp.setCondition(condition);
    bp.flush(null, null);
    assertFalse(isBreakpointChanged);

    bp.setEnabled(enabled);
    bp.flush(null, null);
    assertFalse(isBreakpointChanged);

    bp.flush(null, null);
    assertFalse(isBreakpointChanged);
  }

  @Override
  protected FakeConnection createConnection() {
    return new FakeConnection(messageResponder);
  }

}
