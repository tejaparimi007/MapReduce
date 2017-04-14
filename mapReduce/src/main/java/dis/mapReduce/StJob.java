package dis.mapReduce;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli2.CommandLine;
import org.apache.commons.cli2.Group;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.OptionException;
import org.apache.commons.cli2.builder.ArgumentBuilder;
import org.apache.commons.cli2.builder.DefaultOptionBuilder;
import org.apache.commons.cli2.builder.GroupBuilder;
import org.apache.commons.cli2.commandline.Parser;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//com.hadoop.mr.StJob
public class StJob {

	/*
	 * Read the data of Strom events and calculate No. of deaths, no.of
	 * injuries, no.damage to the props and crops for the given year and month
	 */

	static Logger LOG = LoggerFactory.getLogger(StJob.class.getName());

	public static void main(String[] args) throws OptionException, IOException,
			ClassNotFoundException, InterruptedException {
		DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
		ArgumentBuilder abuilder = new ArgumentBuilder();
		GroupBuilder gbuilder = new GroupBuilder();

		// com.hadoop.mr.WdataJob option1 option2
		// Program -input /inputfolder -output /outfolder
		// input File Path
		Option inputPathOpt = obuilder
				.withLongName("input")
				.withRequired(false)
				.withArgument(
						abuilder.withName("input").withMinimum(1)
								.withMaximum(1).create())
				.withDescription("input path").withShortName("i").create();

		Option outputPathOpt = obuilder
				.withLongName("output")
				.withRequired(false)
				.withArgument(
						abuilder.withName("output").withMinimum(1)
								.withMaximum(1).create())
				.withDescription("output path").withShortName("o").create();

		Group group = gbuilder.withName("Options").withOption(inputPathOpt)
				.withOption(outputPathOpt).create();
		String inputPath = null, outputPath = null;

		try {

			Parser parser = new Parser();
			parser.setGroup(group);
			CommandLine cmdLine = parser.parse(args);
			if (cmdLine.hasOption(inputPathOpt)) {
				inputPath = cmdLine.getValue(inputPathOpt).toString();
			}
			if (cmdLine.hasOption(outputPathOpt)) {
				outputPath = cmdLine.getValue(outputPathOpt).toString();
			}
		} catch (OptionException e) {
			// CommandLineUtil.printHelp(group);
			throw e;
		}

		Configuration conf = new Configuration();
		Job job = createSubmittableJob(conf, inputPath, outputPath);
		System.exit(job.waitForCompletion(true) ? 0 : 1);

	}

