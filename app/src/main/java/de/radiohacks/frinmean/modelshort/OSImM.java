//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.03.26 at 08:36:09 PM CET 
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
 *           &lt;element name="ImID" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *           &lt;element name="ImF" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;/sequence>
 *         &lt;element name="ET" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@Default(DefaultType.FIELD)
@Root(name = "OSImM", strict = false)
public class OSImM {

    @Element(required = false, name = "ImID")
    protected Integer imID;
    @Element(required = false, name = "ImF")
    protected String imF;
    @Element(required = false, name = "ET")
    protected String et;

    /**
     * Gets the value of the imID property.
     *
     * @return possible object is
     * {@link Integer }
     */
    public Integer getImID() {
        return imID;
    }

    /**
     * Sets the value of the imID property.
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setImID(Integer value) {
        this.imID = value;
    }

    /**
     * Gets the value of the imF property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getImF() {
        return imF;
    }

    /**
     * Sets the value of the imF property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setImF(String value) {
        this.imF = value;
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
