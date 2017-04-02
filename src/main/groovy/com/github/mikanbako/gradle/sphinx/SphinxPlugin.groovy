/*
 * Copyright (c) 2015 Keita Kita
 *
 * This software is released under the MIT License.
 * https://opensource.org/licenses/MIT
 */

package com.github.mikanbako.gradle.sphinx

import org.apache.commons.lang3.SystemUtils
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.TaskAction

import groovy.transform.PackageScope

class SphinxPlugin implements Plugin<Project> {
    private static final String SPHINX_EXTENSION_NAME = 'sphinx'

    private static final String SHPINX_AUTOBUILD_EXTENSION_NAME =
        'sphinxAutobuild'

    private static final String DOCTREE_NAME = 'doctree'

    private static List<String> getSphinxConfigurationOptions(
            Project project) {
        return project.sphinx.configurations.collect {
                key, value -> "-D${key}=${value}"
            }
    }

    private static List<String> getCommandPromptExecutableForContinuous(
            Project project, String executable) {
        // Create new command prompt. Because Gradle kills parent process
        // (cmd) only when CTRL-C is pressed.
        return ['cmd', '/c', 'start'] +
            getCommandPromptExecutable(project, executable)
    }

    private static List<String> getCommandPromptExecutable(
            Project project, executable) {
        return ['cmd', '/c', executable]
    }

    private static List<String> getShellExecutable(
            Project project, String executable) {
        return new ByteArrayOutputStream().withCloseable { output ->
            def whichResult = project.exec {
                commandLine 'sh', '-c', 'which ' + executable
                standardOutput = output
                ignoreExitValue true
            }

            try {
                whichResult.assertNormalExitValue()
            } catch (RuntimeException e) {
                throw new GradleException(executable + ' is not found.', e)
            }

            return [output.toString().trim()]
        }
    }

    private static List<String> getExecutable(
            Project project, String executable, boolean isContinuous) {
        if (SystemUtils.IS_OS_WINDOWS) {
            if (isContinuous) {
                return getCommandPromptExecutableForContinuous(
                    project, executable)
            } else {
                return getCommandPromptExecutable(project, executable)
            }
        } else {
            return getShellExecutable(project, executable)
        }
    }

    private static File getDoctreeDir(File buildDir) {
        return new File(buildDir, DOCTREE_NAME)
    }

    private static File getBuilderDir(File buildDir, Project project) {
        return new File(buildDir, project.sphinx.builder)
    }

    @PackageScope static List<String> getSphinxBuildCommandLine(
            Project project,
            File sourceDir,
            File buildDir,
            File warningsFile) {
        def configurationOptions = getSphinxConfigurationOptions(project)
        def warningIsErrorOptions

        if (project.sphinx.isWarningError) {
            warningIsErrorOptions = ['-W']
        } else {
            warningIsErrorOptions = []
        }

        return getExecutable(project, project.sphinx.command, false) +
                ['-b', project.sphinx.builder,
                    '-d', getDoctreeDir(buildDir),
                    '-w', warningsFile] +
                warningIsErrorOptions +
                configurationOptions +
                project.sphinx.options +
                [sourceDir, getBuilderDir(buildDir, project)]
    }

    static void printSphinxAutobuildNotice() {
        if (SystemUtils.IS_OS_WINDOWS) {
            println 'Press CTRL-C on the sphinx-autobuild window to stop.'
        }
    }

    static List<String> getSphinxAutobuildCommandLine(
            Project project,
            File sourceDir,
            File buildDir,
            File warningsFile) {
        def configurationOptions = getSphinxConfigurationOptions(project)

        return getExecutable(project, project.sphinxAutobuild.command, true) +
                ['-H', project.sphinxAutobuild.host,
                    '-p', project.sphinxAutobuild.port,
                    '-b', project.sphinx.builder,
                    '-d', getDoctreeDir(buildDir),
                    '-i', warningsFile] +
                configurationOptions +
                project.sphinx.options +
                project.sphinxAutobuild.options +
                [sourceDir, getBuilderDir(buildDir, project)]
    }

    @Override
    void apply(Project project) {
        project.extensions.create(
            SPHINX_EXTENSION_NAME, SphinxPluginExtension)
        project.extensions.create(
            SHPINX_AUTOBUILD_EXTENSION_NAME, SphinxAutobuildExtension)

        project.afterEvaluate {
            def sourceDir = project.file(project.sphinx.sourceDir)
            def buildDir = project.file(project.sphinx.buildDir)
            def wariningsFile = new File(
                buildDir, project.sphinx.warningsFilename)

            project.task('makeBuildDir') {
                mustRunAfter 'clean'

                doLast {
                    project.mkdir(buildDir)
                }
            }

            project.task('build', type: Exec, dependsOn: 'makeBuildDir') {
                group 'Sphinx'
                description 'Build Sphinx document.'
                mustRunAfter 'clean'

                workingDir sourceDir
                commandLine getSphinxBuildCommandLine(
                    project, sourceDir, buildDir, wariningsFile)
            }

            if (project.sphinx.isProjectBuildDirOverrided) {
                project.buildDir buildDir
            }

            project.task('clean', type: Delete) {
                group 'Sphinx'
                description 'Clean builded document.'
                delete project.file(project.sphinx.buildDir)
            }

            project.task('autobuild', type: Exec, dependsOn: 'makeBuildDir') {
                group 'Sphinx'
                description 'Run sphinx-autobuild.'
                mustRunAfter 'build'

                workingDir sourceDir
                commandLine getSphinxAutobuildCommandLine(
                        project, sourceDir, buildDir, wariningsFile)

                doFirst {
                    printSphinxAutobuildNotice()
                }
            }

            project.task('rebuild', dependsOn: ['clean', 'build']) {
                group 'Sphinx'
                description 'Rebuild Sphinx document.'
            }
        }
    }
}

class SphinxPluginExtension {
    String builder = 'html'
    Object sourceDir = '.'
    Object buildDir = '_build'
    Map<String, String> configurations = [:]
    Iterable<String> options = []
    boolean isWarningError = true
    String warningsFilename = 'warnings.txt'
    String command = 'sphinx-build'
    boolean isProjectBuildDirOverrided = true

    void options(String... options) {
        this.options = Arrays.asList(options)
    }
}

class SphinxAutobuildExtension {
    String command = 'sphinx-autobuild'
    String host = 'localhost'
    int port = 8000
    Iterable<String> options = []

    void options(String... options) {
        this.options = Arrays.asList(options)
    }
}
