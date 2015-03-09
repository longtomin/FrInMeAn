/**
 * Copyright � 2015, Thomas Schreiner, thomas1.schreiner@googlemail.com
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
//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.03.01 at 12:27:25 PM CET 
//


package de.radiohacks.frinmean.model;

import org.simpleframework.xml.Default;
import org.simpleframework.xml.DefaultType;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;


/**
 * <p>Java class for anonymous complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;sequence>
 *           &lt;element name="MessageID" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *           &lt;element name="SendTimestamp" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;/sequence>
 *         &lt;element name="Errortext" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@Default(DefaultType.FIELD)
@Root(name = "OutInsertMessageIntoChat", strict = false)
public class OutInsertMessageIntoChat {

    @Element(required = false, name = "MessageID")
    protected Integer messageID;
    @Element(required = false, name = "SendTimestamp")
    protected Long sendTimestamp;
    @Element(required = false, name = "Errortext")
    protected String errortext;

    /**
     * Gets the value of the messageID property.
     *
     * @return possible object is
     * {@link Integer }
     */
    public Integer getMessageID() {
        return messageID;
    }

    /**
     * Sets the value of the messageID property.
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setMessageID(Integer value) {
        this.messageID = value;
    }

    /**
     * Gets the value of the sendTimestamp property.
     *
     * @return possible object is
     * {@link Long }
     */
    public Long getSendTimestamp() {
        return sendTimestamp;
    }

    /**
     * Sets the value of the sendTimestamp property.
     *
     * @param value allowed object is
     *              {@link Long }
     */
    public void setSendTimestamp(Long value) {
        this.sendTimestamp = value;
    }

    /**
     * Gets the value of the errortext property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getErrortext() {
        return errortext;
    }

    /**
     * Sets the value of the errortext property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setErrortext(String value) {
        this.errortext = value;
    }

}
