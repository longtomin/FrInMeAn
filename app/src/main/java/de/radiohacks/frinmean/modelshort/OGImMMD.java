//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.06.02 at 09:26:35 PM CEST 
//


package de.radiohacks.frinmean.modelshort;

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
 *           &lt;element name="IM" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *           &lt;element name="IS" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *           &lt;element name="IMD5" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;/sequence>
 *         &lt;element name="ET" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@Default(DefaultType.FIELD)
@Root(name = "OGImMMD", strict = false)
public class OGImMMD {

    @Element(required = false, name = "IM")
    protected String im;
    @Element(required = false, name = "IS")
    protected Long is;
    @Element(required = false, name = "IMD5")
    protected String imd5;
    @Element(required = false, name = "ET")
    protected String et;

    /**
     * Gets the value of the im property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getIM() {
        return im;
    }

    /**
     * Sets the value of the im property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setIM(String value) {
        this.im = value;
    }

    /**
     * Gets the value of the is property.
     *
     * @return possible object is
     * {@link Long }
     */
    public Long getIS() {
        return is;
    }

    /**
     * Sets the value of the is property.
     *
     * @param value allowed object is
     *              {@link Long }
     */
    public void setIS(Long value) {
        this.is = value;
    }

    /**
     * Gets the value of the imd5 property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getIMD5() {
        return imd5;
    }

    /**
     * Sets the value of the imd5 property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setIMD5(String value) {
        this.imd5 = value;
    }

    /**
     * Gets the value of the et property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getET() {
        return et;
    }

    /**
     * Sets the value of the et property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setET(String value) {
        this.et = value;
    }

}