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
 *       &lt;sequence>
 *         &lt;element name="MessageID" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="MessageTyp" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="SendTimestamp" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="ReadTimestamp" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="OwningUser">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="OwningUserName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="OwningUserID" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="TextMsgID" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="ImageMsgID" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="ContactMsgID" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="LocationMsgID" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="FileMsgID" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@Default(DefaultType.FIELD)
@Root(name = "Message", strict = false)
public class Message {

    @Element(name = "MessageID", required = false)
    protected int messageID;
    @Element(name = "MessageTyp", required = false)
    protected String messageTyp;
    @Element(required = false, name = "SendTimestamp")
    protected long sendTimestamp;
    @Element(required = false, name = "ReadTimestamp")
    protected long readTimestamp;
    @Element(required = false, name = "ShowTimestamp")
    protected long showTimestamp;
    @Element(name = "OwningUser", required = false)
    protected OwningUser owningUser;
    @Element(required = false, name = "TextMsgID")
    protected int textMsgID;
    @Element(required = false, name = "ImageMsgID")
    protected int imageMsgID;
    @Element(required = false, name = "ContactMsgID")
    protected int contactMsgID;
    @Element(required = false, name = "LocationMsgID")
    protected int locationMsgID;
    @Element(required = false, name = "FileMsgID")
    protected int fileMsgID;
    @Element(required = false, name = "VideoMsgID")
    protected int videoMsgID;
    @Element(required = false, name = "OriginMsgID")
    protected int originMsgID;
    @Element(required = false, name = "NumberTotal")
    protected int numberTotal;
    @Element(required = false, name = "NumberRead")
    protected int numberRead;
    @Element(required = false, name = "NumberShow")
    protected int numberShow;

    public int getMessageID() {
        return messageID;
    }

    public void setMessageID(int value) {
        this.messageID = value;
    }

    public String getMessageTyp() {
        return messageTyp;
    }

    public void setMessageTyp(String value) {
        this.messageTyp = value;
    }

    public long getSendTimestamp() {
        return sendTimestamp;
    }

    public void setSendTimestamp(long value) {
        this.sendTimestamp = value;
    }

    public long getReadTimestamp() {
        return readTimestamp;
    }

    public void setReadTimestamp(long value) {
        this.readTimestamp = value;
    }

    public long getShowTimestamp() {
        return showTimestamp;
    }

    public void setShowTimestamp(long value) {
        this.showTimestamp = value;
    }

    public OwningUser getOwningUser() {
        return owningUser;
    }

    public void setOwningUser(OwningUser value) {
        this.owningUser = value;
    }

    public int getTextMsgID() {
        return textMsgID;
    }

    public void setTextMsgID(int value) {
        this.textMsgID = value;
    }

    public int getImageMsgID() {
        return imageMsgID;
    }

    public void setImageMsgID(int value) {
        this.imageMsgID = value;
    }

    public int getContactMsgID() {
        return contactMsgID;
    }

    public void setContactMsgID(int value) {
        this.contactMsgID = value;
    }

    public int getLocationMsgID() {
        return locationMsgID;
    }

    public void setLocationMsgID(int value) {
        this.locationMsgID = value;
    }

    public int getFileMsgID() {
        return fileMsgID;
    }

    public void setFileMsgID(int value) {
        this.fileMsgID = value;
    }

    public int getVideoMsgID() {
        return videoMsgID;
    }

    public void setVideoMsgID(int value) {
        this.videoMsgID = value;
    }

    public int getOriginMsgID() {
        return originMsgID;
    }

    public void setOriginMsgID(int value) {
        this.originMsgID = value;
    }

    public int getNumberTotal() {
        return numberTotal;
    }

    public void setNumberTotal(int value) {
        this.numberTotal = value;
    }

    public int getNumberRead() {
        return numberRead;
    }

    public void setNumberRead(int value) {
        this.numberRead = value;
    }

    public int getNumberShow() {
        return numberShow;
    }

    public void setNumberShow(int value) {
        this.numberShow = value;
    }
}