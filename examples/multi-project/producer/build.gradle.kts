plugins {
    id("org.dafny.dafny")

    `maven-publish`
}

dafny {
    dafnyVersion.set(providers.environmentVariable("DAFNY_VERSION").orElse("4.11.0"))

    optionsMap.put("function-syntax", 3)
}

dependencies {
    implementation("org.dafny:DafnyRuntime:4.11.0")
}
