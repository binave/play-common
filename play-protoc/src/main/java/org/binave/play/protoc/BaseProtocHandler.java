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

package org.binave.play.protoc;

import com.google.protobuf.GeneratedMessageLite;
import com.google.protobuf.GeneratedMessageLite.Builder;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;
import com.google.protobuf.Parser;
import org.binave.common.util.CharUtil;
import org.binave.common.util.TypeUtil;
import org.binave.play.route.api.BaseHandler;
import org.binave.play.route.args.DataPacket;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Protocol Buffers Handler
 *
 * 对数据进行预处理
 *
 * @author by bin jin on 2017/5/6.
 * @since 1.8
 */
abstract public class BaseProtocHandler<Message extends GeneratedMessageLite> implements BaseHandler {

    /**
     * 业务实现
     */
    abstract public Object call(long id, int pool, Message message);

    @Override
    final public DataPacket call(DataPacket dataPacket) {

        System.out.println("[BaseProtocHandler]{call}: " + dataPacket);

        if (dataPacket == null) throw new IllegalArgumentException(
                this.getClass().getName()
        );

        // 检查传过来的指令码，检测用
        if (dataPacket.getCodeNum() != tab()) throw new IllegalArgumentException(
                "target code num:" + dataPacket.getCodeNum() + "!= source code num:" + tab()
        );

        byte[] data = dataPacket.getData();

        // 进行反序列化
        GeneratedMessageLite message;

        try {
            message = data != null ? (GeneratedMessageLite) getParser().parseFrom(data) : null;
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(this.getClass().getName(), e); // 转换出错
        }

        long id = dataPacket.getId();
        int pool = dataPacket.getPool();

        // 调用实现的逻辑，如果有异常
        Object resp = call(id, pool, (Message) message);

        if (resp instanceof Builder) {
            // 正常返回
            return createDataPacket(id, pool, 0, tab() + 1, (Builder) resp);
        } else if (resp instanceof DataPacket) {
            // 自定义构造
            return (DataPacket) resp;
        } else if (resp == null) {
            // 没有定义
            return new DataPacket(id, pool, 0, 0, null);
        } else
            throw new IllegalArgumentException(CharUtil.replacePlaceholders(
                    "{}", "Type error {} : {} from {}",
                    resp.getClass().getName(), resp.toString(), this.getClass().getName()
            ));
    }

    /**
     * 准备发送数据
     *
     * @param id        session 标识
     * @param codeId    编号
     * @param builder   数据源
     */
    protected final DataPacket createDataPacket(long id, int pool, int status, int codeId, Builder builder) {
        return new DataPacket(id, pool, status, codeId, builder.build().toByteArray());
    }

    // 缓存
    private Parser parser;

    /**
     * 动态生成
     */
    private Parser getParser() {
        Parser p = this.parser;
        if (p == null) {
            Class type = getThisGenericTypes();
            // 找到 MessageLite 类型的泛型
            try {
                GeneratedMessageLite.Builder builder = (Builder) type.getMethod("newBuilder").
                        invoke(null);
                // 获得 Parser
                p = builder.buildPartial().getParserForType();
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
            this.parser = p;
        }
        return this.parser;
    }

    private Class<? extends MessageLite> genericType;

    /**
     * 获得泛型类
     */
    private Class<? extends MessageLite> getThisGenericTypes() {
        Class type = this.genericType;
        if (type == null) {
            for (Type t : TypeUtil.getGenericTypes(this.getClass())) {
                type = (Class<?>) t;
                if (MessageLite.class.isAssignableFrom(type)) {
                    this.genericType = type;
                    break;
                }
            }
            // 没拿到
            if (type == null)
                throw new RuntimeException(
                        "not found generic class extends com.google.protobuf.MessageLite" +
                                this.getClass().getName()
                );
        }
        return this.genericType;
    }

    // 缓存本类的 id
    private int tab;

    /**
     * 区分标记
     * 这里直接获得类泛型名称
     */
    @Override
    public final int tab() {
        Integer i = tab;
        if (i == 0) {
            String name = getThisGenericTypes().getSimpleName();
            // 将类名称中的数字拿出来
            List<Integer> integers = CharUtil.getTextInteger(name);
            if (integers.isEmpty() || (i = integers.get(0)) == null)
                throw new RuntimeException("class name error: " + name);
            tab = i;
        }
        return tab;
    }

}
