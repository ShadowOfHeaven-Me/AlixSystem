package shadow.utils.netty.promise;

import io.netty.channel.Channel;
import io.netty.channel.ChannelProgressivePromise;
import io.netty.channel.DefaultChannelProgressivePromise;

public final class AlixChannelPromise extends DefaultChannelProgressivePromise {

    public AlixChannelPromise(Channel channel) {
        super(channel);
    }

    @Override
    public ChannelProgressivePromise setSuccess(Void result) {
        return this;
    }

    public void markAsDone() {
        //super.setSuccess(null);
        this.setProgress(1, 1);
    }
}