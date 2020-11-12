/**
 * Copyright (c) 2020 Payara Foundation
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.payara.tools.micro;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Platform;
import static org.eclipse.payara.tools.micro.MicroConstants.DEFAULT_DEBUG_PORT;
import static org.eclipse.payara.tools.micro.MicroConstants.WAR_BUILD_ARTIFACT;
import static org.eclipse.payara.tools.micro.MicroConstants.EXPLODED_WAR_BUILD_ARTIFACT;
import static org.eclipse.payara.tools.micro.MicroConstants.UBER_JAR_BUILD_ARTIFACT;

public class GradleBuildTool extends BuildTool {

    public GradleBuildTool(IProject project) {
        super(project);
    }

    @Override
    public String getExecutableHome() throws FileNotFoundException {
        String gradleHome = System.getenv("GRADLE_HOME");
        if (gradleHome == null) {
            throw new FileNotFoundException("Gradle home path not found.");
        }

        boolean gradleHomeEndsWithPathSep = gradleHome.charAt(gradleHome.length() - 1) == File.separatorChar;
        String gradleExecStr = null;
        String executor = gradleHome;
        if (!gradleHomeEndsWithPathSep) {
            executor += File.separatorChar;
        }
        executor += "bin" + File.separatorChar + "gradle";
        if (Platform.OS_WIN32.contentEquals(Platform.getOS())) {
            if (Paths.get(executor + ".bat").toFile().exists()) {
                gradleExecStr = executor + ".bat";
            } else if (Paths.get(executor + ".cmd").toFile().exists()) {
                gradleExecStr = executor + ".cmd";
            } else {
                throw new FileNotFoundException(String.format("Gradle executable %s.cmd not found.", executor));
            }
        } else if (Paths.get(executor).toFile().exists()) {
            gradleExecStr = executor;
        }
        // Gradle executable should exist.
        if (gradleExecStr == null || !Paths.get(gradleExecStr).toFile().exists()) {
            throw new FileNotFoundException(String.format("Gradle executable [%s] not found", gradleExecStr));
        }
        return gradleExecStr;
    }

    @Override
    public String getStartCommand(String contextPath, String microVersion, String buildType, String debugPort) {
        StringBuilder sb = new StringBuilder();
        if (WAR_BUILD_ARTIFACT.equals(buildType)) {
            sb.append("war -DpayaraMicro.deployWar=true");
        } else if (EXPLODED_WAR_BUILD_ARTIFACT.equals(buildType)) {
            sb.append("warExplode -DpayaraMicro.deployWar=true -DpayaraMicro.exploded=true");
        } else if (UBER_JAR_BUILD_ARTIFACT.equals(buildType)) {
            sb.append("microBundle -DpayaraMicro.useUberJar=true");
        } else {
            sb.append("build");
        }
        sb.append(" microStart");
        if (contextPath != null && !contextPath.trim().isEmpty()) {
            sb.append(" -DpayaraMicro.contextRoot=").append(contextPath);
        }
        if (microVersion != null && !microVersion.trim().isEmpty()) {
            sb.append(" -DpayaraMicro.payaraVersion=").append(microVersion);
        }
        sb.append(" -Ddebug=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=")
                .append(debugPort);
        return sb.toString();
    }

}
