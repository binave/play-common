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

import org.binave.play.config.args.UpdateNotice;

/**
 * 更新通知
 * 实现：订阅者
 * 通过 tab 识别身份
 *
 * 注意：
 *      保证多个配置的一致性没有意义。
 *      一致性需要关联配置通过版本号一致来维护
 *
 * [module interface]
 *
 * @author bin jin on 2017/2/24.
 * @since 1.8
 */
public interface UpdateNotify {

    /**
     * 由调用者通知实现者
     */
    void notify(UpdateNotice notice);

    /**
     * 获得实现者的页签
     */
    String[] tab();

}
