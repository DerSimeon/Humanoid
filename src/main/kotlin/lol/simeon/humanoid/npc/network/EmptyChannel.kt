/*
 * MIT License
 *
 * Copyright (c) 2025 Simeon L.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package lol.simeon.humanoid.npc.network

import io.netty.channel.*
import java.net.SocketAddress

public class EmptyChannel(private val parent: Channel?) : AbstractChannel(parent) {

    private val config = DefaultChannelConfig(this)

    override fun config(): ChannelConfig {
        config.setAutoRead(true)
        return config
    }

    override fun isOpen(): Boolean {
        return false
    }

    override fun isActive(): Boolean {
        return false
    }

    override fun metadata(): ChannelMetadata {
        return ChannelMetadata(true)
    }

    override fun newUnsafe(): AbstractUnsafe? {
        return null
    }

    override fun isCompatible(p0: EventLoop?): Boolean {
        return false
    }

    override fun localAddress0(): SocketAddress? {
        return null
    }

    override fun remoteAddress0(): SocketAddress? {
        return null
    }

    override fun doBind(p0: SocketAddress?) {
    }

    override fun doDisconnect() {
    }

    override fun doClose() {
    }

    override fun doBeginRead() {
    }

    override fun doWrite(p0: ChannelOutboundBuffer?) {
    }
}