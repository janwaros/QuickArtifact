package jps;

import jps.serializer.ClassesCopyElementSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.serialization.JpsModelSerializerExtension;
import org.jetbrains.jps.model.serialization.artifact.JpsPackagingElementSerializer;

import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Jaroslaw Koscinski
 * Date: 23.06.2014
 * Time: 01:50
 */
public class JpsClassesCopyModelSerializerExtension extends JpsModelSerializerExtension {

    @NotNull
    public List<? extends JpsPackagingElementSerializer<?>> getPackagingElementSerializers() {
        return Collections.singletonList(new ClassesCopyElementSerializer());
    }

}
