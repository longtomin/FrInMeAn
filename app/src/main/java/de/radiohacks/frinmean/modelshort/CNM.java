//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.05.30 at 08:04:57 PM CEST 
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
 *         &lt;element name="CN" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="CID" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="NOM" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@Default(DefaultType.FIELD)
@Root(name = "CNM", strict = false)
public class CNM {

    @Element(name = "CN", required = false)
    protected String cn;
    @Element(required = false, name = "CID")
    protected int cid;
    @Element(required = false, name = "NOM")
    protected int nom;

    /**
     * Gets the value of the cn property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getCN() {
        return cn;
    }

    /**
     * Sets the value of the cn property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCN(String value) {
        this.cn = value;
    }

    /**
     * Gets the value of the cid property.
     */
    public int getCID() {
        return cid;
    }

    /**
     * Sets the value of the cid property.
     */
    public void setCID(int value) {
        this.cid = value;
    }

    /**
     * Gets the value of the nom property.
     */
    public int getNOM() {
        return nom;
    }

    /**
     * Sets the value of the nom property.
     */
    public void setNOM(int value) {
        this.nom = value;
    }

}