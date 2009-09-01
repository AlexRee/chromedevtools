// Copyright (c) 2009 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.sdk.internal.tools.v8.processor;

import org.chromium.sdk.internal.DebugSession;
import org.chromium.sdk.internal.JsonUtil;
import org.chromium.sdk.internal.tools.v8.V8Protocol;
import org.json.simple.JSONObject;

/**
 * Handles the "continue" V8 command replies.
 */
public class ContinueProcessor extends V8ResponseCallback {

  public ContinueProcessor(DebugSession debugSession) {
    super(debugSession);
  }

  @Override
  public void messageReceived(JSONObject response) {
    if (JsonUtil.getAsBoolean(response, V8Protocol.KEY_RUNNING)) {
      getDebugSession().getDebugEventListener().resumed();
    }
  }

}