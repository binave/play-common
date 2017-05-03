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
 * 配置实体类接口
 * 用于获取角色使用
 *
 * [module args]
 *
 * @author bin jin on 2017/4/17.
 * @since 1.8
 */
public interface Config {

    /**
     * 获得第一个 key，
     * 一般为 excel 的 key
     */
    int getKey();

    /**
     * 获得第二个 key
     */
    int getExtKey();

    /**
     * 版本号，用于比对一致性
     */
    long getVersion();

    /**
     * 显示的重载此方法
     */
    String toString();

}
