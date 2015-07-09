package de.radiohacks.frinmean.modelshort;

import org.simpleframework.xml.Default;
import org.simpleframework.xml.DefaultType;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by thomas on 18.06.15.
 */

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
 *         &lt;element name="UN" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="UID" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="E" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@Default(DefaultType.FIELD)
@Root(name = "U", strict = false)
public class U {

    @Element(name = "UN", required = true)
    protected String un;
    @Element(required = false, name = "UID")
    protected int uid;
    @Element(name = "E", required = true)
    protected String e;

    /**
     * Gets the value of the un property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getUN() {
        return un;
    }

    /**
     * Sets the value of the un property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setUN(String value) {
        this.un = value;
    }

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
     * Gets the value of the e property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getE() {
        return e;
    }

    /**
     * Sets the value of the e property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setE(String value) {
        this.e = value;
    }

}
