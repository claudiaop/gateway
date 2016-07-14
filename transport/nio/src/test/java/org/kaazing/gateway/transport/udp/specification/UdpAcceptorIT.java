/**
 * Copyright 2007-2016, Kaazing Corporation. All rights reserved.
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
package org.kaazing.gateway.transport.udp.specification;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.kaazing.test.util.ITUtil.createRuleChain;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.kaazing.gateway.transport.IoHandlerAdapter;
import org.kaazing.k3po.junit.annotation.Specification;
import org.kaazing.k3po.junit.rules.K3poRule;
import org.kaazing.mina.core.buffer.IoBufferAllocatorEx;
import org.kaazing.mina.core.buffer.IoBufferEx;
import org.kaazing.mina.core.session.IoSessionEx;
import org.kaazing.test.util.ResolutionTestUtils;

/**
 * RFC-768
 */
@RunWith(Parameterized.class)
public class UdpAcceptorIT {

    private final K3poRule k3po = new K3poRule().setScriptRoot("org/kaazing/specification/udp/rfc768");
    
    private UdpAcceptorRule acceptor = new UdpAcceptorRule();

    private static String networkInterface = ResolutionTestUtils.getLoopbackInterface();

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {     
                {"udp://127.0.0.1:8080"}//, {"udp://[@" + networkInterface + "]:8080"}
           });
    }

    @Parameter
    public String uri;

    @Rule
    public TestRule chain = createRuleChain(acceptor, k3po);

    private void bindTo8080(IoHandlerAdapter<IoSessionEx> handler) throws InterruptedException {
        acceptor.bind(uri, handler);
        k3po.start();
        k3po.notifyBarrier("BOUND");
    }

    private void bindTo8081(IoHandlerAdapter<IoSessionEx> handler) throws InterruptedException {
        acceptor.bind("udp://127.0.0.1:8081", handler);
        k3po.start();
        k3po.notifyBarrier("BOUND1");
    }

    private WriteFuture writeStringMessageToSession(String message, IoSession session) {
        ByteBuffer data = ByteBuffer.wrap(message.getBytes());
        IoBufferAllocatorEx<?> allocator = ((IoSessionEx) session).getBufferAllocator();
        return session.write(allocator.wrap(data));
    }

    @Test
    @Specification("establish.connection/client")
    public void establishConnection() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        bindTo8080(new IoHandlerAdapter<IoSessionEx>() {
            @Override
            protected void doSessionOpened(IoSessionEx session) throws Exception {
                latch.countDown();
            }
        });

        k3po.finish();

        latch.await(2, SECONDS);
    }

    @Test
    @Specification("server.sent.data/client")
    public void serverSentData() throws Exception {
        WriteFuture[] futures = new WriteFuture[1];
        bindTo8080(new IoHandlerAdapter<IoSessionEx>() {

            @Override
            protected void doMessageReceived(IoSessionEx session, Object message) throws Exception {
                WriteFuture future = writeStringMessageToSession("server data", session);
                futures[0] = future;
            }
        });

        k3po.finish();
        futures[0].await(2, SECONDS);
        assertTrue(futures[0].isWritten());
    }

    @Test
    @Specification("client.sent.data/client")
    public void clientSentData() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        bindTo8080(new IoHandlerAdapter<IoSessionEx>() {
            @Override
            protected void doMessageReceived(IoSessionEx session, Object message) throws Exception {
                String decoded = new String(((IoBuffer) message).array());
                assertEquals("client data", decoded);
                latch.countDown();
            }
        });
        k3po.finish();

        latch.await(2, SECONDS);
    }

    @Test
    @Specification("echo.data/client")
    public void bidirectionalData() throws Exception {
        bindTo8080(new IoHandlerAdapter<IoSessionEx>(){
            private boolean first = true;

            @Override
            protected void doMessageReceived(IoSessionEx session, Object message) throws Exception {
                String decoded = new String(((IoBuffer) message).array());
                if (first) {
                    assertEquals("client data 1", decoded);
                    writeStringMessageToSession("server data 1", session);
                    first = false;
                } else {
                    assertEquals("client data 2", decoded);
                    writeStringMessageToSession("server data 2", session);
                }
            }
        });


        k3po.finish();
    }

    @Test
    @Specification("server.close/client")
    public void serverClose() throws Exception {
        // TODO Configure timeout as accept option
        CountDownLatch latch = new CountDownLatch(3);
        bindTo8080(new IoHandlerAdapter<IoSessionEx>() {
            @Override
            protected void doSessionOpened(IoSessionEx session) throws Exception {
                latch.countDown();
            }

            @Override
            protected void doMessageReceived(IoSessionEx session, Object message) throws Exception {
                latch.countDown();
            }

            @Override
            protected void doSessionClosed(IoSessionEx session) throws Exception {
                latch.countDown();
            }
        });

        k3po.finish();

        latch.await(2, SECONDS);
    }

    @Test
    @Specification("concurrent.connections/client")
    public void concurrentConnections() throws Exception {
        class ConcurrentHandler extends IoHandlerAdapter<IoSessionEx> {
            @Override
            protected void doMessageReceived(IoSessionEx session, Object message) throws Exception {
                session.write(message);
            }
        };

        bindTo8080(new ConcurrentHandler());
        k3po.finish();
    }

    @Test
    @Specification("idle.concurrent.connections/client")
    public void idleConcurrentConnections() throws Exception {
        class ConcurrentHandler extends IoHandlerAdapter<IoSessionEx> {
            @Override
            protected void doMessageReceived(IoSessionEx session, Object message) throws Exception {
                session.write(message);
            }
        };

        // TODO configure timeout as accept option
        bindTo8080(new ConcurrentHandler());
        bindTo8081(new ConcurrentHandler());
        k3po.finish();
    }

}
