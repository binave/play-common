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

package org.binave.play.config;

import org.binave.play.api.SpaceConfMap;
import org.binave.play.api.ShareConfMap;
import org.binave.play.api.ShareConfMulti;
import org.binave.play.api.ShareConfTable;

/**
 * 获得配置管理类
 * 使用工厂模式隔离实现，减少依赖
 *
 * @author bin jin on 2017/4/14.
 * @since 1.8
 */
public class ConfPoolFactory {

    // Map
    public static SpaceConfMap createSpaceConfMap() {
        return new SpaceConfMapPoolImpl();
    }

    public static ShareConfMap createShareConfMap(String token) {
        return new ShareConfMapPoolImpl(token);
    }

    // Table
    public static ShareConfTable createShareConfTable(String token) {
        return new ShareConfTablePoolImpl(token);
    }

    // MultiMap
    public static ShareConfMulti createShareConfMulti(String token) {
        return new ShareConfMultiPoolImpl(token);
    }

}
