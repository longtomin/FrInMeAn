//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.11.02 at 09:24:09 PM CET 
//


package de.radiohacks.frinmean.modelshort;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.List;


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
 *         &lt;element name="MID" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="SD" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element ref="{}MI" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */

@Root(name = "MIB", strict = false)
public class MIB {

    @Element(required = false, name = "MID")
    protected int mid;
    @Element(required = false, name = "SD")
    protected long sd;
    @ElementList(required = false, name = "MI", inline = true)
    protected List<MI> mi;

    /**
     * Gets the value of the mid property.
     */
    public int getMID() {
        return mid;
    }

    /**
     * Sets the value of the mid property.
     */
    public void setMID(int value) {
        this.mid = value;
    }

    /**
     * Gets the value of the sd property.
     */
    public long getSD() {
        return sd;
    }

    /**
     * Sets the value of the sd property.
     */
    public void setSD(long value) {
        this.sd = value;
    }

    /**
     * Gets the value of the mi property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the mi property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMI().add(newItem);
     * </pre>
     * <p/>
     * <p/>
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link MI }
     */
    public List<MI> getMI() {
        if (mi == null) {
            mi = new ArrayList<MI>();
        }
        return this.mi;
    }

}
