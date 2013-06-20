package randori.plugin.library;

import org.jetbrains.annotations.Nullable;

import com.intellij.lang.javascript.flex.library.FlexLibraryProperties;
import com.intellij.openapi.roots.libraries.LibraryProperties;
import com.intellij.openapi.util.Comparing;
import com.intellij.util.xmlb.annotations.Attribute;

/**
 * @author AsusFred Date: 20/06/13 Time: 00:20
 */
public class RandoriLibraryProperties extends LibraryProperties<RandoriLibraryProperties>
{

    @Attribute("id")
    @Nullable
    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    private String id;

    @Override
    public boolean equals(Object obj)
    {
        return ((obj instanceof FlexLibraryProperties))
                && (Comparing.equal(((FlexLibraryProperties) obj).getId(), getId()));
    }

    @Override
    public int hashCode()
    {
        return id != null ? id.hashCode() : 0;
    }

    /**
     * @return a component state. All properties and public fields are serialized. Only values, which differ from
     * default (i.e. the value of newly instantiated class) are serialized. <code>null</code> value indicates that no
     * state should be stored
     * @see com.intellij.util.xmlb.XmlSerializer
     */
    @Nullable
    @Override
    public RandoriLibraryProperties getState()
    {
        return this;
    }

    /**
     * This method is called when new component state is loaded. A component should expect this method to be called at
     * any moment of its lifecycle. The method can and will be called several times, if config files were externally
     * changed while IDEA running.
     * 
     * @param state loaded component state
     * @see com.intellij.util.xmlb.XmlSerializerUtil#copyBean(Object, Object)
     */
    @Override
    public void loadState(RandoriLibraryProperties state)
    {
        id = state.getId();
    }
}
