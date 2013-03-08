/*
 * Copyright 2011 aVineas IT Consulting
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
package org.avineas.io.notify;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.avineas.io.ReadChannel;

/**
 * Wrapper around another channel to add notification to a read channel. This means that
 * this object can notify objects about data that is available on the channel.
 * 
 * @author Arie van Wijngaarden
 */
public class NotifyingReadChannel<C extends ReadChannel> implements ReadChannel, Notifier {
	private int maxSize;
	private C delegate;
	private CompoundNotifier notify = new CompoundNotifier();
	private List<ReadEntry> entries = new ArrayList<ReadEntry>();
	private Thread thread;
	
	/**
	 * Add notification to a read channel. 
	 * 
	 * @param wrapped The channel that is wrapped
	 * @param maxSize The max. size of packets that can appear on the wrapped channel. This size
	 * is used to read packets from the wrapped channel via a separate thread
	 */
	public NotifyingReadChannel(C wrapped, int maxSize) {
		this.maxSize = maxSize;
		this.delegate = wrapped;
		thread = new Thread(new Runnable() {
			@Override
			public void run() {
				_read();
			}
		}, "NotifyingRead-" + wrapped.toString());
		thread.start();	}
	
	void _read() {
		while (!Thread.currentThread().isInterrupted()) {
			try {
				// Read without timeout from the channel below.
				byte[] data = new byte[maxSize];
				int size = delegate.read(data, 0, Long.MAX_VALUE);
				// Create a read entry. Note that this is also done when
				// the read indicates an error (since these must be passed as well)
				ReadEntry entry = new ReadEntry(data, size);
				synchronized (entries) {
					entries.add(entry);
					entries.notifyAll();
				}
				notify.notifyChilds();
			} catch (Exception exc) {
				break;
			}
		}
	}
	
	@Override
	public int read(byte[] data, int offset, long timeout) {
		synchronized (entries) {
			if (entries.size() <= 0 && timeout > 0) {
				try {
					entries.wait(timeout);
				} catch (InterruptedException exc) {
					Thread.currentThread().interrupt();
					return -1;
				}
			}
			if (entries.size() <= 0) return 0;
			ReadEntry entry = entries.get(0);
			entries.remove(0);
			int size = entry.getSize();
			if (size >= 0) {
			    // Note that an array out of bounds exception may occur here
			    // when the buffer passed isn't large enough. But otherwise for
			    // packet oriented channels, the start and end of a packet
			    // would not be guaranteed to be in one buffer.
				System.arraycopy(entry.getData(), 0, data, offset, size);
			}
			return size;
		}
	}

	@Override
	public void close() throws IOException {
		thread.interrupt();
		delegate.close();
		notify.destroy();
	}

	@Override
	public void notify(Listener toNotify) {
		this.notify.add(toNotify);
	}
	
	C getDelegate() {
		return this.delegate;
	}
}

class ReadEntry {
	private byte[] data;
	private int size;
	
	ReadEntry(byte[] data, int size) {
		this.data = data;
		this.size = size;
	}
	
	byte[] getData() {
		return data;
	}
	
	int getSize() {
		return size;
	}
}
