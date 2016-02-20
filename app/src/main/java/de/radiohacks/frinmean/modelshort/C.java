/**
 * Copyright � 2015, Thomas Schreiner, thomas1.schreiner@googlemail.com
 * All rights reserved.
 * <p/>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p/>
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p/>
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
 * <p/>
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 */
//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.01.05 at 09:19:06 PM CET 
//


package de.radiohacks.frinmean.modelshort;

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
 *       &lt;sequence>
 *         &lt;element name="CN" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="CID" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="ICID" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element ref="{}OU"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@Root(name = "C", strict = false)
public class C {

    @Element(name = "CN", required = true)
    protected String cn;
    @Element(required = false, name = "CID")
    protected int cid;
    @Element(required = false, name = "ICID")
    protected int icid;
    @Element(name = "OU", required = true)
    protected OU ou;

    /**
     * Gets the value of the cn property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getCN() {
        return cn;
    }

    /**
     * Sets the value of the cn property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCN(String value) {
        this.cn = value;
    }

    /**
     * Gets the value of the cid property.
     */
    public int getCID() {
        return cid;
    }

    /**
     * Sets the value of the cid property.
     */
    public void setCID(int value) {
        this.cid = value;
    }

    /**
     * Gets the value of the icid property.
     */
    public int getICID() {
        return icid;
    }

    /**
     * Sets the value of the icid property.
     */
    public void setICID(int value) {
        this.icid = value;
    }

    /**
     * Gets the value of the ou property.
     *
     * @return possible object is
     * {@link OU }
     */
    public OU getOU() {
        return ou;
    }

    /**
     * Sets the value of the ou property.
     *
     * @param value allowed object is
     *              {@link OU }
     */
    public void setOU(OU value) {
        this.ou = value;
    }

}
