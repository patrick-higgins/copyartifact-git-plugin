/*
 * The MIT License
 *
 * Copyright (c) 2014, Patrick Higgins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.plugins.copyartifact.git;

import hudson.EnvVars;
import hudson.Extension;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.Descriptor;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.copyartifact.BuildSelector;
import hudson.plugins.copyartifact.SimpleBuildSelectorDescriptor;
import hudson.plugins.git.util.BuildData;
import org.jenkinsci.plugins.tokenmacro.TokenMacro;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Picks up a build through git branch.
 *
 * @author Patrick Higgins
 */
public class BranchBuildSelector extends BuildSelector {
    public final String branchName;
    private Boolean stable;

    @DataBoundConstructor
    public BranchBuildSelector(boolean stableOnly, String branchName) {
        this.stable = stableOnly ? Boolean.TRUE : null;
        this.branchName = Util.fixNull(branchName);
    }

    public boolean isStable() {
        return stable != null && stable.booleanValue();
    }

    @Override
    protected boolean isSelectable(Run<?,?> run, EnvVars env) {
        if (!run.getResult().isBetterOrEqualTo(isStable() ? Result.SUCCESS : Result.UNSTABLE)) {
            return false;
        }

        String branch = branchName;
        try {
            branch = env.expand(branch);
            branch = TokenMacro.expandAll((AbstractBuild<?,?>)run, TaskListener.NULL, branch);
        } catch (Exception ignore) {}

        for (BuildData buildData : run.getActions(BuildData.class)) {
            if (buildData.getLastBuiltRevision().containsBranchName(branch)) {
                return true;
            }
        }
        return false;
    }

    @Extension(ordinal=200)
    public static final Descriptor<BuildSelector> DESCRIPTOR =
            new SimpleBuildSelectorDescriptor(
                BranchBuildSelector.class, Messages._BranchBuildSelector_DisplayName());

}
