package de.radiohacks.frinmean.model;

/**
 * Created by thomas on 13.09.14.
 */

import org.simpleframework.xml.Default;
import org.simpleframework.xml.DefaultType;
import org.simpleframework.xml.Element;

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
 *         &lt;element name="Chatname" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ChatID" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="NumberOfMessages" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */

@Default(DefaultType.FIELD)
public class CheckNewChat {

    @Element(name = "Chatname", required = true)
    protected String chatname;
    @Element(required = false, name = "ChatID")
    protected int chatID;
    @Element(required = false, name = "NumberOfMessages")
    protected int numberOfMessages;

    /**
     * Gets the value of the chatname property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getChatname() {
        return chatname;
    }

    /**
     * Sets the value of the chatname property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setChatname(String value) {
        this.chatname = value;
    }

    /**
     * Gets the value of the chatID property.
     */
    public int getChatID() {
        return chatID;
    }

    /**
     * Sets the value of the chatID property.
     */
    public void setChatID(int value) {
        this.chatID = value;
    }

    /**
     * Gets the value of the numberOfMessages property.
     */
    public int getNumberOfMessages() {
        return numberOfMessages;
    }

    /**
     * Sets the value of the numberOfMessages property.
     */
    public void setNumberOfMessages(int value) {
        this.numberOfMessages = value;
    }

}