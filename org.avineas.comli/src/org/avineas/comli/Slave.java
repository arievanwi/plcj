/*
 * Copyright, FUJIFILM Manufacturing Europe B.V.
 * Copyright 2010-2012 aVineas IT Consulting
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.avineas.comli;

/**
 * Slave side of the COMLI protocol. To be able to interact with a COMLI master, slaves
 * must implement this interface. As specified by the COMLI protocol, two types of interactions
 * between master and slave are possible: a request (to request data, etc. from the slave) or
 * a transfer (to push data to the slave). These two interactions are specified by different
 * interface methods.
 * 
 * @author A. van Wijngaarden
 * @since 26-sep-2007
 */
public interface Slave {
    /**
     * Handle a request from a master. A master sends a request to retrieve data, etc.
     * from a slave. The slave should perform the action and return the data.
     * 
     * @param type The request type
     * @param startAddress The start address
     * @param toFill The byte array to fill with data. The size is determined by
     * the size of the array
     * @return The transfer type to return to the master. Specify 0 to indicate
     * an error. In that case no data is replied
     */
    public byte handleRequest(byte type, int startAddress, byte[] toFill);
    
    /**
     * Handle a transfer from a master. A master performs a transfer to change data
     * at a slave side.
     * 
     * @param type The type of tranfer
     * @param startAddress The start address
     * @param contents The contents
     * @return An indication whether the transfer went correctly
     */
    public boolean handleTransfer(byte type, int startAddress, byte[] contents);
}