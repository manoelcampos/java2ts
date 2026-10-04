package io.github.manoelcampos.java2ts.javadoc.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;

import java.util.ArrayList;
import java.util.List;

/**
 * A named element inside the xml-doclet output that may have a comment and block tags,
 * such as classes, fields and methods.
 * @author Manoel Campos
 */
@XmlTransient
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class DocumentedXml {
    /**
     * Creates a {@link DocumentedXml} (used by JAXB).
     */
    protected DocumentedXml() {/**/}

    @XmlAttribute
    private String name = "";

    @XmlElement
    private String comment = "";

    @XmlElement(name = "tag")
    private List<TagXml> tags = new ArrayList<>();

    /**
     * {@return the element name}
     */
    public String getName() {
        return name;
    }

    /**
     * {@return the element comment (without block tags), or an empty string if it has no comment}
     */
    public String getComment() {
        return comment == null ? "" : comment;
    }

    /**
     * {@return the block tags of the element}
     */
    public List<TagXml> getTags() {
        return tags;
    }
}
