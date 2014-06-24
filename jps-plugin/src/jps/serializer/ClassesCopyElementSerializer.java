package jps.serializer;

import jps.JpsClassesCopyPackagingElement;
import jps.JpsClassesCopyPackagingElementImpl;
import org.jdom.Element;
import org.jetbrains.jps.model.serialization.artifact.JpsPackagingElementSerializer;

/**
 * Created with IntelliJ IDEA.
 * User: Jaroslaw Koscinski
 * Date: 23.06.2014
 * Time: 00:51
 */
public class ClassesCopyElementSerializer extends JpsPackagingElementSerializer<JpsClassesCopyPackagingElement> {

    public ClassesCopyElementSerializer() {
        super("classes-copy", JpsClassesCopyPackagingElement.class);
    }

    @Override
    public JpsClassesCopyPackagingElement load(Element element) {
        return new JpsClassesCopyPackagingElementImpl(element.getAttributeValue("path"));
    }

    @Override
    public void save(JpsClassesCopyPackagingElement element, Element tag) {
        tag.setAttribute("path", element.getDirectoryPath());
    }
}

