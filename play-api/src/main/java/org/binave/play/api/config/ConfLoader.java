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

package org.binave.play.api.config;

import java.util.List;

/**
 * 获得最新的配置
 * 实现：配置服务
 *
 * [module interface]
 *
 * @author bin jin on 2017/2/24.
 * @since 1.8
 */
public interface ConfLoader {

    /**
     * 获得配置列表
     * 此处获得的配置，其版本号必须已经更新
     */
    List<? extends Configure> load(String token);

    /**
     * 使用键值对方式获取数据
     */
    List<String> get(String token);


}
