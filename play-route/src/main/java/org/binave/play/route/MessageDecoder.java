package org.binave.play.route;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * 处理积累缓冲，不处理、或处理不完将会积攒到下次
 *
 * see http://netty.io/4.1/xref/io/netty/example/factorial/BigIntegerDecoder.html
 *
 * @author by bin jin on 2017/4/26.
 */
public class MessageDecoder extends ByteToMessageDecoder {

    private int size;

    // 数据头长度
    public MessageDecoder(int size) {
        this.size = size;
    }

    /**
     * 每当有数据时，会被调用。
     * 可以决定是否向下发送，或发送多少
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        // 小于数据头长度 + 制令号长度
        if (in.readableBytes() < size) return;

        in.markReaderIndex(); // 记录当前读取进度坐标

        int len = in.readInt();// 本次数据包长度

        if (len < 0) { // 长度异常，关闭连接，正常需要抛出异常来提示问题
            ctx.close();
            return;
        }

        // 后续可读数据长度不够数据包长度，留到下次重新处理
        if (in.readableBytes() < len + Integer.BYTES) {
            in.resetReaderIndex(); // 重置读取进度坐标
            return;
        }

        byte[] body = new byte[len + Integer.BYTES];

        // 读出本次指令数据 【指令号int】【指令数据 byte[dataLength]】
        in.readBytes(body);

        ByteBuf byteBuf = Unpooled.copiedBuffer(body);

        // 发给 MessageHandler 解析指令，执行游戏逻辑
        out.add(byteBuf);
    }

}
