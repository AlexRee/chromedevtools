// Generated source.
// Generator: org.chromium.sdk.internal.wip.tools.protocolgenerator.Generator
// Origin: http://svn.webkit.org/repository/webkit/trunk/Source/WebCore/inspector/Inspector.json@98328

package org.chromium.sdk.internal.wip.protocol.input.inspector;

@org.chromium.sdk.internal.protocolparser.JsonType
public interface DidCreateWorkerEventData {
  long id();

  String url();

  boolean isShared();

  public static final org.chromium.sdk.internal.wip.protocol.input.WipEventType<org.chromium.sdk.internal.wip.protocol.input.inspector.DidCreateWorkerEventData> TYPE
      = new org.chromium.sdk.internal.wip.protocol.input.WipEventType<org.chromium.sdk.internal.wip.protocol.input.inspector.DidCreateWorkerEventData>("Inspector.didCreateWorker", org.chromium.sdk.internal.wip.protocol.input.inspector.DidCreateWorkerEventData.class) {
    @Override public org.chromium.sdk.internal.wip.protocol.input.inspector.DidCreateWorkerEventData parse(org.chromium.sdk.internal.wip.protocol.input.WipGeneratedParserRoot parser, org.json.simple.JSONObject obj) throws org.chromium.sdk.internal.protocolparser.JsonProtocolParseException {
      return parser.parseInspectorDidCreateWorkerEventData(obj);
    }
  };
}
