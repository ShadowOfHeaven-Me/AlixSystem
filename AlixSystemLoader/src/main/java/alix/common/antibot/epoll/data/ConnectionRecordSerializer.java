package alix.common.antibot.epoll.data;

import alix.common.antibot.epoll.TelemetryProfilerImpl.ConnectionRecord;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;


final class ConnectionRecordSerializer implements JsonSerializer<ConnectionRecord> {

    @Override
    public JsonElement serialize(ConnectionRecord src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();

        obj.addProperty("addr", src.addr);
        obj.addProperty("nextState", src.nextState);
        obj.addProperty("l7DeltaT", src.getL7DeltaT());

        //obj.add("evaluation", context.serialize(src.evaluation));
        obj.add("synSignature", context.serialize(src.synSignature));
        obj.add("stats", context.serialize(src.stats));
        obj.add("tcpSamples", context.serialize(src.tcpSamples));

        return obj;
    }
}