	public static Job createSubmittableJob(Configuration conf,
			String inputPath, String outputPath) throws IOException {

		Job job = Job.getInstance(conf);

		job.setJarByClass(StJob.class);
		job.setMapperClass(StJobMapper.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(MapWritable.class);

		job.setReducerClass(StJobReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		FileInputFormat.addInputPath(job, new Path(inputPath));
		FileOutputFormat.setOutputPath(job, new Path(outputPath));
		job.setNumReduceTasks(1);
		return job;

	}

	static final String INJ_KEY = "INJ";
	static final String IND_INJ_KEY = "IND_INJ";
	static final String DTH_KEY = "DTH";
	static final String IND_DTH_KEY = "IND_DTH";
	static final String PROP_KEY = "PROP";
	static final String CROP_KEY = "CROP";

	static class StJobMapper extends
			Mapper<LongWritable, Text, Text, MapWritable> {

		static int bym_ind = -1;
		static int inj_ind = -1;
		static int ind_inj_ind = -1;
		static int dth_ind = -1;
		static int idth_ind = -1;
		static int prop_ind = -1;
		static int crop_ind = -1;

		Pattern p = Pattern.compile("\"([^\"]*)\"");

		static boolean check = true;

		public void map(LongWritable row, Text value, Context context)
				throws IOException, InterruptedException {
			// BEGIN_YEARMONTH,BEGIN_DAY,BEGIN_TIME,END_YEARMONTH,END_DAY,END_TIME,EPISODE_ID,EVENT_ID,STATE,STATE_FIPS,YEAR,MONTH_NAME,EVENT_TYPE,CZ_TYPE,CZ_FIPS,CZ_NAME,WFO,BEGIN_DATE_TIME,CZ_TIMEZONE,END_DATE_TIME,INJURIES_DIRECT,INJURIES_INDIRECT,DEATHS_DIRECT,DEATHS_INDIRECT,DAMAGE_PROPERTY,DAMAGE_CROPS,SOURCE,MAGNITUDE,MAGNITUDE_TYPE,FLOOD_CAUSE,CATEGORY,TOR_F_SCALE,TOR_LENGTH,TOR_WIDTH,TOR_OTHER_WFO,TOR_OTHER_CZ_STATE,TOR_OTHER_CZ_FIPS,TOR_OTHER_CZ_NAME,BEGIN_RANGE,BEGIN_AZIMUTH,BEGIN_LOCATION,END_RANGE,END_AZIMUTH,END_LOCATION,BEGIN_LAT,BEGIN_LON,END_LAT,END_LON,EPISODE_NARRATIVE,EVENT_NARRATIVE,DATA_SOURCE
			//
			// 199507,3,1611,199507,3,1611,,10314233,"ALABAMA",1,1995,"July","Hail","C",73,"JEFFERSON",,"03-JUL-95 16:11:00","CST","03-JUL-95 16:11:00","0","0","0","0","0","0",,".75",,,,,"0","0",,,,,"0",,"Homewood","0",,,,,,,,"*  Pinson,03,1638CST- *,,1645CST,,,?,?,?,?,Hail (1.75) Three-quarters inch hail was reported at Oxmoor Valley Golf Club in Homewood.  One inch hail was reported in the Trace Crossings area of Hoover just south of Interstate 459.  Three to four trees were also reported down in Hoover. Dime to golf ball-size hail was reported in Pinson lasting about seven minutes.","CSV"
			//
			String data = value.toString();

			if (data == null || data.trim().isEmpty())
				return;

			String[] dataArr = data.trim().split(",");
			if (check == true) {
				loadColumnPositions(dataArr);
				return;
			}

			if (bym_ind == -1 || inj_ind == -1)
				return;

			String begin_ym = dataArr[bym_ind].trim();
			String inj = dataArr[inj_ind].trim();
			String ind_inj = dataArr[ind_inj_ind].trim();
			String dth = dataArr[dth_ind].trim();
			String ind_dth = dataArr[idth_ind].trim();
			String props = dataArr[prop_ind].trim();
			String crops = dataArr[crop_ind].trim();

			LOG.info("Values Before Converting : injuries :" + inj
					+ ", ind_injuries :" + ind_inj + ", deaths :" + dth
					+ ", ind_deaths :" + ind_dth + ", prop_loss :" + props
					+ ", crops_loss :" + crops);

			Double injuries = replaceCurrencyChars(inj);
			Double ind_injuries = replaceCurrencyChars(ind_inj);
			Double deaths = replaceCurrencyChars(dth);
			Double ind_deaths = replaceCurrencyChars(ind_dth);
			Double prop_loss = replaceCurrencyChars(props);
			Double crops_loss = replaceCurrencyChars(crops);

			LOG.info("Values : injuries :" + injuries + ", ind_injuries :"
					+ ind_injuries + ", deaths :" + deaths + ", ind_deaths :"
					+ ind_deaths + ", prop_loss :" + prop_loss
					+ ", crops_loss :" + crops_loss);

			MapWritable mw = new MapWritable();

			mw.put(new ImmutableBytesWritable(INJ_KEY.getBytes()),
					new ImmutableBytesWritable(injuries.toString().getBytes()));
			mw.put(new ImmutableBytesWritable(IND_INJ_KEY.getBytes()),
					new ImmutableBytesWritable(ind_injuries.toString()
							.getBytes()));
			mw.put(new ImmutableBytesWritable(DTH_KEY.getBytes()),
					new ImmutableBytesWritable(deaths.toString().getBytes()));
			mw.put(new ImmutableBytesWritable(IND_DTH_KEY.getBytes()),
					new ImmutableBytesWritable(ind_deaths.toString().getBytes()));
			mw.put(new ImmutableBytesWritable(PROP_KEY.getBytes()),
					new ImmutableBytesWritable(prop_loss.toString().getBytes()));
			mw.put(new ImmutableBytesWritable(CROP_KEY.getBytes()),
					new ImmutableBytesWritable(crops_loss.toString().getBytes()));

			context.write(new Text(begin_ym), mw);
		}

		private static void loadColumnPositions(String[] dataArr) {

			if (dataArr[0].equalsIgnoreCase("BEGIN_YEARMONTH")
					&& dataArr[dataArr.length - 1]
							.equalsIgnoreCase("DATA_SOURCE")) {
				int index = 0;
				for (String el : dataArr) {

					if (el.equalsIgnoreCase("BEGIN_YEARMONTH"))
						bym_ind = index;

					if (el.equalsIgnoreCase("INJURIES_DIRECT"))
						inj_ind = index;

					if (el.equalsIgnoreCase("INJURIES_INDIRECT"))
						ind_inj_ind = index;

					if (el.equalsIgnoreCase("DEATHS_DIRECT"))
						dth_ind = index;

					if (el.equalsIgnoreCase("DEATHS_INDIRECT"))
						idth_ind = index;

					if (el.equalsIgnoreCase("DAMAGE_PROPERTY"))
						prop_ind = index;

					if (el.equalsIgnoreCase("DAMAGE_CROPS"))
						crop_ind = index;

					index++;

				}
				check = false;
			}
		}

		public Double replaceCurrencyChars(String str) {
			
			String regex = "\\d+";

			Double rval = 0.0;
			
			Matcher m = p.matcher(str);
			if (m.find())
				str = m.group(1);

			if (str.startsWith("."))
				str = "0" + str;

			if (str.endsWith("H") || str.endsWith("h")) {
				str = str.replace("H", "").replace("h", "");

				rval = Double.parseDouble(str) * 100;
			} else if (str.endsWith("K")) {
				str = str.replace("K", "");

				rval = Double.parseDouble(str) * 1000;
			} else if (str.endsWith("M")) {
				str = str.replace("M", "");

				rval = Double.parseDouble(str) * 1000 * 1000;
			} else if (str.endsWith("B")) {
				str = str.replace("B", "");

				rval = Double.parseDouble(str) * 1000 * 1000 * 1000;
			}else if(!str.isEmpty() && str.matches(regex)){
				String newStr = str;
				double temp = parseDouble(newStr);
				rval = temp;
			}

			return rval;

		}
		
		public Double parseDouble(String str){
			double i = 0.0;
			String temp = "";
			temp = str;
			i = i + Double.parseDouble(temp);
			return i;
			
		}
		
		/*private static String getDigits(String s){
			Matcher m = p.matcher(s);
			if (m.find())
				s = m.group(0);
			else
				s = "0";
			
			return s;
		}*/
	}
	
	static class StJobReducer extends Reducer<Text, MapWritable, Text, Text> {

		static NumberFormat formatter = new DecimalFormat("#0.00"); 
		public void reduce(Text key, Iterable<MapWritable> values,
				Context context) throws IOException, InterruptedException {

			Double injuries = 0.0, ind_injuries = 0.0, deaths = 0.0, ind_deaths = 0.0, props = 0.0, crops = 0.0;

			Double tot_injuries = 0.0, tot_deaths = 0.0, tot_props = 0.0, tot_crops = 0.0;

			for (MapWritable mw : values) {

				LOG.info(mw.toString());
				ImmutableBytesWritable bw = (ImmutableBytesWritable) (mw
						.get(new ImmutableBytesWritable(INJ_KEY.getBytes())));

				LOG.info(bw.toString());

				injuries = Double.parseDouble(Bytes.toString(bw.get()));

				bw = (ImmutableBytesWritable) (mw
						.get(new ImmutableBytesWritable(IND_INJ_KEY.getBytes())));
				ind_injuries = Double.parseDouble(Bytes.toString(bw.get()));

				bw = (ImmutableBytesWritable) (mw
						.get(new ImmutableBytesWritable(DTH_KEY.getBytes())));
				deaths = Double.parseDouble(Bytes.toString(bw.get()));

				bw = (ImmutableBytesWritable) (mw
						.get(new ImmutableBytesWritable(IND_DTH_KEY.getBytes())));
				ind_deaths = Double.parseDouble(Bytes.toString(bw.get()));

				bw = (ImmutableBytesWritable) (mw
						.get(new ImmutableBytesWritable(PROP_KEY.getBytes())));
				props = Double.parseDouble(Bytes.toString(bw.get()));

				bw = (ImmutableBytesWritable) (mw
						.get(new ImmutableBytesWritable(CROP_KEY.getBytes())));
				crops = Double.parseDouble(Bytes.toString(bw.get()));

				tot_injuries += (injuries + ind_injuries);

				tot_deaths += (deaths + ind_deaths);

				tot_props += props;

				tot_crops += crops;
			}
			
			

			String output = key.toString() + "," + formatter.format(tot_injuries) + ","
					+ formatter.format(tot_deaths) + "," + formatter.format(tot_props) + "," + formatter.format(tot_crops);

			context.write(new Text(output), null);

		}
	}



}
