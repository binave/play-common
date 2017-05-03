/*
 * Copyright (c) 2017 bin jin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.binave.play.route;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelOutboundInvoker;
import io.netty.channel.group.ChannelGroup;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.AttributeMap;

/**
 * netty 相关工具的封装
 *
 * @author bin jin on 2017/4/27.
 * @since 1.8
 */
public class NettyUtil {

    /**
     * 获得 session 内容
     */
    public static <T> T get(AttributeMap map, String key) {
        Attribute<T> attribute = map.attr(AttributeKey.valueOf(key));
        return attribute.get();
    }

    /**
     * 设置 session 内容
     */
    public static <T> void set(AttributeMap map, String key, T value) {
        Attribute<T> attribute = map.attr(AttributeKey.valueOf(key));
        attribute.set(value);
    }

    /**
     * 设置 session 内容并拿到旧的值
     */
    public static <T> T getAndSet(AttributeMap map, String key, T value) {
        Attribute<T> attribute = map.attr(AttributeKey.valueOf(key));
        return attribute.getAndSet(value);
    }

    /**
     * 在 session 中不含有目标 key 时设置
     */
    public static <T> T setIfAbsent(AttributeMap map, String key, T value) {
        Attribute<T> attribute = map.attr(AttributeKey.valueOf(key));
        return attribute.setIfAbsent(value);
    }

    /**
     * 下发数据
     *
     * @param invokers  个人
     */
    public static void flush(int codeId, byte[] data, ChannelOutboundInvoker... invokers) {
        ByteBuf newBuf = getByteBuf(codeId, data);
        for (ChannelOutboundInvoker invoker : invokers)
            invoker.writeAndFlush(newBuf); // 下发数据
    }

    /**
     * 群发数据
     *
     * @param groups    组
     */
    public static void flushs(int codeId, byte[] data, ChannelGroup... groups) {
        ByteBuf newBuf = getByteBuf(codeId, data);
        for (ChannelGroup invoker : groups)
            invoker.writeAndFlush(newBuf); // 下发数据
    }

    private static ByteBuf getByteBuf(int codeId, byte[] data) {
        int len = data == null ? 0 : data.length;
        ByteBuf newBuf = Unpooled.buffer(Integer.BYTES * 2 + len);
        newBuf.writeInt(len); // 数据长度
        newBuf.writeInt(codeId); // 业务逻辑编号
        if (len > 0) newBuf.writeBytes(data); // 数据体
        return newBuf;
    }

}
