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
import org.simpleframework.xml.Root;

/**
 * <p>Java-Klasse f�r anonymous complex type.
 * <p/>
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p/>
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;sequence>
 *           &lt;element name="MessageID" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *           &lt;element name="ShowTimestamp" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;/sequence>
 *         &lt;element name="Errortext" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>data/shop/D04/001/780/494/521/25/8908079_Doc_01_DE_20140629005133.pdf
 * </pre>
 */
@Default(DefaultType.FIELD)
@Root(name = "OutSetShowTimeStamp", strict = false)
public class OutSetShowTimeStamp {

    @Element(name = "MessageID", required = false)
    protected Integer messageID;
    @Element(name = "ShowTimestamp", required = false)
    protected Long showTimestamp;
    @Element(name = "Errortext", required = false)
    protected String errortext;

    /**
     * Ruft den Wert der messageID-Eigenschaft ab.
     *
     * @return possible object is
     * {@link Integer }
     */
    public Integer getMessageID() {
        return messageID;
    }

    /**
     * Legt den Wert der messageID-Eigenschaft fest.
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setMessageID(Integer value) {
        this.messageID = value;
    }

    /**
     * Ruft den Wert der showTimestamp-Eigenschaft ab.
     *
     * @return possible object is
     * {@link Long }
     */
    public Long getShowTimestamp() {
        return showTimestamp;
    }

    /**
     * Legt den Wert der showTimestamp-Eigenschaft fest.
     *
     * @param value allowed object is
     *              {@link Long }
     */
    public void setShowTimestamp(Long value) {
        this.showTimestamp = value;
    }

    /**
     * Ruft den Wert der errortext-Eigenschaft ab.
     *
     * @return possible object is
     * {@link String }
     */
    public String getErrortext() {
        return errortext;
    }

    /**
     * Legt den Wert der errortext-Eigenschaft fest.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setErrortext(String value) {
        this.errortext = value;
    }

}
