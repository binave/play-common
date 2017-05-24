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

package org.binave.play.config.factory;

import org.binave.play.config.util.ConfTable;
import org.binave.play.config.util.SpaceConfMap;
import org.binave.play.config.util.ConfMap;
import org.binave.play.config.util.ConfMulti;

/**
 * 获得配置管理类
 * 使用工厂模式隔离实现，减少依赖
 *
 * 注意：
 *      任何一个单独模块，在使用此工具时，都需要实现一个更新订阅  。
 *
 * @author bin jin on 2017/4/14.
 * @since 1.8
 */
public class ConfPoolFactory {

    /**
     * 区域 map
     */
    public static SpaceConfMap createSpaceConfMap() {
        return new SpaceConfMapPoolImpl();
    }

    public static ConfMap createConfMap(String token) {
        return new ConfMapPoolImpl(token);
    }

    // Table
    public static ConfTable createConfTable(String token) {
        return new ConfTablePoolImpl(token);
    }

    // MultiMap
    public static ConfMulti createConfMulti(String token) {
        return new ConfMultiPoolImpl(token);
    }

}
