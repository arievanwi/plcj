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
 * Response from a read command. Contains the data as read.
 * 
 * @author A. van Wijngaarden
 * @since 24-sep-2007
 */
public class ReadResponse extends Response {
    /**
     * Constructor from a FINS payload.
     * 
     * @param payload The payload
     */
    public ReadResponse(byte[] payload) {
        super(payload);
    }

    /**
     * Constructor from a command and a specific response code/contents.
     * 
     * @param command The command this one is a response to
     * @param contents The contents of the read, meaning the data as read
     */
    public ReadResponse(Command command, byte[] contents) {
        super(command, Response.RESPONSEOK, contents);
    }
 
    /**
     * Create a read response from a set of values. 
     * 
     * @param command The original command
     * @param values The values as integers, are coded in FINS layout
     */
    public ReadResponse(Command command, int[] values) {
        super(command, Response.RESPONSEOK);
        byte[] contents = new byte[values.length * 2];
        for (int cnt = 0; cnt < values.length; cnt++) {
            encode(values[cnt], contents, cnt * 2, 2);
        }
        setContents(contents);
    }
    
    /**
     * Get the values from the response.
     * 
     * @return The values as integer array
     */
    public int[] getValues() {
        byte[] contents = getContents();
        int[] values = new int[contents.length / 2];
        for (int cnt = 0; cnt < values.length; cnt++) {
            values[cnt] = decode(contents, 2 * cnt, 2);
        }
        return values;
    }
}