package alix.common.data.fingerprinting.resourcepack;

import io.netty.handler.codec.http.FullHttpResponse;

public record PrependedResponse(FullHttpResponse response, int bit) {
}