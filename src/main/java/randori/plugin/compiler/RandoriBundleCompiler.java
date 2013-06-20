package randori.plugin.compiler;

import org.apache.flex.compiler.internal.workspaces.Workspace;
import randori.compiler.internal.driver.RandoriBackend;
import randori.compiler.internal.projects.RandoriBundleProject;

/**
 * @author Frédéric THOMAS
 * Date: 13/04/13
 * Time: 15:35
 */
class RandoriBundleCompiler extends RandoriBundleProject {
    public RandoriBundleCompiler(Workspace workspace) {
        super(workspace, new RandoriBackend());
    }
}
