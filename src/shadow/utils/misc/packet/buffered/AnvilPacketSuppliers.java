package shadow.utils.misc.packet.buffered;

import io.netty.buffer.ByteBuf;

import static shadow.utils.misc.packet.buffered.PacketConstructor.AnvilGUI.*;

public final class AnvilPacketSuppliers {

    public static AnvilPacketSupplier newVerifiedSupplier() {
        return new VerifiedSupplier();
    }

    public static AnvilPacketSupplier unverifiedVisibleSupplier() {
        return new UnverifiedVisibleSupplier();
    }

    public static AnvilPacketSupplier newUnverifiedInvisibleSupplier() {
        return new UnverifiedInvisibleSupplier();
    }

    private static final class UnverifiedInvisibleSupplier implements AnvilPacketSupplier {

        private final StringBuilder input = new StringBuilder();
        private ByteBuf allItemsBuffer, invalidIndicateBuffer;

        private UnverifiedInvisibleSupplier() {
            this.allItemsBuffer = allItemsInvis(0);
            this.invalidIndicateBuffer = invalidIndicateInvis(0);
        }

        @Override
        public ByteBuf allItemsBuffer() {
            return this.allItemsBuffer;
        }

        @Override
        public ByteBuf invalidIndicateBuffer() {
            return this.invalidIndicateBuffer;
        }

        @Override
        public boolean onInput(String input) {
            int len = input.length();
            int gatheredLen = this.input.length();

            if (len > gatheredLen) {
                int charsAppended = len - gatheredLen;
                char appended = input.charAt(len - 1);
                if (appended == '*') return false;

                //single char append
                if (charsAppended == 1) {
                    this.input.append(appended);//the client typed one char
                } else {
                    this.input.append(input, gatheredLen, len);//the client most likely pasted something
                }
            } else if (len < gatheredLen) {
                if (gatheredLen != 0) {

                    int charsDeleted = gatheredLen - len;

                    //it was a single char delete
                    if (charsDeleted == 1) this.input.deleteCharAt(gatheredLen - 1);
                    else this.input.delete(len, gatheredLen);//it was a batch delete
                }
            } else
                return false;//len == gatheredLen, the client sends this with a duplicate name of our own "*****" invisible password

            this.allItemsBuffer = allItemsInvis(len);
            this.invalidIndicateBuffer = invalidIndicateInvis(len);
            return true;
        }

        @Override
        public String getInput() {
            return this.input.toString();
        }

        @Override
        public void onWindowUpdate(int id) {//don't care
        }
    }

    private static final class UnverifiedVisibleSupplier implements AnvilPacketSupplier {

        private static final ByteBuf allItemsBuffer = allItems(1);
        private static final ByteBuf invalidIndicateBuffer = invalidIndicate(1);
        private String input = "";

        @Override
        public ByteBuf allItemsBuffer() {
            return allItemsBuffer;
        }

        @Override
        public ByteBuf invalidIndicateBuffer() {
            return invalidIndicateBuffer;
        }

        @Override
        public boolean onInput(String input) {
            this.input = input;
            return true;//always spoof
        }

        @Override
        public String getInput() {
            return this.input;
        }

        @Override
        public void onWindowUpdate(int id) {
            //don't care
        }
    }

    private static final class VerifiedSupplier implements AnvilPacketSupplier {

        private String input = "";
        private ByteBuf allItemsBuffer, invalidIndicateBuffer;

        @Override
        public ByteBuf allItemsBuffer() {
            return this.allItemsBuffer;
        }

        @Override
        public ByteBuf invalidIndicateBuffer() {
            return this.invalidIndicateBuffer;
        }

        @Override
        public void onWindowUpdate(int id) {
            this.allItemsBuffer = allItems(id);
            this.invalidIndicateBuffer = invalidIndicate(id);
        }

        @Override
        public boolean onInput(String input) {
            this.input = input;
            return true;//always spoof
        }

        @Override
        public String getInput() {
            return this.input;
        }
    }
}