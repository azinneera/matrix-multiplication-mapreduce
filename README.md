# Matrix Multiplication with MapReduce

This repository contains resources required to try out matrix multiplication with MapReduce technique with Apache Hadoop. 
The resources were tested on an AWS EMR cluster.

Each resource in the repository serves the below needs:

**MatrixMultiplicationMapReduceJavaProject**

The Java project required to create the JAR file with all required dependencies. This project can be built to generate the JAR locally using the command below.
```bash
./gradlew clean build
```

Note: To run on Amazon EMR 6.5.0, the project should be built with Java 8.

*Original source of the Java code is https://github.com/marufaytekin/MatrixMultiply/tree/master/src/main/java/com/lendap/hadoop*

**data.txt**

This contains the matrix that is used to demonstate the MapReduce example for matrix multiplication. The fist matrix is M and the second is N.

M - 1000 x 100 matrix

N - 100 x 1000 matrix

*The original source of this data is https://github.com/SinghHarshita/MapReduce-Examples/tree/main/Matrix%20Multiplication/matrices. The two matrices are merged into one and the data.txt in this repository has the entries of both matrices*

## Steps to execute in an AWS EMR Cluster

1. Create an AWS EMR Cluster (v6.5) with required inbound rule additions and an SSH tunnel
2. Create an S3 bucket and upload the JAR file and the `data.txt` file
3. Add a Step in the EMR cluster with the following inputs:
    * Step type : Custom JAR
    * JAR Location: `<Select the JAR from S3 bucket>`
    * Arguments: 
        `<s3-location>/data.txt`
        `<s3-location>/output>`

    Note: `output/` specified above is not expected to exist. A folder will be created with this name and all the reducers generated will be saved here.

    You can use the Application user interfaces to monitor the status of the job, schdulers, logs etc.
 4. Once the step is completed, the status will be updated to `Completed`. You can view the reducers in the `<s3-location>/output/` folder
 5. Terminate the step and the cluster.