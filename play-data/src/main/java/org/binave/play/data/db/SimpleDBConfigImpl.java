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

package org.binave.play.data.db;

import org.binave.play.data.args.DBConfig;

/**
 * 数据库连接条件
 *
 * 2017/4/14.
 *
 * @author bin jin
 * @since 1.8
 */
public class SimpleDBConfigImpl implements DBConfig {

    private String driverClassName;

    private String password;

    private String url;

    private String username;

    private long maxConnLifetimeMillis;

    @Override
    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public long getMaxConnLifetimeMillis() {
        return maxConnLifetimeMillis;
    }

    public void setMaxConnLifetimeMillis(long maxConnLifetimeMillis) {
        this.maxConnLifetimeMillis = maxConnLifetimeMillis;
    }

    /**
     * todo 获得可以的 classloader
     */
    @Override
    public ClassLoader getDriverClassLoader() {
        return null;
    }

    @Override
    public String toString() {
        return "SimpleDBConfigImpl{" +
                "driverClassName='" + driverClassName + '\'' +
                ", password='" + password + '\'' +
                ", url='" + url + '\'' +
                ", username='" + username + '\'' +
                ", maxConnLifetimeMillis=" + maxConnLifetimeMillis +
                '}';
    }
}
