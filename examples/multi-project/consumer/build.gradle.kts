plugins {
    id("org.dafny.dafny")
}

dafny {
    dafnyVersion.set("4.11.0")
}

dependencies {
    implementation(project(":producer"))

    implementation("org.dafny:DafnyRuntime:4.11.0")
}
