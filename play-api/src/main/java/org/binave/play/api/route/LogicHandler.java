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

package org.binave.play.api.route;

import org.binave.play.api.data.Dao;

/**
 * 业务处理器
 * 用于关联数据与通信
 *
 * [module interface]
 *
 * @author bin jin on 2017/4/27.
 * @since 1.8
 */
public interface LogicHandler<D extends Dao, Pojo extends Pact> {

    /**
     * 执行业务逻辑
     */
    Pact call(D d, Pojo pojo);

}
