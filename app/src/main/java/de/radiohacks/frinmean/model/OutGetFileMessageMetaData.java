//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.12.14 at 08:27:05 PM CET 
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
 *           &lt;element name="FileMessage" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *           &lt;element name="FileSize" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;/sequence>
 *         &lt;element name="Errortext" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@Default(DefaultType.FIELD)
@Root(name = "OutGetFileMessageMetaData", strict = false)
public class OutGetFileMessageMetaData {

    @Element(required = false, name = "FileMessage")
    protected String fileMessage;
    @Element(required = false, name = "FileSize")
    protected Long fileSize;
    @Element(required = false, name = "Errortext")
    protected String errortext;

    /**
     * Gets the value of the fileMessage property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getFileMessage() {
        return fileMessage;
    }

    /**
     * Sets the value of the fileMessage property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setFileMessage(String value) {
        this.fileMessage = value;
    }

    /**
     * Gets the value of the fileSize property.
     *
     * @return possible object is
     * {@link Long }
     */
    public Long getFileSize() {
        return fileSize;
    }

    /**
     * Sets the value of the fileSize property.
     *
     * @param value allowed object is
     *              {@link Long }
     */
    public void setFileSize(Long value) {
        this.fileSize = value;
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
