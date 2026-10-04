package io.github.manoelcampos.java2ts.javadoc.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

import java.util.ArrayList;
import java.util.List;

/**
 * A field or method element inside the xml-doclet output, mapped using JAXB.
 * @author Manoel Campos
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class MemberXml extends DocumentedXml {
    /**
     * Creates a {@link MemberXml} (used by JAXB).
     */
    public MemberXml() {/**/}

    /** The method parameters (only their existence matters, so their content is not mapped). */
    @XmlElement(name = "parameter")
    private List<Object> parameters = new ArrayList<>();

    /**
     * {@return true if the member is a method with parameters, false otherwise}
     */
    public boolean hasParameters() {
        return !parameters.isEmpty();
    }
}
