//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.10.23 at 10:56:51 PM CEST 
//


package de.radiohacks.frinmean.model;

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
public class Chats {

    @Element(name = "Chatname", required = true)
    protected String chatname;
    @Element(required = false, name = "ChatID")
    protected int chatID;
    @Element(required = false, name = "NumberOfMessages")
    protected int numberOfMessages;

    public String getChatname() {
        return chatname;
    }

    public void setChatname(String value) {
        this.chatname = value;
    }

    public int getChatID() {
        return chatID;
    }

    public void setChatID(int value) {
        this.chatID = value;
    }

    public int getNumberOfMessages() {
        return numberOfMessages;
    }

    public void setNumberOfMessages(int value) {
        this.numberOfMessages = value;
    }

}