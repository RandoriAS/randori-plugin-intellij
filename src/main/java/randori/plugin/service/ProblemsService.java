/*
 * Copyright 2013 original Randori IntelliJ Plugin authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package randori.plugin.service;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.apache.flex.compiler.problems.CompilerProblemSeverity;
import org.apache.flex.compiler.problems.ICompilerProblem;
import org.apache.flex.compiler.problems.NoDefinitionForSWCDependencyProblem;
import org.apache.flex.compiler.problems.annotations.DefaultSeverity;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Michael Schmalle
 */
public class ProblemsService
{
    private static final Logger log = Logger.getInstance(ProblemsService.class);
    private final Set<ICompilerProblem> problems = new HashSet<ICompilerProblem>();
    private final ArrayList<OnProblemServiceListener> listeners = new ArrayList<OnProblemServiceListener>();

    public ProblemsService()
    {
    }

    @NotNull
    public static ProblemsService getInstance(Project project)
    {
        return ServiceManager.getService(project, ProblemsService.class);
    }

    // XXX this needs to be non modifiable, clients need to call addAll() and add()
    public Set<ICompilerProblem> getProblems()
    {
        return problems;
    }

    public boolean hasProblems()
    {
        return !problems.isEmpty();
    }

    public boolean hasErrors()
    {
        for (ICompilerProblem problem : problems)
        {
            if (getSeverity(problem) == CompilerProblemSeverity.ERROR)
                return true;
        }
        return false;
    }

    public void clearProblems()
    {
        problems.clear();
        fireOnReset();
    }

    public void addAll(List<ICompilerProblem> problems)
    {
        for (ICompilerProblem problem : problems)
        {
            addProblem(problem);
        }
        fireOnChange();
    }

    public void addAll(Set<ICompilerProblem> problems)
    {
        for (ICompilerProblem problem : problems)
        {
            addProblem(problem);
        }
        fireOnChange();
    }

    void addProblem(ICompilerProblem problem)
    {
        if (problem instanceof NoDefinitionForSWCDependencyProblem)
            return;

        final String path = problem.getSourcePath();
        if (path == null)
        {
            log.error("Problem SourcePath does not exist for "
                    + problem.toString());
            return;
        }

        if (!new File(path).exists())
        {
            log.error("Problem - " + problem.toString());
            return;
        }

        problems.add(problem);
    }

    public void addListener(OnProblemServiceListener l)
    {
        if (listeners.contains(l))
            return;
        listeners.add(l);
    }

    private void fireOnChange()
    {
        for (OnProblemServiceListener l : listeners)
        {
            l.onChange(problems);
        }
    }

    private void fireOnReset()
    {
        for (OnProblemServiceListener l : listeners)
        {
            l.onReset();
        }
    }

    public void filter()
    {
        // XXX hack for now
        HashSet<ICompilerProblem> result = new HashSet<ICompilerProblem>(
                problems);
        problems.clear();

        for (ICompilerProblem problem : result)
        {
            addProblem(problem);
        }

        fireOnChange();
    }

    private CompilerProblemSeverity getSeverity(ICompilerProblem problem)
    {
        DefaultSeverity defaultSeverity = problem.getClass().getAnnotation(
                DefaultSeverity.class);
        return defaultSeverity.value();
    }

    public interface OnProblemServiceListener
    {
        void onReset();

        void onChange(Set<ICompilerProblem> problems);
    }
}
