package com.qmai.android.loadgradleplugin

import com.android.build.gradle.BaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class LaunchPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {

        print(">> instance plugin start <<")
        project.getExtensions().findByType(BaseExtension.class)
                .registerTransform(new CoreTransform())
    }
}