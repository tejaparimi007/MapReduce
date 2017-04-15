# P14 - Getting Started with our DIS-Project
### Team members:
Teja Parimi
Nikhil Kumar Vemula

### 44-564 Design of Data Intensive Systems, Northwest Missouri State University, Maryville, MO.

**Client**
_Denise Case, PhD, PE, Assistant Professor, Assistant Coordinator, Masters of Applied Computer Science Program, School of Computer Science and Information Systems, Colden Hall 2280, Northwest Missouri State University, 800 University Drive, Maryville, MO 64468, dcase@nwmissouri.edu_ 
[GitHub](https://www.linkedin.com/in/denisecase)

# Prerequisites
_Following must be installed to run this application_
1. Cloudera Quick Start running on Virtual Machine
2. Ecllipse(already included in Cloudera_QuickStart)
3. Please make sure Whether Cloudera Manager is started successfull in linux operating system.
**Get started**:
1. Create the workspace or folder where you wanted to clone the repository
2. open terminal
3. navigate to desired folder
4. execute these followint commands :
''' git clone https://github.com/tejaparimi007/MapReduce.git '''
   *mvn clean install
   *hadoop hadoop jar target/mapReduce-1.0-jar-with-dependencies.jar dis.mapReduce.StJob -i /user/cloudera/input_Storm -o /user/cloudera/out0
 <br><br>
 
 
# Overview:

## Mapper Code
'''javascript
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
	
'''

## Reducer Code
'''javascript
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

'''

![GitHub Logo](/https://github.com/tejaparimi007/MapReduce/blob/master/mapper_output.JPG)
Format: ![Alt Text](https://drive.google.com/file/d/0B5yo9UfWdMr4LWY5ekpuZzZxelU/view?usp=sharing)

![GitHub Logo](https://github.com/tejaparimi007/MapReduce/blob/master/mapreducerOutput.JPG)
Format: ![Alt Text](https://drive.google.com/file/d/0B5yo9UfWdMr4WHQtWDVDN2M1WlU/view?usp=sharing)


