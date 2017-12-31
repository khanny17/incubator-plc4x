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
package org.apache.plc4x.java.isotp.netty.model.tpdus;

import io.netty.buffer.ByteBuf;
import org.apache.plc4x.java.isotp.netty.model.params.Parameter;
import org.apache.plc4x.java.isotp.netty.model.types.TpduCode;
import org.apache.plc4x.java.netty.Message;

import java.util.List;

public abstract class Tpdu extends Message {

    private final TpduCode tpduCode;
    private final List<Parameter> parameters;

    public Tpdu(TpduCode tpduCode, List<Parameter> parameters, ByteBuf userData) {
        super(userData);
        this.tpduCode = tpduCode;
        this.parameters = parameters;
    }

    public TpduCode getTpduCode() {
        return tpduCode;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public <T extends Parameter> T getParameter(Class<? extends T> parameterClass) {
        if (parameters != null) {
            for (Parameter parameter : parameters) {
                if (parameter.getClass() == parameterClass) {
                    return (T) parameter;
                }
            }
        }
        return null;
    }

}
