import net.labymod.labygradle.common.extension.LabyModAnnotationProcessorExtension.ReferenceType

dependencies {
    labyProcessor()
    api(project(":api"))

    // An example of how to add an external dependency that is used by the addon.
    // addonMavenDependency("org.jeasy:easy-random:5.0.0")

    // JSON parsing for server registry
    implementation("com.google.code.gson:gson:2.10.1")
}

labyModAnnotationProcessor {
    referenceType = ReferenceType.DEFAULT
}