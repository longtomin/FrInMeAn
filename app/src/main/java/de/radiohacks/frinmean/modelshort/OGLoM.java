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
 *           &lt;element name="LM">
 *             &lt;complexType>
 *               &lt;complexContent>
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                   &lt;sequence>
 *                     &lt;element name="Lat" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                     &lt;element name="Lon" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;/sequence>
 *                 &lt;/restriction>
 *               &lt;/complexContent>
 *             &lt;/complexType>
 *           &lt;/element>
 *         &lt;/sequence>
 *         &lt;element name="ET" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@Default(DefaultType.FIELD)
@Root(name = "OGLoM", strict = false)
public class OGLoM {

    @Element(required = false, name = "LM")
    protected OGLoM.LM lm;
    @Element(required = false, name = "ET")
    protected String et;

    /**
     * Gets the value of the lm property.
     *
     * @return possible object is
     * {@link OGLoM.LM }
     */
    public OGLoM.LM getLM() {
        return lm;
    }

    /**
     * Sets the value of the lm property.
     *
     * @param value allowed object is
     *              {@link OGLoM.LM }
     */
    public void setLM(OGLoM.LM value) {
        this.lm = value;
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
     *         &lt;element name="Lat" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="Lon" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     */
    @Default(DefaultType.FIELD)
    public static class LM {

        @Element(name = "Lat", required = true)
        protected String lat;
        @Element(name = "Lon", required = true)
        protected String lon;

        /**
         * Gets the value of the lat property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getLat() {
            return lat;
        }

        /**
         * Sets the value of the lat property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setLat(String value) {
            this.lat = value;
        }

        /**
         * Gets the value of the lon property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getLon() {
            return lon;
        }

        /**
         * Sets the value of the lon property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setLon(String value) {
            this.lon = value;
        }

    }

}