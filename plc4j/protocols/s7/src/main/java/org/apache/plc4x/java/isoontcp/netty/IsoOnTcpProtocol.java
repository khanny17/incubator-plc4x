/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
*/
package org.apache.plc4x.java.isoontcp.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import org.apache.plc4x.java.api.exceptions.PlcProtocolException;
import org.apache.plc4x.java.isoontcp.netty.model.IsoOnTcpMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class IsoOnTcpProtocol extends MessageToMessageCodec<ByteBuf, IsoOnTcpMessage> {

    public static final byte ISO_ON_TCP_MAGIC_NUMBER = 0x03;

    private static final Logger logger = LoggerFactory.getLogger(IsoOnTcpProtocol.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, IsoOnTcpMessage in, List<Object> out) throws Exception {
        logger.debug("ISO on TCP Message sent");
        // At this point of processing all higher levels have already serialized their payload.
        // This data is passed to the lower levels in form of an IoBuffer.
        final ByteBuf userData = in.getUserData();

        int packetSize = userData.readableBytes() + 4;

        ByteBuf buf = Unpooled.buffer();
        // Version (is always constant 0x03)
        buf.writeByte(ISO_ON_TCP_MAGIC_NUMBER);
        // Reserved (is always constant 0x00)
        buf.writeByte((byte) 0x00);
        // Packet length (including ISOonTCP header)
        // ("remaining" returns the number of bytes left to read in this buffer.
        // It is usually set to a read position of 0 and a limit at the end.
        // So in general remaining is equivalent to a non-existing
        // "userData.size()" method.)
        buf.writeShort((short) packetSize);

        // Output the payload.
        buf.writeBytes(userData);

        out.add(buf);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if(logger.isTraceEnabled()) {
            logger.trace("Got Data: {}", ByteBufUtil.hexDump(in));
        }
        // If at least 4 bytes are readable, peek into them (without changing the read position)
        // and get the packet length. Only if the available amount of readable bytes is larger or
        // equal to this, continue processing the rest.
        if(in.readableBytes() >= 4) {
            logger.debug("ISO on TCP Message received");
            // The ISO on TCP protocol is really simple and in this case the buffer length
            // will take care of the higher levels not reading more than is in the packet.
            // So we just gobble up the header and continue reading in higher levels.
            if (in.getByte(0) != ISO_ON_TCP_MAGIC_NUMBER) {
                logger.warn("Expecting ISO on TCP magic number: {}", ISO_ON_TCP_MAGIC_NUMBER);
                if (logger.isDebugEnabled()) {
                    logger.debug("Got Data: {}", ByteBufUtil.hexDump(in));
                }
                exceptionCaught(ctx, new PlcProtocolException(
                    String.format("Expecting ISO on TCP magic number: %02X", ISO_ON_TCP_MAGIC_NUMBER)));
                return;
            }
            // Byte 1 is a reserved byte set to 0x00
            short packetLength = in.getShort(2);
            if(in.readableBytes() >= packetLength) {
                // Skip the 4 bytes we peeked into manually.
                in.skipBytes(4);
                // Simply place the current buffer to the output ... the next handler will continue.
                ByteBuf payload = in.readBytes(packetLength - 4);
                out.add(new IsoOnTcpMessage(payload));
            }
        }
    }

}
