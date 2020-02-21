# Beam demo

Simple beam pipeline which calculates distance of each gps trip(session). Build with bazel.

Reads data in the following CSV format:
```
<ID>, <Latitude>, <Longitude>, <?>, <timestamp>
```

Run locally:

```bash
➜ bazel run :Runner -- --runner=DirectRunner --output=distances --inputFile=<input>
```

Dataflow:

```bash
➜ bazel run :Runner -- --runner=DataflowRunner --project=<gcp-project> --gcpTempLocation=gs://<beam-team-bucket> --inputFile=gs://<input-bucket-or-folder> --output=gs://<output-bucket-prefix> --region=europe-west1
```
