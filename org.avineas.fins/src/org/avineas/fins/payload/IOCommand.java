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
 * Command that represents IO commands, normally write or read commands. An
 * IO command adds to a command addressing and size information + the contents
 * of the addresses contained. Do not use this type directly, but in stead use
 * the subclasses.
 * 
 * @author A. van Wijngaarden
 * @since 20-9-2007, abstracted from WriteCommand
 */
public class IOCommand extends Command {
    /**
     * The main request code for read/write commands.
     */
    public static final int MRC = 1;
    public static final int AREACODEOFFSET = 2;
    public static final int ADDRESSOFFSET = 3;
    public static final int SIZEOFFSET = 6;
    public static final int STARTDATAOFFSET = 8;
    
    protected IOCommand(int src, byte[] payload) throws Exception {
        super(payload);
        if (getMrc() != MRC || getSrc() != src)
            throw new Exception("command is not the expected IO " +
                    "command (" + MRC + "/" + src + ")");
    }
    
    /**
     * Create a write command.
     * 
     * @param src The sub request code
     * @param areaCode The area code
     * @param startAddress The start address (word, not bit)
     * @param numberOfValues The number of values indication in the header
     */
    protected IOCommand(int src, int areaCode, int startAddress, int numberOfValues) { 
        setRequestCode(MRC, src);
        data[AREACODEOFFSET] = (byte) areaCode;
        setStartAddress(startAddress);
        setNumberOfValues(numberOfValues);
        dataSize = STARTDATAOFFSET;
    }
 
    /**
     * Set the word start address for the write command.
     * 
     * @param address The address (word level)
     */
    public void setStartAddress(int address) {
        encode(address, data, ADDRESSOFFSET, 2);
        data[ADDRESSOFFSET + 2] = 0;
    }
    
    /**
     * Set the start bit address.
     * 
     * @param address The address as 3 bytes encoded bit address
     */
    public void setStartBitAddress(int address) {
        encode(address, data, ADDRESSOFFSET, 3);
    }
    
    public int getStartBitAddress() {
        return decode(data, ADDRESSOFFSET, 3);
    }
    
    public int getStartAddress() {
        return decode(data, ADDRESSOFFSET, 2);
    }
    
    public byte getAreaCode() {
        return data[AREACODEOFFSET];
    }
    
    public int getNumberOfValues() {
        int number = decode(data, SIZEOFFSET, 2);
        return number;
    }
    
    protected void setNumberOfValues(int numberOfValues) {
        encode(numberOfValues, data, SIZEOFFSET, 2);
    }
    
    public static int getMaxAddresses(int bytesPerAddress) {
        return (MAXPAYLOADSIZE - STARTDATAOFFSET) / bytesPerAddress;
    }
}