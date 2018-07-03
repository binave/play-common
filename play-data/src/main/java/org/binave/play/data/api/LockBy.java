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

package org.binave.play.data.api;

/**
 * 排他锁、重锁、悲观锁
 *
 * @see java.util.concurrent.locks.Lock
 *
 * @author bin jin on 2017/4/21.
 * @since 1.8
 */
public interface LockBy {

    /**
     * 锁
     *
     * @param key       锁 key
     * @return 用于解锁的数值
     */
    long lock(String key);

    /**
     * 申请延长锁时间
     */
    boolean delayUnlock(long stamp);

    /**
     * 解锁
     * @param stamp     lock 输出的数值
     */
    void unlock(long stamp);

}
