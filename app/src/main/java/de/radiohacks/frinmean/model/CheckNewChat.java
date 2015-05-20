/*
 * Copyright © 2015, Thomas Schreiner, thomas1.schreiner@googlemail.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 */

package de.radiohacks.frinmean.model;


import org.simpleframework.xml.Default;
import org.simpleframework.xml.DefaultType;
import org.simpleframework.xml.Element;

/**
 * <p>Java class for anonymous complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Chatname" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ChatID" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="NumberOfMessages" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */

@Default(DefaultType.FIELD)
public class CheckNewChat {

    @Element(name = "Chatname", required = true)
    protected String chatname;
    @Element(required = false, name = "ChatID")
    protected int chatID;
    @Element(required = false, name = "NumberOfMessages")
    protected int numberOfMessages;

    /**
     * Gets the value of the chatname property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getChatname() {
        return chatname;
    }

    /**
     * Sets the value of the chatname property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setChatname(String value) {
        this.chatname = value;
    }

    /**
     * Gets the value of the chatID property.
     */
    public int getChatID() {
        return chatID;
    }

    /**
     * Sets the value of the chatID property.
     */
    public void setChatID(int value) {
        this.chatID = value;
    }

    /**
     * Gets the value of the numberOfMessages property.
     */
    public int getNumberOfMessages() {
        return numberOfMessages;
    }

    /**
     * Sets the value of the numberOfMessages property.
     */
    public void setNumberOfMessages(int value) {
        this.numberOfMessages = value;
    }

}