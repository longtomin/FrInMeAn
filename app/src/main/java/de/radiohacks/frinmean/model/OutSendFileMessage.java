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
 *           &lt;element name="FileID" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *           &lt;element name="FileFileName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;/sequence>
 *         &lt;element name="Errortext" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@Default(DefaultType.FIELD)
@Root(name = "OutSendFileMessage", strict = false)
public class OutSendFileMessage {

    @Element(required = false, name = "FileID")
    protected Integer fileID;
    @Element(required = false, name = "FileFileName")
    protected String fileFileName;
    @Element(required = false, name = "Errortext")
    protected String errortext;

    /**
     * Gets the value of the fileID property.
     *
     * @return possible object is
     * {@link Integer }
     */
    public Integer getFileID() {
        return fileID;
    }

    /**
     * Sets the value of the fileID property.
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setFileID(Integer value) {
        this.fileID = value;
    }

    /**
     * Gets the value of the fileFileName property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getFileFileName() {
        return fileFileName;
    }

    /**
     * Sets the value of the fileFileName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setFileFileName(String value) {
        this.fileFileName = value;
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
