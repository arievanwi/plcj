/*
 * Copyright 2012 aVineas IT Consulting. 
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
 * Master interface for COMLI. This is the main entry point for that part
 * of the application that acts as a COMLI master. Users should look up this
 * interface and use it appropriately.
 * 
 * @author A. van Wijngaarden
 */
public interface Master {
    /**
     * Perform a data transfer to a slave node.
     * 
     * @param destination The slave this message is meant for. Since slaves are 
     * numbered, this is the number of the slave (> 0)
     * @param type The transfer type. One of the transfer types 
     * according to the COMLI specification
     * @param startAddress The start address where the IO should be written
     * @param contents The contents of the addresses
     * @param offset The offset in the buffer to start the data transfer
     * @param size The size of the buffer to send
     * @return Indication whether the transfer was successful. True means OK
     */
    public boolean transfer(int destination, byte type, int startAddress, 
            byte[] contents, int offset, int size);
    
    /**
     * Request data from a slave.
     * 
     * @param destination The destination/slave number
     * @param type The type of the message. One of the request types
     * according to the COMLI specification
     * @param startAddress The start address
     * @param size The size to request
     * @return The data that was sent back by the slave as a response to the
     * request. Null if no reply was received
     */
    public byte[] request(int destination, byte type, int startAddress, int size);
}