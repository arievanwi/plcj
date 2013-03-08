/*
 * Copyright 2012, aVineas IT Consulting
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.PreDestroy;

import org.avineas.io.notify.Notifier.Listener;

/**
 * Compound notifier. Is able to notify a group of other objects. This notification is
 * done on a separate thread and as such is very handy to use for channels that implement
 * notification via the {@link org.avineas.io.notify.Notifier} interface.
 * 
 * @author Arie van Wijngaarden
 */
public class CompoundNotifier {
	private Set<Listener> childs = new HashSet<Listener>();
	private Thread thread;
	private int counter = 0;
	
	/**
	 * Construct a compound notifier that is able to let listeners know that something
	 * happened to a channel.
	 */
	public CompoundNotifier() {
		thread = new Thread(new Runnable() {
			@Override
			public void run() {
				notifier();
			}
		});
		thread.start();
	}

	void notifier() {
		// While still running.
		while (!Thread.interrupted()) {
			ArrayList<Listener> toNotify = new ArrayList<Listener>();
			synchronized (childs) {
				// Wait for the counter to be incremented
				while (this.counter == 0) {
					try {
						childs.wait();
						toNotify.addAll(childs);
					} catch (InterruptedException exc) {
						Thread.currentThread().interrupt();
						break;
					}
				}
			}
			// Notify our childs.
			for (Listener child : toNotify) {
				try {
					child.checkChannel();
				} catch (Exception exc) {}
			}
			// Decrement the counter.
			synchronized (childs) {
				this.counter--;
			}
		}
	}
	
	/**
	 * Add a listener to notify.
	 * 
	 * @param toNotify The listener to notify
	 */
	public void add(Listener toNotify) {
		synchronized (childs) {
			childs.add(toNotify);
		}
	}
	
	/**
	 * Remove an existing listener.
	 * 
	 * @param toNotify The listener to remove
	 */
	public void remove(Listener toNotify) {
		synchronized (childs) {
			childs.remove(toNotify);
		}
	}

	/**
	 * Method that can be used by external holders of this type of
	 * notifier to notify asynchronously the listeners to this
	 * notifier.
	 */
	public void notifyChilds() {
		synchronized (childs) {
			this.counter++;
			childs.notifyAll();
		}
	}

	/**
	 * Method that must be called when this notifier is destroyed since it terminates
	 * the thread that performs the notification actions.
	 */
	@PreDestroy
	public void destroy() {
		synchronized (childs) {
			childs.clear();
		}
		thread.interrupt();
		try {
			thread.join();
		} catch (Exception exc) {}
	}
}