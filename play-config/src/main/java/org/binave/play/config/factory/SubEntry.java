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

package org.binave.play.config.factory;

/**
 * 与配置类型一一对应。
 * 用于配置更新
 *
 * @author bin jin on 2017/4/5.
 * @since 1.8
 */
public class SubEntry {

    private long version;
    private int head;
    private int tail;

    SubEntry(long version, int head, int tail) {
        this.version = version;
        this.head = head;
        this.tail = tail;
    }

    void setVersion(long version) {
        this.version = version;
    }

    int getHead() {
        return head;
    }

    int getTail() {
        return tail;
    }

    public long getVersion() {
        return version;
    }

}