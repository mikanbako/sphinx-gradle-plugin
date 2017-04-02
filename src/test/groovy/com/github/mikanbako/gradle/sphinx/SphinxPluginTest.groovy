/*
 * Copyright (c) 2015 Keita Kita
 *
 * This software is released under the MIT License.
 * https://opensource.org/licenses/MIT
 */

package com.github.mikanbako.gradle.sphinx

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.Plugin
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class SphinxPluginTest {
    static String PLUGIN_ID = 'com.github.mikanbako.gradle.sphinx'

    private Project mProject

    private SphinxPlugin mPlugin

    @Before
    void setup() {
        mProject = ProjectBuilder.builder().build()

        mProject.pluginManager.apply PLUGIN_ID

        mPlugin = mProject.plugins.findPlugin PLUGIN_ID
    }

    @Test
    void emptyConfiguration() {
        Assert.assertTrue(
            mPlugin.getSphinxConfigurationOptions(mProject).empty)
    }

    @Test
    void oneConfiguration() {
        mProject.sphinx.configurations = [a:'b']

        def configurationOptions =
            mPlugin.getSphinxConfigurationOptions mProject

        Assert.assertEquals(
            configurationOptions.toString(), 1, configurationOptions.size())
        Assert.assertEquals('-Da=b', configurationOptions.get(0).toString())
    }

    private static void assertCommandLineContains(Task task, String text) {
        Assert.assertTrue(task.commandLine.any {
            it.contains(text)
        })
    }

    @Test
    void buildCommandLine() {
        mProject.sphinx {
            configurations = [a:'b', b:'c']
            sourceDir = 'aaa'
            buildDir = 'bbb'
            options = ['-z', 'ddd']
        }

        def buildTask = mProject.getTasksByName('build', false).getAt(0)
        def sourceDir = mProject.file(mProject.sphinx.sourceDir)
        def buildDir = mProject.file(mProject.sphinx.buildDir)

        assertCommandLineContains(buildTask, sourceDir.absolutePath)
        assertCommandLineContains(buildTask, buildDir.absolutePath)
        assertCommandLineContains(buildTask, '-Da=b')
        assertCommandLineContains(buildTask, 'ddd')
    }

    @Test
    void optionsSetting() {
        mProject.sphinx {
            options '-z', 'b'
        }

        def buildTask = mProject.getTasksByName('build', false).getAt(0)

        assertCommandLineContains(buildTask, '-z')
    }

    @Test
    void overrideBuildDir() {
        mProject.sphinx {
            buildDir = mProject.file('abc')
        }

        mProject.evaluate()

        Assert.assertEquals(mProject.sphinx.buildDir, mProject.buildDir)
    }

    @Test
    void doNotOverrideBuildDir() {
        mProject.sphinx {
            buildDir = mProject.file('abc')
            isProjectBuildDirOverrided = false
        }

        mProject.evaluate()

        Assert.assertNotEquals(mProject.sphinx.buildDir, mProject.buildDir)
    }
}
