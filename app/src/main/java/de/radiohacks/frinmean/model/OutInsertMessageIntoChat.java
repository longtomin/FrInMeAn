//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.10.23 at 10:56:51 PM CEST 
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
