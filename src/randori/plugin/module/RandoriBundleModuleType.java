package randori.plugin.module;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleTypeManager;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Created with IntelliJ IDEA.
 * User: Roland
 * Date: 3/20/13
 * Time: 4:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class RandoriBundleModuleType extends ModuleType<RandoriBundleModuleBuilder> {

    public static final Icon RANDORI_ICON_SMALL = IconLoader
            .getIcon("/randori/plugin/module/randori.png");

    public static final Icon RANDORI_ICON_LARGE = IconLoader
            .getIcon("/randori/plugin/module/randorix2.png");

    @NonNls
    private static final String MODULE_ID = "RANDORI_BUNDLE_MODULE";

    public RandoriBundleModuleType()
    {
        super(MODULE_ID);
    }

    public static RandoriBundleModuleType getInstance()
    {
        return (RandoriBundleModuleType) ModuleTypeManager.getInstance().findByID(
                MODULE_ID);
    }

    public static boolean isOfType(Module module)
    {
        return get(module) instanceof RandoriModuleType;
    }

    // create New Project
    @Override
    public RandoriBundleModuleBuilder createModuleBuilder()
    {
        return new RandoriBundleModuleBuilder();
    }

    @Override
    public String getName()
    {
        return "Randori Bundle Module";
    }

    @Override
    public String getDescription()
    {
        return "This module type is used to create Randori AS3 bundle projects using the JavaScript cross compiler";
    }

    @Override
    public Icon getBigIcon()
    {
        return RANDORI_ICON_LARGE;
    }

    @Override
    public Icon getNodeIcon(boolean isOpened)
    {
        return RANDORI_ICON_SMALL;
    }

    @Override
    public boolean isValidSdk(Module module, @Nullable Sdk projectSdk)
    {
        return super.isValidSdk(module, projectSdk);
    }

}
