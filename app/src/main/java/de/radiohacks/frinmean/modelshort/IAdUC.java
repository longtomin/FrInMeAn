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
 *         &lt;element name="UID" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="CID" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@Default(DefaultType.FIELD)

@Root(name = "IAdUC", strict = false)
public class IAdUC {

    @Element(required = false, name = "UID")
    protected int uid;
    @Element(required = false, name = "CID")
    protected int cid;

    /**
     * Gets the value of the uid property.
     */
    public int getUID() {
        return uid;
    }

    /**
     * Sets the value of the uid property.
     */
    public void setUID(int value) {
        this.uid = value;
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

}
