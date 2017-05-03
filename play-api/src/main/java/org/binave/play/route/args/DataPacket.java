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

package org.binave.play.route.args;

import lombok.*;
import org.binave.play.Gray;

/**
 * 数据包
 *
 * @author by bin jin on 2017/4/27.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DataPacket implements Gray {

    private long id;        // 用户 id
    private int pool;       // 池
    private int status;     // 状态
    private int codeNum;    // 指令
    private byte[] data;

    @Override
    public int hashCode() {
        return (int) id;
    }

    @Override
    public String toString() {
        return "DataPacket{" +
                "id=" + id +
                ", pool=" + pool +
                ", status=" + status +
                ", codeNum=" + codeNum +
                ", data.length=" + (isEmpty() ? 0 : data.length) +
                '}';
    }

    public boolean isEmpty() {
        return data == null || data.length == 0;
    }

    @Override
    public long grayCode() {
        return id;
    }
}
