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
 *       &lt;sequence>
 *         &lt;element name="FM" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="FMD5" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@Default(DefaultType.FIELD)
@Root(name = "ISFiM", strict = false)
public class ISFiM {

    @Element(name = "FM", required = true)
    protected String fm;
    @Element(name = "FMD5", required = true)
    protected String fmd5;

    /**
     * Gets the value of the fm property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getFM() {
        return fm;
    }

    /**
     * Sets the value of the fm property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setFM(String value) {
        this.fm = value;
    }

    /**
     * Gets the value of the fmd5 property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getFMD5() {
        return fmd5;
    }

    /**
     * Sets the value of the fmd5 property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setFMD5(String value) {
        this.fmd5 = value;
    }

}
