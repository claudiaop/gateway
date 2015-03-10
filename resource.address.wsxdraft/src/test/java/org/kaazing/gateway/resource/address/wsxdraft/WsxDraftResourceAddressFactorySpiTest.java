/**
 * Copyright (c) 2007-2014 Kaazing Corporation. All rights reserved.
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.kaazing.gateway.resource.address.wsxdraft;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.kaazing.gateway.resource.address.ResourceAddress.NEXT_PROTOCOL;
import static org.kaazing.gateway.resource.address.ResourceAddress.QUALIFIER;
import static org.kaazing.gateway.resource.address.ResourceAddress.TRANSPORT;
import static org.kaazing.gateway.resource.address.ResourceAddress.TRANSPORT_URI;
import static org.kaazing.gateway.resource.address.ws.WsResourceAddress.CODEC_REQUIRED;
import static org.kaazing.gateway.resource.address.ws.WsResourceAddress.EXTENSIONS;
import static org.kaazing.gateway.resource.address.ws.WsResourceAddress.INACTIVITY_TIMEOUT;
import static org.kaazing.gateway.resource.address.ws.WsResourceAddress.LIGHTWEIGHT;
import static org.kaazing.gateway.resource.address.ws.WsResourceAddress.MAX_MESSAGE_SIZE;
import static org.kaazing.gateway.resource.address.ws.WsResourceAddress.REQUIRED_PROTOCOLS;
import static org.kaazing.gateway.resource.address.ws.WsResourceAddress.SUPPORTED_PROTOCOLS;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.kaazing.gateway.resource.address.ResourceAddress;

public class WsxDraftResourceAddressFactorySpiTest {

    private WsxDraftResourceAddressFactorySpi addressFactorySpi;
    private URI addressURI;
    private Map<String, Object> options;
    
    @Before
    public void before() {
        addressFactorySpi = new WsxDraftResourceAddressFactorySpi();
        addressURI = URI.create("wsx-draft://localhost:2020/");
        options = new HashMap<String, Object>();
        options.put("ws.nextProtocol", "custom");
        options.put("ws.qualifier", "random");
        options.put("ws.codecRequired", FALSE);
        options.put("ws.lightweight", TRUE);
        options.put("ws.extensions", asList("x-kaazing-alpha", "x-kaazing-beta"));
        options.put("ws.maxMessageSize", 1024);
        options.put("ws.inactivityTimeout", SECONDS.toMillis(5));
        options.put("ws.supportedProtocols", new String[] { "amqp/0.91", "amqp/1.0" });
        options.put("ws.requiredProtocols", new String[] { "amqp/0.91", "amqp/1.0" });
        options.put("ws.transport", URI.create("http://localhost:2121/"));
    }

    @Test
    public void shouldHaveWsxDraftSchemeName() throws Exception {
        assertEquals("wsx-draft", addressFactorySpi.getSchemeName());
    }

    @Test (expected = IllegalArgumentException.class)
    public void shouldRequireWsxDraftSchemeName() throws Exception {
        addressFactorySpi.newResourceAddress(URI.create("test://opaque"));
    }

    @Test (expected = IllegalArgumentException.class)
    public void shouldRequireExplicitPath() throws Exception {
        addressFactorySpi.newResourceAddress(URI.create("wsx-draft://localhost:80"));
    }

    @Test 
    public void shouldNotRequireExplicitPort() throws Exception {
        ResourceAddress address = addressFactorySpi.newResourceAddress(URI.create("wsx-draft://localhost/"));
        URI location = address.getResource();
        assertEquals(location.getPort(), 80);
    }

    @Test
    public void shouldCreateAddressWithDefaultOptions() throws Exception {
        ResourceAddress address = addressFactorySpi.newResourceAddress(addressURI);
        assertNull(address.getOption(NEXT_PROTOCOL));
        assertNull(address.getOption(QUALIFIER));
        assertNull(address.getOption(TRANSPORT));
        assertFalse(address.getOption(CODEC_REQUIRED));
        assertFalse(address.getOption(LIGHTWEIGHT));
        assertEmpty(address.getOption(EXTENSIONS));
        assertEquals(0, address.getOption(MAX_MESSAGE_SIZE).intValue());
        assertEquals(0L, address.getOption(INACTIVITY_TIMEOUT).longValue());
        assertEmpty(address.getOption(SUPPORTED_PROTOCOLS));
        assertEmpty(address.getOption(REQUIRED_PROTOCOLS));
    }

    @Test
    public void shouldCreateAddressWithOptions() {
        ResourceAddress address = addressFactorySpi.newResourceAddress(addressURI, options);
        assertEquals("custom", address.getOption(NEXT_PROTOCOL));
        assertEquals("random", address.getOption(QUALIFIER));
        assertNull(address.getOption(TRANSPORT));
        assertFalse(address.getOption(CODEC_REQUIRED));
        assertTrue(address.getOption(LIGHTWEIGHT));
        assertEquals(asList("x-kaazing-alpha", "x-kaazing-beta"), address.getOption(EXTENSIONS));
        assertEquals(1024, address.getOption(MAX_MESSAGE_SIZE).intValue());
        assertEquals(SECONDS.toMillis(5), address.getOption(INACTIVITY_TIMEOUT).longValue());
        assertArrayEquals(new String[] { "amqp/0.91", "amqp/1.0" }, address.getOption(SUPPORTED_PROTOCOLS));
        assertArrayEquals(new String[] { "amqp/0.91", "amqp/1.0" }, address.getOption(REQUIRED_PROTOCOLS));
    }

    @Test
    public void shouldCreateAddressWithDefaultTransport() throws Exception {
        ResourceAddress address = addressFactorySpi.newResourceAddress(addressURI);
        assertNotNull(address.getOption(TRANSPORT_URI));
        assertEquals(URI.create("httpx-draft://localhost:2020/"), address.getOption(TRANSPORT_URI));
    }
    
    @Test
    public void shouldCreateAddressWithTransport() throws Exception {
        ResourceAddress address = addressFactorySpi.newResourceAddress(addressURI, options);
        assertNotNull(address.getOption(TRANSPORT_URI));
        assertEquals(URI.create("http://localhost:2121/"), address.getOption(TRANSPORT_URI));
    }

    private void assertEmpty(String[] objects) {
        if (objects != null) {
            assertEquals(0, objects.length);
        }
    }

    private void assertEmpty(List<String> objects) {
        if (objects != null) {
            assertEquals(0, objects.size());
        }
    }
    
}