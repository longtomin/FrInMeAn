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
 *           &lt;element name="MID" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *           &lt;element name="NT" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *           &lt;element name="NR" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *           &lt;element name="NS" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;/sequence>
 *         &lt;element name="ET" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@Default(DefaultType.FIELD)
@Root(name = "OGMI", strict = false)
public class OGMI {

    @Element(required = false, name = "MID")
    protected Integer mid;
    @Element(required = false, name = "NT")
    protected Integer nt;
    @Element(required = false, name = "NR")
    protected Integer nr;
    @Element(required = false, name = "NS")
    protected Integer ns;
    @Element(required = false, name = "ET")
    protected String et;

    /**
     * Gets the value of the mid property.
     *
     * @return possible object is
     * {@link Integer }
     */
    public Integer getMID() {
        return mid;
    }

    /**
     * Sets the value of the mid property.
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setMID(Integer value) {
        this.mid = value;
    }

    /**
     * Gets the value of the nt property.
     *
     * @return possible object is
     * {@link Integer }
     */
    public Integer getNT() {
        return nt;
    }

    /**
     * Sets the value of the nt property.
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setNT(Integer value) {
        this.nt = value;
    }

    /**
     * Gets the value of the nr property.
     *
     * @return possible object is
     * {@link Integer }
     */
    public Integer getNR() {
        return nr;
    }

    /**
     * Sets the value of the nr property.
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setNR(Integer value) {
        this.nr = value;
    }

    /**
     * Gets the value of the ns property.
     *
     * @return possible object is
     * {@link Integer }
     */
    public Integer getNS() {
        return ns;
    }

    /**
     * Sets the value of the ns property.
     *
     * @param value allowed object is
     *              {@link Integer }
     */
    public void setNS(Integer value) {
        this.ns = value;
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