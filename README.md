# P14 - Getting Started with our DIS-Project

Team members: 

- Nikhil Kumar Vemula
- Teja Parimi

Developed as part of the profession-based learning cirriculum at:

44-564 Design of Data Intensive Systems, 
Northwest Missouri State University, 
Maryville, MO.

# Contributors

Client

- Denise Case, PhD, PE,  Assistant Professor,   Assistant Coordinator, Masters of Applied Computer Science Program, 
  School of Computer Science and Information Systems, 
  Colden Hall 2280, 
  Northwest Missouri State University, 
  800 University Drive, Maryville, MO 64468, 
  dcase@nwmissouri.edu     
  https://www.linkedin.com/in/denisecase

Faculty

- Dr.Denise Case

Assistants

- Sai Sri Ravali Chinthareddy, Course Assistant

Developers, Designers, and Software Engineers

- Nikhil Kumar Vemula 
- Teja Parimi

# Prerequisites

Following must be installed to run this application_
1. Cloudera Quick Start running on Virtual Machine
2. Ecllipse(already included in Cloudera_QuickStart)
3. Please make sure Whether Cloudera Manager is started successfull in linux operating system.

**Get started**:

1. Create the workspace or folder where you wanted to clone the repository
2. Openpen terminal
3. Navigateavigate to desired folder
4. Ecllipse Editor
5. Notepad++ (recommended)

Clone this repo to your local machine. 

- git clone https://github.com/tejaparimi007/MapReduce


## Glimpse about our dataset:

- We are working on a big data problem which focuses on the “Storm Events”. It includes the data set of the people died due to the storm, economical loss such as crop and etc.
## Short introduction to our project:

- Our project is mapreduce program that finds the nuber of people injured and died due to the storm and the economical losses such as the property and crop damaged due to the storm.


## Initialize (do these just once when setting up the project)


Follow the below commands:

```
> mvn clean install
> hadoop hadoop jar target/mapReduce-1.0-jar-with-dependencies.jar dis.mapReduce.StJob -i /user/cloudera/input_Storm -o /user/cloudera/out0
```
## Mapper_Output

![Mapper_Output.PNG](https://github.com/tejaparimi007/MapReduce/blob/master/mapper_output.JPG)

## Reducer_Output

![Reducer_Output.PNG](https://github.com/tejaparimi007/MapReduce/blob/master/mapreducerOutput.JPG)


