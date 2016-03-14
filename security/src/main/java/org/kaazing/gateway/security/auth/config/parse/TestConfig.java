/**
 * Copyright 2007-2015, Kaazing Corporation. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kaazing.gateway.security.auth.config.parse;

import org.kaazing.gateway.security.auth.config.UserConfig;

import java.util.Collection;
import java.util.HashSet;

/**
 * Created by VStratan on 09-Mar-16.
 */
public class TestConfig implements UserConfig {

    private String name = "TestName";
    private String password = "TestPassword";
    private Collection<String> roleNames;

    public TestConfig() {
        roleNames = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public Collection<String> getRoleNames() {
        return roleNames;
    }

    public String getPassword() {
        return password;
    }

}
