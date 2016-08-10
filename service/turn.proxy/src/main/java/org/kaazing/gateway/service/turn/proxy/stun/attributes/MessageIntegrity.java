/**
 * Copyright 2007-2016, Kaazing Corporation. All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kaazing.gateway.service.turn.proxy.stun.attributes;

import static org.kaazing.gateway.service.turn.proxy.stun.attributes.AttributeType.MESSAGE_INTEGRITY;

public class MessageIntegrity extends Attribute {

    protected byte[] value;

    public MessageIntegrity(byte[] value) {
        this.value = value;
    }

    @Override
    public short getType() {
        return MESSAGE_INTEGRITY.getType();
    }

    @Override
    public short getLength() {
        return 20;
    }

    @Override
    public byte[] getVariable() {
        return value;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (byte b : getVariable()) {
            sb.append(String.format("%02X ", b));
        }
        return String.format("%s - %s", super.toString(), sb.toString());
    }
}
