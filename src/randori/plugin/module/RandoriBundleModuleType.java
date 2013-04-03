package randori.plugin.module;

import javax.swing.*;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import randori.plugin.icons.RandoriIcons;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleTypeManager;
import com.intellij.openapi.projectRoots.Sdk;

/**
 * Created with IntelliJ IDEA.
 * User: Roland
 * Date: 3/20/13
 * Time: 4:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class RandoriBundleModuleType extends ModuleType<RandoriBundleModuleBuilder> {

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
        return RandoriIcons.Randori24;
    }

    @Override
    public Icon getNodeIcon(boolean isOpened)
    {
        return RandoriIcons.Randori16;
    }

    @Override
    public boolean isValidSdk(Module module, @Nullable Sdk projectSdk)
    {
        return super.isValidSdk(module, projectSdk);
    }

}
