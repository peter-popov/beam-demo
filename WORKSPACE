workspace(name = "beamdemo")

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

http_archive(
    name = "rules_jvm_external",
    sha256 = "62133c125bf4109dfd9d2af64830208356ce4ef8b165a6ef15bbff7460b35c3a",
    strip_prefix = "rules_jvm_external-3.0",
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/3.0.zip",
)

load("@rules_jvm_external//:defs.bzl", "maven_install")

maven_install(
    artifacts = [
        "com.uber:h3:3.6.3",
        "org.locationtech.spatial4j:spatial4j:0.7",
        "org.junit.jupiter:junit-jupiter:5.6.0",
        "org.junit.platform:junit-platform-console:1.6.0",
        "org.apache.beam:beam-sdks-java-core:2.19.0",
        "org.apache.beam:beam-sdks-java-io-google-cloud-platform:2.19.0",
        "org.apache.beam:beam-runners-direct-java:2.19.0",
        "org.apache.beam:beam-runners-google-cloud-dataflow-java:2.19.0",
        "org.slf4j:slf4j-api:1.7.25",
        "org.slf4j:slf4j-jdk14:1.7.25",
    ],
    generate_compat_repositories = True,
    repositories = [
        "https://repo.maven.apache.org/maven2/",
    ],
)


load("@maven//:compat.bzl", "compat_repositories")

compat_repositories()