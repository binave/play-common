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

package org.binave.play.api.data;

/**
 * id 、pool 分配器
 *
 * [module interface]
 *
 * @author bin jin on 2017/4/13.
 * @since 1.8
 */
public interface Allocator {

    /**
     * 生成全局唯一 id
     */
    long adder();

    /**
     * 生成 pool id
     */
    int createPoolId();

    /**
     * 查看是否有过去的 id
     * 用于倒库
     */
    int lastPoolId(int currentPoolId);

    /**
     * 查看是否有新 pool
     * 用于倒库
     */
    int latestPoolId(int currentPoolId);

}
