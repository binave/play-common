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

package org.binave.play.config.args;

/**
 * 所有的配置实体都要实现此接口
 * 用于设置者使用，
 * 获取者不应当使用这个接口
 *
 * [module args]
 *
 * @author bin jin on 2017/3/24.
 * @since 1.8
 */
public interface Configure extends Config {

    /**
     * 将 join，和其他配置初始化处理放入其中
     */
    void init();

    /**
     * 由框架进行配置
     */
    void setVersion(long version);

}
