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

import org.binave.play.config.args.Config;

import java.util.Collection;

/**
 * 获得基于关键词的 ap 的值为集合
 *
 * @author bin jin on 2017/4/14.
 * @since 1.8
 */
public interface ShareConfMulti extends Refresh {

    <Conf extends Config> Collection<Conf> get(int id);
}
