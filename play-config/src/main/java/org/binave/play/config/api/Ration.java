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
 * 给养，补给
 * 用于分配有限资源
 *
 * @author bin jin on 2017/4/26.
 * @since 1.8
 */
public interface Ration {

    /**
     * 补给
     */
    void supply(int size);

    /**
     * 消耗
     * @return  available
     */
    int consume();

    int consume(int size);

    /**
     * 可获得
     */
    int available();

}
