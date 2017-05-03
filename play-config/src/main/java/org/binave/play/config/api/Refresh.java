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

package org.binave.play.config.api;

/**
 * 刷新配置
 *
 * @author bin jin on 2017/4/14.
 * @since 1.8
 */
interface Refresh {

    /**
     * 递归深度限制
     */
    int RECURSION_DEPTH_LIMIT = 10;

    /**
     * 用于配置刷新
     * @param confLoader 配置获取接口
     * @param version    配置版本号
     * @param override   是否完全覆盖，如果是需要先清空，之后重新加载
     *
     */
    void reload(ConfLoader confLoader, long version, boolean override, String... tokens);

}
