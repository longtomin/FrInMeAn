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
 *         &lt;element name="VM" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="VMD5" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@Default(DefaultType.FIELD)
@Root(name = "ISViM", strict = false)
public class ISViM {

    @Element(name = "VM", required = true)
    protected String vm;
    @Element(name = "VMD5", required = true)
    protected String vmd5;

    /**
     * Gets the value of the vm property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getVM() {
        return vm;
    }

    /**
     * Sets the value of the vm property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setVM(String value) {
        this.vm = value;
    }

    /**
     * Gets the value of the vmd5 property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getVMD5() {
        return vmd5;
    }

    /**
     * Sets the value of the vmd5 property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setVMD5(String value) {
        this.vmd5 = value;
    }

}
