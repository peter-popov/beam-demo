package com.github.peterpopov.beamdemo;

import java.util.Objects;
import java.util.stream.StreamSupport;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.CannotProvideCoderException;
import org.apache.beam.sdk.coders.CoderRegistry;
import org.apache.beam.sdk.coders.DefaultCoder;
import org.apache.beam.sdk.coders.KvCoder;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.Validation.Required;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.GroupByKey;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.SimpleFunction;
import org.apache.beam.sdk.transforms.WithKeys;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.PCollection;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.Instant;
import org.locationtech.spatial4j.distance.DistanceUtils;

public class AggregateTelemetry {

  @DefaultCoder(AvroCoder.class)
  static class Probe implements Comparable<Probe> {
    String session;
    Double latitude;
    Double longitude;
    Long timestamp;

    public Probe() {
    }

    public Probe(String session, Double latitude, Double longitude, Long timestamp) {
      if (session == null || longitude == null || latitude == null || timestamp == null) {
        throw new IllegalArgumentException();
      }
      this.session = session;
      this.latitude = latitude;
      this.longitude = longitude;
      this.timestamp = timestamp;
    }

    public String getSession() {
      return this.session;
    }

    public Double getLatitude() {
      return this.latitude;
    }

    public Double getLongitude() {
      return this.longitude;
    }

    public Long getTimestamp() {
      return this.timestamp;
    }

    @Override
    public int compareTo(Probe other) {
      int strcmp = session.compareTo(other.session);
      if (strcmp == 0) {
        return timestamp.compareTo(other.timestamp);
      } else {
        return strcmp;
      }
    }

    @Override
    public boolean equals(Object object) {
      if (object == null) {
        return false;
      }
      if (object.getClass() != getClass()) {
        return false;
      }
      Probe otherProbe = (Probe) object;
      return Objects.equals(this.session, otherProbe.session) && Objects.equals(this.timestamp, otherProbe.timestamp);
    }

    @Override
    public int hashCode() {
      return 31 * session.hashCode() + timestamp.hashCode();
    }
  }

  static class CsvRecordParser {
    static String parseSession(String[] s) {
      return s.length > 0 ? s[0] : null;
    }

    static Long parseTimestamp(String[] s) {
      return s.length > 4 ? Long.valueOf(s[4]) : null;
    }

    static Double parseLatitude(String[] s) {
      return s.length > 4 ? Double.valueOf(s[1]) : null;
    }

    static Double parseLongitude(String[] s) {
      return s.length > 4 ? Double.valueOf(s[2]) : null;
    }

    public static Probe parseRecord(String s) {
      String[] items = s.split(",");
      return new Probe(parseSession(items), parseLatitude(items), parseLongitude(items), parseTimestamp(items));
    }
  }

  static class ExtractProbesWithTimestamps extends DoFn<String, Probe> {
    private static final long serialVersionUID = 1L;

    @ProcessElement
    public void processElement(ProcessContext c) throws Exception {
      try {
        Probe p = CsvRecordParser.parseRecord(c.element());
        c.outputWithTimestamp(p, new Instant(p.getTimestamp()));
      } catch (IllegalArgumentException e) {
        // Skip the invalid input.
      }
    }
  }

  static class ReadFileAndExtractTimestamps extends PTransform<PBegin, PCollection<Probe>> {
    private static final long serialVersionUID = 1L;
    private final String inputFile;

    public ReadFileAndExtractTimestamps(String inputFile) {
      this.inputFile = inputFile;
    }

    @Override
    public PCollection<Probe> expand(PBegin begin) {
      return begin.apply(TextIO.read().from(inputFile)).apply(ParDo.of(new ExtractProbesWithTimestamps()));
    }
  }

  static class CalculateDistance extends DoFn<KV<String, Iterable<Probe>>, KV<String, Double>> {
    private static final long serialVersionUID = 1L;

    public static Double distance(Probe a, Probe b) {
      return DistanceUtils.radians2Dist(DistanceUtils.distLawOfCosinesRAD(DistanceUtils.toRadians(a.getLatitude()),
          DistanceUtils.toRadians(a.getLongitude()), DistanceUtils.toRadians(b.getLatitude()),
          DistanceUtils.toRadians(b.getLongitude())), DistanceUtils.EARTH_MEAN_RADIUS_KM);
    }

    @ProcessElement
    public void processElement(ProcessContext c) throws Exception {
      Double distance = StreamSupport.stream(c.element().getValue().spliterator(), false).sorted()
          .map(a -> Pair.of(a, Double.valueOf(0.0)))
          .reduce((a, b) -> Pair.of(b.getKey(), a.getValue() + distance(a.getKey(), b.getKey()))).map(p -> p.getValue())
          .orElse(0.0);
      c.output(KV.of(c.element().getKey(), distance));
    }
  }

  public static class FormatAsTextFn extends SimpleFunction<KV<String, Double>, String> {
    private static final long serialVersionUID = 1L;

    @Override
    public String apply(KV<String, Double> input) {
      return input.getKey() + ": " + input.getValue();
    }
  }

  public interface AggregateTelemetryOptions extends PipelineOptions {

    @Description("Path of the file to read from")
    @Default.String("./data/pings_05_*.csv")
    String getInputFile();

    void setInputFile(String value);

    @Description("Path of the file to write to")
    @Required
    String getOutput();

    void setOutput(String value);
  }

  static void runAggregateTelemetry(AggregateTelemetryOptions options) throws CannotProvideCoderException {
    Pipeline p = Pipeline.create(options);

    CoderRegistry cr = p.getCoderRegistry();
    
    p.apply("ReadLines", new ReadFileAndExtractTimestamps(options.getInputFile()))
     .apply(WithKeys.of(probe -> probe.getSession()))
      .setCoder(KvCoder.of(cr.getCoder(String.class), AvroCoder.of(Probe.class)))
     .apply(GroupByKey.create())
     .apply("Generate Sessions", ParDo.of(new CalculateDistance()))
     .apply(MapElements.via(new FormatAsTextFn()))
     .apply("WriteDistances", TextIO.write().to(options.getOutput()));

    p.run().waitUntilFinish();
  }

  public static void main(String[] args) {
    AggregateTelemetryOptions options =
        PipelineOptionsFactory.fromArgs(args).withValidation().as(AggregateTelemetryOptions.class);

    try {
      runAggregateTelemetry(options);
    } catch (CannotProvideCoderException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
