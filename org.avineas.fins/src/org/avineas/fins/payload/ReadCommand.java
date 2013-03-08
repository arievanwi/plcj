/*
 * Copyright, FUJIFILM Manufacturing Europe B.V.
 * Copyright 2010-2011, aVineas IT Consulting
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
package org.avineas.fins.payload;

/**
 * Command that represents a read command, meaning: the request to
 * fetch data starting from a specific address.
 * 
 * @author A. van Wijngaarden
 * @since 24-9-2007
 */
public class ReadCommand extends IOCommand {
    /**
     * The sub response code for read commands.
     */
    public static final int SRC = 1;
    
    /**
     * Construct a read command from a FINS payload.
     * 
     * @param payload The payload of the command
     * @throws Exception In case of errors
     */
    public ReadCommand(byte[] payload) throws Exception {
        super(SRC, payload);
    }
    
    /**
     * Create a read command.
     * 
     * @param areaCode The area code
     * @param startAddress The start address (word, not bit)
     * @param size The size to read, specified in addresses
     */
    public ReadCommand(int areaCode, int startAddress, int size) {
        super(SRC, areaCode, startAddress, size);
    } 
}