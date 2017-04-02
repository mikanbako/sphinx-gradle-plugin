# Sphinx Gradle Plugin

Gradle plugin for [Sphinx](http://sphinx-doc.org/). This plugin adds tasks to
build your Sphinx documentation.

# How to use

1. Install [Sphinx](http://sphinx-doc.org/), [sphinx-autobuild](https://pypi.python.org/pypi/sphinx-autobuild) and create your Sphinx documentation.

```sh
pip install Sphinx sphinx-autobuild
sphinx-quickstart
```

2. Create build.gradle into your source directory of Sphinx documentation.
3. Include the following in build.gradle:

```Gradle
buildscript {
    repositories {
        maven {
            url "https://plugins.gradle.org/m2/"
        }
    }
    dependencies {
        classpath "gradle.plugin.com.github.mikanbako.gradle.sphinx:sphinx-gradle-plugin:0.2.0"
    }
}

apply plugin: "com.github.mikanbako.gradle.sphinx"
```

or

```Gradle
plugins {
    id "com.github.mikanbako.gradle.sphinx" version "0.2.0"
}
```

4. Run task.

```sh
gradle build
```

# Tasks

This plugin adds the following tasks.

Name      | Description
----------|--------------------------------------
build     | Build documentation by Sphinx.
clean     | Clean built documentation directory.
rebuild   | Clean and build documentation.
autobuild | Run sphinx-autobuild.

When Sphinx or sphinx-autobuild is not installed, the tasks will be failed.

Default assumes that builder is "html" and build directory is "_build".

# Configuration

This plugin allows you to configure custom options for Sphinx and
sphinx-autobuild.

## Sphinx configuration

The following example configuration closure is for Sphinx configuration.

```Gradle
sphinx {
    builder = "html"
    buildDir = "_build"
    isWarningError = true
    configurations = [release: '1.0']
}
```

Sphinx configuration has the following properties and methods.

### Properties

Name                        | Type                      | Default value  | Description
----------------------------|---------------------------|----------------|---------------------------------------------------------------------------
builder                     | String                    | "html"         | Name of builder. The value is the same of the value of -b option on sphinx-build.
sourceDir                   | Object                    | "."            | Directory of source files. The value is evaluated by Project.file().
buildDir                    | Object                    | "_build"       | Directory of built documentation. The value is evaluated by Project.file().
isWarningError              | boolean                   | true           | When the value is true, the build is failed by warnings from sphinx-build.
isProjectBuildDirOverrided  | boolean                   | true           | When the value is true, Project.buildDir is set to the value of buildDir property.
configurations              | Map&lt;String, String&gt; | empty          | Key and value paired configuration value set. The value is the same of the value of -D option on sphinx-build.
options                     | Iterable&lt;String&gt;    | empty          | Other options for sphinx-build.
warningsFilename            | String                    | "warnings.txt" | Filename to write warnings from sphinx-build. The file is located on buildDir.
command                     | String                    | "sphinx-build" | Running command for sphinx-build.

### Methods

Name    | Return type | Parameter types | Description
--------|-------------|-----------------|--------------------------------
options | void        | String...       | Other options for sphinx-build.

## sphinx-autobuild configuration

The following example configuration closure is for Sphinx configuration.

```Gradle
sphinxAutobuild {
    host = "localhost"
    port = 8000
}
```

sphinx-autobuild configuration has the following properties and methods.

### Properties

Name    | Type                   | Default value      | Description
--------|------------------------|--------------------|-----------------------------------------------------
host    | String                 | "localhost"        | The host on which the documentation shall be served.
port    | int                    | 8000               | The port on which the documentation shall be served.
options | Iterable&lt;String&gt; | empty              | Options for sphix-autobuild.
command | String                 | "sphinx-autobuild" | Running command for sphinx-autobuild.

Moreover, sphinx-autobuild configuration uses the following Sphinx
configuration to run Sphinx.

* builder
* configurations
* options

### Methods

Name    | Return type | Parameter types | Description
--------|-------------|-----------------|-----------------------------
options | void        | String...       | Options for sphix-autobuild.

# License

This software is released under the MIT License.
See LICENSE.txt for detail.

# For developers

## Installing to your local environment

1. Install the plugin to local maven repository:

 ```sh
./gradlew install
```

2. Include the following in your build.gradle to build the documentation:

 ```Gradle
buildscript {
    repositories {
        mavenLocal()
        jcenter()
    }
    dependencies {
        classpath "com.github.mikanbako.gradle.sphinx:sphinx-gradle-plugin:0.2.0"
    }
}

apply plugin: "com.github.mikanbako.gradle.sphinx"
```

