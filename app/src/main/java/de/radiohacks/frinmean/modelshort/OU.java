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
 *       &lt;sequence>
 *         &lt;element name="OUN" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="OUID" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@Default(DefaultType.FIELD)
@Root(name = "OU", strict = false)
public class OU {

    @Element(name = "OUN", required = true)
    protected String oun;
    @Element(required = false, name = "OUID")
    protected int ouid;

    /**
     * Gets the value of the oun property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getOUN() {
        return oun;
    }

    /**
     * Sets the value of the oun property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setOUN(String value) {
        this.oun = value;
    }

    /**
     * Gets the value of the ouid property.
     */
    public int getOUID() {
        return ouid;
    }

    /**
     * Sets the value of the ouid property.
     */
    public void setOUID(int value) {
        this.ouid = value;
    }

}
