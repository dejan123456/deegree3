//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-792 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.10.07 at 02:02:59 PM MESZ 
//


package org.deegree.commons.configuration;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *         For OWS use, the optional thesaurusName element was
 *         omitted as being complex information that could be referenced by the
 *         codeSpace attribute
 *         of the Type element.
 *       
 * 
 * <p>Java class for KeywordsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="KeywordsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Keyword" type="{http://www.deegree.org/commons}LanguageStringType" maxOccurs="unbounded"/>
 *         &lt;element name="Type" type="{http://www.deegree.org/commons}CodeType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "KeywordsType", propOrder = {
    "keyword",
    "type"
})
public class KeywordsType {

    @XmlElement(name = "Keyword", required = true)
    protected List<LanguageStringType> keyword;
    @XmlElement(name = "Type")
    protected CodeType type;

    /**
     * Gets the value of the keyword property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the keyword property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getKeyword().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LanguageStringType }
     * 
     * 
     */
    public List<LanguageStringType> getKeyword() {
        if (keyword == null) {
            keyword = new ArrayList<LanguageStringType>();
        }
        return this.keyword;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link CodeType }
     *     
     */
    public CodeType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link CodeType }
     *     
     */
    public void setType(CodeType value) {
        this.type = value;
    }

}
