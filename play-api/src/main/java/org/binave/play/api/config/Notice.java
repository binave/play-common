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

/**
 * 订阅获取通知
 * 实现：订阅者
 * 通过 tab 识别身份
 *
 * [module interface]
 *
 * @author bin jin on 2017/2/24.
 * @since 1.8
 */
public interface Notice {

    /**
     * 由调用者通知实现者
     * @param args      参数
     */
    void notify(Object... args);

    /**
     * 获得实现者的页签
     */
    int tab();

}
