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

package org.binave.play.config;

import org.binave.play.config.api.Ration;

import java.util.concurrent.Semaphore;

/**
 * 补给实现
 * todo 没实现超时
 *
 * @author bin jin on 2017/4/26.
 * @since 1.8
 */
class SemaphoreRationImpl implements Ration {

    private Semaphore semaphore;

    SemaphoreRationImpl(int max) {
        semaphore = new Semaphore(max, true);
        semaphore.drainPermits(); // 清空
    }

    /**
     * 补充
     */
    @Override
    public void supply(int size) {
        semaphore.release(size);
    }

    /**
     * 消耗
     */
    @Override
    public int consume() {
        return consume(1);
    }

    @Override
    public int consume(int size) {
        try {
            semaphore.acquire(size); // 尝试获得
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return available();  // 成功则返回
    }

    /**
     * 剩余
     * 由监听进程进行补给
     */
    @Override
    public int available() {
        return semaphore.availablePermits();
    }
}
