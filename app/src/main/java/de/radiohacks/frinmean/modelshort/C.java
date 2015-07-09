package de.radiohacks.frinmean.modelshort;

/**
 * Created by thomas on 18.06.15.
 */

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
 *         &lt;element ref="{}OU"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@Default(DefaultType.FIELD)
@Root(name = "C", strict = false)
public class C {

    @Element(name = "CN", required = true)
    protected String cn;
    @Element(required = false, name = "CID")
    protected int cid;
    @Element(name = "OU", required = true)
    protected OU ou;

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
     * Gets the value of the ou property.
     *
     * @return possible object is
     * {@link OU }
     */
    public OU getOU() {
        return ou;
    }

    /**
     * Sets the value of the ou property.
     *
     * @param value allowed object is
     *              {@link OU }
     */
    public void setOU(OU value) {
        this.ou = value;
    }

}
