/*
 * Copyright 2012 aVineas IT Consulting
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
package org.avineas.io.helper;

/**
 * Dumper of packets. Formats a packet to a readable string.
 * 
 * @author Arie van Wijngaarden
 */
public class PacketDumper {
	/**
	 * Dump a packet with a specific prefix into a string buffer.
	 * 
	 * @param prefix The prefix to use
	 * @param data The data to dump
	 * @param size The size to dump
	 * @return The string buffer with the filled data
	 */
	public static StringBuffer dump(String prefix, byte[] data, int size) {
		StringBuffer buffer = new StringBuffer();
		if (size < 0) return buffer;
		buffer.append(prefix).append(" ");
		for (int cnt = 0; cnt < size; cnt++) {
			buffer.append(Integer.toHexString(data[cnt] & 0xff)).append(" ");
		}
		return buffer;
	}
}
