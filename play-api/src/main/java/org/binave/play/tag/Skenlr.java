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

package org.binave.play.tag;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

/**
 * 不参与运行
 * 仅用于更新 maven Module-Tag 内容
 * 最终用于生成 MANIFEST.MF Module-Tag 内容
 *
 * @author by bin jin on 2017/4/7.
 * @since 1.8
 */
@Documented
@Target({TYPE, FIELD, METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface Skenlr {

//    Tab module();

//    enum Tab {
//        inject, implement, bootstrap
//    }skenlr

    /**
     * 实现接口
     */
    @Documented
    @Target(TYPE)
    @Retention(RetentionPolicy.SOURCE)
    @interface implement {
    }

    /**
     * 需要注入的属性
     */
    @Documented
    @Target(FIELD)
    @Retention(RetentionPolicy.SOURCE)
    @interface inject {
    }

    /**
     * 启动方法
     */
    @Documented
    @Target(METHOD)
    @Retention(RetentionPolicy.SOURCE)
    @interface bootstrap {
    }

}
