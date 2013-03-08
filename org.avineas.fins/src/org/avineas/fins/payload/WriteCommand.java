/*
 * Copyright, Fuji Film B.V., Tilburg
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
 * Command that represents a memory write command (normally
 * memory area write)
 * 
 * @author A. van Wijngaarden
 * @since 7-nov-2005
 */
public class WriteCommand extends IOCommand {
    /**
     * The sub request code for a write command.
     */
    public static final int SRC = 2;
    
    public WriteCommand(byte[] payload) throws Exception {
        super(SRC, payload);
    }
    
    /**
     * Create a write command.
     * 
     * @param areaCode The area code
     * @param startAddress The start address (word, not bit)
     * @param bytesPerAddress The number of bytes per address
     * @param values The values as integer array
     */
    public WriteCommand(int areaCode, int startAddress, 
            int bytesPerAddress, int[] values, int size) {
        super(SRC, areaCode, startAddress, size);
        int offset = STARTDATAOFFSET;
        for (int cnt = 0; cnt < size; cnt++) {
            encode(values[cnt], data, offset, bytesPerAddress);
            offset += bytesPerAddress;
        }
        dataSize = offset;
    }
 
    public WriteCommand(int areaCode, int startAddress, int bytesPerAddress, 
            int values[]) {
        this(areaCode, startAddress, bytesPerAddress, values, values.length);
    }
    
    public int getBytesPerAddress() {
        int number = getNumberOfValues();
        int bytesPerAddress = (dataSize - STARTDATAOFFSET) / number;
        return bytesPerAddress;
    }
    
    /**
     * Get the values as coded in this command.
     */
    public int[] getValues() {
        int number = getNumberOfValues();
        int bytesPerAddress = getBytesPerAddress();
        int[] values = new int[number];
        for (int cnt = 0; cnt < number; cnt++) {
            values[cnt] = decode(data, STARTDATAOFFSET + cnt * bytesPerAddress,
                    bytesPerAddress);
        }
        return values;
    }
    
    /**
     * Set the bytes for the contents of the write command as well as the
     * size this is in units. It is assumed that the passed contents
     * are the memory area values as normally returned by getValues().
     * 
     * @param contents The contents, as byte array
     * @param bytesPerAddress The number of bytes per address
     */
    public void setContents(byte[] contents, int bytesPerAddress) {
        this.dataSize = contents.length + STARTDATAOFFSET;
        System.arraycopy(contents, 0, data, STARTDATAOFFSET, contents.length);
        setNumberOfValues(contents.length / 2);
    }
}