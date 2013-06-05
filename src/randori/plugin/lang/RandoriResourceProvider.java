package randori.plugin.lang;

import com.intellij.javaee.ResourceRegistrar;
import com.intellij.javaee.StandardResourceProvider;

/**
 * @author Frédéric THOMAS
 * Date: 28/05/13
 * Time: 06:57
 */
public class RandoriResourceProvider
        implements StandardResourceProvider
{

    public void registerResources(ResourceRegistrar registrar)
    {
        registrar.addStdResource("urn:Flex:Meta", "/schemas/randoriMetaData.dtd", RandoriResourceProvider.class);
    }

    public RandoriResourceProvider()
    {
    }
}