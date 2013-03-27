package randori.plugin.module;

import com.intellij.openapi.module.ModuleType;

public class RandoriBundleModuleBuilder extends RandoriModuleBuilder {

    @SuppressWarnings("rawtypes")
    @Override
    public ModuleType getModuleType()
    {
        return RandoriBundleModuleType.getInstance();
    }

}
