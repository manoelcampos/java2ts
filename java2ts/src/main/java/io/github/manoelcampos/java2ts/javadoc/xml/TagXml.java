package io.github.manoelcampos.java2ts.javadoc.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

/**
 * A JavaDoc block tag element (such as {@code @param} or {@code @return}) inside the xml-doclet output,
 * mapped using JAXB.
 * @author Manoel Campos
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class TagXml {
    /**
     * Creates a {@link TagXml} (used by JAXB).
     */
    public TagXml() {/**/}

    @XmlAttribute
    private String name = "";

    /** The tag text, which includes the tag name (such as {@code @author Manoel}). */
    @XmlAttribute
    private String text = "";

    /**
     * {@return the tag name, without the @}
     */
    public String getName() {
        return name;
    }

    /**
     * {@return the tag text, which includes the tag name (such as {@code @author Manoel})}
     */
    public String getText() {
        return text;
    }
}
