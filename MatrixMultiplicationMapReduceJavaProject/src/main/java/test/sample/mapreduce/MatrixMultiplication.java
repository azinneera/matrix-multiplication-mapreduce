package test.sample.mapreduce;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import java.io.IOException;
import java.util.HashMap;

public class MatrixMultiplication {

    public static class Map extends Mapper<LongWritable, Text, Text, Text> {
        @Override
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {

            Configuration conf = context.getConfiguration();
            int m = Integer.parseInt(conf.get("m"));
            int p = Integer.parseInt(conf.get("p"));
            String line = value.toString();
            // (M, i, j, Mij);
            String[] indicesAndValue = line.split(",");
            Text outputKey = new Text();
            Text outputValue = new Text();
            if (indicesAndValue[0].equals("M")) {
                for (int k = 0; k < p; k++) {
                    outputKey.set(indicesAndValue[1] + "," + k);
                    // outputKey.set(i,k);
                    outputValue.set(indicesAndValue[0] + "," + indicesAndValue[2]
                            + "," + indicesAndValue[3]);
                    // outputValue.set(M,j,Mij);
                    context.write(outputKey, outputValue);
                }
            } else {
                // (N, j, k, Njk);
                for (int i = 0; i < m; i++) {
                    outputKey.set(i + "," + indicesAndValue[2]);
                    outputValue.set("N," + indicesAndValue[1] + ","
                            + indicesAndValue[3]);
                    context.write(outputKey, outputValue);
                }
            }
        }
    }

    public static class Reduce extends Reducer<Text, Text, Text, Text> {
        @Override
        public void reduce(Text key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {
            String[] value;
            //key=(i,k),
            //Values = [(M/N,j,V/W),..]
            HashMap<Integer, Float> hashA = new HashMap<>();
            HashMap<Integer, Float> hashB = new HashMap<>();
            for (Text val : values) {
                value = val.toString().split(",");
                if (value[0].equals("M")) {
                    hashA.put(Integer.parseInt(value[1]), Float.parseFloat(value[2]));
                } else {
                    hashB.put(Integer.parseInt(value[1]), Float.parseFloat(value[2]));
                }
            }
            int n = Integer.parseInt(context.getConfiguration().get("n"));
            float result = 0.0f;
            float m_ij;
            float n_jk;
            for (int j = 0; j < n; j++) {
                m_ij = hashA.getOrDefault(j, 0.0f);
                n_jk = hashB.getOrDefault(j, 0.0f);
                result += m_ij * n_jk;
            }
            if (result != 0.0f) {
                context.write(null,
                        new Text(key.toString() + "," + result));
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        // M is an m-by-n matrix; N is an n-by-p matrix.
        conf.set("m", "1000");
        conf.set("n", "100");
        conf.set("p", "1000");

        @SuppressWarnings("deprecation")
        Job job = new Job(conf, "MatrixMultiply");
        job.setJarByClass(MatrixMultiplication.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        job.setMapperClass(Map.class);
        job.setReducerClass(Reduce.class);

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        job.waitForCompletion(true);
    }
}
