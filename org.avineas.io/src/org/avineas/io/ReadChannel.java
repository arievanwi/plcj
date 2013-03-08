/*
 * Copyright 2010 aVineas IT Consulting
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
package org.avineas.io;

import java.io.Closeable;

/**
 * Read channel, channel from which information/packets can be read. Basic interface
 * for reading data from any medium via which communication can take place.
 * 
 * @author Arie van Wijngaarden
 */
public interface ReadChannel extends Closeable {
	/**
	 * Read a packet or byte array from the channel, filling up either the data buffer or
	 * the information as present in the packet. 
	 * 
	 * @param data The byte buffer to read into
	 * @param offset The offset in the buffer to start reading
	 * @param timeout The timeout to wait for the first byte of data to become available, in ms. 
	 * If a value <= 0 is passed, no waiting is done at all
	 * @return The number of bytes read. -1 on error, 0 if no data available within
	 * the timeout. The returned number of bytes may be less than would be expected
	 * given the size of the data buffer
	 */
	public int read(byte[] data, int offset, long timeout);
}
