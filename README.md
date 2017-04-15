# P14 - Getting Started with our DIS-Project
### Team members:
Teja Parimi
Nikhil Kumar Vemula

### 44-564 Design of Data Intensive Systems, Northwest Missouri State University, Maryville, MO.

Client
Denise Case, PhD, PE, Assistant Professor, Assistant Coordinator, Masters of Applied Computer Science Program, School of Computer Science and Information Systems, Colden Hall 2280, Northwest Missouri State University, 800 University Drive, Maryville, MO 64468, dcase@nwmissouri.edu 
https://www.linkedin.com/in/denisecase
Faculty
Dr.Denise Case
Assistants
Sai Sri Ravali Chinthareddy, Course Assistant
Developers, Designers, and Software Engineers
Teja Parimi<br>
Nikhil Kumar Vemula<br>

Prerequisites
Following must be installed to run this application:
Cloudera Quick Start running on Virtual Machine
Ecllipse(already included in Cloudera_QuickStart)
Check Whether Cloudera Manager is started successfull in linux operating system.
Get started:
Create the workspace or folder where you wanted to clone the repository
open terminal
navigate to desired folder
execute this command :
git clone https://github.com/tejaparimi007/MapReduce.git <br><br
Overview:<br>

## Mapper Code
'''java 
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
'''

![GitHub Logo](/MapReduce\mapper_output.JPG)
Format: ![Alt Text](https://drive.google.com/file/d/0B5yo9UfWdMr4LWY5ekpuZzZxelU/view?usp=sharing)

![GitHub Logo](/MapReduce\mapreducerOutput.JPG)
Format: ![Alt Text](https://drive.google.com/file/d/0B5yo9UfWdMr4WHQtWDVDN2M1WlU/view?usp=sharing)


