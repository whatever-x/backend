plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "whatever"

include("caramel-api")
include("caramel-domain")
include("caramel-common")
include("caramel-infrastructure")