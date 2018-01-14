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
package org.apache.plc4x.scala.api.messages

import org.apache.plc4x.java.api.model.Address
import items.{ReadRequestItem, ReadResponseItem, RequestItem, ResponseItem}
import scala.collection.immutable.List

sealed trait PlcMessage

sealed trait PlcRequest[REQUEST_ITEM <: RequestItem[_]] extends PlcMessage

final case class PlcReadRequest(readRequestItems: List[ReadRequestItem[_]])
    extends PlcRequest[ReadRequestItem[_]]{

    def addItem(item: ReadRequestItem[_]) = PlcReadRequest(item::readRequestItems)
    def getNumItems = readRequestItems.size
}
object PlcReadRequest{
    def apply(): PlcReadRequest =
        PlcReadRequest(List[ReadRequestItem[_]]())
    def apply(readRequestItems: List[ReadRequestItem[_]]): PlcReadRequest =
        PlcReadRequest(readRequestItems)
    def apply(datatype: Class[_], address: Address, size: Int = 1): PlcReadRequest =
        PlcReadRequest(List(ReadRequestItem(datatype, address, size)))
}

sealed trait PlcResponse[REQUEST <: PlcRequest[_], RESPONSE_ITEM <: ResponseItem[_], REQUEST_ITEM <: RequestItem[_]]
    extends PlcMessage

final case class PlcReadResponse(readRequest: PlcReadRequest, responseItems: List[_ <: ReadResponseItem[_]])
    extends PlcResponse[PlcReadRequest, ReadResponseItem[_], ReadRequestItem[_]]


