java_binary(
    name = "Runner",
    srcs = glob(["src/main/java/com/github/peterpopov/beamdemo/**/*.java"]),
    javacopts = [
        "-source",
        "8",
        "-target",
        "8",
    ],
    main_class = "com.github.peterpopov.beamdemo.AggregateTelemetry",
    runtime_deps = [
        "@maven//:org_apache_beam_beam_runners_direct_java",
        "@maven//:org_apache_beam_beam_runners_google_cloud_dataflow_java",
        "@maven//:org_slf4j_slf4j_jdk14"
    ],
    deps = [
        "@maven//:com_google_api_client_google_api_client",
        "@maven//:com_google_apis_google_api_services_bigquery",
        "@maven//:com_google_apis_google_api_services_pubsub",
        "@maven//:com_google_auth_google_auth_library_credentials",
        "@maven//:com_google_auth_google_auth_library_oauth2_http",
        "@maven//:com_google_cloud_bigdataoss_util",
        "@maven//:com_google_code_findbugs_jsr305",
        "@maven//:com_google_http_client_google_http_client",
        "@maven//:joda_time_joda_time",
        "@maven//:org_apache_avro_avro",
        "@maven//:org_apache_beam_beam_sdks_java_core",
        "@maven//:org_apache_beam_beam_sdks_java_extensions_google_cloud_platform_core",
        "@maven//:org_apache_beam_beam_sdks_java_io_google_cloud_platform",
        "@maven//:org_apache_beam_beam_vendor_guava_26_0_jre",
        "@maven//:org_apache_commons_commons_lang3",
        "@maven//:org_locationtech_spatial4j_spatial4j",
        "@maven//:org_slf4j_slf4j_api",
    ],
)

# java_test(
#     name = "AllTests",
#     use_testrunner = False,
#     args = ["--select-package", "com.github.peterpopov.tracking"],
#     main_class = "org.junit.platform.console.ConsoleLauncher",
#     size = "small",
#     runtime_deps = [
#         "@maven//:org_junit_platform_junit_platform_console",
#         ":tests",
#     ],
# )
