//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.11.02 at 09:24:09 PM CET 
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
 *       &lt;choice>
 *         &lt;sequence>
 *           &lt;element name="IID" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *           &lt;element name="IF" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;/sequence>
 *         &lt;element name="ET" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */

@Root(name = "OSImM", strict = false)
public class OSImM {

    @Element(required = false, name = "IID")
    protected Integer iid;
    @Element(required = false, name = "IF")
    protected String _if;
    @Element(required = false, name = "ET")
    protected String et;

    /**
     * Gets the value of the iid property.
     *
     * @return possible object is
     * {@link Integer }
     */
    public Integer getIID() {
        return iid;
    }

    /**
     * Sets the value of the iid property.
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setIID(Integer value) {
        this.iid = value;
    }

    /**
     * Gets the value of the if property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getIF() {
        return _if;
    }

    /**
     * Sets the value of the if property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setIF(String value) {
        this._if = value;
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
