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

import java.util.Map;

/**
 * 获得基于关键词的配置
 *
 * @author bin jin on 2017/4/14.
 * @since 1.8
 */
public interface ShareConfTable extends Refresh {

    <Conf extends Config> Conf get(int id, int extId);

    Map<Integer, ? extends Config> row(int rowKey);
}
