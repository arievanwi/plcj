/*
 * Copyright 2005, the original author or authors.
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
 * Payload that represents a command, meaning the message sent from a party to another.
 * The class is mainly a naming extension of {@link Payload}
 * 
 * @author Arie van Wijngaarden
 * @since 6-11-2005
 */
public class Command extends Payload {
    public Command(byte[] payload) {
        super(payload);
    }
    
    public Command() {
        super();
    }
}