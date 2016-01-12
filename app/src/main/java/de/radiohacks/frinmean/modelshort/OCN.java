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
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;sequence>
 *           &lt;element ref="{}C" maxOccurs="unbounded" minOccurs="0"/>
 *           &lt;element ref="{}CNM" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;/sequence>
 *         &lt;element name="ET" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@Root(name = "OCN", strict=false)
public class OCN {

    @ElementList(required = false, name = "C", inline = true)
    protected List<C> c;
    @ElementList(required = false, name = "CNM", inline = true)
    protected List<CNM> cnm;
    @Element(required = false, name = "ET")
    protected String et;

    /**
     * Gets the value of the c property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the c property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getC().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link C }
     *
     *
     */
    public List<C> getC() {
        if (c == null) {
            c = new ArrayList<C>();
        }
        return this.c;
    }

    /**
     * Gets the value of the cnm property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the cnm property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCNM().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CNM }
     * 
     * 
     */
    public List<CNM> getCNM() {
        if (cnm == null) {
            cnm = new ArrayList<CNM>();
        }
        return this.cnm;
    }

    /**
     * Gets the value of the et property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getET() {
        return et;
    }

    /**
     * Sets the value of the et property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setET(String value) {
        this.et = value;
    }

}
